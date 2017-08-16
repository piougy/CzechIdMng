package eu.bcvsolutions.idm.vs.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.envers.Audited;
import org.hibernate.validator.constraints.NotEmpty;

import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.domain.Disableable;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.eav.api.entity.FormableEntity;

/**
 * Accounts for virtual system
 * 
 * @author Svanda
 *
 */
@Entity
@Table(name = "vs_account", indexes = { @Index(name = "ux_vs_account_uid", columnList = "uid", unique = true) })
public class VsAccount extends AbstractEntity implements FormableEntity {

	private static final long serialVersionUID = 1L;

	/**
	 * UID - Unique identification of account
	 */
	@Audited
	@NotEmpty
	@Size(min = 1, max = DefaultFieldLengths.NAME)
	@Column(name = "uid", length = DefaultFieldLengths.NAME, nullable = false)
	private String uid;

	@Audited
	@NotNull
	@Column(name = "enable")
	private boolean enable;

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public boolean isEnable() {
		return enable;
	}

	public void setEnable(boolean enable) {
		this.enable = enable;
	}
	
}
