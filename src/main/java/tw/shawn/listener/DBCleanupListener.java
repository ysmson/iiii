package tw.shawn.listener;

// 匯入 Servlet 監聽器與 JDBC 工具類別
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import tw.shawn.util.DBUtil;

/**
 * DBCleanupListener：應用程式層級的 Listener，用來處理 Web 應用的啟動與關閉事件。
 * 此類別會在 WebApp 啟動與關閉時自動被呼叫，常用來進行資源初始化或清理。
 */
@WebListener // 標記這個類別是 Web 應用程式的 Listener（等同 web.xml 註冊 <listener>）
public class DBCleanupListener implements ServletContextListener {

	
    /**
     * ✅ 當 Web 應用程式關閉時（ServletContext 被銷毀），自動呼叫此方法
     * 通常用來釋放資源，例如關閉背景執行緒、釋放 JDBC 驅動等
     */
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("🧹 WebApp 正在關閉，釋放 JDBC 清理執行緒...");
        
        // 呼叫自訂的 JDBC 清理方法
        // 常見用途：避免 JDBC Driver 沒有被正確移除，導致記憶體洩漏（Tomcat 中尤為重要）
        DBUtil.cleanupDriver();
    }

    /**
     * ✅ 當 Web 應用程式啟動時（ServletContext 初始化），自動呼叫此方法
     * 可在此執行初始化邏輯，例如載入設定、初始化資源等
     */
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // 此區塊可自訂 WebApp 啟動時的初始化行為
        // 目前僅列印訊息
        System.out.println("🚀 WebApp 啟動完成！");
    }
}
