package eu.bcvsolutions.idm.acc.entity;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;

/**
 * Schema attributes used in provisioning archive or operation.
 * - flags updated (created) or removed attribute (~if all attribute values was null or empty)
 * - attribute name is schema attribute name ({@link SysSchemaAttribute})
 * 
 * @author Radek Tomiška
 * @since 9.6.3
 */
@Entity
@Table(name = "sys_provisioning_attribute", indexes = {
		@Index(name = "idx_sys_prov_attr_oper_id", columnList = "provisioning_id"),
		@Index(name = "idx_sys_prov_attr_name", columnList = "name")
		})
public class SysProvisioningAttribute extends AbstractEntity {

	private static final long serialVersionUID = 1L;
	
	@NotNull
	@Column(name = "provisioning_id", length = 16, nullable = false)
	private UUID provisioningId; // archive or operation id
	
	@Size(min = 1, max = DefaultFieldLengths.NAME)
	@Column(name = "name", length = DefaultFieldLengths.NAME, nullable = false)
	private String name; // schema attribute name
	
	@Column(name = "removed", nullable = false)
	private boolean removed = false; // if all attribute values was null or empty
	
	public SysProvisioningAttribute() {
	}
	
	public SysProvisioningAttribute(UUID provisioningId, String name) {
		this.provisioningId = provisioningId;
		this.name = name;
	}
	
	/**
	 * Archive or operation identifier.
	 * 
	 * @see SysProvisioningOperation
	 * @see SysProvisioningArchive
	 * @return
	 */
	public UUID getProvisioningId() {
		return provisioningId;
	}
	
	/**
	 * Archive or operation identifier.
	 * 
	 * @see SysProvisioningOperation
	 * @see SysProvisioningArchive
	 * @param provisioningId
	 */
	public void setProvisioningId(UUID provisioningId) {
		this.provisioningId = provisioningId;
	}

	/**
	 * Schema attribute name.
	 * 
	 * @see SysSchemaAttribute#getName()
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * Schema attribute name.
	 * 
	 * @see SysSchemaAttribute#getName()
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Attribute will be removed on the target system => empty value was provided in provisioning context.
	 * 
	 * @return if all attribute values was null or empty
	 */
	public boolean isRemoved() {
		return removed;
	}

	/**
	 * Attribute will be removed on the target system => empty value was provided in provisioning context.
	 * 
	 * @param removed
	 */
	public void setRemoved(boolean removed) {
		this.removed = removed;
	}
}
