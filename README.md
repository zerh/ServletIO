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
    <version>7df088b078</version>
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
public class Prueba extends ServletIO {
    
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
public class Prueba extends ServletIO {

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
