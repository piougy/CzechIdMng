package eu.bcvsolutions.idm.core.api.dto;

import java.util.List;

import org.springframework.hateoas.core.Relation;

/**
 * DTO with information about available service that can be used for example in scripts.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @author Ondrej Husnik 
 */
@Relation(collectionRelation = "availableServices")
public class AvailableServiceDto extends AbstractComponentDto {

	private static final long serialVersionUID = 1L;
	private String serviceName;
	private String packageName;
	private List<AvailableMethodDto> methods;
	
	public AvailableServiceDto() {
	}

	public AvailableServiceDto(String serviceName) {
		this.serviceName = serviceName;
	}
	
	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	} 
	
	public List<AvailableMethodDto> getMethods() {
		return this.methods;
	}
	
	public void setMethods(List<AvailableMethodDto> methods) {
		this.methods = methods;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}
}
