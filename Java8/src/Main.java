import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;
import java.util.Scanner;

public class Main {
    private static Scanner scanner = new Scanner(System.in);
    private static Connection con;

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
                    createTables();
                    break;
                case 3:
                    addDataToDatabase();
                    break;
                case 4:
                    displayDataFromDatabase();
                    break;
                case 5:
                    try {
                        exportToExcel(con);
                    } catch (IOException | SQLException e) {
                        System.out.println("Ошибка при экспорте в Excel: " + e.getMessage());
                    }
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

    private static void createTables() {
        // Запрос названий таблиц
        System.out.print("Введите название таблицы для студентов: ");
        String studentsTable = scanner.nextLine();
        createTable(con, studentsTable);

        System.out.print("Введите название таблицы для работников: ");
        String workersTable = scanner.nextLine();
        createTable(con, workersTable);
    }

    public static void createTable(Connection con, String tableName) {
        try (Statement statement = con.createStatement()) {
            String sql = "CREATE TABLE IF NOT EXISTS " + tableName + " (Name VARCHAR(255), Age INT, Salary INT)";
            statement.executeUpdate(sql);
            System.out.println("Таблица " + tableName + " создана успешно.");
        } catch (SQLException e) {
            System.out.println("Ошибка при создании таблицы: " + e.getMessage());
        }
    }

    private static void addDataToDatabase() {
        Student student = createStudent();
        student.addToDatabase(con, "students");
        Worker worker = createWorker();
        worker.addToDatabase(con, "workers");
    }

    static class RegularStudent extends Student {
        public RegularStudent(String name, int age) {
            super(name, age);
        }

        @Override
        public void addToDatabase(Connection con, String tableName) {
            super.addToDatabase(con, tableName); // Используем реализацию из суперкласса
        }
    }

    public static Student createStudent() {
        System.out.print("Введите имя студента: ");
        String name = scanner.next();
        System.out.print("Введите возраст студента: ");
        int age = scanner.nextInt();
        return new RegularStudent(name, age); // Возвращаем экземпляр RegularStudent вместо Student
    }


    public static Worker createWorker() {
        System.out.print("Введите имя работника: ");
        String name = scanner.next();
        scanner.nextLine(); // Очистка буфера ввода
        System.out.print("Введите возраст работника: ");
        int age = scanner.nextInt();
        System.out.print("Введите зарплату работника: ");
        int salary = scanner.nextInt();
        scanner.nextLine(); // Очистка буфера ввода
        return new Worker(name, age, salary);
    }

    public static void displayDataFromDatabase() {
        System.out.println("Содержимое таблицы студентов:");

        String sql = "SELECT * FROM students";

        try (Statement statement = con.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                String name = resultSet.getString("Name");
                int age = resultSet.getInt("Age");
                System.out.println("Name: " + name + ", Age: " + age);
            }
        } catch (SQLException e) {
            System.out.println("Ошибка при отображении данных: " + e.getMessage());
        }

        System.out.println("Содержимое таблицы работников:");

        sql = "SELECT * FROM workers";

        try (Statement statement = con.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                String name = resultSet.getString("Name");
                int age = resultSet.getInt("Age");
                int salary = resultSet.getInt("Salary");
                System.out.println("Name: " + name + ", Age: " + age + ", Salary: " + salary);
            }
        } catch (SQLException e) {
            System.out.println("Ошибка при отображении данных: " + e.getMessage());
        }
    }

    private static void exportToExcel(Connection con) throws IOException, SQLException {
        // Запрос названий таблиц
        System.out.print("Введите название таблицы для студентов: ");
        String studentsTable = scanner.nextLine();
        System.out.print("Введите название таблицы для работников: ");
        String workersTable = scanner.nextLine();

        String excelFilePath = "results.xlsx";
        Workbook workbook = new XSSFWorkbook();

        // Экспорт данных студентов
        exportTableToSheet(con, studentsTable, workbook.createSheet("Students"));

        // Экспорт данных работников
        exportTableToSheet(con, workersTable, workbook.createSheet("Workers"));

        // Сохранение Excel файла
        try (FileOutputStream outputStream = new FileOutputStream(excelFilePath)) {
            workbook.write(outputStream);
        }

        System.out.println("Данные успешно экспортированы в файл: " + excelFilePath);
    }

    // Вспомогательный метод для экспорта данных из таблицы в лист Excel
    private static void exportTableToSheet(Connection con, String tableName, Sheet sheet) throws SQLException {
        String sql = "SELECT * FROM " + tableName;
        try (Statement statement = con.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();

            // Создание заголовка
            Row headerRow = sheet.createRow(0);
            for (int i = 1; i <= columnCount; i++) {
                headerRow.createCell(i - 1).setCellValue(metaData.getColumnName(i));
            }

            // Заполнение данных
            int rowNumber = 1;
            while (resultSet.next()) {
                Row row = sheet.createRow(rowNumber++);
                for (int i = 1; i <= columnCount; i++) {
                    if (metaData.getColumnType(i) == Types.INTEGER) {
                        row.createCell(i - 1).setCellValue(resultSet.getInt(i));
                    } else {
                        row.createCell(i - 1).setCellValue(resultSet.getString(i));
                    }
                }
            }
        }
    }
}
