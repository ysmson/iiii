package tw.shawn.model;

import java.time.LocalDateTime;

/**
 * Answer 類別：代表使用者對某一題 Quiz 的作答紀錄
 * 每一筆 Answer 代表一位使用者對某題的回答與詳情
 */
public class Answer {
    private int id;                  // 答案記錄 ID（主鍵，自動產生）
    private int userId;             // 使用者 ID（foreign key 對應 users 表）
    private int quizId;             // 題目 ID（foreign key 對應 quiz 表）
    private int selectedOption;     // 使用者選的選項（0~3）
    private boolean correct;        // 是否答對（true = 正確，false = 錯誤）
    private String videoId;         // 所屬影片 ID（YouTube 影片代碼）
    private LocalDateTime answeredAt; // 作答時間（LocalDateTime 格式）
    private String source;          // 題目來源：gpt 或 local

    private String question;        // 題目文字（主要用於 GPT 題儲存完整題幹）
    private String option1;         // 選項 A
    private String option2;         // 選項 B
    private String option3;         // 選項 C
    private String option4;         // 選項 D

    // ==== Getter / Setter 方法區塊 ====

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getQuizId() { return quizId; }
    public void setQuizId(int quizId) { this.quizId = quizId; }

    public int getSelectedOption() { return selectedOption; }
    public void setSelectedOption(int selectedOption) { this.selectedOption = selectedOption; }

    public boolean isCorrect() { return correct; }
    public void setCorrect(boolean correct) { this.correct = correct; }

    public String getVideoId() { return videoId; }
    public void setVideoId(String videoId) { this.videoId = videoId; }

    public LocalDateTime getAnsweredAt() { return answeredAt; }
    public void setAnsweredAt(LocalDateTime answeredAt) { this.answeredAt = answeredAt; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public String getOption1() { return option1; }
    public void setOption1(String option1) { this.option1 = option1; }

    public String getOption2() { return option2; }
    public void setOption2(String option2) { this.option2 = option2; }

    public String getOption3() { return option3; }
    public void setOption3(String option3) { this.option3 = option3; }

    public String getOption4() { return option4; }
    public void setOption4(String option4) { this.option4 = option4; }

    // 🔧 新增欄位：正確答案文字（GPT 題適用）
    private String answer;

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    // 🔧 新增欄位：正確選項索引（0~3），對應正確答案的位置
    private int answerIndex;

    public int getAnswerIndex() { return answerIndex; }

    public void setAnswerIndex(int answerIndex) { this.answerIndex = answerIndex; }
}
