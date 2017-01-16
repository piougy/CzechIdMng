package eu.bcvsolutions.idm.acc.entity;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import com.sun.istack.NotNull;

import eu.bcvsolutions.idm.acc.domain.ResultState;

/**
 * Provisioning operation result
 * 
 * @author Radek Tomiška
 *
 */
@Embeddable
public class SysProvisioningResult {

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "result_state", nullable = false, length = 45)
	private ResultState state = ResultState.CREATED;
	
	@Column(name = "result_code", length = 45)
	private String code;
	
	@Column(name = "result_cause", length = Integer.MAX_VALUE)
	private Throwable cause;
	
	public SysProvisioningResult() {
	}
	
	public SysProvisioningResult(ResultState state) {
		this.state = state;
	}
	
	private SysProvisioningResult(Builder builder) {
		state = builder.state;
		code = builder.code;
		cause = builder.cause;
	}

	public String getCode() {
		return code;
	}
	
	public void setCode(String code) {
		this.code = code;
	}

	public Throwable getCause() {
		return cause;
	}

	public void setCause(Throwable cause) {
		this.cause = cause;
	}

	public ResultState getState() {
		return state;
	}

	public void setState(ResultState state) {
		this.state = state;
	}
	
	/**
	 * {@link SysProvisioningResult} builder
	 * 
	 * @author Radek Tomiška
	 *
	 */
	public static class Builder {
		// required
		private final ResultState state;
		// optional	
		private String code;
		private Throwable cause;
		
		public Builder(ResultState state) {
			this.state = state;
		}

		public Builder setCause(Throwable cause) {
			this.cause = cause;
			return this;
		}
		
		public Builder setCode(String code) {
			this.code = code;
			return this;
		}
		
		public SysProvisioningResult build() {
			return new SysProvisioningResult(this);
		}
	}
	
}
