import javafx.util.Pair;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

public abstract class IdAndRndInfo extends HttpServlet{
    public Pair<Integer,String> getIdAndRnd(HttpServletRequest request){
        Cookie[] cookies = request.getCookies();
        int userId = -1;
        String rnd = "";
        if(cookies == null) {
            return new Pair<Integer, String>(userId,rnd);
        }
        for(Cookie cookie : cookies){
            if(cookie.getName().equals("userid") ){
                userId = Integer.parseInt(cookie.getValue());
            }else if(cookie.getName().equals("rnd") ){
                rnd = cookie.getValue();
            }
        }
        return new Pair<Integer, String>(userId,rnd);
    }
}
