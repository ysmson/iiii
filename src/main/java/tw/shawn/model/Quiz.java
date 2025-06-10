package tw.shawn.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Quiz 精緻類別：代表一題選擇題的資料模型
 * 包含題目文字、選項、正確答案、使用者作答等資訊
 */
public class Quiz {
    private int id;                    // 題目在資料庫的主鍵 ID
    private String videoId;            // 所屬影片的 ID（YouTube videoId）
    private String question;           // 題目文字
    private List<String> options;      // 四個選項（動態 List）
    private int correctIndex;          // 正確答案的索引（0~3）
    private String explanation;        // 題解說明文字
    private Integer userSelected;      // 使用者所選的選項索引（可能為 null）
    private String correctAnswer;      // 正確答案的文字（例如 "牛頓"）
    private String correctOption;      // 正確答案的選項代號（例如 "A"）
    private String source;             // 題目來源（例如 gpt 或 local）

    public Quiz() {
        // 預設建構子：供 Gson、DAO、自動產題工具等使用
    }

    /**
     * 建構子：快速建立一題 quiz（選項直接傳入）
     */
    public Quiz(String videoId, String question, String option1, String option2,
                String option3, String option4, int correctIndex, String explanation) {
        this.videoId = videoId;
        this.question = question;
        this.options = new ArrayList<>();
        this.options.add(option1);
        this.options.add(option2);
        this.options.add(option3);
        this.options.add(option4);
        this.correctIndex = correctIndex;
        this.explanation = explanation;
    }

    // === getter / setter ===

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getVideoId() { return videoId; }
    public void setVideoId(String videoId) { this.videoId = videoId; }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public List<String> getOptions() { return options; }

    /**
     * 設定選項列表（將傳入 list 強制轉為可變動 List）
     * 避免從不可變資料（如 List.of）導致執行時錯誤
     */
    public void setOptions(List<String> options) {
        this.options = new ArrayList<>(options);
    }

    public int getCorrectIndex() { return correctIndex; }
    public void setCorrectIndex(int correctIndex) { this.correctIndex = correctIndex; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public Integer getUserSelected() { return userSelected; }
    public void setUserSelected(Integer userSelected) { this.userSelected = userSelected; }

    public String getCorrectAnswer() { return correctAnswer; }
    public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }

    public String getCorrectOption() { return correctOption; }
    public void setCorrectOption(String correctOption) { this.correctOption = correctOption; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    /**
     * 回傳使用者是否作答正確（userSelected 是否等於 correctIndex）
     * @return boolean，若答對為 true
     */
    public boolean isCorrect() {
        return userSelected != null && userSelected == correctIndex;
    }

    // === 個別選項的 getter ===
    public String getOption1() { return options != null && options.size() > 0 ? options.get(0) : null; }
    public String getOption2() { return options != null && options.size() > 1 ? options.get(1) : null; }
    public String getOption3() { return options != null && options.size() > 2 ? options.get(2) : null; }
    public String getOption4() { return options != null && options.size() > 3 ? options.get(3) : null; }

    // === 個別選項的 setter ===
    public void setOption1(String o) { ensureOptionsSize(4); options.set(0, o); }
    public void setOption2(String o) { ensureOptionsSize(4); options.set(1, o); }
    public void setOption3(String o) { ensureOptionsSize(4); options.set(2, o); }
    public void setOption4(String o) { ensureOptionsSize(4); options.set(3, o); }

    /**
     * 確保 options List 至少有指定大小（避免執行 set(index) 時出錯）
     * 若為 null 則初始化，長度不足則補空字串
     * @param size 至少要有的選項數量（通常為 4）
     */
    private void ensureOptionsSize(int size) {
        if (options == null) {
            options = new ArrayList<>();
        }
        while (options.size() < size) {
            options.add("");
        }
    }

    // ✅ 額外欄位：正確答案文字（可能與 correctAnswer 重複，用於資料同步時使用）
    private String answer;

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }
}
