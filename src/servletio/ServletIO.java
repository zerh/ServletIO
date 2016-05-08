package servletio;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.text.html.HTMLDocument.HTMLReader.IsindexAction;

import servletio.annotation.*;
import servletio.utils.RouteUtils;

public class ServletIO extends HttpServlet {

	private Set<String> allMappedUrl;

	private Map<String, Method> urlGetMap;
	private Map<String, Method> urlPostMap;
	private Map<String, Method> urlPutMap;
	private Map<String, Method> urlDeleteMap;
	private Map<String, Method> urlOptionsMap;

	private List<Method> afterList;
	private List<Method> beforeList;

	public void init(ServletConfig servletConfig) {
		urlGetMap = new HashMap<String, Method>();
		urlPostMap = new HashMap<String, Method>();
		urlPutMap = new HashMap<String, Method>();
		urlDeleteMap = new HashMap<String, Method>();
		urlOptionsMap = new HashMap<String, Method>();

		afterList = new ArrayList<Method>();
		beforeList = new ArrayList<Method>();

		allMappedUrl = new HashSet<String>();

		map();
		try {
			super.init(servletConfig);
		} catch (ServletException e) {
			e.printStackTrace();
		}

		allMappedUrl.addAll(urlGetMap.keySet());
		allMappedUrl.addAll(urlPostMap.keySet());
		allMappedUrl.addAll(urlPutMap.keySet());
		allMappedUrl.addAll(urlDeleteMap.keySet());
		allMappedUrl.addAll(urlOptionsMap.keySet());
	}

	protected boolean isMapped(Request req) {
		boolean returnValue = false;
		Pretty p = new Pretty();

		if (allMappedUrl.contains(RouteUtils.routeOf(req)))
			return true;

		for (Object url : allMappedUrl.toArray()) {
			if (p.match(url.toString()).withRequest(RouteUtils.routeOf(req)))
				returnValue = true;
			break;
		}
		return returnValue;
	}

	protected Result respond(String content) {
		Result result = new Result(content);
		result.status = 200;
		return result;
	}

	protected Result sendFile(InputStream inputStream) {
		Result result = new Result(null);
		result.inputStream = inputStream;
		return result;
	}

	protected Result badRequest(String content) {
		Result result = new Result(content);
		result.status = 400;
		return result;
	}

	protected Result badRequest() {
		return badRequest(null);
	}

	protected Result notFound(String content) {
		Result result = new Result(content);
		result.status = 404;
		return result;
	}

	protected Result notFound() {
		return notFound(null);
	}

	protected Result redirect(String target) {
		Result result = new Result(null);
		result.redirect = target;
		return result;
	}

	protected Result temporaryRedirect(String target) {
		Result result = redirect(target);
		result.status = 307;
		return result;
	}

	protected Result internalServerError(String content) {
		Result result = new Result(content);
		result.status = 500;
		return result;
	}

	private final List<Method> getPublicMethods(Class<?> clazz) {
		List<Method> methods = new ArrayList<Method>();
		for (Method method : clazz.getMethods()) {
			if (Modifier.isPublic(method.getModifiers())) {
				methods.add(method);
			}
		}
		return Collections.unmodifiableList(methods);
	}

	private boolean isMappable(Method m) {
		return m.getReturnType().equals(Render.class);
	}

	private void mapMethod(Map<String, Method> map, String annotationValue, Method m) {
		if (annotationValue.equals("null")) {
			map.put("/" + m.getName().toLowerCase(), m);
		} else {
			if (annotationValue.endsWith("/"))
				annotationValue = annotationValue.substring(0, annotationValue.length() - 1);
			map.put(annotationValue, m);
		}
	}

	private void map() {

		for (Method m : getPublicMethods(getClass())) {

			if (isMappable(m)) {

				if (m.isAnnotationPresent(After.class))
					afterList.add(m);

				if (m.isAnnotationPresent(Before.class))
					beforeList.add(m);

				if (m.isAnnotationPresent(Get.class)) {
					String annotationValue = ((Get) m.getAnnotation(Get.class)).value();
					mapMethod(urlGetMap, annotationValue, m);
				}

				if (m.isAnnotationPresent(Post.class)) {
					String annotationValue = ((Post) m.getAnnotation(Post.class)).value();
					mapMethod(urlGetMap, annotationValue, m);
				}

				if (m.isAnnotationPresent(Put.class)) {
					String annotationValue = ((Put) m.getAnnotation(Put.class)).value();
					mapMethod(urlGetMap, annotationValue, m);
				}

				if (m.isAnnotationPresent(Delete.class)) {
					String annotationValue = ((Delete) m.getAnnotation(Delete.class)).value();
					mapMethod(urlGetMap, annotationValue, m);
				}

				if (m.isAnnotationPresent(Options.class)) {
					String annotationValue = ((Options) m.getAnnotation(Options.class)).value();
					mapMethod(urlGetMap, annotationValue, m);
				}
			}
		}

		Comparator<Method> comp = new Comparator<Method>() {
			@Override
			public int compare(Method o1, Method o2) {
				Method m1 = o1;
				Method m2 = o2;

				if (m1.isAnnotationPresent(Before.class) && m2.isAnnotationPresent(Before.class)) {
					Before before1 = (Before) m1.getAnnotation(Before.class);
					Before before2 = (Before) m2.getAnnotation(Before.class);

					return before1.priority() - before2.priority();
				} else if (m1.isAnnotationPresent(After.class) && m2.isAnnotationPresent(After.class)) {
					After after1 = (After) m1.getAnnotation(After.class);
					After after2 = (After) m2.getAnnotation(After.class);

					return after1.priority() - after2.priority();
				}

				return 0;
			}
		};

		Collections.sort(beforeList, comp);
		Collections.sort(afterList, comp);
	}

