package servletio;

import java.io.IOException;

import javax.servlet.*;

public class Dispatcher implements RequestDispatcher {

  public final RequestDispatcher raw;

  public Dispatcher(final RequestDispatcher dispatcher) {
    this.raw = dispatcher;
  }

  @Override
  public void forward(final ServletRequest req, final ServletResponse resp) throws ServletException, IOException {
    raw.forward(req, resp);
  }

  @Override
  public void include(final ServletRequest req, final ServletResponse resp) throws ServletException, IOException {
    raw.include(req, resp);
  }

  public void forward(final Request req, final Response res) {
    try {
      forward(req.raw, res.raw);
    } catch (IOException | ServletException ex) {
      ex.printStackTrace();
    }
  }

  public void include(final Request req, final Response res) {
    try {
      include(req.raw, res.raw);
    } catch (ServletException | IOException ex) {
      ex.printStackTrace();
    }
  }

}
