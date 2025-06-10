package tw.shawn.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import org.json.JSONObject;
import tw.shawn.dao.VideoDAO;
import tw.shawn.model.Video;
import tw.shawn.util.DBUtil;

import java.io.IOException;
import java.sql.Connection;

// ✅ 註冊為 Servlet，對應的請求 URL 為 /api/getLatestVideo
@WebServlet("/api/getLatestVideo")
public class GetLatestVideoServlet extends HttpServlet {

    // ✅ 處理 HTTP GET 請求：取得資料庫中最新的影片並回傳 JSON 格式
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8"); // 設定回應內容為 JSON 格式

        try (Connection conn = DBUtil.getConnection()) {
            // 建立 DAO 物件操作 video 資料表
            VideoDAO dao = new VideoDAO(conn);

            // 從資料庫取得最新一筆影片資料（依 ID DESC 排序取第一筆）
            Video video = dao.getLatestVideo();

            // 若找不到資料（資料表為空），回傳 HTTP 404 錯誤
            if (video == null) {
                response.sendError(404, "找不到影片");
                return;
            }

            // 取出並清理影片的 videoId（去除 YouTube 網址格式）
            String cleanVideoId = extractYouTubeId(video.getVideoId());

            // 建立 JSON 物件包裝影片資訊
            JSONObject json = new JSONObject();
            json.put("id", video.getId());         // 資料庫主鍵 ID
            json.put("title", video.getTitle());   // 影片標題
            json.put("videoId", cleanVideoId);     // 清理過的 YouTube ID

            // 將 JSON 內容寫入回應輸出，傳送給前端
            response.getWriter().write(json.toString());

        } catch (Exception e) {
            e.printStackTrace(); // 印出錯誤訊息方便除錯
            response.sendError(500, "後端錯誤：" + e.getMessage()); // 發生例外時回傳 500
        }
    }

    /**
     * ✅ extractYouTubeId：從各種 YouTube 網址格式中萃取純粹的 videoId（範例：jYSBsUjXXog）
     * 支援三種常見格式：watch?v=、youtu.be/、embed/，並排除清單（playlist）型網址。
     * 若無法判斷格式，則原樣回傳。
     */
    private String extractYouTubeId(String url) {
        if (url == null || url.isEmpty()) return "";

        // ✅ 處理 YouTube 網址格式： https://www.youtube.com/watch?v=XXXX
        if (url.contains("youtube.com/watch?v=")) {
            String temp = url.substring(url.indexOf("watch?v=") + 8); // 擷取 v= 之後的 ID
            int ampIndex = temp.indexOf("&"); // 判斷是否還有附加參數
            return (ampIndex != -1) ? temp.substring(0, ampIndex) : temp;
        }

        // ✅ 處理短網址格式： https://youtu.be/XXXX
        if (url.contains("youtu.be/")) {
            String temp = url.substring(url.indexOf("youtu.be/") + 9);
            int paramIndex = temp.indexOf("?");
            return (paramIndex != -1) ? temp.substring(0, paramIndex) : temp;
        }

        // ✅ 處理嵌入網址格式： https://www.youtube.com/embed/XXXX
        if (url.contains("embed/")) {
            String temp = url.substring(url.indexOf("embed/") + 6);
            int paramIndex = temp.indexOf("?");
            return (paramIndex != -1) ? temp.substring(0, paramIndex) : temp;
        }

        // ✅ 若是播放清單連結，視為無效 ID，避免誤用
        if (url.contains("list=")) {
            return "INVALID_PLAYLIST_ID";
        }

        // 若不屬於任何已知格式，直接回傳原始字串
        return url;
    }
}
