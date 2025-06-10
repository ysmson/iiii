package tw.shawn.servlet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import tw.shawn.dao.UserDAO;
import tw.shawn.util.DBUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;

/**
 * ExpServlet：接收使用者經驗值更新請求（來源可能是觀看影片或答題）
 * 支援 POST 請求，並以 JSON 格式傳入以下欄位：
 * {
 *     "userId": 123,
 *     "source": "watch" or "quiz",
 *     "exp": 5
 * }
 */
@WebServlet("/api/exp") // 註冊 Servlet 路徑
public class ExpServlet extends HttpServlet {

    /**
     * 處理 POST 請求：接收 JSON 格式的 userId、exp、source，並更新對應使用者的經驗值
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 設定請求與回應編碼
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/plain;charset=UTF-8");

        try (
            BufferedReader reader = request.getReader();           // 讀取請求 JSON 主體
            PrintWriter out = response.getWriter();               // 回應輸出
            Connection conn = DBUtil.getConnection()              // 建立資料庫連線
        ) {
            // 解析 JSON 內容
            Gson gson = new Gson();
            JsonObject json = gson.fromJson(reader, JsonObject.class);

            // ✅ 防呆：檢查 JSON 是否包含所需欄位
            if (json == null || !json.has("userId") || !json.has("source") || !json.has("exp")) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write("❌ 錯誤：請求資料不完整或不是有效的 JSON 格式");
                return;
            }

            // 從 JSON 中讀取欄位
            int userId = json.get("userId").getAsInt();        // 使用者 ID
            String source = json.get("source").getAsString();  // 經驗值來源（watch 或 quiz）
            int exp = json.get("exp").getAsInt();              // 要增加的經驗值

            // 呼叫 DAO 更新資料
            UserDAO userDAO = new UserDAO(conn);
            userDAO.addExp(userId, exp);

            // 成功訊息輸出
            out.write("✅ 成功累加經驗值：" + exp + " 點（來源：" + source + "）");

        } catch (Exception e) {
            // 若處理過程中發生錯誤，回傳 HTTP 500 並印出錯誤訊息
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("🚫 發生錯誤：" + e.getMessage());
        }
    }
}
