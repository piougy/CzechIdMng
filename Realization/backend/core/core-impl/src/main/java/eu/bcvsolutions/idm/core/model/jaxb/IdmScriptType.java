package eu.bcvsolutions.idm.core.model.jaxb;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import eu.bcvsolutions.idm.core.api.domain.IdmScriptCategory;
import eu.bcvsolutions.idm.core.api.jaxb.CDATAAdapter;

@XmlRootElement(name = "script") // root element
public class IdmScriptType {

	private String code;
	private String name;
	private IdmScriptCategory category;
	private String type;
	private String body;
	private String parameters;
	private String description;
	private IdmScriptAllowClassesType allowClasses;
	private IdmScriptServicesType services;

	@XmlElement(type = IdmScriptAllowClassesType.class)
	public IdmScriptAllowClassesType getAllowClasses() {
		return allowClasses;
	}

	public void setAllowClasses(IdmScriptAllowClassesType allowClasses) {
		this.allowClasses = allowClasses;
	}

	@XmlElement(type = IdmScriptServicesType.class)
	public IdmScriptServicesType getServices() {
		return services;
	}

	public void setServices(IdmScriptServicesType services) {
		this.services = services;
	}

	@XmlElement(required = true, type = String.class)
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	@XmlElement(required = true, type = String.class)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@XmlElement(type = IdmScriptCategory.class)
	public IdmScriptCategory getCategory() {
		return category;
	}

	public void setCategory(IdmScriptCategory category) {
		this.category = category;
	}

	@XmlElement(type = String.class)
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@XmlElement(type = String.class)
	@XmlJavaTypeAdapter(CDATAAdapter.class)
	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	@XmlElement(type = String.class)
	public String getParameters() {
		return parameters;
	}

	public void setParameters(String parameters) {
		this.parameters = parameters;
	}

	@XmlJavaTypeAdapter(CDATAAdapter.class)
	@XmlElement(type = String.class)
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
}
