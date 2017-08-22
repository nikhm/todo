import com.google.gson.Gson;
import javafx.util.Pair;

import java.util.*;

public abstract class AbstractContainer {

    ArrayList<Todo> todoList;
    ArrayList<Integer> changesList;
    ArrayList<String>  changesPosition;
    ArrayList<String> changesUsername;

    Gson gson;

    public static AbstractContainer getInstance(){return null;}

    public abstract boolean createUser(String name, String password);

    public abstract Pair<Integer,String> authenticateUser(String username, String password);

    public abstract boolean authenticateCurrentUser(int userId,String rnd);

    public abstract boolean logout(int userId,String rnd);

    public abstract String getUsername(int userId,String rnd);

    public abstract boolean createTodo(int userId,String rnd,String message);

    public abstract boolean deleteTodo(int userId,String rnd,int todoId);

    public abstract boolean progressTodo(int userId,String rnd,int todoId);

    public abstract String getJSONData(int numList,int numChanges);
}
