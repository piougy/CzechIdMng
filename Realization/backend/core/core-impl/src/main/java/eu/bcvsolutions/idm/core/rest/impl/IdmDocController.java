package eu.bcvsolutions.idm.core.rest.impl;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.TemplateVariables;
import org.springframework.hateoas.UriTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerMapping;

import eu.bcvsolutions.idm.core.api.dto.QuickFilter;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadEntityController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseEntityController;



/**
 * Endpoint for quick search documentation for other endpoints
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@RestController
@RequestMapping(value = BaseEntityController.BASE_PATH + "/doc")
public class IdmDocController implements BaseController {
	
	@Autowired
	private ApplicationContext context;
	
	private final String resourceName = "resourceName";
	
	@RequestMapping(value = "/{" + resourceName + "}/search", method = RequestMethod.GET)
	public ResourceSupport getDoc(PersistentEntityResourceAssembler assembler, HttpServletRequest request) throws NoSuchMethodException, SecurityException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		 @SuppressWarnings("rawtypes")
		Map<String, AbstractReadEntityController> controllers = context.getBeansOfType(AbstractReadEntityController.class);
		
		 String resourceName = ((Map<?, ?>)request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE)).get(this.resourceName).toString();

		 
		 for (String key : controllers.keySet()) {
			 Class<?> controller = AopProxyUtils.ultimateTargetClass(controllers.get(key));
			 String[] annotations = controller.getAnnotation(RequestMapping.class).value();

			 
			 if	(annotations.length > 0 && annotations[0].endsWith(resourceName)) {
				ParameterizedType genericSuperClass = (ParameterizedType) controller.getGenericSuperclass();
				Type[] genericTypes = genericSuperClass.getActualTypeArguments();
				
				ArrayList<String> variables = new ArrayList<>();
				
				for (Type type : genericTypes) {
					Object filterObject = Class.forName(type.getTypeName()).newInstance();
					if (QuickFilter.class.isInstance(filterObject)) {
						
						Class<? extends Object> filter = filterObject.getClass();
						
						while (filter != Object.class) {
							Field[] aa = filter.getDeclaredFields();
							for (Field field : aa) {
								variables.add(field.getName());
							}
							filter = filter.getSuperclass();
						}
					}
				}
				
				
				Link link = linkTo(controller).withRel("quick");
				
				Link quickSearchLink = new Link(new UriTemplate(link.getHref() + "/search/quick{?" + String.join(",", variables) + ",page,size,sort}", TemplateVariables.NONE), "quick");
				
				Link selfLink = linkTo(this.getClass()).withSelfRel();
				
				ResourceSupport resource = new ResourceSupport();
				resource.add(quickSearchLink);
				resource.add(selfLink);
				return resource;
			 }
			 
		}
		
		return null;
	}
	
	
}