package servletio;

import java.util.HashMap;
import java.util.Map;

import servletio.utils.RouteUtils;

public class Pretty {

    String route;
    String[] segments;
    Boolean[] toMatch;
    Map<String, Integer> indexByTag;

    Pretty match(String route) {
        this.route = route;
            
        indexByTag = new HashMap<String, Integer>();

        segments = RouteUtils.allSegments(this.route);
        toMatch = new Boolean[segments.length];

        for (int i = 0; i < segments.length; i++) {
            if (segments[i].startsWith(":")) {
                indexByTag.put(segments[i], i + 1);
                toMatch[i] = false;
            } else {
                toMatch[i] = true;
            }
        }

        return this;
    }
    
    public boolean hasParams(){
    	String[] segments = RouteUtils.allSegments(this.route);
    	for (int i = 0; i < segments.length; i++)
            if (segments[i].startsWith(":")) return true;
            	
    	return false;
    }

    boolean withRequest(String route2) {
        boolean flag = true;

            String[] segments2 = RouteUtils.allSegments(route2);
            
            if(segments.length!=segments2.length) return false;
            
            for (int i = 0; i < segments.length; i++) {
                if (toMatch[i]) {
                    
                    if (!segments[i].equals(segments2[i])){
                        flag = false;
                    }
                }
            }
        
        return flag;
    }

}
