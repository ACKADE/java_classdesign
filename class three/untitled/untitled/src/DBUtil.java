import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DBUtil {
    private static final String HOST_URL = "jdbc:mysql://localhost:3306/";
    private static final String DB_NAME = "text";
    private static final String PARAMS = "?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8";

    // 如果你的 MySQL 账号密码不同，只需要改这里。
    private static final String USER = "root";
    private static final String PASSWORD = "liyouxuan81";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("未找到 MySQL JDBC 驱动，请检查 lib/mysql-connector-j-9.6.0.jar", e);
        }
    }

    private DBUtil() {
    }

    public static Connection getServerConnection() throws SQLException {
        return DriverManager.getConnection(HOST_URL + PARAMS, USER, PASSWORD);
    }

    public static Connection getAppConnection() throws SQLException {
        return DriverManager.getConnection(HOST_URL + DB_NAME + PARAMS, USER, PASSWORD);
    }
}

