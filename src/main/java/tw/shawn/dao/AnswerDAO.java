package tw.shawn.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import tw.shawn.model.Answer;

/**
 * AnswerDAO 類別：用於處理 answer 資料表的存取邏輯（DAO：Data Access Object）
 */
public class AnswerDAO {

	
	
    // 資料庫連線物件，透過建構子傳入
    private final Connection conn;

    // 建構子，初始化 DAO 並接收資料庫連線
    public AnswerDAO(Connection conn) {
        this.conn = conn;
    }

    /**
     * AnswerRecord 為內部靜態類別，用來封裝作答詳解的資料結構，
     * 包含題號、選項、使用者選擇、正確答案等欄位，會傳回給前端顯示答題結果。
     */
    public static class AnswerRecord {
        public int quizId;          // 題目 ID
        public int selectedIndex;   // 使用者選擇的選項索引
        public int correctIndex;    // 正確選項的索引
        public String question;     // 題目文字
        public String option1, option2, option3, option4;  // 四個選項內容
        public String source;       // 題目來源（local 或 GPT）
        public String answer;       // 正確答案文字（GPT 題適用）
    }

    /**
     * 查詢指定使用者在某部影片中的所有答題紀錄，回傳 AnswerRecord 清單。
     * 結合本地題與 GPT 題目，使用 COALESCE 來確保欄位來源優先順序。
     *
     * @param userId 使用者 ID
     * @param videoId 影片 ID
     * @return 包含每一題的作答詳解清單
     * @throws SQLException 若資料庫操作失敗
     */
    public List<AnswerRecord> getAnswersByUser(int userId, String videoId) throws SQLException {
        List<AnswerRecord> list = new ArrayList<>();

        String sql = "SELECT a.quiz_id, a.selected_option, a.source, " +
                     "COALESCE(a.answer_index, q.correct_index, -1) AS correct_index, " +
                     "COALESCE(a.question, q.question) AS question, " +
                     "COALESCE(a.option1, q.option1) AS option1, " +
                     "COALESCE(a.option2, q.option2) AS option2, " +
                     "COALESCE(a.option3, q.option3) AS option3, " +
                     "COALESCE(a.option4, q.option4) AS option4, " +
                     "a.answer AS answer_text " +
                     "FROM answer a " +
                     "LEFT JOIN quiz q ON a.quiz_id = q.id " +
                     "WHERE a.user_id = ? AND a.video_id = ? " +
                     "ORDER BY a.id DESC " +
                     "LIMIT 5";  // ✅ 限制最多 5 答案

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, videoId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    AnswerRecord r = new AnswerRecord();
                    r.quizId = rs.getInt("quiz_id");
                    r.selectedIndex = rs.getInt("selected_option");
                    r.correctIndex = rs.getInt("correct_index");
                    r.question = rs.getString("question");
                    r.option1 = rs.getString("option1");
                    r.option2 = rs.getString("option2");
                    r.option3 = rs.getString("option3");
                    r.option4 = rs.getString("option4");
                    r.source = rs.getString("source");
                    r.answer = rs.getString("answer_text");
                    list.add(r);
                }
            }
        }

        return list;
    }


    /**
     * 將一筆使用者作答紀錄插入 answer 表中，支援 GPT 自動產題或本地題目格式。
     * 支援儲存完整的選項與正確答案文字，確保日後可正確顯示與分析。
     *
     * @param answer Answer 物件，封裝所有作答內容
     * @throws SQLException 若資料庫操作失敗
     */
    public void insertAnswer(Answer answer) throws SQLException {
        String sql = "INSERT INTO answer (" +
                     "user_id, quiz_id, selected_option, is_correct, source, " +
                     "created_at, answered_at, question, option1, option2, option3, option4, " +
                     "video_id, answer, answer_index" +
                     ") VALUES (?, ?, ?, ?, ?, NOW(), NOW(), ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, answer.getUserId());            // 使用者 ID
            ps.setInt(2, answer.getQuizId());            // 題目 ID
            ps.setInt(3, answer.getSelectedOption());    // 使用者選擇的選項
            ps.setBoolean(4, answer.isCorrect());        // 是否正確
            ps.setString(5, answer.getSource());         // 題目來源
            ps.setString(6, answer.getQuestion());       // 題目內容
            ps.setString(7, answer.getOption1());        // 選項 A
            ps.setString(8, answer.getOption2());        // 選項 B
            ps.setString(9, answer.getOption3());        // 選項 C
            ps.setString(10, answer.getOption4());       // 選項 D
            ps.setString(11, answer.getVideoId());       // 所屬影片
            ps.setString(12, answer.getAnswer());        // 正確答案文字
            ps.setInt(13, answer.getAnswerIndex());      // 正確答案的索引（支援 GPT 題）
            ps.executeUpdate();                          // 執行 INSERT
        }
    }
    /**
     * 刪除指定使用者在某部影片中的所有答題紀錄（用於避免重複作答殘留）
     *
     * @param userId  使用者 ID
     * @param videoId 影片 ID
     * @throws SQLException 若資料庫操作失敗
     */
    public void deleteAnswersByUser(int userId, String videoId) throws SQLException {
        String sql = "DELETE FROM answer WHERE user_id = ? AND video_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, videoId);
            ps.executeUpdate();  // 執行刪除
        }
    }

}
