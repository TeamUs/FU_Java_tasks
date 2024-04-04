import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;
import java.util.Scanner;

public class Student1 {

    private static Scanner scanner = new Scanner(System.in);
    private static Connection con;
    private static String tableName;

    public static void main(String[] args) {
        try {
            con = getConnection("jdbc:mysql://localhost/test", "root", "root");
            menu();
        } catch (SQLException e) {
            System.out.println("Ошибка подключения к базе данных: " + e.getMessage());
        }
    }

    private static void menu() {
        while (true) {
            System.out.println("Выберите действие:");
            System.out.println("1. Вывести все таблицы из MySQL.");
            System.out.println("2. Создать таблицу в MySQL.");
            System.out.println("3. Ввод с клавиатуры значений ВСЕХ полей студента и работника, сохранить их в MySQL с последующим выводом в консоль.");
            System.out.println("4. Вывести данные о студентах и работниках из MySQL.");
            System.out.println("5. Сохранить результаты из MySQL в Excel и вывести их в консоль.");
            System.out.println("0. Выйти.");

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
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
                    addDataToDatabase();
                    break;
                case 4:
                    displayDataFromDatabase();
                    break;
                case 5:
                    exportToExcel(con);
                    break;
                case 0:
                    System.out.println("Выход из программы.");
                    return;
                default:
                    System.out.println("Некорректный выбор действия. Попробуйте снова.");
            }
        }
    }

    private static Connection getConnection(String url, String username, String password) throws SQLException {
        return DriverManager.getConnection(url, username, password);
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

    public static void createTable(Connection con) {
        System.out.println("Введите название таблицы:");
        tableName = scanner.next();

        try (Statement statement = con.createStatement()) {
            String sql = "CREATE TABLE IF NOT EXISTS " + tableName + " (Name VARCHAR(255), Age INT, Salary INT)";
            statement.executeUpdate(sql);
            System.out.println("Таблица " + tableName + " создана успешно.");
        } catch (SQLException e) {
            System.out.println("Ошибка при создании таблицы: " + e.getMessage());
        }
    }

    private static void addDataToDatabase() {
        addStudentToDatabase();
        addWorkerToDatabase();
    }

    public static void addStudentToDatabase() {
        System.out.println("Введите имя студента:");
        String name = scanner.next();
        System.out.println("Введите возраст студента:");
        int age = scanner.nextInt();

        try {
            String sql = "INSERT INTO " + tableName + " (Name, Age) VALUES (?, ?)";
            PreparedStatement statement = con.prepareStatement(sql);
            statement.setString(1, name);
            statement.setInt(2, age);
            statement.executeUpdate();
            System.out.println("Студент успешно добавлен в базу данных.");
        } catch (SQLException e) {
            System.out.println("Ошибка при добавлении студента в базу данных: " + e.getMessage());
        }
    }

    public static void addWorkerToDatabase() {
        System.out.println("Введите имя работника:");
        String name = scanner.next();
        System.out.println("Введите возраст работника:");
        int age = scanner.nextInt();
        System.out.println("Введите зарплату работника:");
        int salary = scanner.nextInt();

        try {
            // Проверяем, есть ли уже в базе данных запись с таким же именем и возрастом
            String selectSql = "SELECT * FROM " + tableName + " WHERE Name = ? AND Age = ?";
            PreparedStatement selectStatement = con.prepareStatement(selectSql);
            selectStatement.setString(1, name);
            selectStatement.setInt(2, age);
            ResultSet resultSet = selectStatement.executeQuery();

            if (resultSet.next()) {
                // Если запись уже существует, обновляем данные о работнике
                String updateSql = "UPDATE " + tableName + " SET Salary = ? WHERE Name = ? AND Age = ?";
                PreparedStatement updateStatement = con.prepareStatement(updateSql);
                updateStatement.setInt(1, salary);
                updateStatement.setString(2, name);
                updateStatement.setInt(3, age);
                updateStatement.executeUpdate();
                System.out.println("Данные о работнике обновлены.");
            } else {
                // Если записи нет, добавляем нового работника
                String insertSql = "INSERT INTO " + tableName + " (Name, Age, Salary) VALUES (?, ?, ?)";
                PreparedStatement insertStatement = con.prepareStatement(insertSql);
                insertStatement.setString(1, name);
                insertStatement.setInt(2, age);
                insertStatement.setInt(3, salary);
                insertStatement.executeUpdate();
                System.out.println("Работник успешно добавлен в базу данных.");
            }
        } catch (SQLException e) {
            System.out.println("Ошибка при добавлении работника в базу данных: " + e.getMessage());
        }
    }

    public static void displayDataFromDatabase() {
        System.out.println("Студенты:");
        displayTableContents(con, tableName);
        System.out.println("Работники:");
        displayTableContents(con, tableName);
    }

    public static void displayTableContents(Connection con, String tableName) {
        String sql = "SELECT * FROM " + tableName;

        try (Statement statement = con.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();

            System.out.println("Содержимое таблицы " + tableName + ":");

            //Вывод заголовков столбцов
            for (int i = 1; i <= columnCount; i++) {
                System.out.print(metaData.getColumnName(i) + "\t");
            }
            System.out.println();

            //Вывод данных
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
}
