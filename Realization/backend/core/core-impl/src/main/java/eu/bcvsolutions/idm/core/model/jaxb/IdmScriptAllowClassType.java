package eu.bcvsolutions.idm.core.model.jaxb;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import eu.bcvsolutions.idm.core.model.entity.IdmScriptAuthority;

/**
 * Jaxb type for list of allowed classes see {@link IdmScriptAuthority}
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
@XmlRootElement(name = "allowClass")
public class IdmScriptAllowClassType {

	private String className;

	@XmlElement(required = true, type = String.class)
	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}
	
}
