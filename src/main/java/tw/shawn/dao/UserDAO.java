package tw.shawn.dao;

import java.sql.*;

/**
 * UserDAO：負責 users 資料表的資料存取操作
 * 功能包含：新增使用者、檢查是否存在、查詢經驗值、更新經驗值等
 */
public class UserDAO {
    private final Connection conn;  // 資料庫連線物件

    // 建構子：接收外部傳入的資料庫連線
    public UserDAO(Connection conn) {
        this.conn = conn;
    }

    /**
     * 新增經驗值：將指定使用者的 exp 欄位加上指定數值
     *
     * @param userId 使用者 ID
     * @param exp 要增加的經驗值
     * @throws SQLException 若資料庫操作錯誤
     */
    public void addExp(int userId, int exp) throws SQLException {
        String sql = "UPDATE users SET exp = exp + ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, exp);       // 設定要加的經驗值
            stmt.setInt(2, userId);    // 指定使用者 ID
            stmt.executeUpdate();      // 執行更新
        }
    }

    /**
     * 查詢指定使用者的目前經驗值
     *
     * @param userId 使用者 ID
     * @return 該使用者的經驗值，若查無則回傳 0
     * @throws SQLException 若資料庫操作錯誤
     */
    public int getExp(int userId) throws SQLException {
        String sql = "SELECT exp FROM users WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);     // 設定查詢條件
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("exp");  // 回傳查到的經驗值
                }
            }
        }
        return 0; // 查無資料預設為 0
    }

    /**
     * 檢查指定使用者是否存在
     *
     * @param userId 使用者 ID
     * @return 若存在則回傳 true，否則 false
     * @throws SQLException 若資料庫操作錯誤
     */
    public boolean userExists(int userId) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);      // 設定查詢條件
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();        // 有查到資料即代表存在
            }
        }
    }

    /**
     * 新增一位使用者到 users 資料表，預設經驗值為 0
     *
     * @param userId 使用者 ID
     * @param username 使用者帳號
     * @param password 使用者密碼
     * @throws SQLException 若新增失敗
     */
    public void insertUser(int userId, String username, String password) throws SQLException {
        String sql = "INSERT INTO users (id, username, password, exp) VALUES (?, ?, ?, 0)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);           // 設定 ID
            stmt.setString(2, username);      // 設定帳號
            stmt.setString(3, password);      // 設定密碼
            stmt.executeUpdate();             // 執行插入
        }
    }
}
