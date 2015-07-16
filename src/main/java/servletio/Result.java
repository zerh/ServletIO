package servletio;

import javax.servlet.http.HttpServletResponse;

public class Result {
    
    Integer status = HttpServletResponse.SC_OK;
    String content;
    String contentType;
    
    String redirect;
    String foward;
    
    Result(String content){ this.content = content; }
    Result(){}
    
    void resultLogic(Response res){
        
        if(redirect!=null)
            res.redirect(redirect);
        
        if(contentType!=null)
            res.print(content, contentType);
        else
            res.print(content);
        
        res.status(status);
    }
    
    public Result as(String contentType){
        this.contentType = contentType;
        return this;
    }
    
}
