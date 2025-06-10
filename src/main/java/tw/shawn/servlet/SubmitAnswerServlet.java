package tw.shawn.servlet;

import com.google.gson.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import tw.shawn.dao.AnswerDAO;
import tw.shawn.dao.QuizDAO;
import tw.shawn.dao.QuizResultDAO;
import tw.shawn.model.Answer;
import tw.shawn.model.Quiz;
import tw.shawn.util.DBUtil;

import java.io.*;
import java.sql.Connection;

@WebServlet("/api/submitAnswer")
public class SubmitAnswerServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");

        try (
            BufferedReader reader = req.getReader();
            Connection conn = DBUtil.getConnection()
        ) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            Gson gson = new Gson();
            JsonObject json = gson.fromJson(sb.toString(), JsonObject.class);
            int userId = json.get("userId").getAsInt();
            JsonArray answersJson = json.getAsJsonArray("answers");

            AnswerDAO answerDAO = new AnswerDAO(conn);
            QuizDAO quizDAO = new QuizDAO(conn);
            QuizResultDAO resultDAO = new QuizResultDAO(conn);

            int correctCount = 0;
            String videoId = null;

            if (answersJson.size() > 0) {
                videoId = answersJson.get(0).getAsJsonObject().get("videoId").getAsString();
            }

            if (videoId == null || videoId.isEmpty()) {
                throw new IllegalArgumentException("⚠️ videoId 為 null 或空字串！");
            }

            // ✅ 先刪除舊作答紀錄（避免殘留）
            answerDAO.deleteAnswersByUser(userId, videoId);

            // ✅ 逐題寫入新作答
            for (JsonElement elem : answersJson) {
                JsonObject a = elem.getAsJsonObject();

                String quizIdRaw = a.get("quizId").getAsString();
                int selected = a.get("selectedOption").getAsInt();
                String selectedText = a.has("selectedText") ? a.get("selectedText").getAsString() : null;
                String correctAnswer = a.has("correctAnswer") ? a.get("correctAnswer").getAsString() : null;
                String source = a.has("source") ? a.get("source").getAsString() : "local";

                Quiz quiz = null;
                boolean isCorrect = false;
                int quizId = -1;

                Answer ans = new Answer();
                ans.setUserId(userId);
                ans.setSelectedOption(selected);
                ans.setCorrect(false);
                ans.setSource(source);
                ans.setVideoId(videoId);

                try {
                    quizId = Integer.parseInt(quizIdRaw);
                    quiz = quizDAO.getQuizById(quizId);
                } catch (NumberFormatException ignored) {}

                if (quiz != null && !"gpt".equalsIgnoreCase(source)) {
                    isCorrect = (selected == quiz.getCorrectIndex());

                    ans.setQuizId(quiz.getId());
                    ans.setQuestion(quiz.getQuestion());
                    ans.setOption1(quiz.getOption1());
                    ans.setOption2(quiz.getOption2());
                    ans.setOption3(quiz.getOption3());
                    ans.setOption4(quiz.getOption4());
                    ans.setAnswer(quiz.getAnswer());
                    ans.setAnswerIndex(quiz.getCorrectIndex());
                } else {
                    isCorrect = selectedText != null && correctAnswer != null &&
                                normalize(selectedText).equals(normalize(correctAnswer));

                    try {
                        quizId = Integer.parseInt(quizIdRaw);
                    } catch (NumberFormatException e) {
                        quizId = -1;
                    }
                    ans.setQuizId(quizId);

                    ans.setQuestion(a.has("question") && !a.get("question").isJsonNull()
                            ? a.get("question").getAsString() : "");

                    if (a.has("options") && a.get("options").isJsonArray()) {
                        JsonArray options = a.getAsJsonArray("options");
                        ans.setOption1(options.size() > 0 ? options.get(0).getAsString() : "");
                        ans.setOption2(options.size() > 1 ? options.get(1).getAsString() : "");
                        ans.setOption3(options.size() > 2 ? options.get(2).getAsString() : "");
                        ans.setOption4(options.size() > 3 ? options.get(3).getAsString() : "");

                        int idx = -1;
                        for (int i = 0; i < options.size(); i++) {
                            if (normalize(options.get(i).getAsString()).equals(normalize(correctAnswer))) {
                                idx = i;
                                break;
                            }
                        }
                        ans.setAnswerIndex(idx);
                    }

                    ans.setAnswer(correctAnswer != null ? correctAnswer : "");
                }

                ans.setCorrect(isCorrect);
                answerDAO.insertAnswer(ans);
                if (isCorrect) correctCount++;
            }

            resultDAO.insertQuizResult(userId, videoId, correctCount, answersJson.size());

            JsonObject result = new JsonObject();
            result.addProperty("correctCount", correctCount);
            resp.getWriter().write(gson.toJson(result));

        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(500);
            resp.getWriter().write("{\"error\": \"伺服器處理錯誤\"}");
        }
    }

    private String normalize(String text) {
        return text == null ? "" : text.replaceAll("[\\s\\p{Punct}（）]+", "").toLowerCase();
    }
}
