package eu.bcvsolutions.idm.core.api.dto.filter;

import java.util.UUID;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.domain.ExportImportType;
import eu.bcvsolutions.idm.core.api.dto.IdmExportImportDto;
import eu.bcvsolutions.idm.core.api.utils.ParameterConverter;

/**
 * Filter for export and import data
 * 
 * @author Vít Švanda
 *
 */
public class IdmExportImportFilter extends DataFilter {
	
	public final static String PARAMETER_LONG_RUNNING_TASK_ID = "longRunningTaskId"; 
	//
	private ExportImportType type;

	public IdmExportImportFilter() {
		this(new LinkedMultiValueMap<>());
	}
	
	public IdmExportImportFilter(MultiValueMap<String, Object> data) {
		this(data, null);
	}
	
	public IdmExportImportFilter(MultiValueMap<String, Object> data, ParameterConverter parameterConverter) {
		super(IdmExportImportDto.class, data, parameterConverter);
	}
	
	public ExportImportType getType() {
		return type;
	}

	public void setType(ExportImportType type) {
		this.type = type;
	}

	public UUID getLongRunningTaskId() {
		return getParameterConverter().toUuid(getData(), PARAMETER_LONG_RUNNING_TASK_ID);
	}
	
	public void setLongRunningTaskId(UUID longRunningTaskId) {
		set(PARAMETER_LONG_RUNNING_TASK_ID, longRunningTaskId);
	}
}
