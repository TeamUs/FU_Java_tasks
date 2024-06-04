import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;
import java.util.*;

public class Students {
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
            System.out.println("3. Ввести данные о всех студентах и сохранить их в MySQL с последующим табличным (форматированным) выводом в консоль.");
            System.out.println("4. Вывести данные о студенте по ID из MySQL.");
            System.out.println("5. Удалить данные о студенте из MySQL по ID.");
            System.out.println("6. Сохранить итоговые результаты из MySQL в Excel и вывести их в консоль.");
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
                    createTable();
                    break;
                case 3:
                    addStudentsToDatabase();
                    break;
                case 4:
                    displayStudentById();
                    break;
                case 5:
                    deleteStudentById();
                    break;
                case 6:
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

    public static void createTable() {
        // Запрос названия таблицы
        System.out.print("Введите название таблицы: ");
        String tableName = scanner.nextLine();

        try (Statement statement = con.createStatement()) {
            String sql = "CREATE TABLE IF NOT EXISTS " + tableName + " (ID INT PRIMARY KEY, Direction VARCHAR(255), FullName VARCHAR(255), GroupName VARCHAR(255))";
            statement.executeUpdate(sql);
            System.out.println("Таблица " + tableName + " создана успешно.");
        } catch (SQLException e) {
            System.out.println("Ошибка при создании таблицы: " + e.getMessage());
        }
    }

    private static void addStudentsToDatabase() {
        // Ввод данных о студентах
        System.out.print("Введите название таблицы для добавления данных: ");
        String tableName = scanner.nextLine();
        System.out.println("Введите количество студентов:");
        int count = scanner.nextInt();
        scanner.nextLine(); // Очистка буфера

        List<Student> students = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            System.out.println("Студент №" + (i + 1) + ":");
            Student student = createStudent();
            students.add(student);
            student.addToDatabase(con, tableName);
        }

        // Сортировка студентов по алфавиту
        Collections.sort(students, Comparator.comparing(Student::getFullName));

        // Вывод отсортированных студентов в консоль
        System.out.println("Отсортированные данные о студентах:");
        for (Student student : students) {
            System.out.println(student);
        }
    }


    private static Student createStudent() {
        System.out.print("Введите ID студента: ");
        int id = scanner.nextInt();
        scanner.nextLine(); // Очистка буфера
        System.out.print("Введите направление подготовки студента: ");
        String direction = scanner.nextLine();
        System.out.print("Введите ФИО студента: ");
        String fullName = scanner.nextLine();
        System.out.print("Введите группу студента: ");
        String group = scanner.nextLine();

        return new Student(id, direction, fullName, group);
    }

    private static void displayStudentById() {
        System.out.print("Введите имя таблицы откуда берется ID студента: ");
        String tableName = scanner.nextLine();
        System.out.print("Введите ID студента: ");
        int id = scanner.nextInt();
        scanner.nextLine(); // Очистка буфера

        String sql = "SELECT ID, Direction, FullName, GroupName FROM " + tableName + " WHERE `ID` = ?";

        try (PreparedStatement preparedStatement = con.prepareStatement(sql)) {
            preparedStatement.setInt(1, id);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    int studentId = resultSet.getInt("ID");
                    String direction = resultSet.getString("Direction");
                    String fullName = resultSet.getString("FullName");
                    String group = resultSet.getString("GroupName");

                    System.out.println("ID: " + studentId);
                    System.out.println("Направление подготовки: " + direction);
                    System.out.println("ФИО: " + fullName);
                    System.out.println("Группа: " + group);
                } else {
                    System.out.println("Студент с ID " + id + " не найден.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Ошибка при получении данных о студенте: " + e.getMessage());
        }
    }


    private static void deleteStudentById() {
        System.out.print("Введите имя таблицы откуда будет удален студент по ID: ");
        String tableName = scanner.nextLine();
        System.out.print("Введите ID студента для удаления: ");
        int id = scanner.nextInt();
        scanner.nextLine(); // Очистка буфера

        String sql = "DELETE FROM `" + tableName + "` WHERE `ID` = ?";

        try (PreparedStatement preparedStatement = con.prepareStatement(sql)) {
            preparedStatement.setInt(1, id);
            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Студент с ID " + id + " успешно удален.");
            } else {
                System.out.println("Студент с ID " + id + " не найден.");
            }
        } catch (SQLException e) {
            System.out.println("Ошибка при удалении студента: " + e.getMessage());
        }
    }
    private static void exportToExcel(Connection con) {
        try {
            String excelFilePath = "results.xlsx";
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Results");

            // Запрос названия таблицы
            System.out.print("Введите имя таблицы для экспорта в Excel: ");
            String tableName = scanner.nextLine();

            // Создание временной таблицы для сортировки
            String tempTableName = "temp_" + tableName;

            String createTempTableSQL = "CREATE TEMPORARY TABLE " + tempTableName +
                    " AS (SELECT * FROM " + tableName + " ORDER BY FullName)";
            con.createStatement().executeUpdate(createTempTableSQL);

            String sql = "SELECT ID, Direction, FullName, GroupName FROM " + tempTableName;

            try (PreparedStatement preparedStatement = con.prepareStatement(sql);
                 ResultSet resultSet = preparedStatement.executeQuery()) {
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
                for (int i = 0; i < columnCount; i++) {
                    sheet.autoSizeColumn(i);
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


    public static class Student {
        private int id;
        private String direction;
        private String fullName;
        private String group;

        public Student(int id, String direction, String fullName, String group) {
            this.id = id;
            this.direction = direction;
            this.fullName = fullName;
            this.group = group;
        }
        public String getFullName() {
            return fullName;
        }
        @Override
        public String toString() {
            return "ID: " + id + ", Direction: " + direction + ", FullName: " + fullName + ", Group: " + group;
        }

        public void addToDatabase(Connection con, String tableName) {
            try {
                String sql = "INSERT INTO " + tableName + " (ID, Direction, FullName, GroupName) VALUES (?, ?, ?, ?)";
                try (PreparedStatement preparedStatement = con.prepareStatement(sql)) {
                    preparedStatement.setInt(1, id);
                    preparedStatement.setString(2, direction);
                    preparedStatement.setString(3, fullName);
                    preparedStatement.setString(4, group);
                    preparedStatement.executeUpdate();
                    System.out.println("Студент успешно добавлен в базу данных.");
                }
            } catch (SQLException e) {
                System.out.println("Ошибка при добавлении студента в базу данных: " + e.getMessage());
            }
        }
    }
}
