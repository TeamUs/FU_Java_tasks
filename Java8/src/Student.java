import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;


abstract class Student {
    private String name;
    private int age;

    public Student(String name, int age) {
        this.name = name;
        this.age = age;
    }
    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }


    public void addToDatabase(Connection con, String tableName) {
        try {
            String sql = "INSERT INTO " + tableName + " (Name, Age) VALUES (?, ?)";
            try (PreparedStatement preparedStatement = con.prepareStatement(sql)) {
                preparedStatement.setString(1, name);
                preparedStatement.setInt(2, age);
                preparedStatement.executeUpdate();
                System.out.println("Студент успешно добавлен в базу данных.");
            }
        } catch (SQLException e) {
            System.out.println("Ошибка при добавлении студента в базу данных: " + e.getMessage());
        }
    }
}

class Worker extends Student {
    private int salary;

    public Worker(String name, int age, int salary) {
        super(name, age);
        this.salary = salary;
    }

    @Override
    public void addToDatabase(Connection con, String tableName) {
        try {
            String sql = "INSERT INTO " + tableName + " (Name, Age, Salary) VALUES (?, ?, ?)";
            try (PreparedStatement preparedStatement = con.prepareStatement(sql)) {
                preparedStatement.setString(1, getName());
                preparedStatement.setInt(2, getAge());
                preparedStatement.setInt(3, salary);
                preparedStatement.executeUpdate();
                System.out.println("Работник успешно добавлен в базу данных.");
            }
        } catch (SQLException e) {
            System.out.println("Ошибка при добавлении работника в базу данных: " + e.getMessage());
        }
    }

}
