package eu.bcvsolutions.idm.acc.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;

import org.joda.time.DateTime;

import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;

/**
 * Provisioning requests in the same batch. Any operation has request and batch.
 * 
 * @author Filip Mestanek
 * @author Radek Tomiška
 *
 */
@Entity
@Table(name = "sys_provisioning_batch", indexes = {
		@Index(name = "idx_sys_p_b_next", columnList = "next_attempt")
		})
public class SysProvisioningBatch extends AbstractEntity {

	private static final long serialVersionUID = -546573793473482877L;
	
	@Column(name = "next_attempt")
	private DateTime nextAttempt;

	public DateTime getNextAttempt() {
		return nextAttempt;
	}

	public void setNextAttempt(DateTime nextAttempt) {
		this.nextAttempt = nextAttempt;
	}
}