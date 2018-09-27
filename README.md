[![](https://jitpack.io/v/zerh/servlet-io.svg)](https://jitpack.io/#zerh/servlet-io)

# Servlet IO - Convert your servlets to MVC controllers

*Supported since Servlet* **3.0.1**

## Getting Started

Add the **jitpack.io** repository
```xml
<repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
</repository>
```

Add as a maven dependency
```xml
<dependency>
	<groupId>com.github.zerh</groupId>
	<artifactId>servlet-io</artifactId>
	<version>3.1-bb94161f41-1</version>
</dependency>
```

Add **"/*"** to your mapped url, example: ```@WebServlet("/your-path/*")```, and replace the super class ```HttpServlet``` by ```ServletIO```.

```java
import com.github.zerh.servletio.*;
import com.github.zerh.servletio.annotation.Get;

@WebServlet("/base-path/*")
public class MyApp extends ServletIO {
    
    @Get("/hello")
    public Result index(Request req){
        return respond("<h1>hello world</h1>").as("text/html");
    }
}
```

Also the parameters could be mapped in the url:

```java
import javax.servlet.annotation.WebServlet;

import com.github.zerh.servletio.*;
import com.github.zerh.servletio.annotation.Get;

@WebServlet("/base-path/*")
public class MyApp extends ServletIO {
    
    @Get("/edit/:id")
    public Result edit(Request req){
        return respond("<h1>the url has: " + req.param(":id") + "</h1>").as("text/html");
    }
}
```

Of course, you also can use:
- ```@Post```
- ```@Put```
- ```@Delete```
- ```@Options```

### Result

```Result``` is just an elegant way to declare controllers methods. The ```Result``` object wraps with ```ServletIO``` the methods printers of the ```Response``` object, to produce standard HTTP results. The methods```as(String contentType)```, ```withCookies(Cookies... cookies)``` and ```withDiscardingCookies(Cookies... cookies)``` and others returns the same instance of the ```Result``` object.

ServletIO contains some helper methods that return objects Result:

- ```respond(String content)``` returns HTTP results with the 200 code.
- ```internalServerError(String content)``` returns HTTP results with the 500 code.
- ```sendFile(InputStream inputStream)``` send file to client.
- ```badRequest(String optionalContent)```returns HTTP results with the 400 code.
- ```notFound(String optionalContent)``` returns HTTP results with the 404 code.
- ```redirect(String target)``` redirect to the target.
- ```temporaryRedirect(String target)``` redirect to the target with 303 code

### Jsp
Returning ```Jsp``` you can render a jsp file from the controller
```java
import javax.servlet.annotation.WebServlet;

import com.github.zerh.servletio.*;
import com.github.zerh.servletio.annotation.Get;

@WebServlet("/base-path/*")
public class MyApp extends ServletIO {
    
    @Get("/myview")
    public Jsp myJspView(Request req){
        return jsp("/WEB-INF/my-view.jsp");
    }
}
```

### @Before and @After

Methods annotated with the ```@Before``` annotation are executed before each action call for ```ServletIO```, and methods annotated with the ```@After``` annotation are executed after each action call for the Servlet:

```java
import javax.servlet.annotation.WebServlet;

import com.github.zerh.servletio.*;
import com.github.zerh.servletio.annotation.Get;

@WebServlet("/base-path/*")
public class MyApp extends ServletIO {

    @Get
    public Result index(Request req){
        return respond("<h1>hello world</h1>").as("text/html");
    }
    
    @Before
    public Result validateUser(Request req, Response res){
        if(req.session().attribute("user")==null)
            res.printHtml("bye bye!!");
        
        return null;
    }
    
    @After
    public Result log(Request req, Response res){
        System.out.println("Action executed...");
        
        return null;
    }
    
    @After
    public Result onNotFound(Request req){
        if(!isMapped(req))
            return notFound("<h1>404, not found</h1>").as("text/html");
        
        return null;
    }

}
```

If you don’t want the ```@Before``` or ```@After``` methods to intercept all request, you can specify a list of actions using ```only``` param:

```java
import javax.servlet.annotation.WebServlet;

import com.github.zerh.servletio.*;
import com.github.zerh.servletio.annotation.Get;

@WebServlet("/base-path/*")
public class MyApp extends ServletIO {
    
    @Get
    public Result login(Request req){
    	return respond("<h1>login</h1>").as("text/html");
    }
    
    @Before(only="/admin")
    public Result validateUser(Request req, Response res){
        if(req.session().attribute("user")==null)  
            res.redirect("login");
            
        return null;
    }
    
    @After(only={"/login", "/logout"})
    public Result log(Request req, Response res){
        System.out.prinln("/login or /logout executed");
        
        return null;
    }
    
    ...
}
```

Or you can specify a list of actions to exclude for ```@After``` or ```@Before``` using the ```unless``` param:

```java
import javax.servlet.annotation.WebServlet;

import com.github.zerh.servletio.*;
import com.github.zerh.servletio.annotation.Get;

@WebServlet("/base-path/*")
public class MyApp extends ServletIO {
    
    @Get
    public Jsp login(Request req){
    	return jsp("/WEB-INF/login.jsp");
    }
    
    @Before(unless="/login")
    public Result validateUser(Request req, Response res){
        if(req.session().attribute("user")==null)
            req.dispatcher("login").forward(req, res);
            
        return null;
    }
    ...
}
```

You can set execution priority of ```@Before``` or ```@After``` methods using ```priority``` param, example ```@Before(priority=1)```, by default priority is 0 (executed first):

```java
import javax.servlet.annotation.WebServlet;

import com.github.zerh.servletio.*;
import com.github.zerh.servletio.annotation.Get;

@WebServlet("/base-path/*")
public class MyApp extends ServletIO {
    
    @Before(only="/admin/something", priority=2)
    public Result adminSomething(Request req, Response res){
        //some filter for this action
        
        return null;
    }
    
    @Before(only="/admin/*", priority=1)
    public Result validateUser(Request req, Response res){
        if(req.session().attribute("user")==null)
            res.redirect("login");
        
        return null;
    }
    ...
}
```

### Request and Response

You can either use ```Request``` or ```Response``` only as method parameters. This classes wrap HttpServletRequest and HttpServletResponse and offer some facilities. The most prominents are:

###### Request
- ```com.github.zerh.servletio.bindParam(MyBean.class)``` returns an instance of ```MyBean``` with all values of HTML form, by mapping bean properties names.
- ```request.dispatcher(String dispatcherName)``` returns an instance of ```com.github.zerh.servletio.Dispatcher```, wrap of ```RequestDispatcher```.
- ```request.raw``` final property with ```HttpServletRequest``` object reference of the current request.

###### Response
- ```response.print(String htmlString, String contentType)```.
- ```response.print(String text)``` print text plain.
- ```response.printHtml(String htmlString)``` print text with HTML content type.
- ```response.printXml(String xmlString)``` print text with XML content type.
- ```response.printJson(String xmlString)``` print text with JSON content type.
- ```response.redirect(String location)``` redirect to the specified location.
- ```response.redirect(String location, int httpStatusCode)``` redirect to the specified location with status code.
- ```response.sendError(int error)``` send custom error code to the browser.
- ```response.sendBadRequest()``` sen 400 error code to the browser.
- ```response.sendInternalServerError()``` send 500 error code to the browser.
- ```response.raw``` final propierty with ```HttpServletResponse``` object reference of the current request
