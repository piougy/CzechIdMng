package eu.bcvsolutions.idm.core.model.dto;

import org.springframework.hateoas.core.Relation;

/**
 * DTO with information about available service that can be used in scripts.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
@Relation(collectionRelation = "scriptAuthorityServices")
public class IdmScriptAuthorityServiceDto  {

	private String serviceName;
	
	private String serviceClass;
	
	public IdmScriptAuthorityServiceDto() {
	}

	public IdmScriptAuthorityServiceDto(String serviceName, String serviceClass) {
		this.serviceName = serviceName;
		this.serviceClass = serviceClass;
	}
	
	public String getServiceName() {
		return serviceName;
	}

	public String getServiceClass() {
		return serviceClass;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public void setServiceClass(String serviceClass) {
		this.serviceClass = serviceClass;
	}

	
}
