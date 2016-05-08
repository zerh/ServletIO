package servletio;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import servletio.utils.IOUtils;
import servletio.utils.RouteUtils;

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
	public final Cookie[] cookies;
	public final Set<String> attributes;
	public final Set<String> queryParams;
	public final Set<String> headers;
	public Collection<Part> parts;

	private Session session;

	Map<String, Integer> indexByTag = null;

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

		cookies = raw.getCookies();
		attributes = attributes();
		headers = headers();
	}

	private String getFileName(Part part) {
		String contentDisp = part.getHeader("content-disposition");
		String[] tokens = contentDisp.split(";");
		for (String token : tokens) {
			if (token.trim().startsWith("filename")) {
				return token.substring(token.indexOf("=") + 2, token.length() - 1);
			}
		}
		return "";
	}

	private String defaultPath() {
		String applicationPath = raw.getServletContext().getRealPath("");
		return applicationPath + File.separator + "temp";
	}

	public File getFile(String name) {
		try {
			final Part filePart = raw.getPart(name);
			final String fileName = getFileName(filePart);
			try {
				filePart.write(defaultPath() + File.separator + fileName);
				return new File(defaultPath() + File.separator + fileName);

			} catch (FileNotFoundException ex) {
				ex.printStackTrace();
				return null;
			}
		} catch (IOException ex) {
			ex.printStackTrace();
			return null;
		} catch (ServletException ex) {
			ex.printStackTrace();
			return null;
		}
	}

	public Dispatcher dispatcher(String name) {
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

	public Part part(String name) {
		try {
			return raw.getPart(name);
		} catch (IOException ex) {
			ex.printStackTrace();
			return null;
		} catch (ServletException ex) {
			ex.printStackTrace();
			return null;
		}
	}

	public Collection<Part> parts() {
		try {
			return raw.getParts();
		} catch (IOException ex) {
			ex.printStackTrace();
			return null;
		} catch (ServletException ex) {
			ex.printStackTrace();
			return null;
		}
	}

	/**
	 * @return the http parameter
	 */
	public String param(String param) {
		if (param.startsWith(":"))
			return indexByTag != null ? RouteUtils.segment(raw, indexByTag.get(param)) : null;

		return raw.getParameter(param);
	}

	/**
	 * @return the http parameter
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public <T> T param(String param, Class<T> clazz) throws InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		if (param.startsWith(":"))
			return indexByTag != null ? convert(RouteUtils.segment(raw, indexByTag.get(param)), clazz) : null;

		return convert(raw.getParameter(param), clazz);
	}

	public String[] parameterValues(String param) {
		return raw.getParameterValues(param);
	}

	public <T> T[] parameterValues(String param, Class<T> t) throws InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		return convert(raw.getParameterValues(param), t);
	}

	public String queryParams(String queryParam) {
		return raw.getParameter(queryParam);
	}

	
	/**
	 * Gets the value for the provided header
	 *
	 * @param header
	 *            the header
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
	 * Sets an attribute on the request (can be fetched in filters/routes later
	 * in the chain)
	 *
	 * @param attribute
	 *            The attribute
	 * @param value
	 *            The attribute value
	 */
	public void attribute(String attribute, Object value) {
		raw.setAttribute(attribute, value);
	}

	/**
	 * Gets the value of the provided attribute
	 *
	 * @param attribute
	 *            The attribute value or null if not present
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
	 * Returns the current session associated with this request, or if the
	 * request does not have a session, creates one.
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
	 * no current session and <code>create</code> is true, returns a new
	 * session.
	 *
	 * @param create
	 *            <code>true</code> to create a new session for this request if
	 *            necessary; <code>false</code> to return null if there's no
	 *            current session
	 * @return the session associated with this request or <code>null</code> if
	 *         <code>create</code> is <code>false</code> and the request has no
	 *         valid session
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
	 * Gets cookie by name.
	 *
	 * @param name
	 *            name of the cookie
	 * @return <code>Cookie</code> with the specified name
	 */
	public Cookie cookie(String name) {
		Cookie[] cookies = raw.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().equals(name)) {
					return cookie;
				}
			}
		}
		return null;
	}

	private <T> T convert(String value, Class<T> t) throws InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		if (t.isAssignableFrom(char.class))
			return t.getDeclaredConstructor(char.class).newInstance(value.charAt(0));

		return t.getDeclaredConstructor(String.class).newInstance(value);
	}
	
	@SuppressWarnings("unchecked")
	private <T> T[] convert(String[] value, Class<T> t) throws InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {

		Object arr = Array.newInstance(t, value.length);
		int arrLength = ((T[])arr).length;
		for (int i = 0; i < arrLength; i++)
			((T[])arr)[i] = convert(value[i], t);
		
		return (T[]) arr;
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

					String prop = m.getName().substring(3);

					char[] stringArray = prop.trim().toCharArray();
					stringArray[0] = Character.toLowerCase(stringArray[0]);
					prop = new String(stringArray);

					String value = raw.getParameter(prop);

					if (value != null) {

						Class<?> t = m.getParameterTypes()[0];
						convert(value, t);
					}
				}
			}
		} catch (InstantiationException ex) {
			ex.printStackTrace();
		} catch (IllegalAccessException ex) {
			ex.printStackTrace();
		} catch (IllegalArgumentException ex) {
			ex.printStackTrace();
		} catch (InvocationTargetException ex) {
			ex.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return returnObject;
	}
}
