package eu.bcvsolutions.idm.core.eav.api.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.core.Ordered;

import eu.bcvsolutions.idm.core.api.domain.Identifiable;
import eu.bcvsolutions.idm.core.api.service.Configurable;

/**
 * Form projection route - projection configuration.
 * 
 * @param <O> evaluated {@link Identifiable} type - route is designed for owner type. 
 * @author Radek Tomiška
 * @since 10.3.0
 */
public interface FormProjectionRoute<O extends Identifiable> extends Ordered, Configurable {
	
	@Override
	default String getConfigurableType() {
		return "form-projection-route";
	}
	
	/**
	 *  bean name / unique identifier (spring bean name)
	 *  
	 * @return
	 */
	String getId();
	
	/**
	 * Returns owner type class, which supports this route.
	 * 
	 * @return
	 */
	Class<O> getOwnerType();
	
	/**
	 * Returns true, when evaluator supports given authorizable type
	 * 
	 * @param authorizableType
	 * @return
	 */
	boolean supports(Class<?> ownerType);
	
	/**
	 * Returns configuration property names for this configurable object
	 */
	@Override
	default List<String> getPropertyNames() {
		return new ArrayList<>();
	}
}
