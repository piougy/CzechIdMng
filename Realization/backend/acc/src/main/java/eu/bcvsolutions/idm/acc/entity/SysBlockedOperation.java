package eu.bcvsolutions.idm.acc.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.hibernate.envers.Audited;

/**
 * Information about blocked operation for system
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Audited
@Embeddable
public class SysBlockedOperation implements Serializable {

	private static final long serialVersionUID = 8236744385812335134L;

	@Column(name = "create_operation")
	private Boolean createOperation;

	@Column(name = "update_operation")
	private Boolean updateOperation;

	@Column(name = "delete_operation")
	private Boolean deleteOperation;

	public Boolean getCreateOperation() {
		return createOperation;
	}

	public void setCreateOperation(Boolean createOperation) {
		this.createOperation = createOperation;
	}

	public Boolean getUpdateOperation() {
		return updateOperation;
	}

	public void setUpdateOperation(Boolean updateOperation) {
		this.updateOperation = updateOperation;
	}

	public Boolean getDeleteOperation() {
		return deleteOperation;
	}

	public void setDeleteOperation(Boolean deleteOperation) {
		this.deleteOperation = deleteOperation;
	}
}
