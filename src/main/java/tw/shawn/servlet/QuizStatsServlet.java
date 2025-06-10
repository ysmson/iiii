package tw.shawn.servlet;

import com.google.gson.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import tw.shawn.dao.AnswerDAO;
import tw.shawn.dao.QuizDAO;
import tw.shawn.model.Answer;
import tw.shawn.model.Quiz;
import tw.shawn.util.DBUtil;
import java.io.IOException;
import java.sql.Connection;
import java.util.*;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import tw.shawn.dao.QuizResultDAO;
import tw.shawn.model.QuizResult;

// ✅ 註冊為 Servlet，對應路徑為 /api/QuizStatsServlet
@WebServlet("/api/QuizStatsServlet")
public class QuizStatsServlet extends HttpServlet {

    // ✅ 處理 GET 請求：根據 userId 與 videoId 回傳該使用者最近一次測驗結果統計
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // 設定請求與回應的編碼格式為 UTF-8、資料格式為 JSON
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");

        // 從 URL 取得 videoId 與 userId（參數皆為必要）
        String videoId = req.getParameter("videoId");
        int userId = Integer.parseInt(req.getParameter("userId"));

        // ✅ 使用 try-with-resources 自動關閉 DB 連線
        try (Connection conn = DBUtil.getConnection()) {
            // ✅ 建立 QuizResultDAO 用來查詢作答統計資料
            QuizResultDAO resultDAO = new QuizResultDAO(conn);

            // ✅ 查詢該使用者觀看該影片的最近一次測驗結果
            QuizResult latest = resultDAO.getLatestQuizResult(userId, videoId);

            // ✅ 使用 Gson 組成 JSON 回應物件
            JsonObject json = new JsonObject();

            if (latest != null) {
                // 若有作答紀錄，計算並加入正確題數與答對率
                json.addProperty("total", latest.getTotalQuestions());    // 題目總數
                json.addProperty("correct", latest.getCorrectAnswers());  // 答對題數
                double accuracy = (double) latest.getCorrectAnswers() / latest.getTotalQuestions();
                json.addProperty("accuracy", String.format("%.2f", accuracy)); // 正確率（小數 2 位）
            } else {
                // 沒有紀錄的話回傳預設值
                json.addProperty("total", 0);
                json.addProperty("correct", 0);
                json.addProperty("accuracy", "N/A"); // 無資料
            }

            // ✅ 將 JSON 回傳給前端
            resp.getWriter().write(new Gson().toJson(json));

        } catch (Exception e) {
            // 發生例外錯誤時回傳 500 錯誤
            e.printStackTrace();
            resp.setStatus(500);
            resp.getWriter().write("{\"error\": \"無法載入統計資料\"}");
        }
    }
}
