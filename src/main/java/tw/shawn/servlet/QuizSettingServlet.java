package tw.shawn.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;

// ✅ 註冊為 Servlet，處理 /quizSetting 路徑的 GET 請求
@WebServlet("/quizSetting")
public class QuizSettingServlet extends HttpServlet {

    // ✅ 處理 GET 請求，動態輸出 HTML 頁面，提供題目設定 UI
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 設定回應為 UTF-8 編碼的 HTML 格式
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        // ✅ 輸出整個 HTML 網頁內容（包含 head, body, CSS, 表單等）
        out.println("<!DOCTYPE html>");
        out.println("<html lang='zh-Hant'>");
        out.println("<head>");
        out.println("<meta charset='UTF-8'>");
        out.println("<title>題目設定</title>");

        // ✅ 內嵌 CSS：設定整體網頁樣式
        out.println("<style>");
        out.println("body { margin: 0; font-family: sans-serif; background-color: #111; color: #000; padding-top: 80px; padding-bottom: 60px;}");
        out.println(".navbar { background-color: white; padding: 20px; display: flex; justify-content: space-between; align-items: center; position: fixed; top: 0; width: 100%; }");
        out.println(".btn:hover { background-color: #333; }");

        out.println(".nav-left { font-size: 24px; margin-left: 20px; }");
        out.println(".nav-right { display: flex; gap: 20px; margin-right: 40px; font-size: 18px; }");

        out.println(".container { background-color: white; margin: 80px auto; padding: 40px; max-width: 640px; border-radius: 6px; box-shadow: 0 4px 16px rgba(0,0,0,0.2); }");

        out.println(".row { display: flex; align-items: center; margin-bottom: 30px; }");
        out.println(".label { width: 150px; font-size: 20px; }");
        out.println("input[type='text'] { width: 150px; padding: 5px; background-color: black; color: white; border: none; text-align: center; }");
        out.println("input[type='range'] { width: 100%; }");

        out.println(".btn { background-color: black; color: white; padding: 10px 30px; border: none; font-size: 18px; float: right; cursor: pointer; }");

        out.println(".footer { background-color: white; text-align: center; padding: 20px; font-size: 18px; position: fixed; bottom: 0; width: 100%; }");
        out.println("</style>");
        out.println("</head>");
        out.println("<body>");

        // ✅ 導覽列區塊
        out.println("<div class='navbar'>");
        out.println("<div class='nav-left'>Logo</div>");
        out.println("<div class='nav-right'>");
        out.println("<div>測驗中心</div>");
        out.println("<div>商城</div>");
        out.println("<div>經驗值</div>");
        out.println("<div>我的資料</div>");
        out.println("<div>設定</div>");
        out.println("</div>");
        out.println("</div>");

        // ✅ 表單設定區塊（題數與難度）
        out.println("<div class='container'>");
        out.println("<form id='settingForm' action='#' method='post'>");

        // ✅ 題數輸入欄位
        out.println("<div class='row'>");
        out.println("<div class='label'>總題數</div>");
        out.println("<input type='text' name='totalQuestions' placeholder='Input Text'>");
        out.println("</div>");

        // ✅ 難度選擇欄位（radio 單選）
        out.println("<div class='row'>");
        out.println("<div class='label'>難易度分配</div>");
        out.println("<label><input type='radio' name='difficulty' value='basic' checked> 基礎</label>");
        out.println("<label style='margin-left:20px;'><input type='radio' name='difficulty' value='normal'> 普通</label>");
        out.println("<label style='margin-left:20px;'><input type='radio' name='difficulty' value='advanced'> 進階</label>");
        out.println("</div>");

        // ✅ 送出按鈕
        out.println("<button type='submit' class='btn'>確認</button>");
        out.println("</form>");
        out.println("</div>");

        // ✅ 前端 JavaScript：攔截表單送出並導向到 quiz.html，帶上參數
        out.println("<script>");
        out.println("document.getElementById('settingForm').onsubmit = function(e) {");
        out.println("    e.preventDefault();"); // 停止預設送出行為
        out.println("    var quizNum = document.querySelector('[name=totalQuestions]').value;");
        out.println("    var difficulty = document.querySelector('[name=difficulty]:checked').value;");
        out.println("    // 若未輸入題數則警告並中止跳轉");
        out.println("    if(!quizNum) { alert('請輸入總題數'); return; }");
        out.println("    window.location.href = 'quiz.html?quizNum=' + encodeURIComponent(quizNum) + '&difficulty=' + encodeURIComponent(difficulty);");
        out.println("};");
        out.println("</script>");

        // ✅ 頁面底部的固定頁尾
        out.println("<div class='footer'>客服信箱</div>");
        out.println("</body>");
        out.println("</html>");
    }
}
