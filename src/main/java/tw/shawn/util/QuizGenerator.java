package tw.shawn.util;

import tw.shawn.model.Quiz;

import java.util.*;
import java.util.regex.Pattern;

/**
 * QuizGenerator：從字幕內容產生選擇題（非 GPT），使用關鍵字取代方式製造干擾選項。
 */
public class QuizGenerator {

    /**
     * ✅ 根據字幕文字自動產生多題選擇題
     * @param transcript 字幕內容（純文字、已斷句）
     * @param videoId 影片 ID（會轉成字串存入 Quiz 的 videoId 欄位）
     * @param num 要產生的題目數量（例如：5 題）
     * @return List<Quiz> 回傳選擇題列表
     */
    public static List<Quiz> generateMultipleChoice(String transcript, int videoId, int num) {
        List<Quiz> result = new ArrayList<>();             // 最終回傳的題目清單
        Set<String> usedSentences = new HashSet<>();       // 記錄已出過的正確句子，避免重複出題

        // ✅ 用中文標點符號（。！？）切割成句子
        String[] sentences = transcript.split("[。！？]");

        List<String> candidates = new ArrayList<>();
        for (String s : sentences) {
            s = s.trim(); // 去除前後空白
            // 條件：句子長度需 > 20 且含有中文
            if (s.length() > 20 && Pattern.compile("[\u4e00-\u9fa5]").matcher(s).find()) {
                candidates.add(s); // 加入候選句子列表
            }
        }

        Random random = new Random();
        int attempts = 0; // 控制最多重試次數（避免無限迴圈）

        // ✅ 重複嘗試最多 100 次，直到產生指定題數
        while (result.size() < num && attempts++ < 100) {
            if (candidates.isEmpty()) break; // 若沒候選句可用則停止

            // 隨機選一句當作正確答案
            String correct = candidates.get(random.nextInt(candidates.size()));
            if (usedSentences.contains(correct)) continue; // 若已出題過則略過

            // ✅ 製造干擾選項：替換特定關鍵詞（根據 Java 主題常見混淆概念）
            String fake1 = correct.replaceFirst("Java", "Python");
            String fake2 = correct.replaceFirst("JDK", "JRE");
            String fake3 = correct.replaceFirst("VS ?Code", "Notepad");

            // ✅ 建立選項集合，避免重複選項
            Set<String> optionsSet = new LinkedHashSet<>(Arrays.asList(correct, fake1, fake2, fake3));
            if (optionsSet.size() < 4) continue; // 若無法組成 4 個不同選項則跳過

            List<String> options = new ArrayList<>(optionsSet);
            Collections.shuffle(options); // 將選項順序打亂

            int correctIndex = options.indexOf(correct); // 找出正確答案在選項中的位置
            if (correctIndex == -1) continue; // 若找不到則跳過該題

            // ✅ 建立 Quiz 物件並填入題目資料
            Quiz quiz = new Quiz();
            quiz.setVideoId(String.valueOf(videoId)); // videoId 要轉為字串
            quiz.setQuestion("下列哪一項敘述正確？"); // 固定題幹

            // 設定四個選項
            quiz.setOption1(options.get(0));
            quiz.setOption2(options.get(1));
            quiz.setOption3(options.get(2));
            quiz.setOption4(options.get(3));

            quiz.setCorrectIndex(correctIndex + 1); // 正解索引（1-based）
            quiz.setExplanation("根據影片內容，正確敘述為：「" + correct + "」"); // 題目解釋

            result.add(quiz); // 將此題加入結果列表
            usedSentences.add(correct); // 標記此句已使用
        }

        return result; // 回傳產生好的題目清單
    }

    /**
     * ✅ 快速方法：預設產生 5 題題目
     * @param transcript 字幕內容
     * @param videoId 對應的影片 ID
     * @return 題目列表（最多 5 題）
     */
    public static List<Quiz> generateFromText(String transcript, int videoId) {
        return generateMultipleChoice(transcript, videoId, 5); // 呼叫主要方法
    }
}
