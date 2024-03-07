import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;
import java.util.Scanner;

public class ArrayPI {

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
                    inputMatricesAndSaveToDatabase();
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

    public static void inputMatricesAndSaveToDatabase() {
        System.out.print("Введите количество строк первой матрицы: ");
        int rows1 = scanner.nextInt();

        System.out.print("Введите количество столбцов первой матрицы: ");
        int cols1 = scanner.nextInt();

        int[][] matrix1 = new int[rows1][cols1];

        for (int i = 0; i < rows1; i++) {
            for (int j = 0; j < cols1; j++) {
                System.out.print("Введите элемент первой матрицы [" + i + "][" + j + "]: ");
                matrix1[i][j] = scanner.nextInt();
            }
        }

        System.out.print("Введите количество строк второй матрицы: ");
        int rows2 = scanner.nextInt();

        System.out.print("Введите количество столбцов второй матрицы: ");
        int cols2 = scanner.nextInt();

        int[][] matrix2 = new int[rows2][cols2];

        for (int i = 0; i < rows2; i++) {
            for (int j = 0; j < cols2; j++) {
                System.out.print("Введите элемент второй матрицы [" + i + "][" + j + "]: ");
                matrix2[i][j] = scanner.nextInt();
            }
        }

        try {
            saveMatrixToDatabase(matrix1, "Matrix1");
            saveMatrixToDatabase(matrix2, "Matrix2");
            System.out.println("Матрицы сохранены в базе данных.");
        } catch (SQLException e) {
            System.out.println("Ошибка при сохранении матриц в базе данных: " + e.getMessage());
        }
    }

    public static void matrixMultiplicationAndSaveToDatabase() {
        System.out.print("Введите количество строк первой матрицы: ");
        int rows1 = scanner.nextInt();

        System.out.print("Введите количество столбцов первой матрицы: ");
        int cols1 = scanner.nextInt();

        int[][] matrix1 = new int[rows1][cols1];

        System.out.println("Введите элементы первой матрицы:");
        inputMatrix(matrix1);

        System.out.print("Введите количество строк второй матрицы: ");
        int rows2 = scanner.nextInt();

        System.out.print("Введите количество столбцов второй матрицы: ");
        int cols2 = scanner.nextInt();

        int[][] matrix2 = new int[rows2][cols2];

        System.out.println("Введите элементы второй матрицы:");
        inputMatrix(matrix2);

        if (cols1 != rows2) {
            System.out.println("Невозможно перемножить матрицы: количество столбцов первой матрицы не совпадает с количеством строк второй матрицы.");
            return;
        }

        int[][] result = multiplyMatrices(matrix1, matrix2);

        try {
            saveMatrixToDatabase(result, "MatrixResult");
            System.out.println("Результат перемножения матриц сохранен в базе данных.");
            printMatrix(result);
        } catch (SQLException e) {
            System.out.println("Ошибка при сохранении результата в базе данных: " + e.getMessage());
        }
    }

    public static void inputMatrix(int[][] matrix) {
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                matrix[i][j] = scanner.nextInt();
            }
        }
    }

    public static int[][] multiplyMatrices(int[][] matrix1, int[][] matrix2) {
        int rows1 = matrix1.length;
        int cols1 = matrix1[0].length;
        int cols2 = matrix2[0].length;

        int[][] result = new int[rows1][cols2];

        for (int i = 0; i < rows1; i++) {
            for (int j = 0; j < cols2; j++) {
                for (int k = 0; k < cols1; k++) {
                    result[i][j] += matrix1[i][k] * matrix2[k][j];
                }
            }
        }

        return result;
    }

    public static void exportResultsToExcel() {
        try {
            String excelFilePath = "results.xlsx";
            Workbook workbook = new XSSFWorkbook();

            // Сохранение матрицы 1 на отдельном листе
            saveMatrixToExcel(workbook, "Matrix1", "Matrix1");

            // Сохранение матрицы 2 на отдельном листе
            saveMatrixToExcel(workbook, "Matrix2", "Matrix2");

            // Сохранение результата перемножения матриц на отдельном листе
            saveMatrixToExcel(workbook, "MatrixResult", "MatrixResult");

            try (FileOutputStream outputStream = new FileOutputStream(excelFilePath)) {
                workbook.write(outputStream);
            }

            System.out.println("Результаты успешно экспортированы в Excel.");
        } catch (IOException e) {
            System.out.println("Ошибка при экспорте в Excel: " + e.getMessage());
        }
    }

    public static void saveMatrixToExcel(Workbook workbook, String tableName, String sheetName) {
        Sheet sheet = workbook.createSheet(sheetName);

        try {
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
            }
        } catch (SQLException e) {
            System.out.println("Ошибка при сохранении данных в Excel: " + e.getMessage());
        }
    }

    public static void saveMatrixToDatabase(int[][] matrix, String tableName) throws SQLException {
        try (PreparedStatement statement = con.prepareStatement(
                "CREATE TABLE IF NOT EXISTS " + tableName + " (RowNum INT, ColNum INT, Value INT)")) {
            statement.executeUpdate();
        }

        try (PreparedStatement statement = con.prepareStatement(
                "INSERT INTO " + tableName + " (RowNum, ColNum, Value) VALUES (?, ?, ?)")) {
            for (int i = 0; i < matrix.length; i++) {
                for (int j = 0; j < matrix[i].length; j++) {
                    statement.setInt(1, i);
                    statement.setInt(2, j);
                    statement.setInt(3, matrix[i][j]);
                    statement.executeUpdate();
                }
            }
        }
    }

    public static void printMatrix(int[][] matrix) {
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                System.out.print(matrix[i][j] + " ");
            }
            System.out.println();
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
            String sql = "CREATE TABLE IF NOT EXISTS " + tableName + " (OpName VARCHAR(255), OpResult VARCHAR(255))";
            statement.executeUpdate(sql);
            System.out.println("Таблица " + tableName + " создана успешно.");
        } catch (SQLException e) {
            System.out.println("Ошибка при создании таблицы: " + e.getMessage());
        }
    }

    private static void displayTableContents(Connection con, String tableName) {
        String sql = "SELECT * FROM " + tableName;

        try (Statement statement = con.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();

            System.out.println("Содержимое таблицы " + tableName + ":");

            for (int i = 1; i <= columnCount; i++) {
                System.out.print(metaData.getColumnName(i) + "\t");
            }
            System.out.println();

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

    public static Connection getConnection(String url, String username, String password) throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }
}
