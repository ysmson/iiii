package tw.shawn.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import tw.shawn.model.QuizResult;

/**
 * QuizResultDAO 負責處理 quiz_results 資料表的寫入與查詢邏輯
 */
public class QuizResultDAO {
    private final Connection conn;  // 資料庫連線物件

    // 建構子：透過外部傳入資料庫連線
    public QuizResultDAO(Connection conn) {
        this.conn = conn;
    }

    /**
     * 將某使用者的一次測驗結果寫入 quiz_results 資料表
     *
     * @param userId        使用者 ID
     * @param videoId       所屬影片 ID
     * @param correctCount  答對題數
     * @param totalCount    總題數
     * @throws SQLException 若資料庫操作失敗
     */
    public void insertQuizResult(int userId, String videoId, int correctCount, int totalCount) throws SQLException {
        String sql = "INSERT INTO quiz_results (user_id, video_id, correct_answers, total_questions, submitted_at) VALUES (?, ?, ?, ?, NOW())";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);           // 設定使用者 ID
            stmt.setString(2, videoId);       // 設定影片 ID
            stmt.setInt(3, correctCount);     // 設定答對題數
            stmt.setInt(4, totalCount);       // 設定總題數
            stmt.executeUpdate();             // 執行新增
        }
    }

    /**
     * 查詢某使用者針對某部影片的「最近一次」測驗結果
     *
     * @param userId   使用者 ID
     * @param videoId  影片 ID
     * @return QuizResult 物件，若查無資料則回傳 null
     * @throws SQLException 若資料庫操作失敗
     */
    public QuizResult getLatestQuizResult(int userId, String videoId) throws SQLException {
        // 根據 submitted_at 時間倒序排序，取得最新一筆紀錄
        String sql = "SELECT * FROM quiz_results WHERE user_id = ? AND video_id = ? ORDER BY submitted_at DESC LIMIT 1";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);           // 設定使用者 ID
            stmt.setString(2, videoId);       // 設定影片 ID
            ResultSet rs = stmt.executeQuery();  // 執行查詢

            if (rs.next()) {
                // 若查到結果，封裝為 QuizResult 物件並回傳
                QuizResult result = new QuizResult();
                result.setUserId(userId);
                result.setVideoId(videoId);
                result.setCorrectAnswers(rs.getInt("correct_answers"));
                result.setTotalQuestions(rs.getInt("total_questions"));
                return result;
            }
        }
        return null;  // 若無資料則回傳 null
    }
}
