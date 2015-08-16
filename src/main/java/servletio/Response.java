package servletio;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

public class Response {

    public final HttpServletResponse raw;
    private String body;


    Response(HttpServletResponse response) {
        this.raw = response;
    }

    /**
     * Sets the status code for the
     *
     * @param statusCode
     *            the status code
     */
    public void status(int statusCode) {
        raw.setStatus(statusCode);
    }

    /**
     * Sets the content type for the response
     *
     * @param contentType
     *            the content type
     */
    public void type(String contentType) {
        raw.setContentType(contentType);
    }

    /**
     * Sets the body
     *
     * @param body
     *            the body
     */
    public void body(String body) {
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
     *            Where to redirect
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
     *            Where to redirect permanently
     * @param httpStatusCode
     *            the http status code
     */
    public void redirect(String location, int httpStatusCode) {
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
     * Adds a response header
     *
     * @param header
     *            the header
     * @param value
     *            the value
     */
    public void addHeader(String header, String value) {
        raw.addHeader(header, value);
    }
    
    /**
     * Sets a response header
     *
     * @param header
     *            the header
     * @param value
     *            the value
     */
    public void setHeader(String header, String value) {
        raw.setHeader(header, value);
    }
    
    /**
     * Sets a response date header
     *
     * @param header
     *            the header
     * @param value
     *            the value
     */
    public void setDateHeader(String name, long date){
        raw.setDateHeader(name, date);
    }

    /**
     * Adds not persistent cookie to the response. Can be invoked multiple times
     * to insert more than one cookie.
     *
     * @param name
     *            name of the cookie
     * @param value
     *            value of the cookie
     */
    public void cookie(String name, String value) {
        cookie(name, value, -1, false);
    }

    /**
     * Adds cookie to the response. Can be invoked multiple times to insert more
     * than one cookie.
     *
     * @param name
     *            name of the cookie
     * @param value
     *            value of the cookie
     * @param maxAge
     *            max age of the cookie in seconds (negative for the not
     *            persistent cookie, zero - deletes the cookie)
     */
    public void cookie(String name, String value, int maxAge) {
        cookie(name, value, maxAge, false);
    }

    /**
     * Adds cookie to the response. Can be invoked multiple times to insert more
     * than one cookie.
     *
     * @param name
     *            name of the cookie
     * @param value
     *            value of the cookie
     * @param maxAge
     *            max age of the cookie in seconds (negative for the not
     *            persistent cookie, zero - deletes the cookie)
     * @param secured
     *            if true : cookie will be secured zero - deletes the cookie)
     */
    public void cookie(String name, String value, int maxAge, boolean secured) {
        cookie("", name, value, maxAge, secured);
    }

    /**
     * Adds cookie to the response. Can be invoked multiple times to insert more
     * than one cookie.
     *
     * @param path
     *            of the cookie
     * @param name
     *            name of the cookie
     * @param value
     *            value of the cookie
     * @param maxAge
     *            max age of the cookie in seconds (negative for the not
     *            persistent cookie, zero - deletes the cookie)
     * @param secured
     *            if true : cookie will be secured zero - deletes the cookie)
     */
    public void cookie(String path, String name, String value, int maxAge,
            boolean secured) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath(path);
        cookie.setMaxAge(maxAge);
        cookie.setSecure(secured);
        raw.addCookie(cookie);
    }

    /**
     * Removes the cookie.
     *
     * @param name
     *            of the cookie
     */
    public void removeCookie(String name) {
        Cookie cookie = new Cookie(name, "");
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
     *            to print
     */
    public void print(String text, String contentType) {
        try {
            PrintWriter pw = raw.getWriter();
            raw.setContentType(contentType);
            if(text!=null) pw.print(text);
            pw.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void print(String text) {
        print(text, "text/plain");
    }

    public void printHtml(String text) {
        print(text, "text/html");
    }

    public void printJson(String text) {
        print(text, "application/json");
    }

    public void printXml(String text) {
        print(text, "application/xml");
    }
    
    public void sendFile(InputStream inputStream){
        try {
            int ch = -1;
            byte[] buffer = new byte[4096];
            while ((ch = inputStream.read(buffer)) !=-1) {
                raw.setContentType("application/octet-stream");
                raw.getWriter().print((char)ch);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (inputStream != null){
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
