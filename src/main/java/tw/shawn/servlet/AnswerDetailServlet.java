package tw.shawn.servlet;

// 匯入所需類別
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import tw.shawn.dao.AnswerDAO;
import tw.shawn.dao.QuizDAO;
import tw.shawn.dao.AnswerDAO.AnswerRecord;
import tw.shawn.util.DBUtil;

import java.io.IOException;
import java.sql.Connection;
import java.util.List;


/**
 * AnswerDetailServlet：用於查詢某使用者針對某部影片的作答紀錄（答題詳解）
 * 回傳 JSON 陣列，包含每題的題目、選項、答對與否、來源等資料
 */
@WebServlet("/api/answerDetail") // Servlet 註冊路徑
public class AnswerDetailServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // 設定請求與回應的編碼格式
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");

        String videoId = req.getParameter("videoId"); // 從查詢參數取得影片 ID
        int userId;

        try {
            // 將 userId 從字串轉為整數（使用者 ID 必須為有效數字）
            userId = Integer.parseInt(req.getParameter("userId"));

            // 建立資料庫連線並使用 DAO 查詢作答資料
            try (Connection conn = DBUtil.getConnection()) {
                AnswerDAO dao = new AnswerDAO(conn);
                QuizDAO quizDAO = new QuizDAO(conn); // ⚠️ 若未使用 quizDAO 可移除，但保留為未來擴充

                // 從資料庫取得使用者的所有作答紀錄（包含 GPT 題與本地題）
                List<AnswerRecord> records = dao.getAnswersByUser(userId, videoId);

                // 建立要回傳的 JSON 陣列
                JsonArray jsonArr = new JsonArray();

                // ✅ 印出陣列長度（debug 用，可移除）
                System.out.println(jsonArr.size());

                // 逐筆轉換 AnswerRecord 為 JSON 格式
                for (AnswerRecord record : records) {
                    JsonObject obj = new JsonObject();
                    obj.addProperty("question", record.question);     // 題目文字
                    obj.addProperty("option1", record.option1);       // 選項 A
                    obj.addProperty("option2", record.option2);       // 選項 B
                    obj.addProperty("option3", record.option3);       // 選項 C
                    obj.addProperty("option4", record.option4);       // 選項 D

                    // 將四個選項組成 List，便於使用索引擷取選項文字
                    List<String> options = List.of(
                        record.option1 != null ? record.option1 : "",
                        record.option2 != null ? record.option2 : "",
                        record.option3 != null ? record.option3 : "",
                        record.option4 != null ? record.option4 : ""
                    );

                    // 根據正確索引與使用者選擇索引取出對應文字（防呆處理索引範圍）
                    String correctText = (record.correctIndex >= 0 && record.correctIndex < options.size())
                        ? options.get(record.correctIndex) : "";
                    String selectedText = (record.selectedIndex >= 0 && record.selectedIndex < options.size())
                        ? options.get(record.selectedIndex) : "";

                    // 顯示正確答案與使用者所選答案
                    obj.addProperty("correct", correctText);
                    obj.addProperty("selected", selectedText);

                    // 顯示正確與作答索引（前端可用於比對與標記）
                    obj.addProperty("correctIndex", record.correctIndex);   // ✅ 新增：正確選項索引
                    obj.addProperty("selectedIndex", record.selectedIndex); // ✅ 新增：使用者選項索引

                    // 題目來源（GPT、自動產生、本地等）
                    obj.addProperty("source", record.source);

                    // 將此筆記錄加入 JSON 陣列
                    jsonArr.add(obj);
                }

                // 將結果寫出給前端（JSON 字串）
                resp.getWriter().write(jsonArr.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
            // 若發生例外，回傳 HTTP 500 錯誤與錯誤訊息
            resp.sendError(500, "伺服器錯誤：" + e.getMessage());
        }
    }
}
