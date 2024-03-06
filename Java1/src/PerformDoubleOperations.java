import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;
import java.util.Scanner;

public class PerformDoubleOperations {

    private static Scanner scanner = new Scanner(System.in);
    private static Connection con;

    public static void main(String[] args) {
        try {
            con = getConnection("jdbc:mysql://localhost/test", "root", "root");
            performDoubleOperations();
        } catch (SQLException e) {
            System.out.println("Ошибка подключения к базе данных: " + e.getMessage());
        }
    }

    private static void performDoubleOperations() {
        System.out.println("Введите первое целое число:");
        double num1 = scanner.nextDouble();

        System.out.println("Введите второе целое число:");
        double num2 = scanner.nextDouble();

        performArithmeticOperations(num1, num2);
    }

    private static void performArithmeticOperations(double num1, double num2) {
        int operationChoice;

        do {
            System.out.println("Выберите операцию:");
            System.out.println("1. Вывести все таблицы из MySQL.");
            System.out.println("2. Создать таблицу в MySQL.");
            System.out.println("3. Сложение чисел, результат сохранить в MySQL с последующим выводом в консоль.");
            System.out.println("4. Вычитание чисел, результат сохранить в MySQL с последующим выводом в консоль.");
            System.out.println("5. Умножение чисел, результат сохранить в MySQL с последующим выводом в консоль.");
            System.out.println("6. Деление чисел, результат сохранить в MySQL с последующим выводом в консоль.");
            System.out.println("7. Деление чисел по модулю (остаток), результат сохранить в MySQL с последующим выводом в консоль.");
            System.out.println("8. Возведение числа в модуль, результат сохранить в MySQL с последующим выводом в консоль.");
            System.out.println("9. Возведение числа в степень, результат сохранить в MySQL с последующим выводом в консоль.");
            System.out.println("10. Сохранить все данные (вышеполученные результаты) из MySQL в Excel и вывести на экран.");
            System.out.println("0. Выход");

            operationChoice = scanner.nextInt();

            switch (operationChoice) {
                case 1:
                    try {
                        displayTables(con);
                    } catch (SQLException e) {
                        System.out.println("Ошибка при отображении таблиц: " + e.getMessage());
                    }
                    break;
                case 2:
                    createTable(con);
                    break;
                case 3:
                    double sum = num1 + num2;
                    System.out.println("Сумма: " + sum);
                    System.out.println("Введите название таблицы, куда сохранить результат: ");
                    saveResultToDatabase(num1, num2, "Сложение", sum, scanner.next());
                    break;
                case 4:
                    double difference = num1 - num2;
                    System.out.println("Разность: " + difference);
                    System.out.println("Введите название таблицы, куда сохранить результат: ");
                    saveResultToDatabase(num1, num2, "Вычитание", difference, scanner.next());
                    break;
                case 5:
                    double product = num1 * num2;
                    System.out.println("Произведение: " + product);
                    System.out.println("Введите название таблицы, куда сохранить результат: ");
                    saveResultToDatabase(num1, num2, "Умножение", product, scanner.next());
                    break;
                case 6:
                    if (num2 != 0) {
                        double quotient = num1 / num2;
                        System.out.println("Частное: " + quotient);
                        System.out.println("Введите название таблицы, куда сохранить результат: ");
                        saveResultToDatabase(num1, num2, "Деление", quotient, scanner.next());
                    } else {
                        System.out.println("На ноль делить нельзя.");
                    }
                    break;
                case 7:
                    if (num2 != 0) {
                        double remainder = num1 % num2;
                        System.out.println("Остаток от деления: " + remainder);
                        System.out.println("Введите название таблицы, куда сохранить результат: ");
                        saveResultToDatabase(num1, num2, "Деление по модулю", remainder, scanner.next());
                    } else {
                        System.out.println("На ноль делить нельзя.");
                    }
                    break;
                case 8:
                    double absNum1 = Math.abs(num1);
                    double absNum2 = Math.abs(num2);
                    System.out.println("Модуль первого числа: " + absNum1);
                    System.out.println("Модуль второго числа: " + absNum2);
                    System.out.println("Введите название таблицы, куда сохранить результат: ");
                    saveResultToDatabase(num1, num2, "Модуль первого числа", absNum1, scanner.next());
                    System.out.println("Введите название таблицы, куда сохранить результат: ");
                    saveResultToDatabase(num1, num2, "Модуль второго числа", absNum2, scanner.next());
                    break;
                case 9:
                    double power = Math.pow(num1, num2);
                    System.out.println("Первое число в степени второго числа: " + power);
                    System.out.println("Введите название таблицы, куда сохранить результат: ");
                    saveResultToDatabase(num1, num2, "Первое число в степени второго числа", power, scanner.next());
                    break;
                case 10:
                    exportToExcel(con);
                    break;

                case 0:
                    System.out.println("Выход из программы.");
                    break;
                default:
                    System.out.println("Некорректный выбор операции.");
            }

        } while (operationChoice != 0);
    }

