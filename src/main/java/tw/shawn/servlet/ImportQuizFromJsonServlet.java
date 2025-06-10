package tw.shawn.servlet;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.*;

import tw.shawn.dao.QuizDAO;
import tw.shawn.model.Quiz;
import tw.shawn.util.DBUtil;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.util.List;

// ✅ 註冊為 Servlet 並啟用 MultipartConfig（支援上傳檔案）
// 此 Servlet 對應路徑為 /api/importQuiz，可接收 quiz.json 匯入題目
@WebServlet("/api/importQuiz")
@MultipartConfig
public class ImportQuizFromJsonServlet extends HttpServlet {

    // ✅ 處理 POST 請求（上傳 quiz.json 檔案並寫入資料庫）
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        // 設定回應內容型別為 JSON，並使用 UTF-8 編碼
        response.setContentType("application/json;charset=UTF-8");
        request.setCharacterEncoding("UTF-8");

        // ✅ 取得影片 videoId 作為參數（用來對應哪部影片的題庫）
        String videoIdParam = request.getParameter("videoId");
        if (videoIdParam == null) {
            // 若缺少必要參數，回傳 400 錯誤
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "缺少 videoId");
            return;
        }

        // ✅ 將傳入的字串型別轉為 int 型別（對應影片主鍵 ID）
        int videoId = Integer.parseInt(videoIdParam);

        // ✅ 取得檔案上傳區段（名稱應為 "file"）
        Part filePart = request.getPart("file");
        if (filePart == null || filePart.getSize() == 0) {
            // 若未上傳或檔案為空，回傳 400 錯誤
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "未上傳 quiz.json 檔案");
            return;
        }

        // ✅ 使用 try-with-resources 同時開啟檔案讀取與資料庫連線（自動關閉資源）
        try (InputStream inputStream = filePart.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
             Connection conn = DBUtil.getConnection()) {

            // ✅ 使用 Gson 將上傳的 JSON 字串轉換為 List<Quiz>
            Gson gson = new Gson();
            Type quizListType = new TypeToken<List<Quiz>>() {}.getType();
            List<Quiz> quizList = gson.fromJson(reader, quizListType);

            // ✅ 建立 DAO 實例並將 quiz 清單寫入資料庫（內含避免重複的邏輯）
            QuizDAO quizDAO = new QuizDAO(conn);
            int inserted = quizDAO.insertQuizListAvoidDuplicate(String.valueOf(videoId), quizList);

            // ✅ 回傳成功訊息，包含實際成功插入的題數
            PrintWriter out = response.getWriter();
            out.write("{\"success\": true, \"inserted\": " + inserted + "}");

        } catch (Exception e) {
            // 若發生例外錯誤，印出錯誤訊息並回傳 500 狀態碼
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "匯入失敗：" + e.getMessage());
        }
    }
}
