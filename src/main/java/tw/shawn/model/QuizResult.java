package tw.shawn.model;

import java.time.LocalDateTime;

/**
 * QuizResult 類別：代表使用者一次測驗的總結結果紀錄
 * 對應資料庫中的 quiz_results 資料表
 */
public class QuizResult {
    private int id;                      // 主鍵 ID（資料庫自動產生）
    private int userId;                  // 使用者 ID（對應 users 表）
    private String videoId;              // 影片 ID（對應 video 表）
    private int totalQuestions;          // 本次測驗總題數
    private int correctAnswers;          // 答對的題數
    private Integer score;               // 成績（可為 null，使用 Integer 以支援空值）
    private LocalDateTime submittedAt;   // 作答送出時間（對應資料庫 submitted_at）

    // === Getter / Setter ===

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }
    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getVideoId() {
        return videoId;
    }
    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }
    public void setTotalQuestions(int totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public int getCorrectAnswers() {
        return correctAnswers;
    }
    public void setCorrectAnswers(int correctAnswers) {
        this.correctAnswers = correctAnswers;
    }

    public Integer getScore() {
        return score;
    }
    public void setScore(Integer score) {
        this.score = score;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }
    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }
}
