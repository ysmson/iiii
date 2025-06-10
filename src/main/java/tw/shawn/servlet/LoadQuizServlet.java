package tw.shawn.servlet;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import tw.shawn.util.DBUtil;
import tw.shawn.dao.QuizDAO;
import tw.shawn.model.Quiz;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.List;

// ✅ 將此 Servlet 註冊為 API 路徑 /api/loadQuiz
@WebServlet("/api/loadQuiz")
public class LoadQuizServlet extends HttpServlet {

    // ✅ 處理 GET 請求：根據 videoId 與來源（source）載入題目清單
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 設定回應格式為 JSON，並使用 UTF-8 編碼
        response.setContentType("application/json;charset=UTF-8");
        request.setCharacterEncoding("UTF-8");

        // ✅ 從請求中取得 videoId 與 source（例如 local 或 gpt）
        String videoId = request.getParameter("videoId");
        String source = request.getParameter("source");

        // ✅ 基本參數檢查，若缺少必要參數，回傳 400 錯誤
        if (videoId == null || source == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "缺少 videoId 或 source");
            return;
        }

        // ✅ 準備存放題目的 JSON 陣列
        JsonArray quizArray = new JsonArray();

        try (Connection conn = DBUtil.getConnection()) {
            // ✅ 取得 QuizDAO 並查詢符合條件的題目（根據影片與來源）
            QuizDAO quizDAO = new QuizDAO(conn);

            // ✅ 每次只載入最多 5 題（可調整為 30 題等）
            List<Quiz> quizList = quizDAO.getQuizzesByVideoIdAndSource(videoId, source, 5);

            // ✅ 將每一題 Quiz 轉為 JSON 格式
            for (Quiz quiz : quizList) {
                JsonObject obj = new JsonObject();

                obj.addProperty("quizId", quiz.getId());         // 題目 ID
                obj.addProperty("videoId", videoId);             // 所屬影片 ID
                obj.addProperty("question", quiz.getQuestion()); // 題目內容

                // ✅ 選項部分為陣列格式
                JsonArray options = new JsonArray();
                options.add(quiz.getOption1());
                options.add(quiz.getOption2());
                options.add(quiz.getOption3());
                options.add(quiz.getOption4());
                obj.add("options", options);

                // ✅ 顯示正確選項（如 A/B/C/D）
                obj.addProperty("answer", quiz.getCorrectOption());
                obj.addProperty("source", quiz.getSource()); // 題目來源（local 或 gpt）

                // ✅ 將此題加入 JSON 陣列中
                quizArray.add(obj);
            }

            // ✅ 輸出整個 JSON 陣列作為回應
            PrintWriter out = response.getWriter();
            out.print(quizArray.toString());
            out.flush();

        } catch (Exception e) {
            // ✅ 若資料庫或處理過程出錯，回傳 500 錯誤與例外訊息
            e.printStackTrace();
            response.sendError(500, "無法載入 quiz 資料：" + e.getMessage());
        }
    }
}
