package tw.shawn.servlet;

// ✅ 匯入所需套件與工具
import com.google.gson.Gson;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import tw.shawn.dao.VideoDAO;
import tw.shawn.model.Video;
import tw.shawn.util.DBUtil;

import java.io.IOException;
import java.sql.Connection;
import java.util.List;

// ✅ Servlet 註冊路徑為 /api/videoList，提供影片清單資料（可選擇排序方式）
@WebServlet("/api/videoList")
public class VideoListServlet extends HttpServlet {

    // ✅ 處理前端傳來的 GET 請求：回傳影片列表 JSON，可依條件排序
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8"); // 設定回應格式為 JSON
        request.setCharacterEncoding("UTF-8"); // 確保讀取參數時避免亂碼

        // ✅ 接收前端傳來的排序參數（例如 ?sortBy=title），可為 title、published、videoId 等欄位
        String sortBy = request.getParameter("sortBy");

        try (Connection conn = DBUtil.getConnection()) {
            // ✅ 建立 DAO 物件來操作 video 資料表
            VideoDAO dao = new VideoDAO(conn);

            List<Video> videoList;

            if (sortBy != null && !sortBy.isBlank()) {
                // ✅ 若有提供排序欄位，呼叫支援排序的方法
                videoList = dao.getAllVideosSorted(sortBy);
            } else {
                // ✅ 否則使用預設方法（不排序或預設排序）
                videoList = dao.getAllVideos();
            }

            // ✅ 使用 Gson 將 videoList（List<Video>）轉為 JSON 字串
            Gson gson = new Gson();
            String json = gson.toJson(videoList);

            // ✅ 將 JSON 回傳給前端頁面
            response.getWriter().write(json);

        } catch (Exception e) {
            // 發生例外錯誤時，印出錯誤並回傳 HTTP 500 錯誤訊息
            e.printStackTrace();
            response.sendError(500, "伺服器錯誤：" + e.getMessage());
        }
    }
}
