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

	@Column(name = "create", nullable = false)
	private Boolean create;

	@Column(name = "update", nullable = false)
	private Boolean update;

	@Column(name = "delete", nullable = false)
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

}
