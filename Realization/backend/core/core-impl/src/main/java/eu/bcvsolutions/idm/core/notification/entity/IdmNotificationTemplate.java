package eu.bcvsolutions.idm.core.notification.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;

import eu.bcvsolutions.idm.core.api.domain.Codeable;
import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.entity.UnmodifiableEntity;

/**
 * Entity that store email/text templates for apache velocity
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
@Entity
@Table(name = "idm_notification_template", indexes = { 
		@Index(name = "ux_idm_notification_template_code", columnList = "code", unique = true),
		@Index(name = "idx_idm_n_template_name", columnList = "name")})
public class IdmNotificationTemplate extends AbstractEntity implements Codeable, UnmodifiableEntity {

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
	@Column(name = "parameter")
	private String parameter; // TODO: better place/table? Only information characters
	
	@NotNull
	@Column(name = "unmodifiable", nullable = false)
	private boolean unmodifiable = false;
	
	@Audited
	@Size(max = DefaultFieldLengths.NAME)
	@Column(name = "module")
	private String module;

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

	public String getName() {
		return name;
	}

	@Override
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
	
	public String getParameter() {
		return parameter;
	}

	public void setParameter(String parameter) {
		this.parameter = parameter;
	}

	public String getModule() {
		return module;
	}

	public void setModule(String module) {
		this.module = module;
	}

	@Override
	public boolean isUnmodifiable() {
		return this.unmodifiable;
	}

	@Override
	public void setUnmodifiable(boolean unmodifiable) {
		this.unmodifiable = unmodifiable;
	}
}
