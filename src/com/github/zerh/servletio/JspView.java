package com.github.zerh.servletio;

import java.util.HashMap;
import java.util.Map;

public class JspView implements Render{
	
	private String viewName;
	private Map<String, Object> modelMap;
	
	public JspView(String viewName) {
		this.viewName = viewName;
	}
	
	public JspView(String viewName, Map<String, Object> modelMap){
		this.viewName = viewName;
		this.modelMap = modelMap;
	}
	
	public JspView(String viewName, String modelName, Object model){
		this.viewName = viewName;
		Map<String, Object> modelMap = new HashMap<String, Object>();
		modelMap.put(modelName, model);
		this.modelMap = modelMap;
	}
	
	private String completeViewName(String viewName) {
		if (!viewName.startsWith("/")) {
			viewName = "/WEB-INF/views/" + viewName;
		}
		if (!viewName.endsWith(".jsp")) {
			viewName = viewName + ".jsp";
		}
		return viewName;
	}

	@Override
	public void render(Request request, Response response) {
		for (String key : modelMap.keySet()) {
			request.attribute(key, modelMap.get(key));
		}
		request.dispatcher(completeViewName(viewName)).forward(request, response);
	}
}