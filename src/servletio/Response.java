package servletio;

import java.io.*;

import javax.servlet.http.*;

public class Response {

  public HttpServletResponse raw;
  private String             body;

  protected Response() {

  }

  Response(final HttpServletResponse response) {
    this.raw = response;
  }

  /**
   * Sets the status code for the
   *
   * @param statusCode
   *          the status code
   */
  public void status(final int statusCode) {
    raw.setStatus(statusCode);
  }

  /**
   * Sets the content type for the response
   *
   * @param contentType
   *          the content type
   */
  public void type(final String contentType) {
    raw.setContentType(contentType);
  }

  /**
   * Sets the body
   *
   * @param body
   *          the body
   */
  public void body(final String body) {
    this.body = body;
  }

  /**
   * returns the body
   *
   * @return the body
   */
  public String body() {
    return this.body;
  }

  /**
   * Trigger a browser redirect
   *
   * @param location
   *          Where to redirect
   */
  public void redirect(String location) {
    try {
      raw.sendRedirect(location);
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }

  /**
   * Trigger a browser redirect with specific http 3XX status code.
   *
   * @param location
   *          Where to redirect permanently
   * @param httpStatusCode
   *          the http status code
   */
  public void redirect(final String location, final int httpStatusCode) {
    raw.setStatus(httpStatusCode);
    raw.setHeader("Location", location);
    raw.setHeader("Connection", "close");
    try {
      raw.sendError(httpStatusCode);
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }

  /**
   * Adds/Sets a response header
   *
   * @param header
   *          the header
   * @param value
   *          the value
   */
  public void header(final String header, final String value) {
    raw.addHeader(header, value);
  }

  /**
   * Adds not persistent cookie to the response. Can be invoked multiple times
   * to insert more than one cookie.
   *
   * @param name
   *          name of the cookie
   * @param value
   *          value of the cookie
   */
  public void cookie(final String name, final String value) {
    cookie(name, value, -1, false);
  }

  /**
   * Adds cookie to the response. Can be invoked multiple times to insert more
   * than one cookie.
   *
   * @param name
   *          name of the cookie
   * @param value
   *          value of the cookie
   * @param maxAge
   *          max age of the cookie in seconds (negative for the not persistent
   *          cookie, zero - deletes the cookie)
   */
  public void cookie(final String name, final String value, final int maxAge) {
    cookie(name, value, maxAge, false);
  }

  /**
   * Adds cookie to the response. Can be invoked multiple times to insert more
   * than one cookie.
   *
   * @param name
   *          name of the cookie
   * @param value
   *          value of the cookie
   * @param maxAge
   *          max age of the cookie in seconds (negative for the not persistent
   *          cookie, zero - deletes the cookie)
   * @param secured
   *          if true : cookie will be secured zero - deletes the cookie)
   */
  public void cookie(final String name, final String value, final int maxAge,
      final boolean secured) {
    cookie("", name, value, maxAge, secured);
  }

  /**
   * Adds cookie to the response. Can be invoked multiple times to insert more
   * than one cookie.
   *
   * @param path
   *          path of the cookie
   * @param name
   *          name of the cookie
   * @param value
   *          value of the cookie
   * @param maxAge
   *          max age of the cookie in seconds (negative for the not persistent
   *          cookie, zero - deletes the cookie)
   * @param secured
   *          if true : cookie will be secured zero - deletes the cookie)
   */
  public void cookie(final String path, final String name, final String value, final int maxAge, final boolean secured) {
    final Cookie cookie = new Cookie(name, value);
    cookie.setPath(path);
    cookie.setMaxAge(maxAge);
    cookie.setSecure(secured);
    raw.addCookie(cookie);
  }

  /**
   * Removes the cookie.
   *
   * @param name
   *          name of the cookie
   */
  public void removeCookie(final String name) {
    final Cookie cookie = new Cookie(name, "");
    cookie.setMaxAge(0);
    raw.addCookie(cookie);
  }

  public void badRequest() {
    try {
      raw.sendError(HttpServletResponse.SC_BAD_REQUEST);
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }

  /**
   * Print from writer of request.
   *
   * @param Object
   *          to print
   */
  public void print(Object o, String contentType) {
    try {
      PrintWriter pw = raw.getWriter();
      raw.setContentType(contentType);
      pw.print(o);
      // pw.close();
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }

  public void print(Object o) {
    print(o, "text/plain");
  }

  public void printHtml(Object o) {
    print(o, "text/html");
  }

  public void printJson(Object o) {
    print(o, "application/json");
  }

  public void printXml(Object o) {
    print(o, "application/xml");
  }
}
