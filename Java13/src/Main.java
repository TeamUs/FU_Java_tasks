import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.FileOutputStream;
import java.io.IOException;

public class Main {
    private static String inputTable;

    public static void main(String[] args) {
        try (Connection con = getConnection("jdbc:mysql://localhost/test", "root", "root")) {
            menu(con);
        } catch (SQLException e) {
            System.out.println("Ошибка подключения к базе данных: " + e.getMessage());
        }
    }

    private static Connection getConnection(String url, String username, String password) throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    private static void menu(Connection con) {
        Scanner scanner = new Scanner(System.in);
        Listik listik = new Listik();
        DisplayTables displayTables = new DisplayTables();
        CreateTable createTable = new CreateTable();
        InputList inputList = new InputList();
        DeleteElement deleteElement = new DeleteElement();
        ExportToExcel exportToExcel = new ExportToExcel();

        while (true) {
            System.out.println("Выберите действие:");
            System.out.println("1. Вывести все таблицы из базы данных MySQL.");
            System.out.println("2. Создать таблицу в базе данных MySQL.");
            System.out.println("3. Ввести список и сохранить в MySQL.");
            System.out.println("4. Удалить элемент из списка в MySQL по ID.");
            System.out.println("5. Сохранить итоговые результаты из MySQL в Excel и вывести их в консоль.");
            System.out.println("0. Выйти.");

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    displayTables.displayTables(con);
                    break;
                case 2:
                    createTable.createTable(con);
                    break;
                case 3:
                    System.out.print("Введите название таблицы: ");
                    inputTable = scanner.nextLine();
                    inputList.inputList(con, listik.input(), inputTable);
                    break;
                case 4:
                    System.out.print("Введите ID элемента для удаления: ");
                    int id = scanner.nextInt();
                    scanner.nextLine();
                    deleteElement.deleteElement(con, id, inputTable);
                    break;
                case 5:
                    exportToExcel.exportToExcel(con, inputTable, listik.random());
                    break;
                case 0:
                    System.out.println("Выход из программы.");
                    return;
                default:
                    System.out.println("Некорректный выбор действия. Попробуйте снова.");
            }
        }
    }

    public static class Listik {
        protected List<Integer> random() {
            Random random = new Random();
            return random.ints(1000).boxed().collect(Collectors.toList());
        }

        protected List<String> input() {
            Scanner scanner = new Scanner(System.in);
            List<String> inputList = new ArrayList<>();
            System.out.println("Введите 10 значений:");
            for (int i = 0; i < 10; i++) {
                inputList.add(scanner.nextLine());
            }
            return inputList;
        }
    }

    public static class DisplayTables {
        public void displayTables(Connection con) {
            String sql = "SHOW TABLES";
            try (Statement statement = con.createStatement();
                 ResultSet resultSet = statement.executeQuery(sql)) {
                System.out.println("Список таблиц в базе данных:");
                while (resultSet.next()) {
                    System.out.println(resultSet.getString(1));
                }
            } catch (SQLException e) {
                System.out.println("Ошибка при отображении таблиц: " + e.getMessage());
            }
        }
    }

    public static class CreateTable {
        public void createTable(Connection con) {
            Scanner scanner = new Scanner(System.in);
            System.out.print("Введите название таблицы: ");
            String tableName = scanner.nextLine();
            try (Statement statement = con.createStatement()) {
                String sql = "CREATE TABLE IF NOT EXISTS " + tableName + " (ID INT AUTO_INCREMENT PRIMARY KEY, Data VARCHAR(255))";
                statement.executeUpdate(sql);
                System.out.println("Таблица " + tableName + " создана успешно.");
            } catch (SQLException e) {
                System.out.println("Ошибка при создании таблицы: " + e.getMessage());
            }
        }
    }

    public static class InputList {
        public void inputList(Connection con, List<String> inputList, String tableName) {
            try {
                String sql = "INSERT INTO " + tableName + " (Data) VALUES (?)";
                PreparedStatement preparedStatement = con.prepareStatement(sql);
                for (String data : inputList) {
                    preparedStatement.setString(1, data);
                    preparedStatement.addBatch();
                }
                preparedStatement.executeBatch();
                System.out.println("Список успешно сохранен в MySQL.");
            } catch (SQLException e) {
                System.out.println("Ошибка при сохранении списка в MySQL: " + e.getMessage());
            }
        }
    }

    public static class DeleteElement {
        public void deleteElement(Connection con, int id, String tableName) {
            try {
                String sql = "DELETE FROM " + tableName + " WHERE ID = ?";
                PreparedStatement preparedStatement = con.prepareStatement(sql);
                preparedStatement.setInt(1, id);
                int rowsAffected = preparedStatement.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Элемент с ID " + id + " успешно удален.");
                } else {
                    System.out.println("Элемент с ID " + id + " не найден.");
                }
            } catch (SQLException e) {
                System.out.println("Ошибка при удалении элемента из списка в MySQL: " + e.getMessage());
            }
        }
    }

    public static class ExportToExcel {
        public void exportToExcel(Connection con, String tableName, List<Integer> randomList) {
            try {
                String excelFilePath = "results.xlsx";
                Workbook workbook = new XSSFWorkbook();

                // Сохранение рандомных значений в отдельный лист
                Sheet randomSheet = workbook.createSheet("Random");
                int rowNum = 0;
                for (Integer value : randomList) {
                    Row row = randomSheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(value);
                }
                randomSheet.autoSizeColumn(0); // Автоматическая подстройка ширины колонки

                // Сохранение данных из базы в другой лист
                Sheet dataSheet = workbook.createSheet("Data");
                String sql = "SELECT ID, Data FROM " + tableName;

                try (PreparedStatement preparedStatement = con.prepareStatement(sql);
                     ResultSet resultSet = preparedStatement.executeQuery()) {
                    ResultSetMetaData metaData = resultSet.getMetaData();
                    int columnCount = metaData.getColumnCount();
                    Row headerRow = dataSheet.createRow(0);
                    for (int i = 1; i <= columnCount; i++) {
                        headerRow.createCell(i - 1).setCellValue(metaData.getColumnName(i));
                    }
                    int rowNumber = 1;
                    while (resultSet.next()) {
                        Row row = dataSheet.createRow(rowNumber++);
                        for (int i = 1; i <= columnCount; i++) {
                            row.createCell(i - 1).setCellValue(resultSet.getString(i));
                        }
                    }
                    for (int i = 0; i < columnCount; i++) {
                        dataSheet.autoSizeColumn(i);
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
}
