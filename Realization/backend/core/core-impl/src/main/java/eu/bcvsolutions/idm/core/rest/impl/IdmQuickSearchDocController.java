package eu.bcvsolutions.idm.core.rest.impl;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.data.domain.PageRequest;

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
public class IdmQuickSearchDocController implements BaseController {
	
	@Autowired
	private ApplicationContext context;
	
	private final String QUICK_SEARCH = "/search/quick";
	
	private final String RESOURCE_NAME = "resourceName";
	
	@RequestMapping(value = "/{" + RESOURCE_NAME + "}/search", method = RequestMethod.GET)
	public ResponseEntity<?> getDoc(PersistentEntityResourceAssembler assembler, HttpServletRequest request) throws NoSuchMethodException, SecurityException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		 @SuppressWarnings("rawtypes")
		 Map<String, AbstractReadEntityController> controllers = context.getBeansOfType(AbstractReadEntityController.class);

		 String resourceName = ((Map<?, ?>)request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE)).get(this.RESOURCE_NAME).toString();

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
						
						List<Field> pageAbleFields = this.getAllDeclaredFields(filterObject.getClass(), new ArrayList<Field>());
						variables.addAll(this.getListOfFieldsNames(pageAbleFields));
					}
				}
				List<Field> pageAbleFields = this.getAllDeclaredFields(PageRequest.class, new ArrayList<Field>());
				variables.addAll(this.getListOfFieldsNames(pageAbleFields));

				Link link = linkTo(controller).withRel("quick");
				Link quickSearchLink = new Link(new UriTemplate(link.getHref() + QUICK_SEARCH + "{?" + String.join(",", variables) + "}", TemplateVariables.NONE), "quick");
				Link selfLink = linkTo(this.getClass()).withSelfRel();
				
				ResourceSupport resource = new ResourceSupport();
				resource.add(quickSearchLink);
				resource.add(selfLink);
				return new ResponseEntity<>(resource, HttpStatus.OK);
			 }
			 
		}
		
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}
	
	@RequestMapping(method = RequestMethod.GET)
	public ResponseEntity<?> get(PersistentEntityResourceAssembler assembler, HttpServletRequest request) {
		@SuppressWarnings("rawtypes")
		Map<String, AbstractReadEntityController> controllers = context.getBeansOfType(AbstractReadEntityController.class);
		
		ResourceSupport links = new ResourceSupport();
		for (String key : controllers.keySet()) {
			 Class<?> controller = AopProxyUtils.ultimateTargetClass(controllers.get(key));
			 String[] annotations = controller.getAnnotation(RequestMapping.class).value();
			 
			 if	(annotations.length > 0) {
				 String[] completeName = annotations[0].split("/");
				 
				 Link selfLink = linkTo(BaseController.class).withSelfRel();
				 links.add(new Link(new UriTemplate(selfLink.getHref() + annotations[0] + QUICK_SEARCH, TemplateVariables.NONE), completeName[completeName.length - 1]));

			 }
		}
		
		Link selfLink = linkTo(this.getClass()).withSelfRel();
		links.add(selfLink);
		
		return new ResponseEntity<>(links, HttpStatus.OK);
	}
	
	/**
	 * Method get recursive all fields to superClass == null
	 * @param type of class
	 * @param list fields 
	 * @return list fields
	 */
	private List<Field> getAllDeclaredFields(Class<?> type, List<Field> fields) {
		fields.addAll(Arrays.asList(type.getDeclaredFields()));
		
	    if (type.getSuperclass() != null) {
	        fields = getAllDeclaredFields(type.getSuperclass(), fields);
	    }
	    
	    return fields;
	}
	
	/**
	 * Method return list of fields names without attribute 'serialVersionUID'
	 * @param list fields
	 * @return list string
	 */
	private List<String> getListOfFieldsNames(List<Field> fields) {
		List<String> names = new ArrayList<String>();
		for (Field field : fields) {
			if (field.getName() != "serialVersionUID") {
				names.add(field.getName());
			}
		}
		return names;
	}
}