	private void callOnly(String[] only, Method method, HttpServletRequest request, HttpServletResponse response) {

		Pretty p = new Pretty();

		for (String mappedRoute : only) {
			if (mappedRoute.equals(RouteUtils.routeOf(request))
					|| p.match(mappedRoute).withRequest(RouteUtils.routeOf(request))) {
				callRender(method, request, response, p.indexByTag);
				break;
			}
		}
	}

	private void callUnless(String[] unless, Method method, HttpServletRequest request, HttpServletResponse response) {

		Pretty p = new Pretty();

		if (unless.length > 0) {
			boolean call = true;
			for (String mappedPath : unless) {
				if (mappedPath.equals(RouteUtils.routeOf(request))
						|| p.match(mappedPath).withRequest(RouteUtils.routeOf(request))) {
					call = false;
					break;
				}
			}
			if (call)
				callRender(method, request, response, p.indexByTag);
		} else
			callRender(method, request, response, p.indexByTag);
	}

	private void callFilters(Class<?> filterType, HttpServletRequest request, HttpServletResponse response) {

		boolean after = false, before = false;
		if (filterType.isAssignableFrom(After.class))
			after = true;
		else if (filterType.isAssignableFrom(Before.class))
			before = true;

		List<Method> list = after ? afterList : before ? beforeList : null;

		if (list != null) {
			for (Method method : list) {
				String[] only = after ? ((After) method.getAnnotation(After.class)).only()
						: ((Before) method.getAnnotation(Before.class)).only();

				String[] unless = after ? ((After) method.getAnnotation(After.class)).unless()
						: ((Before) method.getAnnotation(Before.class)).unless();

				if (only.length > 0) {
					callOnly(only, method, request, response);
				} else if (unless.length > 0) {
					callUnless(unless, method, request, response);
				} else {
					callRender(method, request, response, null);
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private Object[] getMethosParamsValues(Method m, Request request, Response response, Map<String, Integer> indexByTag) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException{
		Object[] params = new Object[m.getParameterCount()];
		
		for (int i = 0; i < params.length; i++) {
			Class<?> paramType = m.getParameters()[i].getType();
			String param = null;

			if (m.getParameters()[i].getAnnotationsByType(Param.class).length > 0)
				param = m.getParameters()[i].getAnnotationsByType(Param.class)[0].value();
			
			if(param != null){
				
				if(!param.equals("null")){
					if(m.getParameters()[i].getType().isArray()){
						System.out.println("array");
						params[i] = request.parameterValues(param, paramType.getComponentType());
					}else{
						params[i] = request.param(param, paramType);
					}
				}
			}
			
			if(paramType.equals(Request.class))
				params[i] = request;
			
			if(paramType.equals(Response.class))
				params[i] = response;
			
			if(paramType.equals(HttpServletRequest.class))
				params[i] = request.raw;
			
			if(paramType.equals(HttpServletResponse.class))
				params[i] = response.raw;
		}
		return params;
	}

	private void callRender(Method m, HttpServletRequest req, HttpServletResponse res,
			Map<String, Integer> indexByTag) {
	
		Request request = new Request(req);
		Response response = new Response(res);

		request.indexByTag = indexByTag;

		try {
			Object[] params = getMethosParamsValues(m, request, response, indexByTag);
			Render view = (Render) m.invoke(this, params);
			view.render(request, response);
			view=null;
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} finally {
			try {
				res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		request = null;
		response = null;
	}

	protected void process(Map<String, Method> urlMethodMap, HttpServletRequest request, HttpServletResponse response) {
		String route = RouteUtils.routeOf(request);
		if (route.endsWith("/"))
			route = route.substring(0, route.length() - 1);
		Method m = urlMethodMap.get(route);
		callFilters(After.class, request, response);
		if (m != null) {
			callRender(m, request, response, null);
		} else {
			Pretty p = new Pretty();
			boolean exist = false;
			for (String mappedRoute : urlMethodMap.keySet()) {
				if (mappedRoute.contains(":") && p.match(mappedRoute).withRequest(route)) {
					exist = true;
					callRender(urlMethodMap.get(mappedRoute), request, response, p.indexByTag);
					break;
				}
			}

			if (!exist) {
				try {
					response.sendError(404);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		route = null;
		m=null;
		callFilters(Before.class, request, response);
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) {
		process(urlGetMap, request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) {
		process(urlPostMap, request, response);
	}

	protected void doPut(HttpServletRequest request, HttpServletResponse response) {
		process(urlPutMap, request, response);
	}

	protected void doDelete(HttpServletRequest request, HttpServletResponse response) {
		process(urlDeleteMap, request, response);
	}

	protected void doOptions(HttpServletRequest request, HttpServletResponse response) {
		process(urlOptionsMap, request, response);
	}
}
