import java.util.Date;

public class Todo {

    private int id;
    private int currentUser;
    private String message;
    private String userName;
    private Date date;
    private String category;

    public Todo(int currentUser, String userName, String message, int id, Date date){
        this.currentUser = currentUser;
        this.userName = userName;
        this.message = message;
        this.id = id;
        this.category = "new";
    }

    public String getCategory() {
        return category;
    }

    public int getId() {
        return id;
    }

    public int getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(int currentUser,String userName) {
        this.currentUser = currentUser;
        this.userName = userName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public boolean doProgress(){
        if(this.category.equals("new")){
            this.category = "progress";
            return true;
        }else if(this.category.equals("progress")){
            this.category = "complete";
            return true;
        }
        return false;
    }

}