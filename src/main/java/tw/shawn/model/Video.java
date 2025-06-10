package tw.shawn.model;

/**
 * ✅ 影片模型類別：封裝 YouTube 影片的屬性
 * 對應資料庫中的 video 表格欄位，常用於 DAO、Servlet 與前端間的資料傳遞。
 */
public class Video {
    private String id;               // 資料庫主鍵 ID（varchar 型別，通常為 UUID 或流水號）
    private String videoId;          // YouTube 影片 ID（例如："dQw4w9WgXcQ"）
    private String title;            // 影片標題（例如："介紹台中美食"）
    private String description;      // 影片描述文字（影片說明欄內容）
    private String thumbnailUrl;     // YouTube 自動產生的縮圖 URL
    private String publishedAt;      // 影片發佈時間（ISO 格式，例如："2024-06-01T08:00:00Z"）

    // ✅ 無參數建構子（框架如 Gson、Jackson 轉換時需要）
    public Video() {}

    /**
     * ✅ 有參數建構子：快速建立影片資料物件
     * @param videoId YouTube ID
     * @param title 標題
     * @param description 描述
     * @param thumbnailUrl 縮圖連結
     * @param publishedAt 發布時間（ISO 格式）
     */
    public Video(String videoId, String title, String description, String thumbnailUrl, String publishedAt) {
        this.videoId = videoId;
        this.title = title;
        this.description = description;
        this.thumbnailUrl = thumbnailUrl;
        this.publishedAt = publishedAt;
    }

    // ✅ 以下為標準的 Getter / Setter，用於欄位存取與封裝

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getVideoId() { return videoId; }
    public void setVideoId(String videoId) { this.videoId = videoId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }

    public String getPublishedAt() { return publishedAt; }
    public void setPublishedAt(String publishedAt) { this.publishedAt = publishedAt; }

    /**
     * ✅ 回傳可直接嵌入網頁 iframe 的 YouTube 播放網址
     * 例如：若 videoId = "abc123"，回傳 https://www.youtube.com/embed/abc123
     * @return YouTube 影片播放網址
     */
    public String getVideoUrl() {
        return "https://www.youtube.com/embed/" + videoId;
    }
}
