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

	@Column(name = "create_operation", nullable = false)
	private boolean createOperation = false;

	@Column(name = "update_operation", nullable = false)
	private boolean updateOperation = false;

	@Column(name = "delete_operation", nullable = false)
	private boolean deleteOperation = false;

	public boolean getCreateOperation() {
		return createOperation;
	}

	public void setCreateOperation(boolean createOperation) {
		this.createOperation = createOperation;
	}

	public boolean getUpdateOperation() {
		return updateOperation;
	}

	public void setUpdateOperation(boolean updateOperation) {
		this.updateOperation = updateOperation;
	}

	public boolean getDeleteOperation() {
		return deleteOperation;
	}

	public void setDeleteOperation(boolean deleteOperation) {
		this.deleteOperation = deleteOperation;
	}
}
