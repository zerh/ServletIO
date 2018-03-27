package com.github.zerh.servletio;

import java.util.HashMap;
import java.util.Map;

public class Jsp implements Render{
	
	private String viewName;
	private Map<String, Object> modelMap;
	private ViewResolver viewResolver;
	
	Jsp(String viewName) {
		this.viewName = viewName;
	}
	
	Jsp(String viewName, Map<String, Object> modelMap){
		this.viewName = viewName;
		this.modelMap = modelMap;
	}
	
	Jsp(String viewName, String modelName, Object model){
		this.viewName = viewName;
		Map<String, Object> modelMap = new HashMap<String, Object>();
		modelMap.put(modelName, model);
		this.modelMap = modelMap;
	}

	public ViewResolver getViewResolver() {
		return viewResolver;
	}

	public void setViewResolver(ViewResolver viewResolver) {
		this.viewResolver =  viewResolver;
	}

	private String completeViewName(String viewName) {

		if(viewResolver!=null) {

			if (viewResolver.prefix() != null) viewName = viewResolver.prefix() + viewName;

			if (viewResolver.suffix() != null) viewName = viewName + viewResolver.suffix();

		}

		return viewName;
	}

	@Override
	public void render(Request request, Response response) {
		if(modelMap!=null) {
			for (String key : modelMap.keySet()) {
				request.attribute(key, modelMap.get(key));
			}
		}

		request.dispatcher(completeViewName(viewName)).forward(request, response);
	}
}