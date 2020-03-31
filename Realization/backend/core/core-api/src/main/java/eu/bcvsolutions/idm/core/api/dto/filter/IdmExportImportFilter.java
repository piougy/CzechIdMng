package eu.bcvsolutions.idm.core.api.dto.filter;

import java.time.ZonedDateTime;
import java.util.UUID;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.domain.ExportImportType;
import eu.bcvsolutions.idm.core.api.dto.IdmExportImportDto;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;

/**
 * Filter for export and import data
 * 
 * @author Vít Švanda
 *
 */
public class IdmExportImportFilter extends DataFilter {
	
	public final static String PARAMETER_LONG_RUNNING_TASK_ID = "longRunningTaskId"; 
	//
	private ZonedDateTime from;
	private ZonedDateTime till;
	private ExportImportType type;

	public IdmExportImportFilter() {
		this(new LinkedMultiValueMap<>());
	}
	
	public IdmExportImportFilter(MultiValueMap<String, Object> data) {
		super(IdmExportImportDto.class, data);
	}
	
	public ZonedDateTime getFrom() {
		return from;
	}

	public void setFrom(ZonedDateTime from) {
		this.from = from;
	}

	public ZonedDateTime getTill() {
		return till;
	}

	public void setTill(ZonedDateTime till) {
		this.till = till;
	}
	
	public ExportImportType getType() {
		return type;
	}

	public void setType(ExportImportType type) {
		this.type = type;
	}

	public UUID getLongRunningTaskId() {
		return DtoUtils.toUuid(data.getFirst(PARAMETER_LONG_RUNNING_TASK_ID));
	}
	
	public void setLongRunningTaskId(UUID longRunningTaskId) {
		data.set(PARAMETER_LONG_RUNNING_TASK_ID, longRunningTaskId);
	}
}
