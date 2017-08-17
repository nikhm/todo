import javafx.util.Pair;

import javax.rmi.CORBA.Util;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by nikhil.mol on 10/08/17.
 */
public class CreateTodo extends IdAndRndInfo{

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Utilities.createTodo(req,resp,getIdAndRnd(req));
    }
}
