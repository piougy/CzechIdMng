package eu.bcvsolutions.idm.core.scheduler.jaxb;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * One instance of parameter for scheduled task type.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@XmlRootElement(name = "parameter")
public class IdmScheduledTaskParameterType {

	private String value;
	private String key;

	@XmlElement(required = false, type = String.class)
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@XmlElement(required = true, type = String.class)
	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

}
