package eu.bcvsolutions.idm.acc.dto;

import com.google.common.base.Throwables;

import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.ResultModel;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;

/**
 * Operation result DTO for {@link OperationResult}
 * 
 * @author Radek Tomiška
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

public class OperationResultDto {

	private OperationState state = OperationState.CREATED;
	private String code;
	private ResultModel model;
	private String cause;
	
	public OperationResultDto() {
	}
	
	public OperationResultDto(OperationState state) {
		this.state = state;
	}
	
	private OperationResultDto(Builder builder) {
		state = builder.state;
		code = builder.code;
		if (builder.cause != null) {
			cause = Throwables.getStackTraceAsString(builder.cause);
		}
		model = builder.model;
	}

	public OperationState getState() {
		return state;
	}

	public void setState(OperationState state) {
		this.state = state;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public ResultModel getModel() {
		return model;
	}

	public void setModel(ResultModel model) {
		this.model = model;
	}

	public String getCause() {
		return cause;
	}

	public void setCause(String cause) {
		this.cause = cause;
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
		
		public OperationResultDto build() {
			return new OperationResultDto(this);
		}
	}
}
