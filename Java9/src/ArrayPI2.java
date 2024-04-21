import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.ArrayType;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;
import java.util.Arrays;
import java.util.Scanner;

public class ArrayPI2 {
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
            System.out.println("4. Перемножить, сложить, вычесть, возвести в степень матрицы, а также сохранить результаты в MySQL и вывести в консоль.");
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
                    performMatrixOperationsInternal();
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

    public static void performMatrixOperationsInternal() {
        // Проверяем, были ли введены матрицы
        if (matrix1 == null || matrix2 == null) {
            System.out.println("Пожалуйста, введите две матрицы.");
            return;
        }

        // Запрашиваем у пользователя название таблицы для сохранения результатов
        System.out.println("Введите название таблицы для сохранения результатов:");
        String tableName = scanner.nextLine();

        // Вывод матриц перед выполнением операций
        System.out.println("Первая матрица:");
        printMatrix(matrix1);
        System.out.println("Вторая матрица:");
        printMatrix(matrix2);

        // Выполнение математических операций и вывод результатов
        performMatrixMultiplication(tableName);
        performMatrixAddition(tableName);
        performMatrixSubtraction(tableName);
        performMatrixPower(tableName, 2); // Пример возведения в квадрат
        deleteAllExceptOneRow(con, tableName);
    }

