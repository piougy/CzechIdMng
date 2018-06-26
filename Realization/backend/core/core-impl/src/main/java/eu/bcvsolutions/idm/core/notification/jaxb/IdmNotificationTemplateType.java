package eu.bcvsolutions.idm.core.notification.jaxb;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import eu.bcvsolutions.idm.core.api.jaxb.CDATAAdapter;
import eu.bcvsolutions.idm.core.notification.entity.IdmNotificationTemplate;

/**
 * Jaxb type for check schema {@link IdmNotificationTemplate}
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@XmlRootElement(name = "template") // root element
@XmlType(propOrder = { "code", "name", "subject", "bodyHtml", "bodyText", "parameter", "systemTemplate", "moduleId", "sender"}) // order
public class IdmNotificationTemplateType {

	private String code;
	private String name;
	private String subject;
	private String bodyHtml;
	private String bodyText;
	private String parameter;
	private boolean systemTemplate;
	private String moduleId;
	private String sender;

	@XmlElement(required = true, type = String.class)
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	@XmlElement(type = String.class)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@XmlElement(type = String.class)
	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	@XmlElement(type = String.class)
	@XmlJavaTypeAdapter(CDATAAdapter.class)
	public String getBodyHtml() {
		return bodyHtml;
	}

	public void setBodyHtml(String bodyHtml) {
		this.bodyHtml = bodyHtml;
	}

	@XmlElement(type = String.class)
	public String getBodyText() {
		return bodyText;
	}

	public void setBodyText(String bodyText) {
		this.bodyText = bodyText;
	}

	@XmlElement(type = String.class)
	public String getParameter() {
		return parameter;
	}

	public void setParameter(String parameter) {
		this.parameter = parameter;
	}

	@XmlElement(type = Boolean.class)
	public boolean isSystemTemplate() {
		return systemTemplate;
	}

	public void setSystemTemplate(boolean systemTemplate) {
		this.systemTemplate = systemTemplate;
	}

	@XmlElement(type = String.class)
	public String getModuleId() {
		return moduleId;
	}

	public void setModuleId(String moduleId) {
		this.moduleId = moduleId;
	}

	@XmlElement(type = String.class)
	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}
}
