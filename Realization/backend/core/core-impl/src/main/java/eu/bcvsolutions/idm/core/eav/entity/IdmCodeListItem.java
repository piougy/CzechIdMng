package eu.bcvsolutions.idm.core.eav.entity;

import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import org.hibernate.envers.Audited;
import javax.validation.constraints.NotEmpty;

import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.domain.ExternalIdentifiable;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.eav.api.entity.FormableEntity;
import eu.bcvsolutions.idm.core.notification.api.domain.NotificationLevel;

/**
 * Code list item => instance with extended attributes
 * 
 * @author Radek Tomiška
 * @since 9.4.0
 */
@Entity
@Table(name = "idm_code_list_item", indexes = { 
		@Index(name = "idx_idm_code_l_i_code", columnList = "code"),
		@Index(name = "idx_idm_code_l_i_codelist", columnList = "code_list_id"),
		@Index(name = "ux_idm_code_l_i_list_code", columnList = "code_list_id,code", unique = true),
		@Index(name = "idx_idm_code_l_i_ext_id", columnList = "external_id"),
})
public class IdmCodeListItem extends AbstractEntity implements FormableEntity, ExternalIdentifiable {

	private static final long serialVersionUID = 1L;
	
	@ManyToOne(optional = false)
	@JoinColumn(name = "code_list_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey( name = "none" )
	private IdmCodeList codeList;
	
	@Audited
	@NotEmpty
	@Size(min = 1, max = DefaultFieldLengths.NAME)
	@Column(name = "code", length = DefaultFieldLengths.NAME, nullable = false)
	private String code;

	@Audited
	@NotEmpty
	@Size(min = 1, max = DefaultFieldLengths.NAME)
	@Column(name = "name", length = DefaultFieldLengths.NAME, nullable = false)
	private String name;
	
	@Audited
	@Size(max = DefaultFieldLengths.NAME)
	@Column(name = "external_id", length = DefaultFieldLengths.NAME)
	private String externalId;
	
	@Audited
	@Size(max = DefaultFieldLengths.DESCRIPTION)
	@Column(name = "description", length = DefaultFieldLengths.DESCRIPTION)
	private String description;
	
	@Audited
	@Enumerated(EnumType.STRING)
	@Column(name = "level", length = 45)
	private NotificationLevel level;
	
	@Audited
	@Size(max = DefaultFieldLengths.NAME)
	@Column(name = "icon", length = DefaultFieldLengths.NAME)
	private String icon;

	public String getCode() {
		return code;
	}
	
	public void setCode(String code) {
		this.code = code;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public IdmCodeList getCodeList() {
		return codeList;
	}
	
	public void setCodeList(IdmCodeList codeList) {
		this.codeList = codeList;
	}
	
	@Override
	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	@Override
	public String getExternalId() {
		return externalId;
	}
	
	public void setLevel(NotificationLevel level) {
		this.level = level;
	}
	
	public NotificationLevel getLevel() {
		return level;
	}
	
	public void setIcon(String icon) {
		this.icon = icon;
	}
	
	public String getIcon() {
		return icon;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
