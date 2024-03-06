import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;
import java.util.Scanner;

public class NumberChecker {

    //Объявляем объект Scanner для считывания ввода пользователя
    private static Scanner scanner = new Scanner(System.in);

    //Объявляем объект Connection для подключения к базе данных
    private static Connection con;

    //Точка входа в программу
    public static void main(String[] args) {
        try {
            //Устанавливаем соединение с базой данных MySQL
            con = getConnection("jdbc:mysql://localhost/test", "root", "root");
            //Выполняем операции с числами
            performStringOperations();
        } catch (SQLException e) {
            //В случае ошибки выводим сообщение
            System.out.println("Ошибка подключения к базе данных: " + e.getMessage());
        }
    }

    //Метод для выполнения операций с числами
    private static void performStringOperations() {
        //Бесконечный цикл для выполнения операций, пока пользователь не выберет выход
        while (true) {
            //Выводим меню операций
            System.out.println("Выберите действие:");
            System.out.println("1. Вывести все таблицы из MySQL.");
            System.out.println("2. Создать таблицу в MySQL.");
            System.out.println("3. Проверить целостность и четность чисел результат сохранить в MySQL с последующим выводом в консоль.");
            System.out.println("4. Сохранить все данные (вышеполученные результаты) из MySQL в Excel и вывести на экран.");
            System.out.println("0. Выйти.");

            //Считываем выбор пользователя
            int choice = scanner.nextInt();
            scanner.nextLine();

            //Обрабатываем выбор пользователя с помощью оператора switch
            switch (choice) {
                case 1:
                    try {
                        //Отображаем все таблицы в базе данных MySQL
                        displayTables(con);
                    } catch (SQLException e) {
                        //В случае ошибки выводим сообщение
                        System.out.println("Ошибка при отображении таблиц: " + e.getMessage());
                    }
                    break;
                case 2:
                    //Создаем новую таблицу в базе данных MySQL
                    createTable(con);
                    break;
                case 3:
                    //Проверяем целостность и четность чисел
                    checkIntegrityAndEvenness();
                    break;
                case 4:
                    //Экспортируем все данные из MySQL в Excel и выводим на экран
                    exportToExcel(con);
                    break;
                case 0:
                    //Выходим из программы
                    System.out.println("Выход из программы.");
                    return;
                default:
                    System.out.println("Некорректный выбор действия. Попробуйте снова.");
            }
        }
    }

    //Метод для отображения таблиц в базе данных MySQL
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

    //Метод для создания таблицы в базе данных MySQL
    private static void createTable(Connection con) {
        System.out.println("Введите название таблицы:");
        String tableName = scanner.next();

        try (Statement statement = con.createStatement()) {
            String sql = "CREATE TABLE IF NOT EXISTS " + tableName + " (Num INT, OpResult1 VARCHAR(255),  OpResult2 VARCHAR(255))";
            statement.executeUpdate(sql);
            System.out.println("Таблица " + tableName + " создана успешно.");
        } catch (SQLException e) {
            System.out.println("Ошибка при создании таблицы: " + e.getMessage());
        }
    }

    //Метод для проверки целостности и четности чисел
    private static void checkIntegrityAndEvenness() {
        System.out.println("Введите числа через пробел:");
        String input = scanner.nextLine();
        String[] numbers = input.split("\\s+");

        for (String number : numbers) {
            try {
                int num = Integer.parseInt(number);
                System.out.println("Число " + num + " является целым.");
                if (num % 2 == 0) {
                    System.out.println("Число " + num + " является четным.");
                } else {
                    System.out.println("Число " + num + " является нечетным.");
                }
                System.out.println("Введите название таблицы, куда сохранить результат: ");
                saveResultToDatabase(num, "Является целым", (num % 2 == 0) ? "Является четным" : "Является нечетным", scanner.next());
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: '" + number + "' не является целым числом.");
            }
        }
    }

    //Метод для отображения содержимого таблицы в базе данных MySQL
    private static void displayTableContents(Connection con, String tableName) {
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

    //Метод для экспорта всех данных из MySQL в Excel
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

    //Метод для сохранения результата в базе данных MySQL
    private static void saveResultToDatabase(int num, String opResult1, String opResult2, String tableName) {
        try (PreparedStatement statement = con.prepareStatement(
                "INSERT INTO " + tableName + " (Num, OpResult1, OpResult2) VALUES (?, ?, ?)")) {
            statement.setInt(1, num);
            statement.setString(2, opResult1);
            statement.setString(3, opResult2);
            statement.executeUpdate();
            System.out.println("Результат сохранен в базе данных.");
        } catch (SQLException e) {
            System.out.println("Ошибка при сохранении результата в базе данных: " + e.getMessage());
            //Если таблица не существует, перенаправляем пользователя на создание таблицы
            if (e.getMessage().contains("doesn't exist")) {
                System.out.println("Таблица '" + tableName + "' не существует. Создайте новую таблицу.");
                createTable(con); //Вызываем метод создания таблицы
                //После создания таблицы просим пользователя ввести название таблицы снова
                System.out.println("Введите название таблицы, куда сохранить результат: ");
                String newTableName = scanner.next();
                //Рекурсивно вызываем метод сохранения с новым названием таблицы
                saveResultToDatabase(num, opResult1, opResult2, newTableName);
            }
        }
    }

    //Метод для установления соединения с базой данных MySQL
    private static Connection getConnection(String url, String username, String password) throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }
}