    public static double[][] multiplyMatrices(double[][] matrix1, double[][] matrix2) {
        if (matrix1[0].length != matrix2.length) {
            throw new IllegalArgumentException("Нельзя перемножить матрицы с данными размерами");
        }

        int m = matrix1.length;
        int n = matrix2[0].length;
        int p = matrix2.length;
        double[][] result = new double[m][n];

        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                for (int k = 0; k < p; k++) {
                    result[i][j] += matrix1[i][k] * matrix2[k][j];
                }
            }
        }

        return result;
    }

    public static double[][] addMatrices(double[][] matrix1, double[][] matrix2) {
        if (matrix1.length != matrix2.length || matrix1[0].length != matrix2[0].length) {
            throw new IllegalArgumentException("Невозможно сложить матрицы с разными размерами");
        }

        int m = matrix1.length;
        int n = matrix1[0].length;
        double[][] result = new double[m][n];

        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                result[i][j] = matrix1[i][j] + matrix2[i][j];
            }
        }

        return result;
    }

    public static double[][] subtractMatrices(double[][] matrix1, double[][] matrix2) {
        if (matrix1.length != matrix2.length || matrix1[0].length != matrix2[0].length) {
            throw new IllegalArgumentException("Невозможно вычесть матрицы с разными размерами");
        }

        int m = matrix1.length;
        int n = matrix1[0].length;
        double[][] result = new double[m][n];

        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                result[i][j] = matrix1[i][j] - matrix2[i][j];
            }
        }

        return result;
    }

    public static double[][] powerMatrix(double[][] matrix, int power) {
        if (matrix.length != matrix[0].length) {
            throw new IllegalArgumentException("Матрица должна быть квадратной для возведения в степень");
        }

        int n = matrix.length;
        double[][] result = new double[n][n];

        if (power == 0) {
            for (int i = 0; i < n; i++) {
                result[i][i] = 1; // Единичная матрица
            }
        } else {
            result = matrix;
            for (int i = 1; i < power; i++) {
                result = multiplyMatrices(result, matrix);
            }
        }

        return result;
    }

    public static void performMatrixMultiplication(String tableName) {
        try {
            double[][] resultMatrix = multiplyMatrices(matrix1, matrix2);
            System.out.println("Результат умножения матриц:");
            printMatrix(resultMatrix);
            saveResultToDatabase(tableName, matrix1, matrix2, resultMatrix, null, null, null);
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка при умножении матриц: " + e.getMessage());
        }
    }

    public static void performMatrixAddition(String tableName) {
        try {
            double[][] resultMatrix = addMatrices(matrix1, matrix2);
            System.out.println("Результат сложения матриц:");
            printMatrix(resultMatrix);
            saveResultToDatabase(tableName, matrix1, matrix2, null, resultMatrix, null, null);
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка при сложении матриц: " + e.getMessage());
        }
    }

    public static void performMatrixSubtraction(String tableName) {
        try {
            double[][] resultMatrix = subtractMatrices(matrix1, matrix2);
            System.out.println("Результат вычитания матриц:");
            printMatrix(resultMatrix);
            saveResultToDatabase(tableName, matrix1, matrix2, null, null, resultMatrix, null);
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка при вычитании матриц: " + e.getMessage());
        }
    }

    // Метод для возведения матрицы в степень
    public static void performMatrixPower(String tableName, int power) {
        try {
            double[][] resultMatrix = powerMatrix(matrix1, power);
            System.out.println("Результат возведения матрицы в степень " + power + ":");
            printMatrix(resultMatrix);
            saveResultToDatabase(tableName, matrix1, matrix1, null, null, null, resultMatrix);
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка при возведении матрицы в степень: " + e.getMessage());
        }
    }

    public static void deleteAllExceptOneRow(Connection connection, String tableName) {
        try (Statement statement = connection.createStatement()) {
            // Выбор ID строки, которую нужно оставить
            String selectSql = "SELECT id FROM " + tableName + " ORDER BY id LIMIT 1";
            ResultSet resultSet = statement.executeQuery(selectSql);
            int idToKeep = -1;
            if (resultSet.next()) {
                idToKeep = resultSet.getInt("id");
            }

            // Удаление всех строк, кроме выбранной
            if (idToKeep != -1) {
                String deleteSql = "DELETE FROM " + tableName + " WHERE id != " + idToKeep;
                int rowsAffected = statement.executeUpdate(deleteSql);
            }
        } catch (SQLException e) {
            System.out.println("Ошибка при выполнении запроса: " + e.getMessage());
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

        // Выполняем операции
        double[][] multiplicationResult = multiplyMatrices(matrix1, matrix2);
        double[][] additionResult = addMatrices(matrix1, matrix2);
        double[][] subtractionResult = subtractMatrices(matrix1, matrix2);
        double[][] powerResult = powerMatrix(matrix1, 2); // Пример возведения в квадрат

        // Сохраняем все результаты в базу данных
        saveResultToDatabase(tableName, matrix1, matrix2, multiplicationResult, additionResult, subtractionResult, powerResult);
    }

    public static void saveResultToDatabase(String tableName, double[][] matrix1, double[][] matrix2, double[][] multiplicationResult, double[][] additionResult, double[][] subtractionResult, double[][] powerResult) {
        try (PreparedStatement statement = con.prepareStatement("INSERT INTO " + tableName + " (Matrix1, Matrix2, Multiplication, Addition, Subtraction, Power) VALUES (?, ?, ?, ?, ?, ?)")) {
            statement.setString(1, matrixToString(matrix1));
            statement.setString(2, matrixToString(matrix2));
            statement.setString(3, matrixToString(multiplicationResult));
            statement.setString(4, matrixToString(additionResult));
            statement.setString(5, matrixToString(subtractionResult));
            statement.setString(6, matrixToString(powerResult));
            statement.executeUpdate();
            System.out.println("Матрицы и результаты операций успешно сохранены в таблице '" + tableName + "'.");
        } catch (SQLException e) {
            System.out.println("Ошибка при сохранении матриц и результатов: " + e.getMessage());
        }
    }

    public static String matrixToString(double[][] matrix) {
        if (matrix == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < matrix.length; i++) {
            sb.append(Arrays.toString(matrix[i]));
            if (i < matrix.length - 1) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
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

    public static void exportResultsToExcel() {
        try {
            Workbook workbook = new XSSFWorkbook();
            System.out.println("Введите название таблицы, содержащей результаты для экспорта:");
            String tableName = scanner.nextLine();

            try (Connection con = getConnection("jdbc:mysql://localhost/test", "root", "root");
                 Statement statement = con.createStatement()) {

                String sql = "SELECT Matrix1, Matrix2, Multiplication, Addition, Subtraction, Power FROM " + tableName;
                ResultSet resultSet = statement.executeQuery(sql);

                Sheet[] sheets = new Sheet[6];
                String[] sheetNames = {"Matrix1", "Matrix2", "Multiplication", "Addition", "Subtraction", "Power"};

                for (int i = 0; i < 6; i++) {
                    sheets[i] = workbook.createSheet(sheetNames[i]);
                }

                int rowCount = 0;
                while (resultSet.next()) {
                    String[] data = {resultSet.getString("Matrix1"), resultSet.getString("Matrix2"),
                            resultSet.getString("Multiplication"), resultSet.getString("Addition"),
                            resultSet.getString("Subtraction"), resultSet.getString("Power")};

                    for (int i = 0; i < 6; i++) {
                        double[][] matrix = deserializeMatrix(data[i]);
                        exportMatrixToSheet(matrix, sheets[i], rowCount);
                    }

                    rowCount += data.length;

                }

                String excelFilePath = "results.xlsx";
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

    private static void exportMatrixToSheet(double[][] matrix, Sheet sheet, int startRow) {
        int rownum = startRow;
        for (double[] row : matrix) {
            Row excelRow = sheet.createRow(rownum++);
            int cellnum = 0;
            for (double value : row) {
                Cell cell = excelRow.createCell(cellnum++);
                cell.setCellValue(value);
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
            String sql = "CREATE TABLE IF NOT EXISTS " + tableName + " (id INT AUTO_INCREMENT PRIMARY KEY, Matrix1 TEXT, Matrix2 TEXT, Multiplication TEXT, Addition TEXT, Subtraction TEXT, Power TEXT)";
            statement.executeUpdate(sql);
            System.out.println("Таблица '" + tableName + "' успешно создана.");
        } catch (SQLException e) {
            System.out.println("Ошибка при создании таблицы: " + e.getMessage());
        }
    }

    public static Connection getConnection(String url, String username, String password) throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }
}
