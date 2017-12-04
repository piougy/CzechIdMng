package eu.bcvsolutions.idm.acc.dto;

import java.io.Serializable;

import javax.persistence.Embeddable;

import eu.bcvsolutions.idm.acc.entity.SysBlockedOperation;

/**
 * DTO for {@link SysBlockedOperation}
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Embeddable
public class SysBlockedOperationDto implements Serializable {

	private static final long serialVersionUID = -4513864456041703836L;

	private boolean createOperation;
	private boolean updateOperation;
	private boolean deleteOperation;

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

	public void blockCreate() {
		this.setCreateOperation(true);
	}
	
	public void blockUpdate() {
		this.setUpdateOperation(true);	
	}
	
	public void blockDelete() {
		this.setDeleteOperation(true);
	}
}
