package servletio;

import java.io.*;

//import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/static/*")
public class DefaultWrapperServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		/*
		
		RequestDispatcher rd = getServletContext()
				.getNamedDispatcher("default");

		HttpServletRequest wrapped = new HttpServletRequestWrapper(req) {
			public String getServletPath() {
				return "";
			}
		};

		rd.forward(wrapped, resp);
		*/
	}
}