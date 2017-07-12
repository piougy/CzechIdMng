package eu.bcvsolutions.idm.core.model.jaxb;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "services")
public class IdmScriptServicesType {
	
	private List<IdmScriptServiceType> services;

	@XmlElement(name="service", type = IdmScriptServiceType.class)
	public List<IdmScriptServiceType> getServices() {
		return services;
	}

	public void setServices(List<IdmScriptServiceType> services) {
		this.services = services;
	}
	
}
