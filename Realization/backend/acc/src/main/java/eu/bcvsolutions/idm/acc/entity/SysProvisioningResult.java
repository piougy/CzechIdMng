package eu.bcvsolutions.idm.acc.entity;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import com.google.common.base.Throwables;
import com.sun.istack.NotNull;

import eu.bcvsolutions.idm.acc.domain.ResultState;
import eu.bcvsolutions.idm.core.api.dto.ResultModel;

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
	
	@Column(name = "result_model", length = Integer.MAX_VALUE)
	private ResultModel model;
	
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
		model = builder.model;
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
	
	public String getStackTrace() {
		if(cause == null) {
			return null;
		}
		return Throwables.getStackTraceAsString(cause);
	}
	
	public void setModel(ResultModel model) {
		this.model = model;
	}
	
	public ResultModel getModel() {
		return model;
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
		private ResultModel model;
		
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
		
		public Builder setModel(ResultModel model) {
			this.model = model;
			if (model != null) {
				this.code = model.getStatusEnum();
			}
			return this;
		}
		
		public SysProvisioningResult build() {
			return new SysProvisioningResult(this);
		}
	}
	
}
