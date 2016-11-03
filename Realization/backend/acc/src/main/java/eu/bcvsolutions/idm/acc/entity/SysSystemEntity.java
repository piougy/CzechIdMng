package eu.bcvsolutions.idm.acc.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotEmpty;

import com.sun.istack.NotNull;

import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;

/**
 * An entity on target system. Entity could be linked to idm entitites (identity accounts, groups etc.).
 * 
 * @author Radek Tomiška
 *
 */
@Entity
@Table(name = "sys_system_entity", indexes = {
		@Index(name = "ux_system_entity_type_uid", columnList = "entity_type,uid", unique = true),
		@Index(name = "idx_sys_system_entity_system_id", columnList = "system_id")
		})
public class SysSystemEntity extends AbstractEntity {
	
	private static final long serialVersionUID = -8243399066902498023L;

	@NotEmpty
	@Size(min = 1, max = DefaultFieldLengths.UID)
	@Column(name = "uid", length = DefaultFieldLengths.UID, nullable = false)
	private String uid;
	
	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "entity_type", nullable = false)
	private SystemEntityType entityType;
	
	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(name = "system_id", referencedColumnName = "id")
	private SysSystem system;

	public void setUid(String uid) {
		this.uid = uid;
	}
	
	public String getUid() {
		return uid;
	}

	public SystemEntityType getEntityType() {
		return entityType;
	}

	public void setEntityType(SystemEntityType entityType) {
		this.entityType = entityType;
	}

	public SysSystem getSystem() {
		return system;
	}

	public void setSystem(SysSystem system) {
		this.system = system;
	}
}
