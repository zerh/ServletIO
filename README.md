# ServletIO - Convert your servlets to MVC controllers

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
    <version>f87b07fdf2</version>
</dependency>
```

---
Or download [servletio-1.1.jar](https://mega.nz/#!h0lVGYjL!pKf_q6WPZRQT_5epzcY4TzNbPaKz05oejEXIAoVIc6M)

## Getting Started
Add **"/*"** to your mapped url, example: ```@WebServlet("/your-path/*")```, and replace the super class ```HttpServlet``` by ```ServletIO```

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
Or put your old ```doGet``` code inside another method mapped with the base path
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

#### Result
Totally inspired by (Play Framework)[https://www.playframework.com/], is an elegant way to declare controllers methods. The ```Result``` object wrap with ```ServletIO``` the printers methos of the ```Response``` object, to produce standard HTTP results. 

ServletIO contains some helper methods that return objects Result:

- ```ok(content)``` returns HTTP results with the 200 code;.
- ```internalServerError(content)``` returns HTTP results with the 500 code.
- ```status(statusCode, content)``` returns HTTP results with the specified status.
- ```badRequest(optionalContent)```returns HTTP results with the 400 code.
- ```notFound(optionalContent)``` returns HTTP results with the 404 code.

#### @Before and @After

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
    
    @Before()
    public void validateUser(Request req, Response res){
        if(req.session().attribute("user")==null)
            res.printHtml("bye bye!!");
    }
    
    @After()
    public void log(Request req, Response res){
        System.out.println("Action executed...");
    }

}
```
If you don’t want the ```@Before``` or ```@After``` method to intercept all request, you can specify a list of actions using only param:

```java
import javax.servlet.annotation.WebServlet;

import servletio.*;
import servletio.annotation.*;

@WebServlet("/base-path/*")
public class MyApp extends ServletIO {
    
    @Get("/login")
    public Result login(Request req, Response res){
    	return ok("<h1>login</h1>").as("text/html");
    }
    
    @Before(only={"/admin"})
    public void validateUser(Request req, Response res){
        if(req.session().attribute("user")==null)  
            req.dispatcher("/base-path/login").forward(req, res);
    }
    
    @After(only={"/login", "/logout"})
    public void log(Request req, Response res){
        System.out.prinln("/login or /logout executed")
    }
    ...
}
```

Or you can specify a list of actions to exclude for ```@After``` or ```@Before``` using the unless param:

```java
import javax.servlet.annotation.WebServlet;

import servletio.*;
import servletio.annotation.*;

@WebServlet("/base-path/*")
public class MyApp extends ServletIO {
    
    @Before(unless={"/index"})
    public void validateUser(Request req, Response res){
        if(req.session().attribute("user")==null)
            res.printHtml("bye bye!!");
    }
    ...
}
```

#### Request and Response

You can either use ```Request``` or ```Response``` only as method parameters. This classes wrap HttpServletRequest and HttpServletResponse and offer some facilities. The most prominents are:

###### Request
- ```request.bindParam(MyBean.class)``` return a instance of ```MyBean``` with all values of HTML form, mapping by bean properties names.
- ```request.getFile("paramName")``` return a File with the uploaded file reference.
- ```request.dispatcher(dispatcherName)``` return an instance of ```servletio.Dispatcher```, wrap of ```RequestDispatcher```.
- ```request.raw``` final propierty with ```HttpServletRequest``` object reference of the current request.

###### Response
- ```response.print(htmlString, contentType)```.
- ```response.print(text)``` print text plain.
- ```response.printHtml(htmlString)``` print text with HTML content type.
- ```response.printXml(xmlString)``` print text with XML content type.
- ```response.printJson(xmlString)``` print text with JSON content type.
- ```response.redirect(location)``` redirect to the specified location.
- ```response.redirect(location, httpStatusCode)``` redirect to the specified location with status code (int).
- ```response.badRequest()``` throws 404 to the browser
- ```response.raw``` final propierty with ```HttpServletResponse``` object reference of the current request
