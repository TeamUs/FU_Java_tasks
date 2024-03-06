import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;
import java.util.Scanner;

public class StringManipulation {

    // Создаем объект Scanner для считывания ввода пользователя
    private static Scanner scanner = new Scanner(System.in);

    // Объявляем объект Connection для подключения к базе данных
    private static Connection con;

    // Точка входа в программу
    public static void main(String[] args) {
        try {
            // Устанавливаем соединение с базой данных MySQL
            con = getConnection("jdbc:mysql://localhost/test", "root", "root");
            // Выполняем операции со строками
            performStringOperations();
        } catch (SQLException e) {
            // В случае ошибки выводим сообщение
            System.out.println("Ошибка подключения к базе данных: " + e.getMessage());
        }
    }

    // Метод для выполнения операций со строками
    private static void performStringOperations() {
        String firstString = "";
        String secondString = "";
        boolean stringsEntered = false;

        // Бесконечный цикл для выполнения операций, пока пользователь не выберет выход
        while (true) {
            // Выводим меню операций
            System.out.println("Выберите действие:");
            System.out.println("1. Вывести все таблицы из MySQL.");
            System.out.println("2. Создать таблицу в MySQL.");
            System.out.println("3. Ввести две строки с клавиатуры, результат сохранить в MySQL с последующим выводом в консоль.");
            System.out.println("4. Изменить порядок символов строки на обратный, результат сохранить в MySQL с последующим выводом в консоль.");
            System.out.println("5. Добавить одну строку в другую, результат сохранить в MySQL с последующим выводом в консоль.");
            System.out.println("6. Сохранить все данные (вышеполученные результаты) из MySQL в Excel и вывести на экран.");
            System.out.println("0. Выйти.");

            // Считываем выбор пользователя
            int choice = scanner.nextInt();
            scanner.nextLine(); // съедаем символ новой строки

            // Обрабатываем выбор пользователя с помощью оператора switch
            switch (choice) {
                case 1:
                    try {
                        // Отображаем все таблицы в базе данных MySQL
                        displayTables(con);
                    } catch (SQLException e) {
                        // В случае ошибки выводим сообщение
                        System.out.println("Ошибка при отображении таблиц: " + e.getMessage());
                    }
                    break;
                case 2:
                    // Создаем новую таблицу в базе данных MySQL
                    createTable(con);
                    break;
                case 3:
                    //Вводим две строки с клавиатуры и сохраняем их в переменные
                    System.out.println("Введите первую строку:");
                    firstString = scanner.nextLine();
                    System.out.println("Введите вторую строку:");
                    secondString = scanner.nextLine();
                    stringsEntered = true;
                    //Сохраняем введенные строки в базу данных MySQL
                    System.out.println("Введите название таблицы, куда сохранить результат: ");
                    saveResultToDatabase("Первая строка", firstString, scanner.next());
                    System.out.println("Введите название таблицы, куда сохранить результат: ");
                    saveResultToDatabase("Вторая строка", secondString, scanner.next());
                    break;
                case 4:
                    // Изменяем порядок символов строки на обратный
                    if (!stringsEntered) {
                        System.out.println("Сначала введите две строки.");
                        break;
                    }
                    reverseStringAndSave(firstString, "Первая строка");
                    reverseStringAndSave(secondString, "Вторая строка");
                    break;
                case 5:
                    // Добавляем одну строку в другую
                    if (!stringsEntered) {
                        System.out.println("Сначала введите две строки.");
                        break;
                    }
                    addStringsAndSave(firstString, secondString, "Сложенная строка");
                    break;
                case 6:
                    // Сохраняем все данные из MySQL в Excel и выводим на экран
                    exportToExcel(con);
                    break;
                case 0:
                    // Выходим из программы
                    System.out.println("Выход из программы.");
                    return;
                default:
                    System.out.println("Некорректный выбор действия. Попробуйте снова.");
            }
        }
    }

    // Метод для создания таблицы в базе данных MySQL
    private static void createTable(Connection con) {
        System.out.println("Введите название таблицы:");
        String tableName = scanner.next();
        try (Statement statement = con.createStatement()) {
            String sql = "CREATE TABLE IF NOT EXISTS " + tableName + " (OpName VARCHAR(255), OpResult VARCHAR(255))";
            statement.executeUpdate(sql);
            System.out.println("Таблица " + tableName + " создана успешно.");
        } catch (SQLException e) {
            System.out.println("Ошибка при создании таблицы: " + e.getMessage());
        }
    }

