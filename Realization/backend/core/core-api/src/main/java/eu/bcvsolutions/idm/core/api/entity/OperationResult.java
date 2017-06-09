package eu.bcvsolutions.idm.core.api.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;

import com.google.common.base.Throwables;

import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.ResultModel;

/**
 * Universal operation result
 * 
 * @author Radek Tomiška
 */
@Embeddable
public class OperationResult implements Serializable {

	private static final long serialVersionUID = 1L;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "result_state", nullable = false, length = 45)
	private OperationState state = OperationState.CREATED;
	
	@Column(name = "result_code", length = DefaultFieldLengths.NAME)
	private String code;
	
	@Column(name = "result_model", length = Integer.MAX_VALUE)
	private ResultModel model;
	
	@Column(name = "result_cause")
	@Type(type = "org.hibernate.type.StringClobType")
	private String cause;
	
	public OperationResult() {
	}
	
	public OperationResult(OperationState state) {
		this.state = state;
	}
	
	private OperationResult(Builder builder) {
		state = builder.state;
		code = builder.code;
		if (builder.cause != null) {
			cause = Throwables.getStackTraceAsString(builder.cause);
		}
		model = builder.model;
	}

	public String getCode() {
		return code;
	}
	
	public void setCode(String code) {
		this.code = code;
	}

	public String getCause() {
		return cause;
	}

	public void setCause(String cause) {
		this.cause = cause;
	}

	public OperationState getState() {
		return state;
	}

	public void setState(OperationState state) {
		this.state = state;
	}
	
	public String getStackTrace() {
		return cause;
	}
	
	public void setModel(ResultModel model) {
		this.model = model;
	}
	
	public ResultModel getModel() {
		return model;
	}
	
	/**
	 * {@link OperationResult} builder
	 * 
	 * @author Radek Tomiška
	 */
	public static class Builder {
		// required
		private final OperationState state;
		// optional	
		private String code;
		private Throwable cause;
		private ResultModel model;
		
		public Builder(OperationState state) {
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
		
		public OperationResult build() {
			return new OperationResult(this);
		}
	}
	
}
