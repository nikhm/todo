import com.google.gson.Gson;
import javafx.util.Pair;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class Utilities {

    public static Gson gson = null;

    public static String prepareJson(String key,String message){
        if(gson == null) gson = new Gson();
        Map<String,String> currentMap = new HashMap<String, String>();
        currentMap.put(key,message);
        return gson.toJson(currentMap);
    }

    public static void sendJsonResponse(HttpServletResponse resp,String s) throws IOException{
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter writer = resp.getWriter();
        writer.write(s);
        writer.flush();
    }

    public static void sendResultMessage(HttpServletResponse resp,String message) throws ServletException, IOException{
        String s = Utilities.prepareJson("result",message);
        Utilities.sendJsonResponse(resp,s);
    }

    public static void createTodo(HttpServletRequest req, HttpServletResponse resp,Pair<Integer,String> pair) throws ServletException, IOException {
        Container container = Container.getInstance();
        //Pair<Integer,String> pair = getIdAndRnd(req);
        int userId = pair.getKey();
        String rnd = pair.getValue();
        String message = req.getParameter("message");
        if(container.createTodo(userId,rnd,message)){
            resp.sendRedirect("/main.html?addtodo=successful");
        }else{
            resp.sendRedirect("/main.html?addtodo=failed");
        }
    }

    public static void loginHelper(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException{
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        if(username == null || password == null){
            return;
        }
        Container container = Container.getInstance();
        Pair<Integer,String> useridAndRnd = container.authenticateUser(username,password);
        if(useridAndRnd != null){
            int userId = useridAndRnd.getKey();
            String rnd = useridAndRnd.getValue();
            resp.addCookie(new Cookie("userid",Integer.toString(userId)));
            resp.addCookie(new Cookie("rnd",rnd));
            resp.sendRedirect("/main.html");
        }else{
            resp.sendRedirect("/login.html?authentication=false");
        }
    }

    public static void logoutHelper(HttpServletRequest req, HttpServletResponse resp,Pair<Integer,String> idAndRnd) throws ServletException, IOException {
        Container container = Container.getInstance();
        int userId = idAndRnd.getKey();
        String rnd = idAndRnd.getValue();
        if(container.logout(userId,rnd)){
            resp.sendRedirect("/login.html");
        }else{
            resp.sendRedirect("/login.html");
        }
    }

    public static void signupHelper(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        if(username == null || password == null){
            resp.sendRedirect("/login.html");
            return;
        }
        Container container = Container.getInstance();
        if(container.createUser(username,password)){
            resp.sendRedirect("/login.html?signup=true");
        }else{
            resp.sendRedirect("/login.html"); // add params saying login not successful.
        }
    }

    public static void mainHelper(HttpServletRequest req, HttpServletResponse resp,Pair<Integer,String> idAndRnd) throws ServletException, IOException {
        Container container = Container.getInstance();
        //Pair<Integer,String> pair = getIdAndRnd(req);
        int userId = idAndRnd.getKey();
        String rnd = idAndRnd.getValue();
        if(userId == -1 || !container.authenticateCurrentUser(userId,rnd)){
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
        }else{
            int numList = Integer.parseInt(req.getParameter("numlist"));
            int changesList = Integer.parseInt(req.getParameter("changeslist"));
            String s = container.getJSONData(numList,changesList);
            Utilities.sendJsonResponse(resp,s);
        }
    }
    public static void progressHelper(HttpServletRequest req, HttpServletResponse resp,Pair<Integer,String> idAndRnd) throws ServletException, IOException {
        Container container = Container.getInstance();
        //Pair<Integer,String> pair = getIdAndRnd(req);
        int userId = idAndRnd.getKey();
        String rnd = idAndRnd.getValue();
        int todoId = Integer.parseInt(req.getParameter("todoid"));
        if(container.progressTodo(userId,rnd,todoId)){
            Utilities.sendResultMessage(resp,"Progress has been made on the Todo");
        }else{
            Utilities.sendResultMessage(resp,"No progress made!");
        }
    }

    public static void deleteHelper(HttpServletRequest req, HttpServletResponse resp,Pair<Integer,String> idAndRnd) throws ServletException, IOException {
        Container container = Container.getInstance();
        //Pair<Integer,String> pair = getIdAndRnd(req);
        int userId = idAndRnd.getKey();
        String rnd = idAndRnd.getValue();
        int todoId = Integer.parseInt(req.getParameter("todoid"));
        if(container.deleteTodo(userId,rnd,todoId)){
            Utilities.sendResultMessage(resp,"Todo has been deleted");
        }else{
            Utilities.sendResultMessage(resp,"You cannot delete this todo");
        }
    }

    public static void getUsernameHelper(HttpServletRequest req, HttpServletResponse resp,Pair<Integer,String> idAndRnd) throws ServletException, IOException {
        Container container = Container.getInstance();
        //Pair<Integer,String> pair = getIdAndRnd(req);
        String name = container.getUsername(idAndRnd.getKey(),idAndRnd.getValue());
        Utilities.sendResultMessage(resp,name);
    }
}