    // Метод для отображения таблиц в базе данных MySQL
    private static void displayTables(Connection con) throws SQLException {
        String sql = "SHOW TABLES";
        try (Statement statement = con.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            System.out.println("Список таблиц в базе данных:");
            while (resultSet.next()) {
                System.out.println(resultSet.getString(1));
            }
        }
    }

    // Метод для сохранения результата в базе данных MySQL
    private static void saveResultToDatabase(String opName, String opResult, String tableName) {
        try (PreparedStatement statement = con.prepareStatement(
                "INSERT INTO " + tableName + " (OpName, OpResult) VALUES (?, ?)")) {
            statement.setString(1, opName);
            statement.setString(2, opResult);
            statement.executeUpdate();
            System.out.println("Результат сохранен в базе данных.");
        } catch (SQLException e) {
            System.out.println("Ошибка при сохранении результата в базе данных: " + e.getMessage());
        }
    }

    // Метод для изменения порядка символов строки на обратный и сохранения результата в базе данных MySQL
    private static void reverseStringAndSave(String inputString, String tableName) {
        StringBuffer reversedString = new StringBuffer(inputString).reverse();
        saveResultToDatabase("Обратный порядок символов " + tableName, reversedString.toString(), tableName);
        System.out.println("Результат сохранен в базе данных и выведен в консоль.");
        System.out.println("Обратный порядок символов для " + tableName + ": " + reversedString);
    }

    // Метод для добавления одной строки в другую и сохранения результата в базе данных MySQL
    private static void addStringsAndSave(String firstString, String secondString, String tableName) {
        String concatenatedString = firstString + secondString;
        saveResultToDatabase("Сложенная строка", concatenatedString, tableName);
        System.out.println("Результат сохранен в базе данных и выведен в консоль.");
        System.out.println("Результат сложения строк: " + concatenatedString);
    }

    // Метод для экспорта всех данных из MySQL в Excel
    private static void exportToExcel(Connection con) {
        try {
            String excelFilePath = "results.xlsx";
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Results");
            System.out.println("Введите имя таблицы для экспорта в Excel:");
            String tableName = scanner.next();
            displayTableContents(con, tableName);
            String sql = "SELECT * FROM " + tableName;
            try (Statement statement = con.createStatement();
                 ResultSet resultSet = statement.executeQuery(sql)) {
                ResultSetMetaData metaData = resultSet.getMetaData();
                int columnCount = metaData.getColumnCount();
                Row headerRow = sheet.createRow(0);
                for (int i = 1; i <= columnCount; i++) {
                    headerRow.createCell(i - 1).setCellValue(metaData.getColumnName(i));
                }
                int rowNumber = 1;
                while (resultSet.next()) {
                    Row row = sheet.createRow(rowNumber++);
                    for (int i = 1; i <= columnCount; i++) {
                        row.createCell(i - 1).setCellValue(resultSet.getString(i));
                    }
                }
                try (FileOutputStream outputStream = new FileOutputStream(excelFilePath)) {
                    workbook.write(outputStream);
                }
                System.out.println("Результаты успешно экспортированы в Excel.");
            }
        } catch (SQLException | IOException e) {
            System.out.println("Ошибка при экспорте в Excel: " + e.getMessage());
        }
    }

    // Метод для отображения содержимого таблицы в базе данных MySQL
    private static void displayTableContents(Connection con, String tableName) {
        String sql = "SELECT * FROM " + tableName;
        try (Statement statement = con.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            System.out.println("Содержимое таблицы " + tableName + ":");
            // Вывод заголовков столбцов
            for (int i = 1; i <= columnCount; i++) {
                System.out.print(metaData.getColumnName(i) + "\t");
            }
            System.out.println();
            // Вывод данных
            while (resultSet.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    System.out.print(resultSet.getString(i) + "\t");
                }
                System.out.println();
            }
        } catch (SQLException e) {
            System.out.println("Ошибка при отображении содержимого таблицы: " + e.getMessage());
        }
    }

    // Метод для установления соединения с базой данных MySQL
    private static Connection getConnection(String url, String username, String password) throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }
}
