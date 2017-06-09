package eu.bcvsolutions.idm.core.model.entity;

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
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.envers.Audited;

import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.domain.ScriptAuthorityType;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;

/**
 * Entity that store extra authority (allowed beans) for script
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Entity
@Table(name = "idm_script_authority", indexes = { 
		@Index(name = "idx_idm_script_auth_script", columnList = "script_id") 
		})
public class IdmScriptAuthority extends AbstractEntity {

	private static final long serialVersionUID = -6593526501130733610L;

	@Audited
	@Size(max = DefaultFieldLengths.NAME)
	@Column(name = "service", length = DefaultFieldLengths.NAME)
	private String service;
	
	@Audited
	@Size(max = DefaultFieldLengths.NAME)
	@Column(name = "class_name", length = DefaultFieldLengths.NAME)
	private String className;
	
	@NotNull
	@Audited
	@ManyToOne(optional = false)
	@JoinColumn(name = "script_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey( name = "none" )
	private IdmScript script;
	
	@Audited
	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "type", nullable = false)
	private ScriptAuthorityType type = ScriptAuthorityType.SERVICE;
	
	public String getService() {
		return service;
	}

	public String getClassName() {
		return className;
	}

	public IdmScript getScript() {
		return script;
	}

	public ScriptAuthorityType getType() {
		return type;
	}

	public void setService(String service) {
		this.service = service;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public void setScript(IdmScript script) {
		this.script = script;
	}

	public void setType(ScriptAuthorityType type) {
		this.type = type;
	}
}
