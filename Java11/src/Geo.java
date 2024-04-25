import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;
import java.util.Arrays;
import java.util.Scanner;

public class Geo {
    public static Scanner scanner = new Scanner(System.in);
    public static Connection con;

    public static void main(String[] args) {
        try {
            con = getConnection("jdbc:mysql://localhost/test", "root", "root");
            menu();
        } catch (SQLException e) {
            System.out.println("Ошибка подключения к базе данных: " + e.getMessage());
        }
    }

    public static void menu() {
        while (true) {
            System.out.println("Выберите действие:");
            System.out.println("1. Вывести все таблицы из MySQL.");
            System.out.println("2. Создать таблицу в MySQL.");
            System.out.println("3. Решение базового варианта, сохранение результатов в MySQL.");
            System.out.println("4. Вывод данных с условием: вывести данные по ID строки. Каждая строка – результаты, сохраненные в MySQL в ходе решения подзадач №1 и №2 базового варианта");
            System.out.println("5. Сохранить итоговые результаты из MySQL в Excel и вывести их в консоль.");
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
                    // Запрос названия таблицы
                    System.out.print("Введите имя таблицы для сохранения данных: ");
                    String tableName = scanner.nextLine();

                    // Запрос сторон треугольника
                    System.out.println("Введите длины сторон треугольника:");
                    System.out.print("Сторона 1: ");
                    double side1 = scanner.nextDouble();
                    System.out.print("Сторона 2: ");
                    double side2 = scanner.nextDouble();
                    System.out.print("Сторона 3: ");
                    double side3 = scanner.nextDouble();

                    // Создание треугольника
                    Triangle triangle = new Triangle(side1, side2, side3);

                    // Вычисление площади и периметра
                    double area = triangle.calculateArea();
                    double perimeter = triangle.calculatePerimeter();

                    // Проверка на прямоугольность
                    boolean isRectangular = checkRectangular(side1, side2, side3);

                    // Вычисление четных и нечетных факториалов
                    FactorialCalculator calculator = new FactorialCalculator();
                    int evenFactorial = calculator.calculateEvenFactorial((int) perimeter);
                    int oddFactorial = calculator.calculateOddFactorial((int) area);

                    // Вывод результатов
                    System.out.println("Площадь треугольника: " + area);
                    System.out.println("Периметр треугольника: " + perimeter);
                    System.out.println("Прямоугольный треугольник: " + isRectangular);
                    System.out.println("Четный факториал периметра: " + evenFactorial);
                    System.out.println("Нечетный факториал площади: " + oddFactorial);

                    // Сохранение результатов в базу данных
                    saveTriangleData(tableName, side1, side2, side3, perimeter, area, isRectangular, evenFactorial, oddFactorial);
                    break;

                case 4:
                    // Запрос названия таблицы
                    System.out.print("Введите имя таблицы для сохранения данных: ");
                    tableName = scanner.nextLine();

                    try {
                        // Запрос ID строки у пользователя
                        System.out.print("Введите ID строки: ");
                        int id = scanner.nextInt();
                        scanner.nextLine(); // Очистка буфера после nextInt()

                        // Запрос данных из таблицы по указанному ID
                        String sql = "SELECT * FROM " + tableName + " WHERE ID = ?";
                        try (PreparedStatement preparedStatement = con.prepareStatement(sql)) {
                            preparedStatement.setInt(1, id);
                            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                                // Проверка наличия данных
                                if (resultSet.next()) {
                                    // Вывод данных на экран
                                    System.out.println("Данные по треугольнику с ID " + id + ":");
                                    System.out.println("Сторона 1: " + resultSet.getDouble("side1"));
                                    System.out.println("Сторона 2: " + resultSet.getDouble("side2"));
                                    System.out.println("Сторона 3: " + resultSet.getDouble("side3"));
                                    System.out.println("Периметр: " + resultSet.getDouble("perimeter"));
                                    System.out.println("Площадь: " + resultSet.getDouble("area"));
                                    System.out.println("Прямоугольный: " + resultSet.getBoolean("isRectangular"));
                                    System.out.println("Четный факториал периметра: " + resultSet.getInt("evenFactorial"));
                                    System.out.println("Нечетный факториал площади: " + resultSet.getInt("oddFactorial"));
                                } else {
                                    System.out.println("Запись с ID " + id + " не найдена.");
                                }
                            }
                        }
                    } catch (SQLException e) {
                        System.out.println("Ошибка при выполнении запроса: " + e.getMessage());
                    }
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

    public static Connection getConnection(String url, String username, String password) throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    public static void displayTables(Connection con) throws SQLException {
        String sql = "SHOW TABLES";

        try (Statement statement = con.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            System.out.println("Список таблиц в базе данных:");
            while (resultSet.next()) {
                System.out.println(resultSet.getString(1));
            }
        }
    }
    public static boolean checkRectangular(double side1, double side2, double side3) {
        // Проверка на прямоугольность треугольника по теореме Пифагора
        double[] sides = {side1, side2, side3};
        Arrays.sort(sides);
        return Math.pow(sides[0], 2) + Math.pow(sides[1], 2) == Math.pow(sides[2], 2);
    }

    public static void saveTriangleData(String tableName, double side1, double side2, double side3, double perimeter, double area, boolean isRectangular, int evenFactorial, int oddFactorial) {
        try (Statement statement = con.createStatement()) {
            String sql = "INSERT INTO " + tableName + " (side1, side2, side3, perimeter, area, isRectangular, evenFactorial, oddFactorial) VALUES (" +
                    side1 + ", " + side2 + ", " + side3 + ", " + perimeter + ", " + area + ", " + isRectangular + ", " + evenFactorial + ", " + oddFactorial + ")";
            statement.executeUpdate(sql);
            System.out.println("Данные успешно сохранены в базе данных.");
        } catch (SQLException e) {
            System.out.println("Ошибка при сохранении данных в базу данных: " + e.getMessage());
        }
    }


    public static void createTable() {
        // Запрос названия таблицы
        System.out.print("Введите название таблицы: ");
        String tableName = scanner.nextLine();

        try (Statement statement = con.createStatement()) {
            String sql = "CREATE TABLE IF NOT EXISTS " + tableName + " (ID INT AUTO_INCREMENT PRIMARY KEY, side1 DOUBLE, side2 DOUBLE, side3 DOUBLE, isRectangular BOOLEAN, area DOUBLE, perimeter DOUBLE, evenFactorial INT, oddFactorial INT)";
            statement.executeUpdate(sql);
            System.out.println("Таблица " + tableName + " создана успешно.");
        } catch (SQLException e) {
            System.out.println("Ошибка при создании таблицы: " + e.getMessage());
        }
    }

    static class GeometricFigure {
        double calculateArea() {
            return 0;
        }

        double calculatePerimeter() {
            return 0;
        }
    }

    static class Triangle extends GeometricFigure {
        private double side1;
        private double side2;
        private double side3;

        Triangle(double side1, double side2, double side3) {
            this.side1 = side1;
            this.side2 = side2;
            this.side3 = side3;
        }

        @Override
        double calculateArea() {
            // Реализация формулы Герона
            double p = calculatePerimeter() / 2;
            return Math.sqrt(p * (p - side1) * (p - side2) * (p - side3));
        }

        @Override
        double calculatePerimeter() {
            return side1 + side2 + side3;
        }
    }

    static class RightTriangle extends Triangle {
        RightTriangle(double base, double height) {
            super(base, height, Math.sqrt(base * base + height * height));
        }
    }

    static class FactorialCalculator {
        int calculateEvenFactorial(int n) {
            int result = 1;
            for (int i = 2; i <= n; i += 2) {
                result *= i;
            }
            return result;
        }

        int calculateOddFactorial(int n) {
            int result = 1;
            for (int i = 1; i <= n; i += 2) {
                result *= i;
            }
            return result;
        }
    }

    public static void exportToExcel(Connection con) {
        try {
            String excelFilePath = "results.xlsx";
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Results");

            // Запрос названия таблицы
            System.out.print("Введите имя таблицы для экспорта в Excel: ");
            String tableName = scanner.nextLine();

            String sql = "SELECT ID, side1, side2, side3, isRectangular, area, perimeter, evenFactorial, oddFactorial FROM " + tableName;

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
                        Cell cell = row.createCell(i - 1);
                        if (metaData.getColumnType(i) == Types.BOOLEAN) {
                            cell.setCellValue(resultSet.getBoolean(i));
                        } else if (metaData.getColumnType(i) == Types.INTEGER) {
                            cell.setCellValue(resultSet.getInt(i));
                        } else if (metaData.getColumnType(i) == Types.DOUBLE) {
                            cell.setCellValue(resultSet.getDouble(i));
                        } else {
                            cell.setCellValue(resultSet.getString(i));
                        }
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
}
