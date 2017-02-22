package eu.bcvsolutions.idm.core.notification.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;

import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.domain.IdentifiableByName;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;

/**
 * Entity that store email/text templates for apache velocity
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Entity
@Table(name = "idm_notification_template", indexes = { 
		@Index(name = "ux_idm_notification_template_name", columnList = "name", unique = true),
		@Index(name = "ux_idm_notification_template_code", columnList = "code", unique = true)})
public class IdmNotificationTemplate extends AbstractEntity implements IdentifiableByName {

	private static final long serialVersionUID = 4978924621333160086L;
	
	@Audited
	@NotNull
	@Size(max = DefaultFieldLengths.NAME)
	@Column(name = "name")
	private String name;
	
	@Audited
	@NotNull
	@Size(max = DefaultFieldLengths.NAME)
	@Column(name = "code")
	private String code;
	
	@Audited
	@NotNull
	@Size(max = DefaultFieldLengths.NAME)
	@Column(name = "subject")
	private String subject;
	
	@Audited
	@Type(type = "org.hibernate.type.StringClobType")
	@Column(name = "body_html")
	private String bodyHtml;
	
	@Audited
	@Type(type = "org.hibernate.type.StringClobType")
	@Column(name = "body_text")
	private String bodyText;
	
	@Audited
	@Column(name = "system_template", nullable = false)
	private boolean systemTemplate = false;
	
	public boolean isSystemTemplate() {
		return systemTemplate;
	}

	public void setSystemTemplate(boolean systemTemplate) {
		this.systemTemplate = systemTemplate;
	}

	public String getBodyHtml() {
		return bodyHtml;
	}

	public String getBodyText() {
		return bodyText;
	}

	public void setBodyHtml(String bodyHtml) {
		this.bodyHtml = bodyHtml;
	}

	public void setBodyText(String bodyText) {
		this.bodyText = bodyText;
	}

	@Override
	public String getName() {
		return name;
	}

	public String getCode() {
		return code;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}
	
	public void setBody(String body) {
		this.bodyHtml = body;
		this.bodyText = body;
	}
}
