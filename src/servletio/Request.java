package servletio;

import static java.util.Arrays.asList;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.*;
import java.math.BigInteger;
import java.util.*;

import javax.servlet.ServletException;
import javax.servlet.http.*;

import servletio.utils.IOUtils;

public class Request {

  private static final String      USER_AGENT = "user-agent";

  private final List<Class<?>>     assignables = asList(Boolean.class,
                                                  Character.class, Short.class,
                                                  Integer.class, Long.class,
                                                  Float.class, Double.class,
                                                  BigInteger.class,
                                                  String.class);

  public final HttpServletRequest  raw;

  public final String              scheme;
  public final String              method;
  public final String              uri;
  public final String              url;
  public final String              ip;
  public final String              userAgent;
  public final String              pathInfo;
  public final String              protocol;
  public final String              servletPath;
  public final String              contextPath;
  public final String              contentType;
  public final int                 contentLength;

  public final int                 port;

  public final Map<String, String> cookies;

  public final Set<String>         attributes;
  public final Set<String>         queryParams;
  public final Set<String>         headers;
  public Collection<Part>          parts;

  private Optional<Session>        session;

  Request(final HttpServletRequest request) {
    raw    = request;
    scheme = request.getScheme();
    method = request.getMethod();
    port   = request.getServerPort();
    uri    = request.getRequestURI();
    url    = request.getRequestURL().toString();
    ip     = request.getRemoteAddr();

    userAgent     = request.getHeader(USER_AGENT);
    pathInfo      = request.getPathInfo();
    protocol      = request.getProtocol();
    servletPath   = request.getServletPath();
    contextPath   = request.getContextPath();
    contentType   = request.getContentType();
    contentLength = request.getContentLength();
    queryParams   = request.getParameterMap().keySet();

    cookies    = cookies();
    attributes = attributes();
    headers    = headers();
  }

  private String getFileName(Part part) {
    return Arrays.stream(part.getHeader("content-disposition").split(";"))
    .filter(token -> token.trim().startsWith("filename"))
    .map   (token -> token.substring(token.indexOf("=") + 2, token.length() - 1))
    .findFirst()
    .orElse("");
  }

  private String tempPath() {
    return raw.getServletContext().getRealPath("") + File.separator + "temp";
  }

  public File getFile(final String name) {
    try {
      final Part filePart = raw.getPart(name);
      final String filePath = tempPath() + File.separator + getFileName(filePart);
      filePart.write(filePath);
      return new File(filePath);
    } catch (IOException | ServletException ex) {
      ex.printStackTrace();
      return null;
    }
  }

  public Dispatcher dispatcher(final String name) {
    return new Dispatcher(raw.getRequestDispatcher(name));
  }

  public String body() {
    try {
      return IOUtils.toString(raw.getInputStream());
    } catch (IOException ex) {
      ex.printStackTrace();
      return null;
    }
  }

  public Part part(final String name) {
    try {
      return raw.getPart(name);
    } catch (IOException | ServletException e) {
      e.printStackTrace();
      return null;
    }
  }

  public Collection<Part> parts() {
    try {
      return raw.getParts();
    } catch (IOException | ServletException e) {
      e.printStackTrace();
      return null;
    }
  }

  public String param(final String param) {
    return raw.getParameter(param);
  }

  /**
   * @return the context path
   */
  public String queryParams(String queryParam) {
    return raw.getParameter(queryParam);
  }

  /**
   * Gets the value for the provided header
   *
   * @param header
   *          the header
   * @return the value of the provided header
   */
  public String headers(final String header) {
    return raw.getHeader(header);
  }

  /**
   * @return all headers
   */
  public Set<String> headers() {
    return new TreeSet<>(Collections.list(raw.getHeaderNames()));
  }

  /**
   * @return the query string
   */
  public String queryString() {
    return raw.getQueryString();
  }

