package servletio;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import servletio.annotation.*;
    
public class ServletIO extends HttpServlet{
	private static final long serialVersionUID = 1L;

	private ServletConfig servletConfig;
	
	private Map<String, Method> urlGetMap;
	private Map<String, Method> urlPostMap;
	private Map<String, Method> urlPutMap;
	private Map<String, Method> urlDeleteMap;
	private Map<String, Method> urlOptionsMap;
	
	private List<Method> afterList;
	private List<Method> beforeList;
	
	
	public void init(ServletConfig servletConfig) {
		
		this.servletConfig = servletConfig;
		
		urlGetMap = new HashMap<String, Method>();
		urlPostMap = new HashMap<String, Method>();
		urlPutMap = new HashMap<String, Method>();
		urlDeleteMap = new HashMap<String, Method>();
		urlOptionsMap = new HashMap<String, Method>();
		
		afterList = new ArrayList<Method>();
		beforeList = new ArrayList<Method>();
		
		map();
	}
	
	public ServletConfig getServletConfig(){
		return servletConfig;
	}

	private void map(){
		
		for (Method m : getPublicMethods(getClass())) {

			if(m.isAnnotationPresent(After.class)) afterList.add(m);
			if(m.isAnnotationPresent(Before.class)) beforeList.add(m);
			
			if(m.isAnnotationPresent(Get.class)){
				String annotationValue = ((Get)m.getAnnotation(Get.class)).value();
				urlGetMap.put(annotationValue, m);
			}
			
			if(m.isAnnotationPresent(Post.class)){
				String annotationValue = ((Post)m.getAnnotation(Post.class)).value();
				urlPostMap.put(annotationValue, m);
			}
			
			if(m.isAnnotationPresent(Put.class)){
				String annotationValue = ((Put)m.getAnnotation(Put.class)).value();
				urlPutMap.put(annotationValue, m);
			}
			
			if(m.isAnnotationPresent(Delete.class)){
				String annotationValue = ((Delete)m.getAnnotation(Delete.class)).value();
				urlDeleteMap.put(annotationValue, m);
			}
			
			if(m.isAnnotationPresent(Options.class)){
				String annotationValue = ((Options)m.getAnnotation(Options.class)).value();
				urlOptionsMap.put(annotationValue, m);
			}
		}
		
		Comparator<Method> comp = new Comparator<Method>(){
			@Override
			public int compare(Method o1, Method o2) {
				Method m1 = (Method)o1;
				Method m2 = (Method)o2;
				
				if(m1.isAnnotationPresent(Before.class) && m2.isAnnotationPresent(Before.class)){
					Before before1 = (Before)m1.getAnnotation(Before.class);
					Before before2 = (Before)m2.getAnnotation(Before.class);
					
					return before1.priority() - before2.priority();
				}else if(m1.isAnnotationPresent(After.class) && m2.isAnnotationPresent(After.class)){
					After after1 = (After)m1.getAnnotation(After.class);
					After after2 = (After)m2.getAnnotation(After.class);
					
					return after1.priority() - after2.priority();
				}
			
				return 0;
			}
		};
		
		beforeList.sort(comp);
		afterList.sort(comp);
	}

	private String urlPath(HttpServletRequest request) {
		String uri = request.getRequestURI();
		String ctx = request.getContextPath();
		String servletPath = request.getServletPath();
		return uri.substring(ctx.length() + servletPath.length(), uri.length());
	}
	
	private void callBeforeMethods(HttpServletRequest request, HttpServletResponse response){
		if(!beforeList.isEmpty()){
			for(Method beforeMethod : beforeList){
				String[] only = ((Before)beforeMethod.getAnnotation(Before.class)).only();
				if(only.length>0){
					for(String path : only){
						if(path.equals(urlPath(request))){
							callMethod(beforeMethod, request, response);
						}
					}
				}else{
					String[] unless = ((Before)beforeMethod.getAnnotation(Before.class)).unless();
					if(unless.length>0){
						boolean call = true;
						for(String path : unless){
							if(path.equals(urlPath(request))){
								call = false;		
							}
						}
						
						if(call) callMethod(beforeMethod, request, response);
					}else
						callMethod(beforeMethod, request, response);
				}
			}
		}
	}
	
	private void callAfterMethods(HttpServletRequest request, HttpServletResponse response){
		if(!afterList.isEmpty()){
			for(Method afterMethod : afterList){
				String[] paths = ((After)afterMethod.getAnnotation(After.class)).only();
				if(paths.length>0){
					for(String path : paths){
						if(path.equals(urlPath(request))){
							callMethod(afterMethod, request, response);
						}
					}
				}else{
					String[] unless = ((After)afterMethod.getAnnotation(After.class)).unless();
					if(unless.length>0){
						boolean call = true;
						for(String path : unless){
							if(path.equals(urlPath(request))){
								call = false;		
							}
						}
						
						if(call) callMethod(afterMethod, request, response);
					}else
						callMethod(afterMethod, request, response);
				}
			}
		}
	}

	private void callMethod(Method m, HttpServletRequest request, HttpServletResponse response) {
		try {
			Class<?>[] types = m.getParameterTypes();
			if(m!=null){
				if (types.length == 1) {
					if (types[0].isAssignableFrom(Request.class)) {
						m.invoke(this, new Request(request));
					} else if (types[0]
							.isAssignableFrom(Response.class)) {
						m.invoke(this, new Response(response));
					} else if (types[0]
							.isAssignableFrom(HttpServletResponse.class)) {
						m.invoke(this, response);
					} else if (types[0]
							.isAssignableFrom(HttpServletRequest.class)) {
						m.invoke(this, request);
					}
				} 
					
				if (types.length == 2) {
					if (types[0].isAssignableFrom(HttpServletRequest.class)
							&& types[1]
										.isAssignableFrom(HttpServletResponse.class)) {
						m.invoke(this, request, response);
					}else if (types[0].isAssignableFrom(Request.class)
							&& types[1]
									.isAssignableFrom(Response.class)) {
						m.invoke(this, new Request(request), new Response(response));
					}		
				}
			}
		} catch (IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			e.printStackTrace();
		}
	}
	
	public static final List<Method> getPublicMethods(Class<?> clazz) {
		List<Method> methods = new ArrayList<Method>();
		for (Method method : clazz.getMethods()) {
			if (Modifier.isPublic(method.getModifiers())) {
				methods.add(method);
			}
		}
		return Collections.unmodifiableList(methods);
	}

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		
		Method m = urlGetMap.get(urlPath(request));	
		callBeforeMethods(request, response);
		callMethod(m, request, response);
		callAfterMethods(request, response);
		
	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		Method m = urlPostMap.get(urlPath(request));
		callBeforeMethods(request, response);
		callMethod(m, request, response);
		callAfterMethods(request, response);
	}

	protected void doPut(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		Method m = urlPutMap.get(urlPath(request));
		callBeforeMethods(request, response);
		callMethod(m, request, response);
		callAfterMethods(request, response);
	}

	protected void doDelete(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		Method m = urlDeleteMap.get(urlPath(request));
		callBeforeMethods(request, response);
		callMethod(m, request, response);
		callAfterMethods(request, response);
	}

	protected void doOptions(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		Method m = urlOptionsMap.get(urlPath(request));
		callBeforeMethods(request, response);
		callMethod(m, request, response);
		callAfterMethods(request, response);
	}	
}
