package eu.bcvsolutions.idm.core.scheduler.jaxb;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Parameters for scheduled task type.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@XmlRootElement(name = "parameters")
public class IdmScheduledTaskParametersType {

	private List<IdmScheduledTaskParameterType> parameters;

	@XmlElement(name="parameter", type = IdmScheduledTaskParameterType.class)
	public List<IdmScheduledTaskParameterType> getParameters() {
		return parameters;
	}

	public void setParameters(List<IdmScheduledTaskParameterType> parameters) {
		this.parameters = parameters;
	}

}
