package servletio;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import servletio.utils.IOUtils;

public class Request {

    private static final String USER_AGENT = "user-agent";
    
    public final HttpServletRequest raw;

    public final String scheme;
    public final String method;
    public final String uri;
    public final String url;
    public final String ip;
    public final String userAgent;
    public final String pathInfo;
    public final String protocol;
    public final String servletPath;
    public final String contextPath;
    public final String contentType;
    public final int contentLength;

    public final int port;

    public final Map<String, String> cookies;
    public final Set<String> attributes;
    public final Set<String> queryParams;
    public final Set<String> headers;
    public Collection<Part> parts;
    
	private Session session;

    Request(HttpServletRequest request) {
        raw = request;
        scheme = request.getScheme();
        method = request.getMethod();
        port = request.getServerPort();
        uri = request.getRequestURI();
        url = request.getRequestURL().toString();
        ip = request.getRemoteAddr();
        userAgent = request.getHeader(USER_AGENT);
        pathInfo = request.getPathInfo();
        protocol = request.getProtocol();
        servletPath = request.getServletPath();
        contextPath = request.getContextPath();
        contentType = request.getContentType();
        contentLength = request.getContentLength();
        queryParams = request.getParameterMap().keySet();
        
        cookies = cookies();
        attributes = attributes();
        headers = headers();
    }
    
    
    
    private String getFileName(Part part) {
        String contentDisp = part.getHeader("content-disposition");
        String[] tokens = contentDisp.split(";");
        for (String token : tokens) {
            if (token.trim().startsWith("filename")) {
                return token.substring(token.indexOf("=") + 2, token.length()-1);
            }
        }
        return "";
    }
    
    private String tempPath(){
		String applicationPath = raw.getServletContext().getRealPath("");
	    return applicationPath + File.separator + "temp";
	}
        
    public File getFile(String name){
		try {
		    final Part filePart = raw.getPart(name);
		    final String fileName = getFileName(filePart);
		    try {
		        filePart.write(tempPath() + File.separator + fileName);
		        return new File(tempPath() + File.separator + fileName);
		        
		    } catch (FileNotFoundException ex) {
		    	ex.printStackTrace();
		    	return null;
		    }
		} catch (IOException | ServletException ex) {
			ex.printStackTrace();
			return null;
		}
	}
    
    public Dispatcher dispatcher(String name){
    	return new Dispatcher(raw.getRequestDispatcher(name));
    }

    public String body(){
    	try{
        	return IOUtils.toString(raw.getInputStream());
        }catch(IOException ex){
        	ex.printStackTrace();
        	return null;  
        }
    }
    
    public Part part(String name){
    	try {
			return raw.getPart(name);
		} catch (IOException | ServletException e) {
			e.printStackTrace();
			return null;
		}
    }
    
