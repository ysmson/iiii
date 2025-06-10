package tw.shawn.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import org.json.JSONObject;
import tw.shawn.dao.VideoDAO;
import tw.shawn.model.Video;
import tw.shawn.util.DBUtil;

import java.io.IOException;
import java.sql.Connection;

// ✅ 註冊為 Servlet，設定對應路徑為 /api/getVideo，可接收前端影片查詢請求
@WebServlet("/api/getVideo")
public class GetVideoServlet extends HttpServlet {

    // ✅ 處理 GET 請求：依據 videoId 查詢資料庫中對應影片並以 JSON 格式回傳
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 從請求中取得參數 videoId（需轉為 int，資料庫主鍵）
        int videoId = Integer.parseInt(request.getParameter("videoId"));

        // 使用 try-with-resources 自動關閉資料庫連線
        try (Connection conn = DBUtil.getConnection()) {
            // ✅ 建立 DAO 物件操作 video 資料表
            VideoDAO dao = new VideoDAO(conn);

            // ✅ 查詢對應 videoId 的影片資料（注意此處 videoId 傳入為字串）
            Video video = dao.getVideoById(String.valueOf(videoId));

            // 若找不到資料，回傳 HTTP 404 錯誤與錯誤訊息
            if (video == null) {
                response.sendError(404, "找不到影片");
                return;
            }

            // ✅ 擷取影片的原始 YouTube 連結欄位
            String rawVideoId = video.getVideoId();

            // ✅ 呼叫工具方法將網址清理成純粹的 YouTube videoId（去除參數等）
            String cleanVideoId = extractYouTubeId(rawVideoId);

            // ✅ 將影片資訊轉換為 JSON 格式
            JSONObject json = new JSONObject();
            json.put("id", video.getId());           // 資料庫中主鍵 ID
            json.put("title", video.getTitle());     // 影片標題
            json.put("videoId", cleanVideoId);       // 清理後的 YouTube ID

            // ✅ 設定回應為 JSON 並將資料傳回前端
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(json.toString());

        } catch (Exception e) {
            // 發生任何例外錯誤（轉型、連線、DAO 操作等）時，回傳 HTTP 500
            e.printStackTrace();
            response.sendError(500, "後端錯誤：" + e.getMessage());
        }
    }

    /**
     * ✅ extractYouTubeId：擷取純粹的 YouTube 影片 ID（去除網址參數）
     * 支援多種常見連結格式：
     * - https://www.youtube.com/watch?v=xxxx
     * - https://youtu.be/xxxx
     * - https://www.youtube.com/embed/xxxx
     * 若遇到播放清單網址（list=），回傳特定識別字串「INVALID_PLAYLIST_ID」
     * 若為原始 ID（非網址格式），則直接回傳
     */
    private String extractYouTubeId(String url) {
        if (url == null || url.isEmpty()) return "";

        // ✅ 處理 watch?v=xxxx 格式
        if (url.contains("youtube.com/watch?v=")) {
            String temp = url.substring(url.indexOf("watch?v=") + 8);
            int ampIndex = temp.indexOf("&"); // 若有 & 表示後續有參數
            return (ampIndex != -1) ? temp.substring(0, ampIndex) : temp;
        }

        // ✅ 處理 youtu.be/xxxx 短網址格式
        if (url.contains("youtu.be/")) {
            String temp = url.substring(url.indexOf("youtu.be/") + 9);
            int paramIndex = temp.indexOf("?");
            return (paramIndex != -1) ? temp.substring(0, paramIndex) : temp;
        }

        // ✅ 處理 embed/xxxx 嵌入網址格式
        if (url.contains("embed/")) {
            String temp = url.substring(url.indexOf("embed/") + 6);
            int paramIndex = temp.indexOf("?");
            return (paramIndex != -1) ? temp.substring(0, paramIndex) : temp;
        }

        // ✅ 偵測播放清單網址並回傳無效 ID 字串，避免後續誤用
        if (url.contains("list=")) {
            return "INVALID_PLAYLIST_ID";
        }

        // ✅ 若不是網址格式，視為已經是 videoId，直接回傳
        return url;
    }
}
