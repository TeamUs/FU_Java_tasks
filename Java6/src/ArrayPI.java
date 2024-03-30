import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.ArrayType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;
import java.util.Arrays;
import java.util.Scanner;


public class ArrayPI {
    private static double[][] matrix1;
    private static double[][] matrix2;

    public static Scanner scanner = new Scanner(System.in);
    public static Connection con;

    public static void main(String[] args) {
        try {
            con = getConnection("jdbc:mysql://localhost/test", "root", "root");
            performMatrixOperations();
        } catch (SQLException e) {
            System.out.println("Ошибка подключения к базе данных: " + e.getMessage());
        }
    }

    public static void performMatrixOperations() {
        while (true) {
            System.out.println("Выберите действие:");
            System.out.println("1. Вывести все таблицы из MySQL.");
            System.out.println("2. Создать таблицу в MySQL.");
            System.out.println("3. Ввести две матрицы с клавиатуры и каждую из них сохранить в MySQL с последующим выводом в консоль.");
            System.out.println("4. Перемножить матрицу, сохранить перемноженную матрицу в MySQL и вывести в консоль.");
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
                    inputMatrices();
                    break;
                case 4:
                    matrixMultiplicationAndSaveToDatabase();
                    break;
                case 5:
                    exportResultsToExcel();
                    break;
                case 0:
                    System.out.println("Выход из программы.");
                    return;
                default:
                    System.out.println("Некорректный выбор действия. Попробуйте снова.");
            }
        }
    }

    public static void inputMatrices() {
        System.out.println("Введите название таблицы для сохранения матриц:");
        String tableName = scanner.nextLine();

        int rows1, columns1, rows2, columns2;
        do {
            System.out.print("Введите количество строк первой матрицы: ");
            rows1 = scanner.nextInt();
            System.out.print("Введите количество столбцов первой матрицы: ");
            columns1 = scanner.nextInt();

            System.out.print("Введите количество строк второй матрицы: ");
            rows2 = scanner.nextInt();
            System.out.print("Введите количество столбцов второй матрицы: ");
            columns2 = scanner.nextInt();

            if (columns1 != rows2) {
                System.out.println("Невозможно перемножить матрицы, так как число столбцов первой матрицы не совпадает с числом строк второй матрицы.");
                System.out.println("Пожалуйста, укажите новые размерности матриц.");
            }
        } while (columns1 != rows2);

        System.out.println("Введите элементы первой матрицы:");
        matrix1 = inputMatrix(rows1, columns1);
        System.out.println("Первая матрица:");
        printMatrix(matrix1);

        System.out.println("Введите элементы второй матрицы:");
        matrix2 = inputMatrix(rows2, columns2);
        System.out.println("Вторая матрица:");
        printMatrix(matrix2);

        saveResultToDatabase(tableName, matrix1, matrix2);
    }

    public static double[][] inputMatrix(int rows, int columns) {
        double[][] matrix = new double[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                System.out.printf("Введите элемент матрицы [%d][%d]: ", i, j);
                matrix[i][j] = scanner.nextDouble();
            }
        }
        return matrix;
    }

    public static void printMatrix(double[][] matrix) {
        for (double[] row : matrix) {
            for (double element : row) {
                System.out.print(element + " ");
            }
            System.out.println();
        }
    }

    public static void matrixMultiplicationAndSaveToDatabase() {
        if (matrix1 == null || matrix2 == null) {
            System.out.println("Пожалуйста, сначала введите матрицы.");
            return;
        }

        try {
            Matrix.multiplyAndPrintResult(matrix1, matrix2);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }
    public static double[][] multiplyMatrices(double[][] matrix1, double[][] matrix2) {
        if (matrix1[0].length != matrix2.length) {
            throw new IllegalArgumentException("Нельзя перемножить матрицы с данными размерами");
        }

        double[][] resultMatrix = new double[matrix1.length][matrix2[0].length];

        for (int i = 0; i < matrix1.length; i++) {
            for (int j = 0; j < matrix2[0].length; j++) {
                for (int k = 0; k < matrix2.length; k++) {
                    resultMatrix[i][j] += matrix1[i][k] * matrix2[k][j];
                }
            }
        }
        return resultMatrix;
    }
    public static void saveResultToDatabase(String tableName, double[][] matrix1, double[][] matrix2) {
        double[][] resultMatrix = multiplyMatrices(matrix1, matrix2);

        try (PreparedStatement statement = con.prepareStatement("INSERT INTO " + tableName + " (Matrix1, Matrix2, Result) VALUES (?, ?, ?)")) {
            statement.setString(1, Arrays.deepToString(matrix1));
            statement.setString(2, Arrays.deepToString(matrix2));
            statement.setString(3, Arrays.deepToString(resultMatrix));
            statement.executeUpdate();
            System.out.println("Матрицы успешно сохранены в таблице '" + tableName + "'.");
        } catch (SQLException e) {
            System.out.println("Ошибка при сохранении матриц и результата: " + e.getMessage());
        }
    }


    public static void exportResultsToExcel() {
        try {
            String excelFilePath = "results.xlsx";
            Workbook workbook = new XSSFWorkbook();

            System.out.println("Введите название таблицы, содержащей результаты для экспорта:");
            String tableName = scanner.nextLine();

            // Создаем соединение с базой данных
            try (Connection con = getConnection("jdbc:mysql://localhost/test", "root", "root");
                 Statement statement = con.createStatement()) {

                // Выполняем SQL-запрос для получения данных из таблицы
                String sql = "SELECT Matrix1, Matrix2, Result FROM " + tableName;
                ResultSet resultSet = statement.executeQuery(sql);

                // Итерируемся по результатам запроса
                while (resultSet.next()) {
                    // Получаем строки для каждой матрицы
                    String matrix1String = resultSet.getString("Matrix1");
                    String matrix2String = resultSet.getString("Matrix2");
                    String resultMatrixString = resultSet.getString("Result");

                    // Преобразуем JSON-строки в массивы двумерных массивов типа double
                    double[][] matrix1 = deserializeMatrix(matrix1String);
                    double[][] matrix2 = deserializeMatrix(matrix2String);
                    double[][] resultMatrix = deserializeMatrix(resultMatrixString);

                    // Создаем новый лист для каждой матрицы и результата
                    Sheet matrix1Sheet = workbook.createSheet("Matrix1");
                    exportMatrixToSheet(matrix1, matrix1Sheet);

                    Sheet matrix2Sheet = workbook.createSheet("Matrix2");
                    exportMatrixToSheet(matrix2, matrix2Sheet);

                    Sheet resultSheet = workbook.createSheet("Result");
                    exportMatrixToSheet(resultMatrix, resultSheet);
                }

                // Сохраняем книгу Excel на диск
                try (FileOutputStream outputStream = new FileOutputStream(excelFilePath)) {
                    workbook.write(outputStream);
                }

                System.out.println("Результаты успешно экспортированы в Excel.");
            } catch (SQLException | IOException e) {
                System.out.println("Ошибка при экспорте в Excel: " + e.getMessage());
            }
        } catch (Exception e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    public static void exportMatrixToSheet(double[][] matrix, Sheet sheet) {
        int rowCount = 0;
        for (double[] row : matrix) {
            Row excelRow = sheet.createRow(rowCount++);
            int columnCount = 0;
            for (double value : row) {
                excelRow.createCell(columnCount++).setCellValue(value);
            }
        }
    }

    public static double[][] deserializeMatrix(String matrixString) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            ArrayType arrayType = objectMapper.getTypeFactory().constructArrayType(double[].class);
            return objectMapper.readValue(matrixString, arrayType);
        } catch (IOException e) {
            System.out.println("Ошибка при десериализации матрицы: " + e.getMessage());
            return null;
        }
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

    private static void createTable(Connection con) {
        System.out.println("Введите название таблицы:");
        String tableName = scanner.next();

        try (Statement statement = con.createStatement()) {
            String sql = "CREATE TABLE IF NOT EXISTS " + tableName + " (id INT AUTO_INCREMENT PRIMARY KEY, Matrix1 TEXT, Matrix2 TEXT, Result TEXT)";
            statement.executeUpdate(sql);
            System.out.println("Таблица '" + tableName + "' успешно создана.");
        } catch (SQLException e) {
            System.out.println("Ошибка при создании таблицы: " + e.getMessage());
        }
    }

    public static Connection getConnection(String url, String username, String password) throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    public static class Matrix {
        public static void multiplyAndPrintResult(double[][] matrix1, double[][] matrix2) {
            if (matrix1[0].length != matrix2.length) {
                throw new IllegalArgumentException("Нельзя перемножить матрицы с данными размерами");
            }

            double[][] resultMatrix = new double[matrix1.length][matrix2[0].length];

            for (int i = 0; i < matrix1.length; i++) {
                for (int j = 0; j < matrix2[0].length; j++) {
                    for (int k = 0; k < matrix2.length; k++) {
                        resultMatrix[i][j] += matrix1[i][k] * matrix2[k][j];
                    }
                }
            }

            System.out.println("Результат умножения матриц:");
            printMatrix(resultMatrix);
            System.out.println("Результат перемножения матриц успешно сохранен в MySql.");
        }
    }
}
