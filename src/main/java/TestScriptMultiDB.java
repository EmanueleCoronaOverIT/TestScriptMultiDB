import utils.TestScriptConst;
import utils.TestScriptConst.FieldType;
import utils.TestScriptUtils;

import java.io.*;
import java.util.Scanner;

// Jira per testare: HWFHERAWFM-4868
public class TestScriptMultiDB {

    private static final Scanner _scanner = new Scanner(System.in);
    private static boolean isFieldName = true;
    private static final int EXIT = 0;
    private static final int TABLE_CREATION = 1;
    private static final int ADD_COLUMN = 2;
    private static final String READ_FILE_PATH = "src/main/files/fields.txt";
    private static final String WRITE_FILE_PATH = "src/main/files/script.sql";


    public static void main(String[] args) {
        boolean keepGoing = true;

        while (keepGoing) {
            showMenu();
            int choice = _scanner.nextInt();

            switch (choice) {
                case EXIT: {
                    keepGoing = false;
                    break;
                }
                case TABLE_CREATION: {
                    System.out.println("Creazione Tabella");
                    break;
                }
                case ADD_COLUMN: {
                    addColumns();
                    break;
                }
            }
        }
    }

    private static void showMenu() {
        System.out.println("\n========== Test Script MultiDb ==========");
        System.out.println("0 ESCI");
        System.out.println("1 CREAZIONE TABELLA");
        System.out.println("2 AGGIUNTA COLONNE \n");
        System.out.print("Scegliere un'opzione: ");
    }

    private static void addColumns() {
        BufferedWriter writer = null;
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(READ_FILE_PATH));
            writer = new BufferedWriter(new FileWriter(WRITE_FILE_PATH));

            writer.append(TestScriptConst.GEOCALL_HEADER).append("\n");

            String sql = TestScriptConst.ADD_COLUMN;

            System.out.println("========== Aggiunta Colonne ==========");
            System.out.print("Inserisci il nome della tabella: ");
            String tableName = _scanner.next();

            sql = sql.replace("nome_tabella", tableName);

            String row;

            String fieldName = "nome_campo";
            String fieldType = "tipo_dato";

            while ((row = reader.readLine()) != null) {
                if (!row.isEmpty()) {
                    boolean readyToWrite;

                    if (isFieldName) {
                        // Fase impostazione nome campo
                        sql = sql.replace(fieldName, row);
                        fieldName = row;
                        readyToWrite = false;
                        isFieldName = false;
                    } else {
                        // Fase impostazione tipo capo
                        sql = sql.replace(fieldType, getFormattedFieldType(row));
                        fieldType = getFormattedFieldType(row);
                        readyToWrite = true;
                        isFieldName = true;
                    }

                    // Stampa lo script dopo che sono stati impostati sia il nome che il tipo del campo
                    if (readyToWrite) {
                        writer.append(sql).append("\n");
                    }
                }
            }

        } catch (FileNotFoundException e) {
            throw new RuntimeException("File non trovato");
        } catch (IOException e) {
            throw new RuntimeException("Errore durante il tentativo di aggiunta delle colonne");
        } finally {
            TestScriptUtils.closeBufferedWriter(writer);
            TestScriptUtils.closeBufferedReader(reader);
        }
    }

    private static String getFormattedFieldType(String fieldType) {
        if (fieldType.contains(FieldType.NUMBER)) {
            if (fieldType.contains(",")) {
                fieldType = "NUMBER " + fieldType.replaceAll("[^0-9,]", "").replace(",", " ");
            } else {
                fieldType = "NUMBER " + fieldType.replaceAll("[^0-9]", "") + " 0";
            }
        }

        if (fieldType.contains(FieldType.VARCHAR)) {
            fieldType = fieldType.replaceFirst("VARCHAR2", "");
            return FieldType.VARCHAR + " " + fieldType.replaceAll("[^0-9]", "");
        }

        if (fieldType.contains(FieldType.CHAR)) {
            return FieldType.CHAR + " 1";
        }

        if (fieldType.contains(FieldType.TIMESTAMP)) {
            return FieldType.TIMESTAMP;
        }

        return fieldType;
    }
}