package eu.bcvsolutions.idm.core.rest.domain;

import org.springframework.hateoas.ResourceSupport;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

/**
 * 
 * @author svandav
 *
 * @param <T> dto for resource
 */
@JsonIgnoreProperties({ "id" })
public class ResourceWrapper<T> extends ResourceSupport {

	@JsonUnwrapped
	private T resource;
	
	public ResourceWrapper(T resource) {
		this.resource = resource;
	}
	
	public T getResource() {
		return resource;
	}
}