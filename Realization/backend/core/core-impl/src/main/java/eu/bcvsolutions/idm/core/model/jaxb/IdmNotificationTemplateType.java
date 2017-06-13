package eu.bcvsolutions.idm.core.model.jaxb;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import eu.bcvsolutions.idm.core.notification.entity.IdmNotificationTemplate;

/**
 * Jaxb type for check schema {@link IdmNotificationTemplate}
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@XmlRootElement(name = "template")
public class IdmNotificationTemplateType {

	private String code;
	private String name;
	private String subject;
	private String bodyHtml;
	private String bodyText;
	private String parameter;
	private boolean systemTemplate;
	private String moduleId;

	@XmlElement
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	@XmlElement
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@XmlElement
	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	@XmlElement
	public String getBodyHtml() {
		return bodyHtml;
	}

	public void setBodyHtml(String bodyHtml) {
		this.bodyHtml = bodyHtml;
	}

	@XmlElement
	public String getBodyText() {
		return bodyText;
	}

	public void setBodyText(String bodyText) {
		this.bodyText = bodyText;
	}

	@XmlElement
	public String getParameter() {
		return parameter;
	}

	public void setParameter(String parameter) {
		this.parameter = parameter;
	}

	@XmlElement
	public boolean isSystemTemplate() {
		return systemTemplate;
	}

	public void setSystemTemplate(boolean systemTemplate) {
		this.systemTemplate = systemTemplate;
	}
	
	@XmlElement
	public String getModuleId() {
		return moduleId;
	}

	public void setModuleId(String moduleId) {
		this.moduleId = moduleId;
	}

}
