[Getting Started](#getting-started) | [Quick Installation](#quick-installation) | [Notes](#notes) | [Documentation](#documentation) | [Lincense](#license)

***

***
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
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        super.doGet(request, response);
        //your old code
    }
    
    @Get
    public Result hello(Request req){
        return respond("<h1>hello world</h1>").as("text/html");
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
        HttpServletRequest request = req.raw;
        HttpServletResponse response = res.raw;
        
        //old code
    }

    @Get
    public Result hello(Request req){
        return respond("<h1>hello world</h1>").as("text/html");
    }
}
```

***

## Quick installation
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
    <version>eb986730b6</version>
</dependency>
```

***

##Notes
*Supported since Servlet* **3.0.1** *or higher, and Java* **6** *or higher*

***

## Documentation
[HTTP Routing](#http-routing) | [Servlet style methods](#servlet-style-methods) | [Result methods](#result-methods) | [@Before and @After](#Before-and-after)

### HTTP routing

The path reference to a method is the method name, but also could be a specified path in the annotation params ```@Get```, ```@Post```, ```@Put```, ```@Delete```, ```@Options```

The parameters could be mapped as pretty url using **":"** before the url parameter as below:

```java
import javax.servlet.annotation.WebServlet;

import servletio.*;
import servletio.annotation.*;

@WebServlet("/base-path/*")
public class MyApp extends ServletIO {
    
    @Get("/edit/:id")
    public Result edit(Request req){
        return respond("<h1>the url has: " + req.param(":id") + "</h1>").as("text/html");
    }
}
```

***

### Servlet Style Methods

A Servlet style method is a Java method that receive a ```servletio.Request``` and a ```servletio.Response``` parameters as below:

```java
import javax.servlet.annotation.WebServlet;

import servletio.*;
import servletio.annotation.*;

@WebServlet("/base-path/*")
public class MyApp extends ServletIO {
    
    @Get
    public void hello(Request req, Response res){
        res.printHtml("<h1>hellow world</h1>");
    }
}
```

You can either use ```Request``` or ```Response``` only as method parameters. This classes wrap HttpServletRequest and HttpServletResponse and offer some facilities. The most prominents are:

##### Request
- ```request.bindParam(MyBean.class)``` return a instance of ```MyBean``` with all values of HTML form, mapping by bean properties names.
- ```request.getFile(String paramName)``` return a File with the uploaded file reference.
- ```request.dispatcher(String dispatcherName)``` return an instance of ```servletio.Dispatcher```, wrap of ```RequestDispatcher```.
- ```request.raw``` final propierty with ```HttpServletRequest``` object reference of the current request.

##### Response
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

***

### Result methods

```Result``` is just an elegant way to declare controllers methods. The ```Result``` object wraps with ```ServletIO``` the methods printers of the ```Response``` object, to produce standard HTTP results. 

```java
import javax.servlet.annotation.WebServlet;

import servletio.*;
import servletio.annotation.*;

@WebServlet("/base-path/*")
public class MyApp extends ServletIO {
    
    @Get
    public Result hello(Request req){
        return respond("content");
    }
}
```


The object Result has methods ```asHtml()```, ```asJson()```, ```as(String contentType)```, ```withCookies(Cookies... cookies)```, ```withDiscardingCookies(Cookies... cookies)``` and others helpers methods that returns the same instance of the ```Result``` object like the next example:

```java
import javax.servlet.annotation.WebServlet;

import servletio.*;
import servletio.annotation.*;

@WebServlet("/base-path/*")
public class MyApp extends ServletIO {
    
    @Get
    public Result hello(Request req){
        return respond("<h1>content</h1>").asHtml();
    }
}
```

ServletIO contains some helper in addition to ```respond(String content)``` method, that return objects Result:

- ```internalServerError(String content)``` returns HTTP results with the 500 code.
- ```sendFile(InputStream inputStream)``` send file to client.
- ```badRequest(String optionalContent)```returns HTTP results with the 400 code.
- ```notFound(String optionalContent)``` returns HTTP results with the 404 code.
- ```redirect(String target)``` redirect to the target.
- ```temporaryRedirect(String target)``` redirect to the target with 303 code

***

### @Before and @After

Methods annotated with the ```@Before``` annotation are executed before each action call for ```ServletIO```, and methods annotated with the ```@After``` annotation are executed after each action call for the Servlet:

```java
import javax.servlet.annotation.WebServlet;

import servletio.*;
import servletio.annotation.*;

@WebServlet("/base-path/*")
public class MyApp extends ServletIO {

    @Get
    public Result index(Request req){
        return respond("<h1>hello world</h1>").as("text/html");
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

import servletio.*;
import servletio.annotation.*;

@WebServlet("/base-path/*")
public class MyApp extends ServletIO {
    
    @Get
    public Result login(Request req){
    	return respond("<h1>login</h1>").as("text/html");
    }
    
    @Before(only="/admin")
    public void validateUser(Request req, Response res){
        if(req.session().attribute("user")==null)  
            res.redirect("login");
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
    
    @Before(unless="/login")
    public void validateUser(Request req, Response res){
        if(req.session().attribute("user")==null)
            req.dispatcher("login").forward(req, res);
    }
    ...
}
```

You can set execution priority of ```@Before``` or ```@After``` methods using ```priority``` param, example ```@Before(priority=1)```, by default priority is 0 (executed first):

```java
import javax.servlet.annotation.WebServlet;

import servletio.*;
import servletio.annotation.*;

@WebServlet("/base-path/*")
public class MyApp extends ServletIO {
    
    @Before(only="/admin/something", priority=2)
    public void adminSomething(Request req, Response res){
        //some filter for this action
    }
    
    @Before(only="/admin/*", priority=1)
    public void validateUser(Request req, Response res){
        if(req.session().attribute("user")==null)
            res.redirect("login");
    }
    ...
}
```

***

## License 

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at <http://www.apache.org/licenses/LICENSE-2.0>
