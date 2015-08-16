package test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.servlet.annotation.WebServlet;

import servletio.Request;
import servletio.Response;
import servletio.Result;
import servletio.ServletIO;
import servletio.annotation.After;
import servletio.annotation.Get;
import servletio.utils.RouteUtils;

@WebServlet("/Prueba/*")
public class Prueba extends ServletIO {
    
	private static final long serialVersionUID = 1L;
	
    @Get("/bash/pepe/:a/jejeje")
    public Result bash(Request request){
        return respond("mierda : " + request.param(":a")).asHtml();
    }
    
    @Get
    public Result admin(Request request){
        File f = new File("/home/eliezer/servletio-1.0.jar");
        InputStream is;
        try {
            is = new FileInputStream(f);
            return sendFile(is);
        } catch (FileNotFoundException e) {
            return internalServerError("no funca y " + e.getMessage());
        }
        
    }
    
    
    public void vaina2(Request request){
        /*
        if(!isMapped(request)){
            return respond("<h1>no encontrado</h1>").as("text/html");
        }
        return respond("<h1>si existe</h1>").as("text/html");
        */
        
    }
    
    @After(only="/bash/pepe/:a/jejeje/")
    public void vaina(Request request, Response response){
        System.out.println();
        System.out.println("Funcionaaaa!!!!");
    }
}