  /**
   * Sets an attribute on the request (can be fetched in filters/routes later in
   * the chain)
   *
   * @param attribute
   *          The attribute
   * @param value
   *          The attribute value
   */
  public void attribute(final String attribute, final Object value) {
    raw.setAttribute(attribute, value);
  }

  /**
   * Gets the value of the provided attribute
   *
   * @param attribute
   *          The attribute value or null if not present
   * @return the value for the provided attribute
   */
  public Object attribute(final String attribute) {
    return raw.getAttribute(attribute);
  }

  /**
   * @return all attributes
   */
  private Set<String> attributes() {
    return new HashSet<String>(Collections.list(raw.getAttributeNames()));
  }

  /**
   * Returns the current session associated with this request, or if the request
   * does not have a session, creates one.
   *
   * @return the session associated with this request
   */
  public Session session() {
    if (!session.isPresent()) {
      session =  Optional.of(new Session(raw.getSession()));
    }
    return session.get();
  }

  /**
   * Returns the current session associated with this request, or if there is no
   * current session and <code>create</code> is true, returns a new session.
   *
   * @param create
   *          <code>true</code> to create a new session for this request if
   *          necessary; <code>false</code> to return null if there's no current
   *          session
   * @return the session associated with this request or <code>null</code> if
   *         <code>create</code> is <code>false</code> and the request has no
   *         valid session
   *
   */
  public Session session(final boolean create) {
    if (!session.isPresent()) {
      Optional<HttpSession> httpSession = Optional.ofNullable(raw.getSession(create));
      if (httpSession.isPresent()) {
        session = Optional.of(new Session(httpSession.get()));
      }
    }
    return session.get();
  }

  /**
   * @return request cookies (or empty Map if cookies dosn't present)
   */
  private Map<String, String> cookies() {
    Map<String, String> result = new HashMap<String, String>();
    Optional<Cookie[]> cookies = Optional.ofNullable(raw.getCookies());
    if (cookies.isPresent()) {
      Arrays.stream(cookies.get())
      .parallel()
      .forEach(cookie -> result.put(cookie.getName(), cookie.getValue()));
    }
    return result;
  }

  /**
   * Gets cookie by name.
   *
   * @param name
   *          name of the cookie
   * @return cookie value or null if the cookie was not found
   */
  public Optional<String> cookie(String name) {
    Optional<Cookie[]> cookies = Optional.ofNullable(raw.getCookies());
    if (cookies.isPresent()) {
     return Arrays.stream(cookies.get())
      .filter(cookie -> cookie.getName().equals(name))
      .map(cookie -> cookie.getValue())
      .findFirst();
    }
    return Optional.empty();
  }

  public <T> Optional<T> bindParams(final Class<T> clazz) {
    final Optional<T> bindObject = bindInstance(clazz);
    bindObject.ifPresent(bo -> {
      Arrays.stream(clazz.getDeclaredMethods())
      .filter (m -> m.getName().startsWith("set"))
      .forEach(m -> {
        String prop = m.getName().substring(2).toLowerCase();
        Optional<String> value = Optional.ofNullable(raw.getParameter(prop));

        if (value.isPresent())
          assignables
          .stream()
          .parallel()
          .filter(c -> m.getParameterTypes()[0].isAssignableFrom(c))
          .findFirst()
          .ifPresent(c -> { 
             try                 { m.invoke(bo, c.cast(value));} 
             catch (Exception e) { e.printStackTrace();        }
          });
      });
    });
    return bindObject;
  }

  private <T> Optional<T> bindInstance(final Class<T> clazz) {
    T newInstance = null;
    try {
      if (Modifier.isStatic(clazz.getModifiers())) {
        Constructor<T> ctr = clazz.getDeclaredConstructor();
        ctr.setAccessible(true);
        newInstance = (T) ctr.newInstance();
      } else {
        newInstance = (T) clazz.newInstance();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return Optional.ofNullable(newInstance);
  }
}
