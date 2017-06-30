package eu.bcvsolutions.idm.core.model.jaxb;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "allowClasses")
public class IdmScriptAllowClassesType {

	private List<IdmScriptAllowClassType> allowClasses;

	@XmlElement(name="allowClass", type = IdmScriptAllowClassType.class)
	public List<IdmScriptAllowClassType> getAllowClasses() {
		return allowClasses;
	}

	public void setAllowClasses(List<IdmScriptAllowClassType> allowClasses) {
		this.allowClasses = allowClasses;
	}
}
