package eu.bcvsolutions.idm.acc.entity;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sun.istack.NotNull;

import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;

/**
 * Single provisioning request. Any operation has request and batch.
 * 
 * @author Filip Mestanek
 * @author Radek Tomiška
 *
 */
@Entity
@Table(name = "sys_provisioning_request")
public class SysProvisioningRequest extends AbstractEntity {

	private static final long serialVersionUID = -783887291617766366L;

	@Column(name = "current_attempt")
	private int currentAttempt = 0;

	@Column(name = "max_attempts")
	private int maxAttempts;

	@JsonIgnore
	@NotNull
	@OneToOne(optional = false)
	@JoinColumn(name = "provisioning_operation_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey( name = "none" )
	private SysProvisioningOperation operation;

	@Embedded
	private OperationResult result;

	@NotNull
	@ManyToOne
	@JoinColumn(name = "provisioning_batch_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey( name = "none" )
	private SysProvisioningBatch batch;

	protected SysProvisioningRequest() {
	}

	public SysProvisioningRequest(SysProvisioningOperation operation) {
		this.operation = operation;
	}

	public int getCurrentAttempt() {
		return currentAttempt;
	}

	public void setCurrentAttempt(int attempt) {
		this.currentAttempt = attempt;
	}

	public void increaseAttempt() {
		this.currentAttempt++;
	}

	public int getMaxAttempts() {
		return maxAttempts;
	}

	public void setMaxAttempts(int maxAttempts) {
		this.maxAttempts = maxAttempts;
	}

	public SysProvisioningOperation getOperation() {
		return operation;
	}
	
	public void setOperation(SysProvisioningOperation operation) {
		this.operation = operation;
	}

	public SysSystem getSystem() {
		return operation.getSystem();
	}

	public SysProvisioningBatch getBatch() {
		return batch;
	}

	public void setBatch(SysProvisioningBatch batch) {
		if (Objects.equals(this.batch, batch)) {
			return;
		}

		this.batch = batch;
		batch.addRequest(this);
	}
	
	public OperationResult getResult() {
		return result;
	}
	
	public void setResult(OperationResult result) {
		this.result = result;
	}
	
	public DateTime getNextAttempt() {
		if (batch == null) {
			return null;
		}
		return batch.getNextAttempt();
	}
}