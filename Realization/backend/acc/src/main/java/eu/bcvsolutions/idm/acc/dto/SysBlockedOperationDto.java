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

	private Boolean create;
	private Boolean update;
	private Boolean delete;

	public Boolean getCreate() {
		return create;
	}

	public void setCreate(Boolean create) {
		this.create = create;
	}

	public Boolean getUpdate() {
		return update;
	}

	public void setUpdate(Boolean update) {
		this.update = update;
	}

	public Boolean getDelete() {
		return delete;
	}

	public void setDelete(Boolean delete) {
		this.delete = delete;
	}

	public void blockCreate() {
		this.setCreate(Boolean.TRUE);
	}
	
	public void blockUpdate() {
		this.setUpdate(Boolean.TRUE);	
	}
	
	public void blockDelete() {
		this.setDelete(Boolean.TRUE);
	}
}
