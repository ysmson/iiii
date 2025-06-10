package tw.shawn.dao;

import tw.shawn.model.Quiz;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * QuizDAO 類別：處理 quiz 題目的資料存取與查詢邏輯
 */
public class QuizDAO {
    private final Connection conn;  // 資料庫連線物件

    // 建構子：接收資料庫連線
    public QuizDAO(Connection conn) {
        this.conn = conn;
    }

    /**
     * 批次新增多筆 quiz 題目到 quiz 資料表中（無重複檢查）
     * @param videoId 影片 ID（每題會綁定此影片）
     * @param quizList 題目清單
     */
    public void insertQuizList(String videoId, List<Quiz> quizList) {
        String sql = "INSERT INTO quiz (video_id, question, option1, option2, option3, option4, correct_index, explanation) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (Quiz q : quizList) {
                stmt.setString(1, videoId);                 // 影片 ID
                stmt.setString(2, q.getQuestion());         // 題目文字
                stmt.setString(3, q.getOption1());          // 選項 A
                stmt.setString(4, q.getOption2());          // 選項 B
                stmt.setString(5, q.getOption3());          // 選項 C
                stmt.setString(6, q.getOption4());          // 選項 D
                stmt.setInt(7, q.getCorrectIndex());        // 正確答案索引
                stmt.setString(8, q.getExplanation());      // 題解
                stmt.addBatch();                            // 加入批次
            }
            stmt.executeBatch(); // 一次執行所有 insert
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 根據影片 ID 取得該影片的所有 quiz 題目（包含完整內容）
     * @param videoId 影片 ID
     * @return Quiz 題目清單
     */
    public List<Quiz> getQuizzesByVideoId(String videoId) throws SQLException {
        List<Quiz> quizList = new ArrayList<>();
        String sql = "SELECT * FROM quiz WHERE video_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, videoId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    quizList.add(toQuiz(rs)); // 將每筆資料轉為 Quiz 物件
                }
            }
        }
        return quizList;
    }

    /**
     * 根據影片 ID 與來源，取得特定數量的 quiz 題目
     * @param videoId 影片 ID
     * @param source 題目來源（如 GPT、本地）
     * @param limit 題數限制
     * @return Quiz 題目清單
     */
    public List<Quiz> getQuizzesByVideoIdAndSource(String videoId, String source, int limit) throws SQLException {
        List<Quiz> quizList = new ArrayList<>();
        String sql = "SELECT * FROM quiz WHERE video_id = ? AND source = ? ORDER BY id LIMIT ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, videoId);
            stmt.setString(2, source);
            stmt.setInt(3, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    quizList.add(toQuiz(rs)); // 將結果轉為 Quiz 物件
                }
            }
        }
        return quizList;
    }

    /**
     * 工具方法：將查詢結果 ResultSet 轉換為 Quiz 物件
     * @param rs 資料表查詢結果
     * @return Quiz 題目物件
     */
    private Quiz toQuiz(ResultSet rs) throws SQLException {
        Quiz quiz = new Quiz();
        quiz.setId(rs.getInt("id"));
        quiz.setVideoId(rs.getString("video_id"));
        quiz.setQuestion(rs.getString("question"));
        List<String> options = List.of(
            rs.getString("option1"),
            rs.getString("option2"),
            rs.getString("option3"),
            rs.getString("option4")
        );
        quiz.setOptions(options); // 設定四個選項
        int correctIndex = rs.getInt("correct_index");
        quiz.setCorrectIndex(correctIndex);
        quiz.setExplanation(rs.getString("explanation"));
        quiz.setSource(rs.getString("source"));

        // 如果正確索引在合法範圍內，則設定正確答案與選項代號（A~D）
        if (correctIndex >= 0 && correctIndex < options.size()) {
            quiz.setCorrectAnswer(options.get(correctIndex));
            quiz.setCorrectOption(new String[]{"A", "B", "C", "D"}[correctIndex]);
        }
        return quiz;
    }

    /**
     * 根據影片 ID，取得所有 quiz 的題目文字（不包含選項）
     * @param videoId 影片 ID
     * @return 題目文字清單
     */
    public List<String> getQuestionsByVideoId(String videoId) throws SQLException {
        List<String> questions = new ArrayList<>();
        String sql = "SELECT question FROM quiz WHERE video_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, videoId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    questions.add(rs.getString("question"));
                }
            }
        }
        return questions;
    }

    /**
     * 批次新增 quiz 題目，若題目文字已存在（完全相同）則略過新增
     * @param videoId 影片 ID
     * @param quizzes 題目清單
     * @return 成功新增的題數
     */
    public int insertQuizListAvoidDuplicate(String videoId, List<Quiz> quizzes) throws SQLException {
        int count = 0;
        String checkSql = "SELECT COUNT(*) FROM quiz WHERE question = ?";
        String insertSql = "INSERT INTO quiz (video_id, question, option1, option2, option3, option4, correct_index, explanation) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql);
             PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {

            for (Quiz quiz : quizzes) {
                // 檢查是否已存在相同題目
                checkStmt.setString(1, quiz.getQuestion());
                ResultSet rs = checkStmt.executeQuery();
                rs.next();
                if (rs.getInt(1) > 0) continue; // 已存在則略過

                List<String> options = quiz.getOptions();
                insertStmt.setString(1, videoId);
                insertStmt.setString(2, quiz.getQuestion());
                insertStmt.setString(3, options.get(0));
                insertStmt.setString(4, options.get(1));
                insertStmt.setString(5, options.get(2));
                insertStmt.setString(6, options.get(3));
                insertStmt.setInt(7, quiz.getCorrectIndex());
                insertStmt.setString(8, quiz.getExplanation());
                insertStmt.addBatch(); // 加入批次
                count++; // 成功新增計數
            }
            insertStmt.executeBatch(); // 執行所有新增
        }
        return count;
    }

    /**
     * 根據 quizId 查詢該題完整資料
     * @param quizId 題目 ID
     * @return Quiz 物件（若無則為 null）
     */
    public Quiz getQuizById(int quizId) throws SQLException {
        String sql = "SELECT * FROM quiz WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, quizId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return toQuiz(rs); // 回傳該題目完整物件
                }
            }
        }
        return null;
    }

    /**
     * 查詢指定 quizId 與來源的題目正確選項索引
     * @param quizId 題目 ID
     * @param source 題目來源（如 GPT、本地）
     * @return 正確選項索引
     * @throws SQLException 若查不到則丟出例外
     */
    public int getCorrectOptionById(int quizId, String source) throws SQLException {
        String sql = "SELECT correct_index FROM quiz WHERE id = ? AND source = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, quizId);
            stmt.setString(2, source);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("correct_index");
            } else {
                throw new SQLException("找不到符合 quizId 與來源的題目");
            }
        }
    }

    /**
     * 根據影片 ID 與題目順序（index），查詢對應的 quizId
     * @param videoId 影片 ID
     * @param index 題目索引（從 0 開始）
     * @return quizId，若查無則回傳 -1
     */
    public int getQuizIdByVideoIdAndIndex(String videoId, int index) throws SQLException {
        String sql = "SELECT id FROM quiz WHERE video_id = ? ORDER BY id LIMIT 1 OFFSET ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, videoId);
            stmt.setInt(2, index);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt("id");
            }
        }
        return -1;
    }

    /**
     * 根據 quizId 反查對應的 videoId
     * @param quizId 題目 ID
     * @return videoId（若查無則回傳 null）
     */
    public String getVideoIdByQuizId(int quizId) throws SQLException {
        String sql = "SELECT video_id FROM quiz WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, quizId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getString("video_id") : null;
            }
        }
    }
}
