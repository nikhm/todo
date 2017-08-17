import javafx.util.Pair;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by nikhil.mol on 10/08/17.
 */
public class Logout extends IdAndRndInfo{
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Utilities.logoutHelper(req,resp,getIdAndRnd(req));
    }
}
