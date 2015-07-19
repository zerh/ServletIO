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

public class ServletIO extends HttpServlet {

    private static final long serialVersionUID = -2453789677978887728L;

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

    public ServletConfig getServletConfig() {
        return servletConfig;
    }

    protected static Result ok(String content) {
        Result result = new Result(content);
        result.status = HttpServletResponse.SC_OK;
        return result;
    }

    protected static Result internalServerError(String content) {
        Result result = new Result(content);
        result.status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
        return result;
    }

    protected static Result status(int status, String content) {
        Result result = new Result(content);
        result.status = status;
        return result;
    }

    protected static Result badRequest() {
        Result result = new Result();
        result.status = HttpServletResponse.SC_BAD_REQUEST;
        return result;
    }

    protected static Result badRequest(String content) {
        Result result = new Result(content);
        result.status = HttpServletResponse.SC_BAD_REQUEST;
        return result;
    }

    protected static Result notFound(String content) {
        Result result = new Result(content);
        result.status = HttpServletResponse.SC_NOT_FOUND;
        return result;
    }

    protected static Result notFound() {
        Result result = new Result();
        result.status = HttpServletResponse.SC_NOT_FOUND;
        return result;
    }

    protected static Result redirect(int status, String target) {
        Result result = new Result();
        result.redirect = target;
        result.status = status;
        return result;
    }

    protected static Result redirect(String target) {
        Result result = new Result();
        result.redirect = target;
        return result;
    }

    protected static Result temporaryRedirect(String target) {
        Result result = new Result();
        result.redirect = target;
        result.status = HttpServletResponse.SC_TEMPORARY_REDIRECT;
        return result;
    }

    private void map() {

        for (Method m : getPublicMethods(getClass())) {

            if (m.isAnnotationPresent(After.class))
                afterList.add(m);

            if (m.isAnnotationPresent(Before.class))
                beforeList.add(m);

            if (m.isAnnotationPresent(Get.class)) {
                String annotationValue = ((Get) m.getAnnotation(Get.class))
                        .value();
                urlGetMap.put(annotationValue, m);
            }

            if (m.isAnnotationPresent(Post.class)) {
                String annotationValue = ((Post) m.getAnnotation(Post.class))
                        .value();
                urlPostMap.put(annotationValue, m);
            }

            if (m.isAnnotationPresent(Put.class)) {
                String annotationValue = ((Put) m.getAnnotation(Put.class))
                        .value();
                urlPutMap.put(annotationValue, m);
            }

            if (m.isAnnotationPresent(Delete.class)) {
                String annotationValue = ((Delete) m
                        .getAnnotation(Delete.class)).value();
                urlDeleteMap.put(annotationValue, m);
            }

            if (m.isAnnotationPresent(Options.class)) {
                String annotationValue = ((Options) m
                        .getAnnotation(Options.class)).value();
                urlOptionsMap.put(annotationValue, m);
            }
        }

        Comparator<Method> comp = new Comparator<Method>() {
            @Override
            public int compare(Method o1, Method o2) {
                Method m1 = (Method) o1;
                Method m2 = (Method) o2;

                if (m1.isAnnotationPresent(Before.class)
                        && m2.isAnnotationPresent(Before.class)) {
                    Before before1 = (Before) m1.getAnnotation(Before.class);
                    Before before2 = (Before) m2.getAnnotation(Before.class);

                    return before1.priority() - before2.priority();
                } else if (m1.isAnnotationPresent(After.class)
                        && m2.isAnnotationPresent(After.class)) {
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

    private String urlPath(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String ctx = request.getContextPath();
        String servletPath = request.getServletPath();
        return uri.substring(ctx.length() + servletPath.length(), uri.length());
    }
    
    private void callOnly(String[] only, Method method, HttpServletRequest request, HttpServletResponse response){
        for (String path : only) {
            if(path.endsWith("/*")){
                
                String path2 = path.substring(0, path.length()-2);
                if(urlPath(request).startsWith(path2)){
                    callMethod(method, request, response);
                    break;
                }
                
            }else if (path.equals(urlPath(request))) {
                callMethod(method, request, response);
                break;
            }
        }
    }
    
    private void callUnless(String[] unless, Method method, HttpServletRequest request, HttpServletResponse response){
        if (unless.length > 0) {
            boolean call = true;
            for (String path : unless) {
                if(path.endsWith("/*")){
                    
                    String path2 = path.substring(0, path.length()-2);
                    if(urlPath(request).startsWith(path2)){ 
                        call = false;
                        break;
                    }
                    
                }else if (path.equals(urlPath(request))){
                    call = false;
                    break;
                }
            }
            if (call) callMethod(method, request, response);
        } else callMethod(method, request, response);
    }

    private void callFilter(Class<?> filterType, HttpServletRequest request, HttpServletResponse response){

        boolean after=false, before=false; 
        if(filterType.isAssignableFrom(After.class))
            after = true;
        else if(filterType.isAssignableFrom(Before.class))
            before = true;
        
        List<Method> list = after ? afterList : before ? beforeList : null;      
                
        if (list!=null) {
            for (Method method : list) {
                
                String[] only = after ? ((After) method
                        .getAnnotation(After.class)).only() : ((Before) method
                                .getAnnotation(Before.class)).only();
              
                if (only.length > 0) 
                    callOnly(only, method, request, response);
                
                else {
                    String[] unless = after ? ((After) method
                            .getAnnotation(After.class)).unless() : ((Before) method
                                    .getAnnotation(Before.class)).unless();
                    
                    callUnless(unless, method, request, response);
                }
            }
        }

    }
    
    private void callMethod(Method m, HttpServletRequest req,
            HttpServletResponse res) {
        Request request = new Request(req);
        Response response = new Response(res);
        try {
            if (m != null) {
                Class<?>[] types = m.getParameterTypes();

                if (m.getReturnType().equals(Result.class) && types.length == 1
                        && types[0].isAssignableFrom(Request.class)) {
                    try {
                        Result result = (Result) m.invoke(this, request);
                        result.resultLogic(response);
                    } catch (Exception ex) {
                        response.badRequest();
                    }
                }

                if (types.length == 2) {
                    if (types[0].isAssignableFrom(HttpServletRequest.class)
                            && types[1]
                                    .isAssignableFrom(HttpServletResponse.class)) {
                        m.invoke(this, req, res);
                    } else if (types[0].isAssignableFrom(Request.class)
                            && types[1].isAssignableFrom(Response.class)) {
                        m.invoke(this, request, response);
                    }
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
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

    protected void process(Method method, HttpServletRequest request,
            HttpServletResponse response) {
        callFilter(Before.class, request, response);
        callMethod(method, request, response);
        callFilter(After.class, request, response);
    }

    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        Method m = urlGetMap.get(urlPath(request));
        process(m, request, response);
    }

    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        Method m = urlPostMap.get(urlPath(request));
        process(m, request, response);
    }

    protected void doPut(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        Method m = urlPutMap.get(urlPath(request));
        process(m, request, response);
    }

    protected void doDelete(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        Method m = urlDeleteMap.get(urlPath(request));
        process(m, request, response);
    }

    protected void doOptions(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        Method m = urlOptionsMap.get(urlPath(request));
        process(m, request, response);
    }
}
