package tw.shawn.dao;

// 匯入 Video 模型類別與 JDBC 所需套件
import tw.shawn.model.Video;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * VideoDAO：負責對 video 資料表進行 CRUD 操作（建立、讀取、更新、刪除）
 */
public class VideoDAO {
    // 資料庫連線物件（由建構子注入）
    private final Connection conn;

    // 建構子：接收 Connection 並初始化 DAO
    public VideoDAO(Connection conn) {
        this.conn = conn;
    }

    /**
     * ✅ 新增單筆影片資料至資料表
     * @param v Video 物件，包含影片所有屬性
     */
    public void insertVideo(Video v) throws SQLException {
        String sql = "INSERT INTO video (video_id, title, description, thumbnail_url, published_at) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, v.getVideoId());         // 設定 YouTube video_id
            stmt.setString(2, v.getTitle());           // 影片標題
            stmt.setString(3, v.getDescription());     // 影片描述
            stmt.setString(4, v.getThumbnailUrl());    // 縮圖網址
            stmt.setString(5, v.getPublishedAt());     // 發布時間（字串格式）
            stmt.executeUpdate();                      // 執行新增
        }
    }

    /**
     * ✅ 批次新增影片資料（呼叫 insertVideo 重複執行）
     * @param videos 多筆 Video 物件清單
     */
    public void insertVideoList(List<Video> videos) throws SQLException {
        for (Video v : videos) {
            insertVideo(v); // 每筆都呼叫 insertVideo()
        }
    }

    /**
     * ✅ 根據資料庫主鍵 id 取得單筆影片資料
     * @param id 資料庫內部主鍵（不是 YouTube ID）
     * @return Video 物件，若查不到則回傳 null
     */
    public Video getVideoById(String id) throws SQLException {
        String sql = "SELECT * FROM video WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id); // 設定查詢條件（主鍵 id）
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Video v = new Video();
                    v.setId(rs.getString("id"));                     // 影片主鍵 ID
                    v.setVideoId(rs.getString("video_id"));          // YouTube ID
                    v.setTitle(rs.getString("title"));               // 標題
                    v.setDescription(rs.getString("description"));   // 描述
                    v.setThumbnailUrl(rs.getString("thumbnail_url"));// 縮圖
                    v.setPublishedAt(rs.getString("published_at"));  // 發布日期
                    return v;
                }
            }
        }
        return null; // 查無資料時回傳 null
    }

    /**
     * ✅ 取得所有影片，依標題長度與字典順序排序（方便使用者瀏覽）
     * @return 所有影片的清單
     */
    public List<Video> getAllVideos() throws SQLException {
        List<Video> list = new ArrayList<>();
        String sql = "SELECT * FROM video ORDER BY LENGTH(title), title";

        // 使用 Statement 執行查詢
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Video v = new Video();
                v.setId(rs.getString("id"));                 // 主鍵 ID
                v.setVideoId(rs.getString("video_id"));     // YouTube ID
                v.setTitle(rs.getString("title"));          // 標題
                v.setDescription(rs.getString("description")); // 描述
                v.setThumbnailUrl(rs.getString("thumbnail_url")); // 縮圖
                v.setPublishedAt(rs.getString("published_at"));   // 發布時間
                list.add(v);
            }
        }
        return list; // 回傳影片清單
    }

    /**
     * ✅ 根據主鍵 id 取得對應影片的 YouTube 網址（video 表應包含 youtube_url 欄位）
     * @param id 資料庫主鍵
     * @return YouTube 網址字串，找不到則回傳 null
     */
    public String getYoutubeUrl(String id) throws SQLException {
        String sql = "SELECT youtube_url FROM video WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id); // 指定影片 id
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("youtube_url"); // 回傳網址
                }
            }
        }
        return null; // 找不到時回傳 null
    }

    /**
     * ✅ 取得資料表中最新的影片（依 id DESC 取第一筆）
     * @return 最新一筆 Video 物件，若無資料則回傳 null
     */
    public Video getLatestVideo() throws SQLException {
        String sql = "SELECT * FROM video ORDER BY id DESC LIMIT 1";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                Video v = new Video();
                v.setId(rs.getString("id"));             // 影片主鍵
                v.setVideoId(rs.getString("video_id"));  // YouTube ID
                v.setTitle(rs.getString("title"));       // 標題
                return v;
            }
        }
        return null; // 若沒有影片則回傳 null
    }

    /**
     * ✅ 依指定欄位進行影片排序（支援 title、published、videoId）
     * @param sortBy 排序欄位名稱
     * @return 排序後的影片清單
     */
    public List<Video> getAllVideosSorted(String sortBy) throws SQLException {
        List<Video> list = new ArrayList<>();

        // 動態決定 ORDER BY 子句內容
        String orderClause = switch (sortBy) {
            case "title" -> "ORDER BY title";                    // 依標題
            case "published" -> "ORDER BY published_at DESC";    // 依發布時間（新到舊）
            case "videoId" -> "ORDER BY video_id";               // 依 YouTube ID
            default -> "ORDER BY title";                         // 預設為標題
        };

        // 組合完整查詢語句
        String sql = "SELECT * FROM video " + orderClause;
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Video v = new Video();
                v.setId(rs.getString("id"));
                v.setVideoId(rs.getString("video_id"));
                v.setTitle(rs.getString("title"));
                v.setDescription(rs.getString("description"));
                v.setThumbnailUrl(rs.getString("thumbnail_url"));
                v.setPublishedAt(rs.getString("published_at"));
                list.add(v);
            }
        }
        return list; // 回傳排序後的影片列表
    }
}
