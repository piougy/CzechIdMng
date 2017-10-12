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

	private Boolean createOperation;
	private Boolean updateOperation;
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

	public void blockCreate() {
		this.setCreateOperation(Boolean.TRUE);
	}
	
	public void blockUpdate() {
		this.setUpdateOperation(Boolean.TRUE);	
	}
	
	public void blockDelete() {
		this.setDeleteOperation(Boolean.TRUE);
	}
}
