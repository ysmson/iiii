package tw.shawn.servlet;

import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import tw.shawn.dao.UserDAO;
import tw.shawn.util.DBUtil;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;

/**
 * GetExpServlet：用於回傳指定使用者的經驗值
 * 請求格式：GET /api/getExp?userId=123
 * 回傳格式：{ "exp": 25 }
 */
@WebServlet("/api/getExp") // Servlet 註冊路徑
public class GetExpServlet extends HttpServlet {

    /**
     * 處理 GET 請求：根據 userId 查詢經驗值並回傳 JSON 格式
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8"); // 設定回應為 JSON 格式

        try (
            PrintWriter out = response.getWriter();                  // 輸出工具
            Connection conn = DBUtil.getConnection()                // 資料庫連線
        ) {
            // 取得使用者 ID 參數並轉換為整數
            int userId = Integer.parseInt(request.getParameter("userId"));

            // 使用 UserDAO 查詢該使用者的經驗值
            UserDAO dao = new UserDAO(conn);
            int exp = dao.getExp(userId);

            // 建立 JSON 物件回傳 { "exp": 數值 }
            JsonObject json = new JsonObject();
            json.addProperty("exp", exp);
            out.write(json.toString());

        } catch (Exception e) {
            // 發生錯誤時回傳 HTTP 500 錯誤與錯誤訊息
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
}
