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
    <version>a01c1c79d6</version>
</dependency>
```

## Get Started
Add **"/*"** to your mapped url, example: ```@WebServlet("/your-path/*")``` and replace the super class ```HttpServlet``` by ```ServletIO```

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
    public void index(Response res){
        res.printHtml("<h1>hello world</h1>");
    }
}

```
Or put your old ```doGet``` code inside another method mapped with the old path
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
    public void index(Request req, Response res){
        res.printHtml("<h1>hello world</h1>");
    }
}
```
#### @Before and @After

Methods annotated with the @Before annotation are executed before each action call for ServletIO, and methods annotated with the @After annotation are executed after each action call for the Servlet:

```java
import javax.servlet.annotation.WebServlet;

import servletio.Request;
import servletio.Response;
import servletio.ServletIO;
import servletio.annotation.Before;
import servletio.annotation.Get;

@WebServlet("/base-path/*")
public class MyApp extends ServletIO {
       
    @Get("/index")
    public void index(Response res){
        res.printHtml("<h1>hello world</h1>");
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
If you don’t want the @Before or @After method to intercept all request, you can specify a list of actions using only param:

```java
import javax.servlet.annotation.WebServlet;

import servletio.Request;
import servletio.Response;
import servletio.ServletIO;
import servletio.annotation.*;

@WebServlet("/base-path/*")
public class MyApp extends ServletIO {
    
    @Before(only={"/admin"})
    public void validateUser(Request req, Response res){
        if(req.session().attribute("user")==null)
            res.printHtml("bye bye!!");
    }
    
    @After(only={"/login", "/logout"})
    public void log(Request req, Response res){
        System.out.prinln("/login or /logout executed")
    }
    ...
}
```

Or you can specify a list of actions to exclude for @After or @Before using the unless param:

```java
import javax.servlet.annotation.WebServlet;

import servletio.Request;
import servletio.Response;
import servletio.ServletIO;
import servletio.annotation.*;

@WebServlet("/base-path/*")
public class MyApp extends ServletIO {
    
    @Before(unless={"/index"})
    public void validateUser(Request req, Response res){
        if(req.session().attribute("user")==null)
            res.printHtml("bye bye!!");
    }
}
```

#### Request and Response

you can use only ```Request``` or only ```Response``` like method parameters. This classes wrap HttpServletRequest and HttpServletResponse and offer some facilities. The most prominent are:

###### Request
- ```request.bindParam(MyBean.class)``` return a instance of ```MyBean``` with all values of HTML form, mapping by bean properties names;
- ```request.getFile("paramName")``` return a File with the uploaded file reference.

###### Response
- ```response.print(htmlString, contentType)```
- ```response.print(text)``` print text plain
- ```response.printHtml(htmlString)``` print text with HTML content type
- ```response.printXml(xmlString)``` print text with xML content type
- ```response.printJson(xmlString)``` print text with JSON content type
- ```response.redirect(location)``` redirect
- ```response.redirect(location, httpStatusCode)``` redirect with status code (int)
- ```response.badRequest()``` throws 404 to browser
- ```response.raw``` final propierty with ```HttpServletResponse``` object reference
