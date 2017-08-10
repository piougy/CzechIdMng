package eu.bcvsolutions.idm.core.model.jaxb;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import eu.bcvsolutions.idm.core.model.entity.IdmScriptAuthority;

/**
 * Jaxb type for list of services see {@link IdmScriptAuthority}
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
@XmlRootElement(name = "service")
public class IdmScriptServiceType {

	private String name;
	private String className;

	@XmlElement(required = true, type = String.class)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@XmlElement(required = true, type = String.class)
	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

}
