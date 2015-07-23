# ServletIO - Convert your servlets to MVC controllers

*Supported since Servlet* **3.0.1** *or higher, and Java* **6** *or higher*

#### Add repository:
```xml
<repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
</repository>
```
#### Add dependency:
```xml


<dependency>
    <groupId>com.github.zerh</groupId>
    <artifactId>ServletIO</artifactId>
    <version>8b0d775b18</version>
</dependency>
```

## Getting Started
Add **"/*"** to your mapped url, example: ```@WebServlet("/your-path/*")```, and replace the super class ```HttpServlet``` by ```ServletIO```.

```java
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import servletio.*;
import servletio.annotation.Get;

@WebServlet("/base-path/*")
public class MyApp extends ServletIO {
    
    protected void doGet(HttpServletRequest req, HttpServletResponse res) {
        super.doGet(req, res);
        //your old code
    }
    
    @Get("/hello")
    public Result index(Request req){
        return ok("<h1>hello world</h1>").as("text/html");
    }
}

```
Or put your old ```doGet``` code inside another method mapped with the base path:
```java
import javax.servlet.annotation.WebServlet;

import servletio.*;
import servletio.annotation.Get;

@WebServlet("/base-path/*")
public class MyApp extends ServletIO {

    @Get("/")
    public void oldCode(Request req, Response res){
        //old code
    }

    @Get("/index")
    public Result index(Request req){
        return ok("<h1>hello world</h1>").as("text/html");
    }
}
```

Of course, you also can use:
- ```@Post```
- ```@Put```
- ```@Delete```
- ```@Options```

### Result

Inspired by [Play Framework](https://www.playframework.com/), ```Result``` is just an elegant way to declare controllers methods. The ```Result``` object wraps with ```ServletIO``` the methods printers of the ```Response``` object, to produce standard HTTP results. The methods```as(String contentType)```, ```withCookies(Cookies... cookies)``` and ```discardingCookies(Cookies... cookies)```  returns the same instance of the ```Result``` object.

ServletIO contains some helper methods that return objects Result:

- ```ok(String content)``` returns HTTP results with the 200 code.
- ```internalServerError(String content)``` returns HTTP results with the 500 code.
- ```status(int statusCode, String content)``` returns HTTP results with the specified status.
- ```badRequest(String optionalContent)```returns HTTP results with the 400 code.
- ```notFound(String optionalContent)``` returns HTTP results with the 404 code.
- ```redirect(int optionalStatusCode, String target)``` redirect to the target.
- ```temporaryRedirect(String target)``` redirect to the target with 303 code

### @Before and @After

Methods annotated with the ```@Before``` annotation are executed before each action call for ```ServletIO```, and methods annotated with the ```@After``` annotation are executed after each action call for the Servlet:

```java
import javax.servlet.annotation.WebServlet;

import servletio.*;
import servletio.annotation.*;

@WebServlet("/base-path/*")
public class MyApp extends ServletIO {

    @Get("/index")
    public Result index(Request req){
        return ok("<h1>hello world</h1>").as("text/html");
    }
    
    @Before
    public void validateUser(Request req, Response res){
        if(req.session().attribute("user")==null)
            res.printHtml("bye bye!!");
    }
    
    @After
    public void log(Request req, Response res){
        System.out.println("Action executed...");
    }

}
```
If you don’t want the ```@Before``` or ```@After``` method to intercept all request, you can specify a list of actions using ```only``` param:

```java
import javax.servlet.annotation.WebServlet;

import servletio.*;
import servletio.annotation.*;

@WebServlet("/base-path/*")
public class MyApp extends ServletIO {
    
    @Get("/login")
    public Result login(Request req){
    	return ok("<h1>login</h1>").as("text/html");
    }
    
    @Before(only={"/admin"})
    public void validateUser(Request req, Response res){
        if(req.session().attribute("user")==null)  
            res.redirect("/context/base-path/login");
    }
    
    @After(only={"/login", "/logout"})
    public void log(Request req, Response res){
        System.out.prinln("/login or /logout executed")
    }
    ...
}
```

Or you can specify a list of actions to exclude for ```@After``` or ```@Before``` using the ```unless``` param:

```java
import javax.servlet.annotation.WebServlet;

import servletio.*;
import servletio.annotation.*;

@WebServlet("/base-path/*")
public class MyApp extends ServletIO {
    
    @Before(unless={"/login"})
    public void validateUser(Request req, Response res){
        if(req.session().attribute("user")==null)
            req.dispatcher("/base-path/login").forward(req, res);
    }
    ...
}
```

The parameters unless or only could receive paths with "**/***" at the end, so ```@Before``` or ```@After``` could filter every request that begins with the specified paths:

```java
import javax.servlet.annotation.WebServlet;

import servletio.*;
import servletio.annotation.*;

@WebServlet("/base-path/*")
public class MyApp extends ServletIO {
    
    @Before(only={"/admin/*"})
    public void validateUser(Request req, Response res){
        if(req.session().attribute("user")==null)
            res.redirect("/context/base-path/login");
    }
    ...
}
```

### Request and Response

You can either use ```Request``` or ```Response``` only as method parameters. This classes wrap HttpServletRequest and HttpServletResponse and offer some facilities. The most prominents are:

###### Request
- ```request.bindParam(MyBean.class)``` return a instance of ```MyBean``` with all values of HTML form, mapping by bean properties names.
- ```request.getFile(String paramName)``` return a File with the uploaded file reference.
- ```request.dispatcher(String dispatcherName)``` return an instance of ```servletio.Dispatcher```, wrap of ```RequestDispatcher```.
- ```request.raw``` final propierty with ```HttpServletRequest``` object reference of the current request.

###### Response
- ```response.print(String htmlString, String contentType)```.
- ```response.print(String text)``` print text plain.
- ```response.printHtml(String htmlString)``` print text with HTML content type.
- ```response.printXml(String xmlString)``` print text with XML content type.
- ```response.printJson(String xmlString)``` print text with JSON content type.
- ```response.redirect(String location)``` redirect to the specified location.
- ```response.redirect(String location, int httpStatusCode)``` redirect to the specified location with status code.
- ```response.badRequest()``` throws 404 to the browser.
- ```response.raw``` final propierty with ```HttpServletResponse``` object reference of the current request
