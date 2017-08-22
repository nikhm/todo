import com.google.gson.Gson;
import javafx.util.Pair;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Container extends AbstractContainer{

    private static Container container;
    private static int userId = 0;
    private static int todoId = 0;
    //private static int numChanges = 0; // keeps track of all changes to todos list
    //private static int numList = 0;

    Gson gson;

    ConcurrentHashMap<Integer,User> idToUser;
    ConcurrentHashMap<Integer,String> idToPassword;
    ConcurrentHashMap<String,Integer> nameToId;
    ConcurrentHashMap<Integer,String> idToCookie;
    ConcurrentHashMap<Integer,Todo> idToTodo;
    ConcurrentHashMap<Integer,Integer> todoToUserid;
    ArrayList<Todo> todoList;
    ArrayList<Integer> changesList;
    ArrayList<String>  changesPosition;
    ArrayList<String> changesUsername;

    public static Container getInstance(){
        if(container == null){
            container = new Container();
        }
        return container;
    }

    private Container(){
        gson = new Gson();

        idToUser = new ConcurrentHashMap<Integer, User>();
        idToPassword = new ConcurrentHashMap<Integer, String>();
        nameToId = new ConcurrentHashMap<String, Integer>();
        idToCookie = new ConcurrentHashMap<Integer, String>();
        idToTodo = new ConcurrentHashMap<Integer, Todo>();
        todoToUserid = new ConcurrentHashMap<Integer, Integer>();
        todoList = new ArrayList<Todo>();

        changesList = new ArrayList<Integer>();
        changesPosition = new ArrayList<String>();
        changesUsername = new ArrayList<String>();
    }

    public synchronized boolean createUser(String name, String password){
        if(!customSanitize(name)) return false;
        if(nameToId.containsKey(name)) return false;
        User user = new User(name,++userId);
        idToUser.put(userId,user);
        idToPassword.put(userId,password);
        nameToId.put(user.getName(),userId);
        return true;
    }

    public Pair<Integer,String> authenticateUser(String username, String password){
        if(!nameToId.containsKey(username)){
            return null;
        }
        if(!password.equals(idToPassword.get(userId))){
            return null;
        }
        int userId = nameToId.get(username);
        String rnd = UUID.randomUUID().toString();
        idToCookie.put(userId,rnd);
        Pair<Integer,String> pair = new Pair<Integer, String>(userId,rnd);

        return new Pair<Integer, String>(userId,rnd);
    }

    public boolean authenticateCurrentUser(int userId,String rnd){
        if(idToCookie.containsKey(userId)){
            return idToCookie.get(userId).equals(rnd);
        }
        return false;
    }

    public boolean logout(int userId,String rnd){
        if(authenticateCurrentUser(userId,rnd)){
            idToCookie.remove(userId);
            return true;
        }
        return false;
    }

    public String getUsername(int userId,String rnd){
        String def = "User";
        if(authenticateCurrentUser(userId,rnd)){
            def = idToUser.get(userId).getName();
        }
        return def;
    }

    public boolean createTodo(int userId,String rnd,String message){
        if(!customSanitize(message)) return false;
        if(authenticateCurrentUser(userId,rnd)){
            Todo todo;
            synchronized ((Integer)this.todoId) {
                todo = new Todo(userId, idToUser.get(userId).getName(),message, ++todoId, new Date());
            }
            synchronized (this.todoList) {
                todoList.add(todo);
            }
            idToTodo.put(todoId, todo);
            return true;
        }else{
            return false;
        }
    }

    public synchronized boolean deleteTodo(int userId,String rnd,int todoId){
        if(authenticateCurrentUser(userId,rnd)){
            Todo todo = idToTodo.get(todoId);
            if(todo.getCurrentUser() == userId) {
                idToTodo.remove(todoId);
                todoList.remove(todo);
                changesList.add(todoId);
                changesPosition.add("delete");
                changesUsername.add(idToUser.get(userId).getName());
                return true;
            }else{
                return false;
            }
        }
        return false;
    }

    public synchronized boolean progressTodo(int userId,String rnd,int todoId){
        //System.out.println("Progress to do:");
        if(authenticateCurrentUser(userId,rnd)){
            Todo todo = idToTodo.get(todoId);
            if(todo.getCategory().equals("progress") && !(todo.getCurrentUser() == userId)){
                return false;
            }
            workWithTodo(todo,userId,todoId);
            //System.out.println("What to do here!");
            return true;
        }else{
            return false;
        }
    }


    public void workWithTodo(Todo todo,int userId,int todoId){
        //System.out.println("workWithTOdo!");
        idToUser.get(userId).assignTodo(todoId);
        String currentCategory = idToTodo.get(todoId).getCategory();
        todo.setCurrentUser(userId,idToUser.get(userId).getName());
        boolean isChanged = todo.doProgress();
        //System.out.println("Reached here");
        synchronized(this.changesList) {
            //System.out.println("Here as well!");
            if(isChanged){
                changesList.add(todoId);
                if(currentCategory.equals("new")){
                    changesPosition.add("progress");
                }else if(currentCategory.equals("progress")){
                    changesPosition.add("complete");
                }
                changesUsername.add(idToUser.get(todo.getCurrentUser()).getName());
            }
        }
    }

    public String getJSONData(int numList,int numChanges){
        numList = Math.max(0,numList);
        numChanges = Math.max(0,numChanges);
        Map<String,List> list = new HashMap<String, List>();
        list.put("todos",extractFrom(todoList,numList));

        ArrayList numTodos = new ArrayList<Integer>(1); numTodos.add(todoList.size());
        list.put("numlist",numTodos);

        ArrayList numChange = new ArrayList<Integer>(1); numChange.add(changesList.size());
        list.put("changelist",numChange);

        list.put("changes",extractFrom(changesList,numChanges));

        list.put("changeposition",extractFrom(changesPosition,numChanges));

        list.put("changeusername",extractFrom(changesUsername,numChanges));
        //System.out.println(gson.toJson(todoList));
        return gson.toJson(list);
    }

    public <T> ArrayList<T> extractFrom(ArrayList<T> list,int index){
        int length = list.size();
        ArrayList<T> arrayList = new ArrayList<T>();
        for(int i=index;i<length;i++){
            arrayList.add(list.get(i));
        }
        return arrayList;
    }
}
