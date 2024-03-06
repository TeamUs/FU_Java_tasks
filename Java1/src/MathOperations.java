import java.sql.*;
import java.util.Scanner;

public class MathOperations {
    protected static Scanner scanner = new Scanner(System.in);

    protected static String mysqlUrl = "jdbc:mysql://localhost/test";

    protected static Connection con;

    static {
        try {
            con = DriverManager.getConnection(mysqlUrl, "root", "root");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws SQLException {
        MathOperations mathOperations = new MathOperations(); // Создаем экземпляр класса, чтобы получить доступ к нестатическим полям и методам

        int dataTypeChoice;

        do {
            System.out.println("Выберите тип данных:");
            System.out.println("1. Целочисленный");
            System.out.println("2. Байтовый");
            System.out.println("3. Вещественный");
            System.out.println("0. Выход");

            // Используем сканнер экземпляра класса, чтобы получить ввод от пользователя
            dataTypeChoice = mathOperations.scanner.nextInt();

            switch (dataTypeChoice) {
                case 1:
                    // Используем метод main класса PerformIntegerOperations
                    PerformIntegerOperations.main(args);
                    break;
                case 2:
                    // Вызываем метод performByteOperations(), который требуется создать
                    PerformByteOperations.main(args);
                    break;
                case 3:
                    // Вызываем метод performDoubleOperations(), который требуется создать
                    PerformDoubleOperations.main(args);
                    break;
                case 0:
                    System.out.println("Выход из программы.");
                    break;
                default:
                    System.out.println("Некорректный выбор.");
            }
        } while (dataTypeChoice != 0);

        mathOperations.scanner.close(); // Закрываем сканнер после использования
    }
}
