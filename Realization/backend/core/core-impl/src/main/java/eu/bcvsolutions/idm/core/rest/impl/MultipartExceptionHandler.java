package eu.bcvsolutions.idm.core.rest.impl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

//@Component
public class MultipartExceptionHandler implements HandlerExceptionResolver, Ordered {

	@Override
	public int getOrder() {
		// TODO Auto-generated method stub
		return Integer.MIN_VALUE;
	}

	@Override
	public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler,
			Exception ex) {
		// TODO Auto-generated method stub
		if(ex != null) {
			System.out.println("WOOhoooo!");
			System.out.println(ex);
		}
		
		ModelAndView mvc = new ModelAndView();
		mvc.addObject(ex);
		return mvc;
	}	
}
