package data;

import lombok.SneakyThrows;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLHelper {
    private static final QueryRunner query_runner = new QueryRunner();

    private SQLHelper() {
    }

    private static Connection getConnected() throws SQLException { // подключение к БД
        return DriverManager.getConnection(System.getProperty("db.url"), "app", "pass");
    }

    @SneakyThrows
    public static DataHelper.VerificationCode getVerificationCode() { // последний проверочный код
        var codeSQL = "SELECT code FROM auth_codes ORDER BY created DESC LIMIT 1"; // все коды сортированные по дате лимит 1
        var conn = getConnected();
        var code = query_runner.query(conn, codeSQL, new ScalarHandler<String>());
        return new DataHelper.VerificationCode(code);
    }

    @SneakyThrows
    public static void cleanDataBases() {
        var connection = getConnected();
        query_runner.execute(connection, "DELETE FROM auth_codes");
        query_runner.execute(connection, "DELETE FROM card_transactions");
        query_runner.execute(connection, "DELETE FROM cards");
        query_runner.execute(connection, "DELETE FROM users");
    }

    @SneakyThrows
    public static void cleanAuthCodes() {
        var conn = getConnected();
        query_runner.execute(conn, "DELETE FROM auth_codes");
    }
}
