package eu.bcvsolutions.idm.rpt.api.dto;

import java.util.UUID;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.hateoas.core.Relation;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDto;
import eu.bcvsolutions.idm.core.ecm.api.dto.IdmAttachmentDto;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmLongRunningTaskDto;

/**
 * IdM report data ~ generated report
 * 
 * @author Radek Tomiška
 *
 */
@Relation(collectionRelation = "reports")
public class RptReportDto extends AbstractDto {

	private static final long serialVersionUID = 1L;
	//
	private String name; // user friendly report name
	@NotEmpty
	private String executorName; // executor's name
	@JsonProperty(access = Access.READ_ONLY)
	@Embedded(dtoClass = IdmAttachmentDto.class)
	private UUID data; // json report data are stored in attachment
	@JsonProperty(access = Access.READ_ONLY)
	private OperationResult result; // result from LRT
	@JsonProperty(access = Access.READ_ONLY)
	@Embedded(dtoClass = IdmLongRunningTaskDto.class)
	private UUID longRunningTask;
	private IdmFormDto filter;
	// TODO: columns - lists or idmForm?

	public RptReportDto() {
	}
	
	public RptReportDto(UUID id) {
		super(id);
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void setExecutorName(String executorName) {
		this.executorName = executorName;
	}
	
	public String getExecutorName() {
		return executorName;
	}
	
	public UUID getData() {
		return data;
	}

	public void setData(UUID data) {
		this.data = data;
	}
	
	public void setResult(OperationResult result) {
		this.result = result;
	}
	
	public OperationResult getResult() {
		return result;
	}

	public void setLongRunningTask(UUID longRunningTask) {
		this.longRunningTask = longRunningTask;
	}
	
	public UUID getLongRunningTask() {
		return longRunningTask;
	}
	
	public IdmFormDto getFilter() {
		return filter;
	}
	
	public void setFilter(IdmFormDto filter) {
		this.filter = filter;
	}
}
