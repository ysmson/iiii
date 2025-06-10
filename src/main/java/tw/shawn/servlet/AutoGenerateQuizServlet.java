package tw.shawn.servlet;

// 必要的套件與工具類別
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import org.json.JSONArray;
import org.json.JSONObject;
import okhttp3.*;
import tw.shawn.util.DBUtil;

import java.io.*;
import java.nio.file.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.concurrent.TimeUnit;
import java.util.Properties;

/**
 * AutoGenerateQuizServlet：從影片 transcript 檔案呼叫 OpenAI 產生測驗題目，並寫入資料庫
 * 路徑：/api/autoGenerateQuiz?videoId=xxx
 */
@WebServlet("/api/autoGenerateQuiz")
public class AutoGenerateQuizServlet extends HttpServlet {

    private static Properties config; // 儲存載入的 config.properties 設定檔

    /**
     * Servlet 啟動時自動載入 config.properties（只做一次）
     */
    @Override
    public void init() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (input != null) {
                config = new Properties();
                config.load(input);
                System.out.println("✅ 成功載入 config.properties");
                config.forEach((k, v) -> System.out.println("✔️ " + k + " = " + v));
            } else {
                System.out.println("⚠️ 找不到 config.properties");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 主要處理邏輯：從影片文字（transcript）呼叫 GPT 產生選擇題 → 寫入資料庫 → 回傳 JSON
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");

        String videoId = req.getParameter("videoId"); // 從 query string 取得影片 ID
        System.out.println("📥 收到 videoId: " + videoId);

        // 檢查參數是否缺漏
        if (videoId == null || videoId.trim().isEmpty()) {
            resp.setStatus(400);
            resp.getWriter().write("{\"error\":\"缺少 videoId\"}");
            return;
        }

        // 嘗試讀取 transcript 檔案（txt 格式）
        String transcriptPath = req.getServletContext().getRealPath("/transcripts/" + videoId + ".txt");
        System.out.println("📄 嘗試讀取 transcript 檔案：" + transcriptPath);

        String transcript;
        try {
            transcript = new String(Files.readAllBytes(Paths.get(transcriptPath)), "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(500);
            resp.getWriter().write("{\"error\":\"找不到 transcript 檔案\"}");
            return;
        }

        // 組成送給 GPT 的 prompt，請其回傳 JSON 陣列格式的題目
        String prompt = "請僅回傳 JSON 陣列，不要加上任何註解或文字。" +
                "請產生 5 題繁體中文選擇題，語意要清楚、適合 Java 初學者的測驗題目，每題包含 question、options（陣列）與 answer（正確答案文字）\n\n" + transcript;

        System.out.println("🧠 呼叫 OpenAI API 前準備完成");

        String quizJsonText;
        try {
            quizJsonText = callOpenAIGPT(prompt);
        } catch (Exception ex) {
            ex.printStackTrace();
            resp.setStatus(500);
            resp.getWriter().write("{\"error\":\"OpenAI API 請求失敗\"}");
            return;
        }

        if (quizJsonText == null) {
            System.out.println("⚠️ GPT 回傳為 null");
            resp.setStatus(500);
            resp.getWriter().write("{\"error\":\"GPT 回傳為 null\"}");
            return;
        }

        System.out.println("🧠 GPT 原始回傳內容：\n" + quizJsonText);

        // 擷取 GPT 回傳的 JSON 陣列
        quizJsonText = extractJsonArray(quizJsonText);
        System.out.println("🧾 擷取出來的 JSON 陣列內容：\n" + quizJsonText);

        JSONArray quizArr;
        try {
            quizArr = new JSONArray(quizJsonText); // 將字串轉成 JSON 陣列
        } catch (Exception jsonEx) {
            jsonEx.printStackTrace();
            resp.setStatus(500);
            // 傳回錯誤 JSON，raw 為原始內容（改用單引號避免 JSON 格式錯誤）
            resp.getWriter().write("{\"error\":\"GPT 回傳格式無法解析為 JSON 陣列\", \"raw\": \"" +
                    quizJsonText.replace("\"", "'") + "\"}");
            return;
        }

        // 寫入 quiz 題庫資料表
        try (Connection conn = DBUtil.getConnection()) {
            String sql = "INSERT INTO quiz (video_id, question, option1, option2, option3, option4, correct_index, explanation) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);

            for (int i = 0; i < quizArr.length(); i++) {
                JSONObject q = quizArr.getJSONObject(i);

                // 基本欄位檢查
                if (!q.has("question") || !q.has("options") || !q.has("answer")) {
                    System.err.println("⚠️ 題目缺少必要欄位，跳過：" + q.toString());
                    continue;
                }

                JSONArray opts = q.getJSONArray("options");
                if (opts.length() < 2) {
                    System.err.println("⚠️ 選項數不足，跳過：" + q.toString());
                    continue;
                }

                // 尋找正確答案的索引位置
                String correctAnswer = q.getString("answer").trim();
                int correctIndex = -1;
                for (int j = 0; j < opts.length(); j++) {
                    if (opts.getString(j).trim().equals(correctAnswer)) {
                        correctIndex = j;
                        break;
                    }
                }

                if (correctIndex == -1) {
                    System.err.println("⚠️ 找不到正確答案位置，跳過：" + q.toString());
                    continue;
                }

                // 最多只取四個選項
                String option1 = opts.length() > 0 ? opts.getString(0) : "";
                String option2 = opts.length() > 1 ? opts.getString(1) : "";
                String option3 = opts.length() > 2 ? opts.getString(2) : "";
                String option4 = opts.length() > 3 ? opts.getString(3) : "";

                // 填入 SQL 欄位值
                ps.setString(1, videoId);
                ps.setString(2, q.getString("question"));
                ps.setString(3, option1);
                ps.setString(4, option2);
                ps.setString(5, option3);
                ps.setString(6, option4);
                ps.setInt(7, correctIndex);
                ps.setString(8, "根據影片內容產生的題目");
                ps.addBatch();
            }

            ps.executeBatch(); // 批次執行 INSERT
        } catch (Exception ex) {
            ex.printStackTrace();
            resp.setStatus(500);
            resp.getWriter().write("{\"error\":\"資料庫寫入錯誤\", \"message\": \"" +
                    ex.getMessage().replace("\"", "'") + "\"}");
            return;
        }

        // 回傳成功的 JSON 陣列內容
        resp.getWriter().write(quizJsonText);
        System.out.println("✅ 題目處理完成並已回傳");
    }

    /**
     * 將 prompt 發送給 OpenAI Chat Completion 並回傳回應字串
     */
    private String callOpenAIGPT(String prompt) throws IOException {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        String apiKey = getOpenAIApiKey();
        String apiUrl = getOpenAIApiUrl();

        if (apiKey == null || apiUrl == null) {
            System.out.println("❌ API 金鑰或 URL 為 null");
            return null;
        }

        // 建立 Chat Completion 請求內容
        JSONObject requestJson = new JSONObject();
        requestJson.put("model", config.getProperty("openai.model.chat", "gpt-3.5-turbo"));

        JSONArray messages = new JSONArray();
        messages.put(new JSONObject().put("role", "user").put("content", prompt));
        requestJson.put("messages", messages);

        // 建立 HTTP 請求
        Request request = new Request.Builder()
                .url(apiUrl)
                .addHeader("Authorization", "Bearer " + apiKey)
                .post(RequestBody.create(requestJson.toString(), MediaType.get("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            String bodyStr = response.body().string();

            if (!response.isSuccessful()) {
                System.out.println("❌ OpenAI 錯誤狀態碼：" + response.code());
                System.out.println("❌ 錯誤內容：" + bodyStr);
                return null;
            }

            System.out.println("✅ OpenAI 成功回傳內容：\n" + bodyStr);

            JSONObject respJson = new JSONObject(bodyStr);
            return respJson.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");
        }
    }

    /**
     * 從 GPT 回傳的字串中擷取 JSON 陣列
     */
    private String extractJsonArray(String text) {
        int start = text.indexOf("[");
        int end = text.lastIndexOf("]");
        if (start != -1 && end != -1 && end > start) {
            return text.substring(start, end + 1);
        }
        return "[]"; // 若格式錯誤則回傳空陣列
    }

    // 讀取 OpenAI API 金鑰
    private static String getOpenAIApiKey() {
        return config != null ? config.getProperty("openai.api.key") : null;
    }

    // 讀取 OpenAI API URL
    private static String getOpenAIApiUrl() {
        return config != null ? config.getProperty("openai.api.url") : null;
    }
}
