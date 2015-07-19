package servletio;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

public class Result {
    
    Integer status = HttpServletResponse.SC_OK;
    String content;
    String contentType = "text/html";
    String redirect;
    String foward;
    Cookie[] cookies;
    Cookie[] discardingCookies;
    
    Result(String content){
        this.content = content;
    }
    
    Result(){}
    
    void resultLogic(Response res){
        
        if(redirect!=null) res.redirect(redirect);
        
        if(cookies!=null)
            for(Cookie cookie : cookies) res.raw.addCookie(cookie);
        
        if(discardingCookies!=null){
            for(Cookie cookie: discardingCookies){
                cookie.setMaxAge(0);
                res.raw.addCookie(cookie);
            }
        }
        
        if(contentType!=null){
            res.print(content, contentType);
        }else{
            res.print(content);
        }
        
        res.status(status);
    }
    
    public Result as(String contentType){
        this.contentType = contentType;
        return this;
    }
    
    public Result withCookies(Cookie... cookies){
        this.cookies = cookies;
        return this;
    }
    
    public Result discardingCookies(Cookie... discardingCookies){
        this.discardingCookies = discardingCookies;
        return this;
    }
}
