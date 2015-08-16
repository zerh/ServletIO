package servletio;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

public class Result {
    
    Integer status = HttpServletResponse.SC_OK;
    String content;
    String contentType;
    String redirect;
    String foward;
    Cookie[] cookies;
    Cookie[] discardingCookies;
    InputStream inputStream;
    
    Map<String, String> header;
    Map<String, String> overwrittenHeader;
    Map<String, Long> dateHeader;
    
    
    Result(String content){
        this.content = content;
        init();
    }

    void init(){
        header = new HashMap<String, String>();
        overwrittenHeader = new HashMap<String, String>();
        dateHeader = new HashMap<String, Long>();
    }
    
    public Result withStatus(int status){
        this.status = status;
        return this;
    }
    
    public Result asHtml(){
        contentType = "text/html";
        return this;
    }
    
    public Result asJson(){
        contentType = "application/json";
        return this;
    }
    
    public Result asXml(){
        contentType = "application/xml";
        return this;
    }
    
    public Result as(String contentType){
        this.contentType = contentType;
        return this;
    }
    
    public Result withCookies(Cookie... cookies){
        this.cookies = cookies;
        return this;
    }
    
    public Result withDiscardingCookies(Cookie... discardingCookies){
        this.discardingCookies = discardingCookies;
        return this;
    }
    
    public Result withHeader(String key, String value){
        header.put(key, value);
        return this;
    }
    
    public Result withEditHeader(String key, String value){
        overwrittenHeader.put(key, value);
        return this;
    }
    
    public Result withDateHeader(String key, Number value){
        dateHeader.put(key, value.longValue());
        return this;
    }
    
    public Result withNoCache(){
        withEditHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1
        withEditHeader("Pragma", "no-cache"); // HTTP 1.0
        withDateHeader("Expires", 0); // Proxies.
        return this;
    }
    
    public Result cache(int seconds){
        int CACHE_DURATION_IN_SECOND = seconds;
        long CACHE_DURATION_IN_MS = CACHE_DURATION_IN_SECOND  * 1000;
        long now = System.currentTimeMillis();
        
        withHeader("Cache-Control", "max-age=" + CACHE_DURATION_IN_SECOND);
        withDateHeader("Last-Modified", now);
        withDateHeader("Expires", now + CACHE_DURATION_IN_MS);
        return this;
    }
    
    public Result withCcacheRevalidate(int seconds){
        cache(seconds).withHeader("Cache-Control", "must-revalidate");
        return this;
    }
}
