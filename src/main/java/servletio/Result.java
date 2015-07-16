package servletio;

import javax.servlet.http.HttpServletResponse;

public class Result {
    
    Integer status = HttpServletResponse.SC_OK;
    String content;
    String contentType;
    
    Result(String content){ this.content = content; }
    Result(){}
    
    void printContent(Response res){
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
