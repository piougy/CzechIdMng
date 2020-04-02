package eu.bcvsolutions.idm.core.api.dto.filter;

import java.util.UUID;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.domain.RequestOperationType;
import eu.bcvsolutions.idm.core.api.dto.IdmExportImportDto;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;

/**
 * Filter for import log
 * 
 * @author Vít Švanda
 *
 */
public class IdmImportLogFilter extends DataFilter {

	
	public static final String PARAMETER_BATCH_ID = "batchId";
	public static final String PARAMETER_PARENT = "parent";
	public static final String PARAMETER_DTO_ID = "dtoId";
	public static final String PARAMETER_ROOTS = "roots";
	public static final String PARAMETER_OPERATION = "operation";
	public static final String PARAMETER_OPERATION_STATE = "operationState";
	

	public UUID getBatchId() {
		return DtoUtils.toUuid(data.getFirst(PARAMETER_BATCH_ID));
	}

	public void setBatchId(UUID batchId) {
		data.set(PARAMETER_BATCH_ID, batchId);
	}

	public UUID getParent() {
		return DtoUtils.toUuid(data.getFirst(PARAMETER_PARENT));
	}
 
	public void setParent(UUID parentId) {
		data.set(PARAMETER_PARENT, parentId);
	}

	public UUID getDtoId() {
		return DtoUtils.toUuid(data.getFirst(PARAMETER_DTO_ID));
	}

	public void setDtoId(UUID dtoId) {
		data.set(PARAMETER_DTO_ID, dtoId);
	}

	public Boolean getRoots() {
		return getParameterConverter().toBoolean(data, PARAMETER_ROOTS);
	}

	public void setRoots(Boolean roots) {
		data.set(PARAMETER_ROOTS, roots);
	}
	
	public void setOperationState(OperationState operation) {
		data.set(PARAMETER_OPERATION_STATE, operation);
	}
	
	public OperationState getOperationState() {
		return getParameterConverter().toEnum(data, PARAMETER_OPERATION_STATE, OperationState.class);
	}
	
	public void setOperation(RequestOperationType operation) {
		data.set(PARAMETER_OPERATION, operation);
	}
	
	public RequestOperationType getOperation() {
		return getParameterConverter().toEnum(data, PARAMETER_OPERATION, RequestOperationType.class);
	}

	public IdmImportLogFilter() {
		this(new LinkedMultiValueMap<>());
	}

	public IdmImportLogFilter(MultiValueMap<String, Object> data) {
		super(IdmExportImportDto.class, data);
	}

}