    private static void createTable(Connection con) {
        System.out.println("Введите название таблицы:");
        String tableName = scanner.next();

        try (Statement statement = con.createStatement()) {
            String sql = "CREATE TABLE IF NOT EXISTS " + tableName + " (num1 INT, num2 INT, operation VARCHAR(255), OpResult INT)";
            statement.executeUpdate(sql);
            System.out.println("Таблица " + tableName + " создана успешно.");
        } catch (SQLException e) {
            System.out.println("Ошибка при создании таблицы: " + e.getMessage());
        }
    }

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

    private static void saveResultToDatabase(double num1, double num2, String operation, double result, String tableName) {
        try (PreparedStatement statement = con.prepareStatement(
                "INSERT INTO " + tableName + " (num1, num2, operation, OpResult) VALUES (?, ?, ?, ?)")) {
            statement.setInt(1, (int) num1);
            statement.setInt(2, (int) num2);
            statement.setString(3, operation);
            statement.setInt(4, (int) result);
            statement.executeUpdate();
            System.out.println("Результат сохранен в базе данных.");
        } catch (SQLException e) {
            System.out.println("Ошибка при сохранении результата в базе данных: " + e.getMessage());
        }
    }

    private static void exportToExcel(Connection con) {
        try {
            String excelFilePath = "results.xlsx";
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Results");

            // Запрос имени таблицы с клавиатуры
            System.out.println("Введите имя таблицы для экспорта в Excel:");
            String tableName = scanner.next();

            displayTableContents(con, tableName);

            String sql = "SELECT * FROM " + tableName;
            try (Statement statement = con.createStatement();
                 ResultSet resultSet = statement.executeQuery(sql)) {

                ResultSetMetaData metaData = resultSet.getMetaData();
                int columnCount = metaData.getColumnCount();

                // Записываем заголовки столбцов
                Row headerRow = sheet.createRow(0);
                for (int i = 1; i <= columnCount; i++) {
                    headerRow.createCell(i - 1).setCellValue(metaData.getColumnName(i));
                }

                // Записываем данные
                int rowNumber = 1;
                while (resultSet.next()) {
                    Row row = sheet.createRow(rowNumber++);
                    for (int i = 1; i <= columnCount; i++) {
                        row.createCell(i - 1).setCellValue(resultSet.getString(i));
                    }
                }

                // Сохраняем результаты в файл Excel
                try (FileOutputStream outputStream = new FileOutputStream(excelFilePath)) {
                    workbook.write(outputStream);
                }

                System.out.println("Результаты успешно экспортированы в Excel.");
            }
        } catch (SQLException | IOException e) {
            System.out.println("Ошибка при экспорте в Excel: " + e.getMessage());
        }
    }
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
    private static Connection getConnection(String url, String username, String password) throws SQLException {
        return DriverManager.getConnection(url, "root", "root");
    }
}
