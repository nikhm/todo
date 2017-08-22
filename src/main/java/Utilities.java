import com.google.gson.Gson;
import javafx.util.Pair;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class Utilities {

    private static final boolean USE_DATABASE = true;

    public static Gson gson = null;

    private static AbstractContainer container;

    public static String prepareJson(String key,String message){
        if(gson == null) gson = new Gson();
        Map<String,String> currentMap = new HashMap<String, String>();
        currentMap.put(key,message);
        return gson.toJson(currentMap);
    }

    public static AbstractContainer getContainer(){
        if(USE_DATABASE){
            return DatabaseContainer.getInstance();
        }else{
            return Container.getInstance();
        }
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
        //Pair<Integer,String> pair = getIdAndRnd(req);
        container = getContainer();
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
        //Container container = Container.getInstance();
        container = getContainer();
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
        //Container container = Container.getInstance();
        container = getContainer();
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
        //Container container = Container.getInstance();
        container = getContainer();
        if(container.createUser(username,password)){
            resp.sendRedirect("/login.html?signup=true");
        }else{
            resp.sendRedirect("/login.html"); // add params saying login not successful.
        }
    }

    public static void mainHelper(HttpServletRequest req, HttpServletResponse resp,Pair<Integer,String> idAndRnd) throws ServletException, IOException {
        container = getContainer();
        //Pair<Integer,String> pair = getIdAndRnd(req);
        int userId = idAndRnd.getKey();
        String rnd = idAndRnd.getValue();
        if(userId == -1 || !container.authenticateCurrentUser(userId,rnd)){
            //resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            //resp.sendRedirect("/login.html");
            String s = "{\"redirect\":\"/login.html\"}";
            Utilities.sendJsonResponse(resp,s);
        }else{
            int numList = Integer.parseInt(req.getParameter("numlist"));
            int changesList = Integer.parseInt(req.getParameter("changeslist"));
            String s = container.getJSONData(numList,changesList);
            Utilities.sendJsonResponse(resp,s);
        }
    }
    public static void progressHelper(HttpServletRequest req, HttpServletResponse resp,Pair<Integer,String> idAndRnd) throws ServletException, IOException {
        container = getContainer();
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
        container = getContainer();
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
        container = getContainer();
        //Pair<Integer,String> pair = getIdAndRnd(req);
        String name = container.getUsername(idAndRnd.getKey(),idAndRnd.getValue());
        Utilities.sendResultMessage(resp,name);
    }

    public static void jsHelper(HttpServletRequest req, HttpServletResponse resp, ServletContext context) throws ServletException, IOException {
        String allContent = Utilities.getCombinedFile(Utilities.getFilesList(req.getParameter("filenames")),context);
        resp.setContentType("application/javascript");
        resp.getWriter().write(allContent);
        resp.getWriter().flush();
    }

    public static String getCombinedFile(ArrayList<String> filesNames, ServletContext context) throws IOException{
        StringBuffer allContent = new StringBuffer();
        for(String fileName : filesNames){
            allContent.append(readFromFile(fileName,context));
        }
        return allContent.toString();
    }

    public static ArrayList<String> getFilesList(String filesString) {
        StringTokenizer tokenizer = new StringTokenizer(filesString,",");
        ArrayList<String> files = new ArrayList<String>();
        while(tokenizer.hasMoreElements()){
            files.add((String) tokenizer.nextElement());
        }
        return files;
    }

    public static String readFromFile(String file, ServletContext context) throws IOException{
        StringBuffer content = new StringBuffer();
        BufferedReader br = new BufferedReader( new InputStreamReader(context.getResourceAsStream(file)));
        String line = null;
        while( (line = br.readLine()) != null ){
            content.append(line);
            content.append("\n");
        }
        return content.toString();
    }

}
