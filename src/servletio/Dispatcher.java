package servletio;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public class Dispatcher implements RequestDispatcher{

	public final RequestDispatcher raw;
	
	public Dispatcher(RequestDispatcher dispatcher){
		this.raw = dispatcher;
	}
	
	@Override
	public void forward(ServletRequest arg0, ServletResponse arg1)
			throws ServletException, IOException {
		raw.forward(arg0, arg1);
	}

	@Override
	public void include(ServletRequest arg0, ServletResponse arg1)
			throws ServletException, IOException {
		raw.include(arg0, arg1);;
	}
	
	public void forward(Request req, Response res){
		try{
			forward(req.raw, res.raw);
		}catch(IOException | ServletException ex){
			ex.printStackTrace();
		}
	}
	
	public void include(Request req, Response res){
		try {
			include(req.raw, res.raw);
		} catch (ServletException | IOException ex) {
			ex.printStackTrace();
		}
	}

}
