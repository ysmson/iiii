package tw.shawn.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DBUtil 類別：資料庫工具類，提供 JDBC 的連線建立、關閉連線與釋放背景資源功能。
 * 用於統一管理與 MySQL 資料庫的連接邏輯。
 */
public class DBUtil {

    // ✅ 設定資料庫的連線資訊（URL、帳號、密碼）
    private static final String URL = "jdbc:mysql://localhost:3306/videolist"; // 資料庫位置與名稱（host:port/db）
    private static final String USER = "root";       // 資料庫使用者帳號
    private static final String PASSWORD = "";       // 資料庫密碼（視你的 MySQL 安裝設定而定）

    // ✅ 類別載入時執行一次的區塊：註冊 JDBC 驅動程式
    static {
        try {
            // 嘗試載入 MySQL JDBC 驅動，供 DriverManager 建立連線時使用
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("✅ JDBC 驅動載入成功！");
        } catch (ClassNotFoundException e) {
            // 若找不到驅動類別，顯示錯誤並輸出詳細堆疊資訊
            System.err.println("❌ 載入 JDBC Driver 失敗！");
            e.printStackTrace();
        }
    }

    /**
     * ✅ 提供資料庫連線給 DAO 或 Servlet 使用
     * @return Connection 資料庫連線物件（由 DriverManager 提供）
     * @throws SQLException 若連線建立過程出錯
     */
    public static Connection getConnection() throws SQLException {
        // 傳回一個新的資料庫連線物件
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /**
     * ✅ 關閉資料庫連線，避免連線資源外洩（避免 connection pool 泄漏）
     * @param conn 要關閉的資料庫連線物件
     */
    public static void close(Connection conn) {
        try {
            // 判斷非 null 才呼叫 close()
            if (conn != null) conn.close();
        } catch (SQLException e) {
            // 關閉時如發生錯誤，僅印出錯誤但不中斷程式
            e.printStackTrace();
        }
    }

    /**
     * ✅ 釋放 MySQL JDBC 驅動所建立的背景執行緒
     * Tomcat 關閉時建議呼叫，避免警告：AbandonedConnectionCleanupThread 沒關掉
     */
    public static void cleanupDriver() {
        try {
            // MySQL 提供的 API：正式關閉驅動背景清理執行緒
            com.mysql.cj.jdbc.AbandonedConnectionCleanupThread.checkedShutdown();
            System.out.println("✅ MySQL CleanupThread 已關閉。");
        } catch (Exception e) {
            // 若關閉失敗，顯示錯誤（不影響整體系統運作）
            System.err.println("❌ 無法關閉 CleanupThread：" + e.getMessage());
        }
    }

}
