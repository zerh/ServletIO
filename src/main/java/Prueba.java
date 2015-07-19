import javax.servlet.annotation.WebServlet;

import servletio.Request;
import servletio.Result;
import servletio.ServletIO;
import servletio.annotation.Get;

/**
 * Servlet implementation class Prueba
 */
@WebServlet("/algo/*")
public class Prueba extends ServletIO{
	
    private static final long serialVersionUID = 4749514773659822009L;

    @Get("/pepito")
    public Result pepito(Request req){
        return ok("<h1>Joder Tio</h1>").as("text");
    }
    
}
