package eu.bcvsolutions.idm.acc.entity;

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

import org.hibernate.envers.Audited;

import com.sun.istack.NotNull;

import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.domain.SystemOperationType;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;

/**
 * <i>SysSystemEntityHandling</i> is responsible for mapping attribute to entity
 * type and operations (Provisioning, Reconciliace, Synchronisation) to idm
 * entity
 * 
 * @author svandav
 *
 */
@Entity
@Table(name = "sys_system_entity_handling", indexes = {
		@Index(name = "ux_system_enth_types_sys", columnList = "entity_type,operation_type,object_class_id", unique = true) })
public class SysSystemEntityHandling extends AbstractEntity {

	private static final long serialVersionUID = -8492560756893726050L;

	@Audited
	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "entity_type", nullable = false)
	private SystemEntityType entityType;
	
	@Audited
	//@NotNull
	@ManyToOne(optional = true)
	@JoinColumn(name = "object_class_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey( name = "none" )
	private SysSchemaObjectClass objectClass;

	@Audited
	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "operation_type", nullable = false)
	private SystemOperationType operationType;

	public SystemEntityType getEntityType() {
		return entityType;
	}

	public void setEntityType(SystemEntityType entityType) {
		this.entityType = entityType;
	}

	public SystemOperationType getOperationType() {
		return operationType;
	}

	public void setOperationType(SystemOperationType operationType) {
		this.operationType = operationType;
	}

	public SysSystem getSystem() {
		if (objectClass == null) {
			return null;
		}
		return objectClass.getSystem();
	}

	public void setObjectClass(SysSchemaObjectClass objectClass) {
		this.objectClass = objectClass;
	}
	
	public SysSchemaObjectClass getObjectClass() {
		return objectClass;
	}
}
