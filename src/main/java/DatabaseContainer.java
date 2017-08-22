import com.google.gson.Gson;
import javafx.util.Pair;
import java.sql.*;
import java.util.*;
import java.util.Date;

public class DatabaseContainer extends AbstractContainer{
    private static AbstractContainer container;
    private static final String USER = "root";
    private static final String PASS = "";

    private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    private static final String DB_URL = "jdbc:mysql://localhost/todo";
    private Connection connection;
    //private PreparedStatement statement;

    private static int userId = 0;
    private static int todoId = 0;
    private static int changeId = 0;

    public static AbstractContainer getInstance(){
        if(container == null){
            container = new DatabaseContainer();
        }
        return container;
    }

    private DatabaseContainer(){
        try {
            Class.forName(JDBC_DRIVER);
            connection = DriverManager.getConnection(DB_URL, USER, PASS);
            gson = new Gson();
            resetIDs();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void resetIDs(){
        String userIdQuery = "SELECT MAX(id) AS id FROM USERS;";
        String todoIdQuery = "SELECT MAX(id) AS id FROM TODOS;";
        String changeIdQuery = "SELECT MAX(id) AS id FROM CHANGES;";
        try{
            PreparedStatement statement;
            statement = connection.prepareStatement(userIdQuery);
            ResultSet r = statement.executeQuery();
            statement = connection.prepareStatement(todoIdQuery);
            ResultSet s = statement.executeQuery();
            statement = connection.prepareStatement(changeIdQuery);
            ResultSet t = statement.executeQuery();
            if(r.next()){
                userId = r.getInt("id");
            }
            if(s.next()){
                todoId = s.getInt("id");
            }
            if(t.next()){
                changeId = t.getInt("id");
            }
            System.out.println("userId:"+userId+" todoId:"+todoId + " changeId:"+changeId);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private boolean usernameExists(String username){
        String userQuery = "SELECT username FROM USERS WHERE username = ?;";
        try{
            PreparedStatement statement;
            statement = connection.prepareStatement(userQuery);
            statement.setString(1,username);
            boolean result =  (statement.executeQuery().next());
            if(result) System.out.println(username + " exists already!");
            else System.out.println("No user " + username);
            return result;
        }catch(Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public synchronized boolean createUser(String name, String password){
        if(!customSanitize(name)) return false;
        if(usernameExists(name)) return false;
        String insertQuery = "INSERT INTO USERS VALUES (?,?,?)";
        try {
            PreparedStatement statement;
            statement = connection.prepareStatement(insertQuery);
            statement.setInt(1,++userId);
            statement.setString(2,name);
            statement.setString(3,password);
            int numRows = statement.executeUpdate();
            boolean result = (numRows == 1);
            if(result) System.out.println("User " + name + " has been created!");
            else System.out.println(name + " already exists");
            return result;
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public Pair<Integer,String> authenticateUser(String username, String password){
        String authenticateQuery = "SELECT * FROM USERS WHERE username = ? AND password = ?;";
        String tokenQuery = "INSERT INTO TOKENS VALUES (?,?)";
        try {
            PreparedStatement statement;
            statement = connection.prepareStatement(authenticateQuery);
            statement.setString(1, username);
            statement.setString(2,password);
            ResultSet resultSet = statement.executeQuery();
            if(resultSet.next()){
                System.out.println("User " + username + " is being logged in");
                int userId = resultSet.getInt("id");
                String rnd = UUID.randomUUID().toString();
                statement = connection.prepareStatement(tokenQuery);
                statement.setInt(1,userId);
                statement.setString(2,rnd);
                int numRows = statement.executeUpdate();
                return (numRows == 1) ? new Pair<Integer,String>(userId,rnd) : null;
            }
            return null;
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public boolean authenticateCurrentUser(int userId,String rnd){
        String authenticateQuery = "SELECT * FROM TOKENS WHERE id = ? AND rnd = ?;";
        try{
            PreparedStatement statement;
            statement = connection.prepareStatement(authenticateQuery);
            statement.setInt(1,userId);
            statement.setString(2,rnd);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        }catch(Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public boolean logout(int userId,String rnd){
        if(!authenticateCurrentUser(userId, rnd)) return false;
        String logoutQuery = "DELETE FROM TOKENS WHERE id = ?;";
        try{
            PreparedStatement statement;
            statement = connection.prepareStatement(logoutQuery);
            statement.setInt(1,userId);
            //statement.setString(2,rnd);
            int numRows = statement.executeUpdate();
            return (numRows == 1);
        }catch(Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public String getUsername(int userId,String rnd){
        if(authenticateCurrentUser(userId,rnd)){
            return getUsername(userId);
        }
        return "User";
    }

    private String getUsername(int userId){
        String getNameQuery = "SELECT username FROM USERS WHERE id = ?;";
        try{
            PreparedStatement statement;
            statement = connection.prepareStatement(getNameQuery);
            statement.setInt(1,userId);
            System.out.println("Getting Name\n userid: " + userId);
            ResultSet resultSet = statement.executeQuery();
            if(resultSet != null && resultSet.next()){
                System.out.println("Result set inside");
                return resultSet.getString("username");
            }
            System.out.println("Result Set is " + resultSet);
        }catch(Exception e){
            e.printStackTrace();
        }
        return "User";
    }

    public boolean createTodo(int userId,String rnd,String message){
        if(!customSanitize(message)) return false;
        if(!authenticateCurrentUser(userId,rnd)) return false;
        String todoQuery = "INSERT INTO TODOS VALUES (?,?,?,?);";
        try{
            PreparedStatement statement;
            statement = connection.prepareStatement(todoQuery);
            synchronized ((Integer)this.todoId) {
                statement.setInt(1, ++todoId);
            }
            statement.setString(2,message);
            statement.setInt(3,userId);
            statement.setString(4,"new");
            int numRows  = statement.executeUpdate();
            return (numRows == 1);
        }catch(Exception e){
            e.printStackTrace();
        }
        return false;
    }

    private boolean userDoesTodo(int userId, int todoId){
        String getTodoRow = "SELECT userid FROM TODOS WHERE id = ?;";
        try{
            PreparedStatement statement = connection.prepareStatement(getTodoRow);
            statement.setInt(1,todoId);
            ResultSet resultSet = statement.executeQuery();
            if(resultSet != null && resultSet.next()){
                return (resultSet.getInt("userid") == userId);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public synchronized boolean deleteTodo(int userId,String rnd,int todoId){
        if(!authenticateCurrentUser(userId,rnd) || !userDoesTodo(userId,todoId)) return false;
        String deleteTodoQuery = "DELETE FROM TODOS WHERE id = ?;";
        try{
            PreparedStatement statement;
            statement = connection.prepareStatement(deleteTodoQuery);
            statement.setInt(1,todoId );
            int numRows = statement.executeUpdate();
            if(numRows == 1){
                String updateChange = "INSERT INTO CHANGES VALUES (?,?,?,?);";
                statement = connection.prepareStatement(updateChange);
                synchronized ((Integer)this.changeId) {
                    statement.setInt(1, ++changeId);
                }
                statement.setInt(2,todoId);
                statement.setString(3,"delete");
                statement.setInt(4,userId);
                return ( 1 == statement.executeUpdate() );
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public synchronized boolean progressTodo(int userId,String rnd,int todoId){
        if(!authenticateCurrentUser(userId,rnd)) return false;
        try{
            PreparedStatement statement;
            String todoQuery = "SELECT category,userid FROM TODOS WHERE id = ?;";
            statement = connection.prepareStatement(todoQuery);
            statement.setInt(1,todoId);
            ResultSet resultSet = statement.executeQuery();
            if(resultSet != null && resultSet.next()){
                String category = resultSet.getString("category");
                int userIdFromTodo = resultSet.getInt("userid");
                if(category.equals("progress") && (userIdFromTodo != userId)) return false;

                String updateString = "UPDATE TODOS SET category = ? , userid = ? WHERE id = ?;";
                statement = connection.prepareStatement(updateString);

                if(category.equals("new")){
                    statement.setString(1,"progress");
                    statement.setInt(2,userId);
                    statement.setInt(3,todoId);
                    int numRows = statement.executeUpdate();
                    if(numRows != 1) return false;
                    return workWithTodo(todoId,"progress",userId);
                }else if(category.equals("progress")){
                    statement.setString(1,"complete");
                    statement.setInt(2,userIdFromTodo); // userId == userIdFromTodo
                    statement.setInt(3,todoId);
                    int numRows = statement.executeUpdate();
                    if(numRows != 1) return false;
                    return workWithTodo(todoId,"complete",userId);
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public boolean workWithTodo(int todoId,String category,int userId){
        String changeString = "INSERT INTO CHANGES VALUES (?,?,?,?)";
        try {
            PreparedStatement statement;
            statement = connection.prepareStatement(changeString);
            statement.setInt(1, ++changeId);
            statement.setInt(2, todoId);
            statement.setString(3, category);
            statement.setInt(4, userId);
            return (1 == statement.executeUpdate());
        }catch(Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public String getJSONData(int numList, int numChanges) {
        Map<String,List> list = new HashMap<String, List>();
        if(numList == -1 && numChanges == -1){
            //First request from client. So just send complete list
            numList = 0; numChanges = changeId;
        }
        String todoListQuery = "SELECT * FROM TODOS WHERE id > ?;";
        String changeListQuery = "SELECT * FROM CHANGES WHERE id > ?;";
        insertTodos(list,todoListQuery,numList);
        insertChanges(list,changeListQuery,numChanges);
        String ret = gson.toJson(list);
        System.out.println("Returned response:\n" + ret);
        return ret;
    }

    public void insertTodos(Map<String,List> list,String query,int numList){
        ArrayList<Todo> todoArrayList = new ArrayList<Todo>();
        int count = todoId;
        try {
            PreparedStatement statement;
            statement = connection.prepareStatement(query);
            statement.setInt(1,numList);
            ResultSet resultSet = statement.executeQuery();
            while(resultSet.next()){
                int todoId = resultSet.getInt("id");
                count = Math.max(count,todoId);
                String message = resultSet.getString("message");
                int userId = resultSet.getInt("userid");
                String category = resultSet.getString("category");
                Todo tmp = new Todo(userId,getUsername(userId),message,todoId,new Date());
                tmp.setCategory(category);
                todoArrayList.add(tmp);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        ArrayList<Integer> arr = new ArrayList<Integer>(); arr.add(count);
        list.put("numlist",arr);
        list.put("todos",todoArrayList);
        return;
    }

    public void insertChanges(Map<String,List> list,String query,int numChanges){
        ArrayList<Integer> changesList = new ArrayList<Integer>();
        ArrayList<String> changesPosition = new ArrayList<String>();
        ArrayList<String> changesUsername = new ArrayList<String>();
        int count = changeId;
        try{
            PreparedStatement statement;
            statement = connection.prepareStatement(query);
            statement.setInt(1,numChanges);
            ResultSet resultSet = statement.executeQuery();
            while(resultSet.next()){
                int changeId = resultSet.getInt("id");
                count = Math.max(count,changeId);
                int todoId = resultSet.getInt("todoid");
                changesList.add(todoId);
                String category = resultSet.getString("category");
                changesPosition.add(category);
                int userId = resultSet.getInt("userid");
                changesUsername.add(getUsername(userId));
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        ArrayList<Integer> arr = new ArrayList<Integer>(); arr.add(count);
        list.put("changelist",arr);
        list.put("changes",changesList);
        list.put("changeposition",changesPosition);
        list.put("changeusername",changesUsername);
        return;
    }

    public <T> ArrayList<T> extractFrom(ArrayList<T> list, int index) {
        return null;
    }
}
