import java.sql.*;
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



