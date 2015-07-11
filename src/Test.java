

import java.io.File;

import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;

import servletio.*;
import servletio.annotation.*;

@MultipartConfig
@WebServlet("/path/*")
public class Test extends ServletIO {

	private static final long serialVersionUID = 1L;

	@Get("/nestle")
	public void chiquito(Request req, Response res) {
    	req.dispatcher("/WEB-INF/views/index.jsp").forward(req, res);
	}
	
	@Get("/nestle2")
	public void chiquindolo(Request req, Response res) {
    	res.print("test");
	}
}
