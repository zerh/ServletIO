package com.github.zerh.servletio.utils;

import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;

import com.github.zerh.servletio.Request;

public class RouteUtils {
    
    public static String[] allSegments(HttpServletRequest req){
        return allSegments(routeOf(req));
    }
    
    public static String[] allSegments(Request req){
        return allSegments(routeOf(req));
    }
    
    public static String[] allSegments(String route){
        String[] segments = route.split("/");
        return Arrays.copyOfRange(segments, 1, segments.length);
    }
    
    public static String segment(HttpServletRequest req, int segment){
        return segment(routeOf(req), segment);
    }
    
    public static String segment(Request req, int segment){
        return segment(routeOf(req), segment);
    }
    
    public static String segment(String route, int segment){
        String[] segments = route.split("/");
        segments = Arrays.copyOfRange(segments, 1, segments.length);
        try{
            return segments[segment-1];
        }catch(IndexOutOfBoundsException ex){
            ex.printStackTrace();
            return null;
        }
    }
    
    public static String routeOf(HttpServletRequest req) {
        String uri = req.getRequestURI();
        String ctx = req.getContextPath();
        String servletPath = req.getServletPath();
        String methodPath = uri.substring(ctx.length() + servletPath.length(), uri.length());
        return methodPath;
    }
    
    public static String routeOf(Request req) {
        return routeOf(req.raw);
    }
}
