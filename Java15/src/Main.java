import java.sql.*;
import java.util.*;
import java.io.FileOutputStream;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.IOException;

public class Main {
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
        DisplayTables displayTables = new DisplayTables();
        CreateTable createTable = new CreateTable();
        SaveDataToMySQL saveDataToMySQL = new SaveDataToMySQL();
        DeleteElement deleteElement = new DeleteElement();
        ExportToExcel exportToExcel = new ExportToExcel();
        Listik listik = new Listik();
        Listik1 listik1 = new Listik1();

        while (true) {
            System.out.println("Выберите действие:");
            System.out.println("1. Вывести все таблицы из базы данных MySQL.");
            System.out.println("2. Создать таблицу в базе данных MySQL.");
            System.out.println("3. Сохранить вводимый с клавиатуры список, а также строку и множество в MySQL.");
            System.out.println("4. Удалить элемент из списка, строки и множества в MySQL по ID.");
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
                    List<Integer> inputList = listik.input(); // Получаем список, введенный пользователем
                    Set<Integer> inputSet = listik1.convert(inputList); // Преобразуем список в множество
                    saveDataToMySQL.saveData(con, inputList, inputSet);
                    listik1.convertAndPrint(inputList); // Передаем список в метод convertAndPrint класса Listik1
                    break;
                case 4:
                    deleteElement.deleteElement(con);
                    break;
                case 5:
                    exportToExcel.exportToExcel(con);
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
        public List<Integer> input() {
            Scanner scanner = new Scanner(System.in);
            List<Integer> inputList = new ArrayList<>();
            System.out.println("Введите не менее 1 и не более 50 чисел:");
            for (int i = 0; i < 50; i++) {
                System.out.print("Введите число " + (i + 1) + ": ");
                int number = scanner.nextInt();
                inputList.add(number);
            }
            return inputList;
        }
    }

    public static class Listik1 extends Listik {
        public Set<Integer> convert(List<Integer> inputList) {
            return new HashSet<>(inputList); // Преобразуем список в множество
        }

        public void convertAndPrint(List<Integer> inputList) {
            ArrayList<Integer> inputList1 = new ArrayList<>(inputList); // Создаем новый ArrayList с копией введенного списка
            System.out.println("Список:");
            System.out.println(inputList1);
            // Создаем множество на основе введенного списка
            Set<Integer> inputSet = new HashSet<>(inputList);
            System.out.println("Множество:");
            System.out.println(inputSet);
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
                String sql = "CREATE TABLE IF NOT EXISTS " + tableName + " (ID INT AUTO_INCREMENT PRIMARY KEY, Data INT, DataList TEXT, DataSet TEXT)";
                statement.executeUpdate(sql);
                System.out.println("Таблица " + tableName + " создана успешно.");
            } catch (SQLException e) {
                System.out.println("Ошибка при создании таблицы: " + e.getMessage());
            }
        }
    }

    public static class SaveDataToMySQL {
        public void saveData(Connection con, List<Integer> inputList, Set<Integer> inputSet) {
            try {
                Scanner scanner = new Scanner(System.in);
                System.out.print("Введите название таблицы: ");
                String tableName = scanner.nextLine();

                String sql = "INSERT INTO " + tableName + " (Data, DataList, DataSet) VALUES (?, ?, ?)";
                PreparedStatement preparedStatement = con.prepareStatement(sql);
                for (Integer data : inputList) {
                    preparedStatement.setInt(1, data);
                    preparedStatement.setString(2, String.valueOf(data)); // Сохраняем каждый элемент списка в отдельной строке
                    preparedStatement.setString(3, setToString(inputSet));
                    preparedStatement.addBatch();
                }
                preparedStatement.executeBatch();
                System.out.println("Данные успешно сохранены в MySQL.");
            } catch (SQLException e) {
                System.out.println("Ошибка при сохранении данных в MySQL: " + e.getMessage());
            }
        }


        private String setToString(Set<Integer> set) {
            return set.toString().replace("[", "").replace("]", "").replace(", ", ",");
        }
    }

    public static class DeleteElement {
        public void deleteElement(Connection con) {
            Scanner scanner = new Scanner(System.in);
            System.out.print("Введите название таблицы: ");
            String tableName = scanner.nextLine();

            System.out.print("Введите ID элемента для удаления: ");
            int id = scanner.nextInt();
            scanner.nextLine(); // Очистка буфера
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
        public void exportToExcel(Connection con) {
            try {
                Scanner scanner = new Scanner(System.in);
                System.out.print("Введите название таблицы: ");
                String tableName = scanner.nextLine();

                String excelFilePath = "results.xlsx";
                Workbook workbook = new XSSFWorkbook();
                Sheet resultsSheet = workbook.createSheet("Results");
                Sheet listSheet = workbook.createSheet("List");
                Sheet setSheet = workbook.createSheet("Set");

                String sql = "SELECT * FROM " + tableName;

                try (Statement statement = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                     ResultSet resultSet = statement.executeQuery(sql)) {
                    ResultSetMetaData metaData = resultSet.getMetaData();
                    int columnCount = metaData.getColumnCount();

                    // Создаем заголовки для страницы Results
                    Row headerRow = resultsSheet.createRow(0);
                    int headerIndex = 0;
                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = metaData.getColumnName(i);
                        if (!columnName.equals("DataList") && !columnName.equals("DataSet")) {
                            headerRow.createCell(headerIndex++).setCellValue(columnName);
                        }
                    }

                    // Заполняем страницу Results
                    int rowNumber = 1;
                    while (resultSet.next()) {
                        Row row = resultsSheet.createRow(rowNumber++);
                        int cellIndex = 0;
                        for (int i = 1; i <= columnCount; i++) {
                            String columnName = metaData.getColumnName(i);
                            if (!columnName.equals("DataList") && !columnName.equals("DataSet")) {
                                row.createCell(cellIndex++).setCellValue(resultSet.getString(i));
                            }
                        }
                    }

                    // Создаем и заполняем страницы List и Set
                    List<String> dataList = new ArrayList<>();
                    Set<String> dataSet = new HashSet<>();
                    resultSet.beforeFirst();  // Перемещаемся в начало ResultSet для повторного чтения
                    while (resultSet.next()) {
                        dataList.add(resultSet.getString("DataList"));
                        dataSet.add(resultSet.getString("DataSet"));
                    }

                    // Заполняем страницу List
                    rowNumber = 0;
                    for (String data : dataList) {
                        for (String element : data.split(",")) {
                            Row row = listSheet.createRow(rowNumber++);
                            row.createCell(0).setCellValue(element.trim());
                        }
                    }

                    // Заполняем страницу Set
                    rowNumber = 0;
                    for (String data : dataSet) {
                        for (String element : data.split(",")) {
                            Row row = setSheet.createRow(rowNumber++);
                            row.createCell(0).setCellValue(element.trim());
                        }
                    }

                    // Авторазмер колонок
                    for (int i = 0; i < headerIndex; i++) {
                        resultsSheet.autoSizeColumn(i);
                    }

                    listSheet.autoSizeColumn(0);
                    setSheet.autoSizeColumn(0);

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
