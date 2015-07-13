package servletio;

import java.io.IOException;
import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Stream;

import javax.servlet.*;
import javax.servlet.http.*;

import servletio.annotation.*;

public class ServletIO extends HttpServlet {
  private static final long   serialVersionUID = 1L;

  private Comparator<Method> comp = new Comparator<Method>() {
    @Override
    public int compare(Method o1, Method o2) {
      Method m1 = (Method) o1;
      Method m2 = (Method) o2;

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
  
  private final List<Class<?>> assignables = Arrays.asList(Request.class, Response.class, HttpServletRequest.class, HttpServletResponse.class);
  
  private ServletConfig       servletConfig;

  private Map<String, Method> urlGetMap;
  private Map<String, Method> urlPostMap;
  private Map<String, Method> urlPutMap;
  private Map<String, Method> urlDeleteMap;
  private Map<String, Method> urlOptionsMap;

  private List<Method>        afterList;
  private List<Method>        beforeList;

  public void init(ServletConfig servletConfig) {

    this.servletConfig = servletConfig;

    urlGetMap     = new HashMap<>();
    urlPostMap    = new HashMap<>();
    urlPutMap     = new HashMap<>();
    urlDeleteMap  = new HashMap<>();
    urlOptionsMap = new HashMap<>();

    afterList   = new ArrayList<>();
    beforeList  = new ArrayList<>();

    map();
  }

  public ServletConfig getServletConfig() {
    return servletConfig;
  }

  private void map() {
    final Stream<Method> stream = getPublicMethods(getClass());
    
    stream.filter(m -> m.isAnnotationPresent(After.class))
    .forEach(m -> afterList.add(m));
    
    stream.filter(m -> m.isAnnotationPresent(Before.class))
    .forEach(m -> beforeList.add(m));
    
    stream.filter(m -> m.isAnnotationPresent(Get.class))
    .forEach(m -> {
      String annotationValue = ((Get) m.getAnnotation(Get.class)).value();
      urlGetMap.put(annotationValue, m);
    });
    
    stream.filter(m -> m.isAnnotationPresent(Post.class))
    .forEach(m -> {
      String annotationValue = ((Post) m.getAnnotation(Post.class)).value();
      urlPostMap.put(annotationValue, m);
    });
    
    stream.filter(m -> m.isAnnotationPresent(Put.class))
    .forEach(m -> {
      String annotationValue = ((Put) m.getAnnotation(Put.class)).value();
      urlPutMap.put(annotationValue, m);
    });
    
    stream.filter(m -> m.isAnnotationPresent(Delete.class))
    .forEach(m -> {
      String annotationValue = ((Delete) m.getAnnotation(Delete.class)).value();
      urlDeleteMap.put(annotationValue, m);
    });
   
    stream.filter(m -> m.isAnnotationPresent(Options.class))
    .forEach(m -> {
      String annotationValue = ((Options) m.getAnnotation(Options.class)).value();
      urlOptionsMap.put(annotationValue, m);
    });
    
    beforeList.sort(comp);
    afterList.sort(comp);
  }

  private String urlPath(final HttpServletRequest request) {
    String uri = request.getRequestURI();
    String ctx = request.getContextPath();
    String servletPath = request.getServletPath();
    return uri.substring(ctx.length() + servletPath.length(), uri.length());
  }

  private void callBeforeMethods(final HttpServletRequest request, final HttpServletResponse response) {
    if (!beforeList.isEmpty()) {
      for (Method beforeMethod : beforeList) {
        String[] only = ((Before) beforeMethod.getAnnotation(Before.class)).only();
        if (only.length > 0) {
          for (String path : only) {
            if (path.equals(urlPath(request))) {
              callMethod(beforeMethod, request, response);
            }
          }
        } else {
          String[] unless = ((Before) beforeMethod.getAnnotation(Before.class)).unless();
          if (unless.length > 0) {
            boolean call = true;
            for (String path : unless) {
              if (path.equals(urlPath(request))) {
                call = false;
              }
            }

            if (call)
              callMethod(beforeMethod, request, response);
          } else
            callMethod(beforeMethod, request, response);
        }
      }
    }
  }

  private void callAfterMethods(HttpServletRequest request, HttpServletResponse response) {
    if (!afterList.isEmpty()) {
      for (Method afterMethod : afterList) {
        String[] paths = ((After) afterMethod.getAnnotation(After.class)).only();
        if (paths.length > 0) {
          for (String path : paths) {
            if (path.equals(urlPath(request))) {
              callMethod(afterMethod, request, response);
            }
          }
        } else {
          String[] unless = ((After) afterMethod.getAnnotation(After.class)).unless();
          if (unless.length > 0) {
            boolean call = true;
            for (String path : unless) {
              if (path.equals(urlPath(request))) {
                call = false;
              }
            }

            if (call)
              callMethod(afterMethod, request, response);
          } else
            callMethod(afterMethod, request, response);
        }
      }
    }
  }

  
  private void callMethod(final Method me, final HttpServletRequest request, final HttpServletResponse response) {
    Optional.ofNullable(me).ifPresent(m -> {
      
      Optional<Class<?>[]> typesStream = Optional.of(m.getParameterTypes());
      
      typesStream
      .filter(ctypes -> ctypes.length == 1)
      .map(types -> types[0])
      .ifPresent(type -> {
         assignables
         .stream()
         .parallel()
         .filter(c -> type.isAssignableFrom(c))
         .findFirst()
         .ifPresent(c -> {
           try {
             switch (assignables.indexOf(c)) {
              case 0: m.invoke(this, new Request(request))   ; break;
              case 1: m.invoke(this, new Response(response)) ; break;
              case 2: m.invoke(this, response)               ; break;
              case 4: m.invoke(this, request)                ; break;
             } 
           } catch (Exception e) {
             e.printStackTrace();
           }
         });
       });
      
      typesStream
      .filter(ctypes -> ctypes.length == 2)
      .ifPresent(types -> {
        try {
          if (types[0].isAssignableFrom(HttpServletRequest.class) && types[1].isAssignableFrom(HttpServletResponse.class)) {
            m.invoke(this, request, response);
          } else if (types[0].isAssignableFrom(Request.class) && types[1].isAssignableFrom(Response.class)) {
            m.invoke(this, new Request(request), new Response(response));
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
      });
    });
  }

  public static final Stream<Method> getPublicMethods(final Class<?> clazz) {
    return Arrays.stream(clazz.getMethods())
    .filter(method -> Modifier.isPublic(method.getModifiers()));
  }
  protected void doGet(HttpServletRequest request, HttpServletResponse response)  throws ServletException, IOException {
    Method m = urlGetMap.get(urlPath(request));
    callBeforeMethods(request, response);
    callMethod(m, request, response);
    callAfterMethods(request, response);

  }

  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    Method m = urlPostMap.get(urlPath(request));
    callBeforeMethods(request, response);
    callMethod(m, request, response);
    callAfterMethods(request, response);
  }

  protected void doPut(HttpServletRequest request, HttpServletResponse response)  throws ServletException, IOException {
    Method m = urlPutMap.get(urlPath(request));
    callBeforeMethods(request, response);
    callMethod(m, request, response);
    callAfterMethods(request, response);
  }

  protected void doDelete(HttpServletRequest request,  HttpServletResponse response) throws ServletException, IOException {
    Method m = urlDeleteMap.get(urlPath(request));
    callBeforeMethods(request, response);
    callMethod(m, request, response);
    callAfterMethods(request, response);
  }

  protected void doOptions(HttpServletRequest request,  HttpServletResponse response) throws ServletException, IOException {
    Method m = urlOptionsMap.get(urlPath(request));
    callBeforeMethods(request, response);
    callMethod(m, request, response);
    callAfterMethods(request, response);
  }
}
