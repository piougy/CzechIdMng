package eu.bcvsolutions.idm.core.api.dto;

import java.io.Serializable;

import com.google.common.base.Throwables;

import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;

/**
 * Universal operation result
 * 
 * @author Radek Tomiška
 * @author svandav
 */
public class OperationResultDto implements Serializable {

	private static final long serialVersionUID = 1L;

	private OperationState state = OperationState.CREATED;
	private String code;
	private ResultModel model;
	private String cause;
	private transient Throwable exception;
	
	public OperationResultDto() {
	}
	
	public OperationResultDto(OperationState state) {
		this.state = state;
	}
	
	private OperationResultDto(Builder builder) {
		state = builder.state;
		code = builder.code;
		if (builder.cause != null) {
			exception = builder.cause;
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
	
	public Throwable getException() {
		return exception;
	}
	
	/**
	 * {@link OperationResultDto} builder
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
		
		public Builder setException(ResultCodeException ex) {
			this.setCause(ex);
			this.setModel(ex == null || ex.getError() == null ? null : ex.getError().getError());
			return this;
		}
		
		public OperationResultDto build() {
			return new OperationResultDto(this);
		}
	}
	
}