    public Collection<Part> parts(){
    	try {
			return raw.getParts();
		} catch (IOException | ServletException e) {
			e.printStackTrace();
			return null;
		}
    }
    
    
    public String param(String param) {
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
     * @param header the header
     * @return the value of the provided header
     */
    public String headers(String header) {
        return raw.getHeader(header);
    }

    /**
     * @return all headers
     */
    public Set<String> headers() {
    	Set<String> headers = new TreeSet<String>();
        Enumeration<String> enumeration = raw.getHeaderNames();
        while (enumeration.hasMoreElements()) {
        	headers.add(enumeration.nextElement());
        }
        return headers;
    }

    /**
     * @return the query string
     */
    public String queryString() {
        return raw.getQueryString();
    }

    /**
     * Sets an attribute on the request (can be fetched in filters/routes later in the chain)
     *
     * @param attribute The attribute
     * @param value     The attribute value
     */
    public void attribute(String attribute, Object value) {
        raw.setAttribute(attribute, value);
    }

    /**
     * Gets the value of the provided attribute
     *
     * @param attribute The attribute value or null if not present
     * @return the value for the provided attribute
     */
    public Object attribute(String attribute) {
        return raw.getAttribute(attribute);
    }


    /**
     * @return all attributes
     */
    private Set<String> attributes() {
        Set<String> attrList = new HashSet<String>();
        Enumeration<String> attributes = (Enumeration<String>) raw.getAttributeNames();
        while (attributes.hasMoreElements()) {
            attrList.add(attributes.nextElement());
        }
        return attrList;
    }

    /**
     * Returns the current session associated with this request,
     * or if the request does not have a session, creates one.
     *
     * @return the session associated with this request
     */
    public Session session() {
        if (session == null) {
            session = new Session(raw.getSession());
        }
        return session;
    }

    /**
     * Returns the current session associated with this request, or if there is
     * no current session and <code>create</code> is true, returns  a new session.
     *
     * @param create <code>true</code> to create a new session for this request if necessary;
     *               <code>false</code> to return null if there's no current session
     * @return the session associated with this request or <code>null</code> if
     * <code>create</code> is <code>false</code> and the request has no valid session
     *
     */
    public Session session(boolean create) {
        if (session == null) {
            HttpSession httpSession = raw.getSession(create);
            if (httpSession != null) {
                session = new Session(httpSession);
            }
        }
        return session;
    }

    /**
     * @return request cookies (or empty Map if cookies dosn't present)
     */
    private Map<String, String> cookies() {
        Map<String, String> result = new HashMap<String, String>();
        Cookie[] cookies = raw.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                result.put(cookie.getName(), cookie.getValue());
            }
        }
        return result;
    }

    /**
     * Gets cookie by name.
     *
     * @param name name of the cookie
     * @return cookie value or null if the cookie was not found
     */
    public String cookie(String name) {
        Cookie[] cookies = raw.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }    
    
    public <T> T bindParams(Class<T> clazz) {
		T returnObject = null;
		try {
			Method[] methods = clazz.getDeclaredMethods();

			if (Modifier.isStatic(clazz.getModifiers())) {
				try {
					Constructor<T> ctr = clazz.getDeclaredConstructor();
					ctr.setAccessible(true);
					returnObject = (T) ctr.newInstance();
				} catch (Exception ex) {
					ex.printStackTrace();
					return null;
				}
			} else {
				returnObject = (T) clazz.newInstance();
			}

			for (Method m : methods) {
				if (m.getName().startsWith("set")) {

					String prop = m.getName().substring(3).toLowerCase();
					String value = raw.getParameter(prop);

					if (value != null) {

						Class<?> t = m.getParameterTypes()[0];

						if (t.isAssignableFrom(Boolean.class)) {
							m.invoke(returnObject, Boolean.valueOf(value));
						} else if (t.isAssignableFrom(Byte.class)) {
							m.invoke(returnObject, Byte.valueOf(value));
						} else if (t.isAssignableFrom(Character.class)) {
							m.invoke(returnObject, value.charAt(0));
						} else if (t.isAssignableFrom(Short.class)) {
							m.invoke(returnObject, Short.valueOf(value));
						} else if (t.isAssignableFrom(Integer.class)) {
							m.invoke(returnObject, Integer.valueOf(value));
						} else if (t.isAssignableFrom(Long.class)) {
							m.invoke(returnObject, Long.valueOf(value));
						} else if (t.isAssignableFrom(Float.class)) {
							m.invoke(returnObject, Float.valueOf(value));
						} else if (t.isAssignableFrom(Double.class)) {
							m.invoke(returnObject, Double.valueOf(value));
						} else if (t.isAssignableFrom(BigInteger.class)) {
							m.invoke(returnObject, new BigInteger(value));
						} else if (t.isAssignableFrom(BigDecimal.class)) {
							m.invoke(returnObject, new BigDecimal(value));
						} else if (t.isAssignableFrom(String.class)) {
							m.invoke(returnObject, value);
						}
					}
				}
			}
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}

        return returnObject;
    }
}
