import java.util.TreeSet;

public class User {
    private String name;
    private int id;
    private TreeSet<Integer> todos;
    public User(String name,int id){
        this.name = name;
        todos = new TreeSet<Integer>();
        this.id = id;
    }
    public String getName(){
        return name;
    }
    public int getId(){
        return id;
    }
    public void assignTodo(int todoId){
        todos.add(todoId);
    }
    public void removeTodo(int todoId){
        todos.remove(todoId);
    }
}
