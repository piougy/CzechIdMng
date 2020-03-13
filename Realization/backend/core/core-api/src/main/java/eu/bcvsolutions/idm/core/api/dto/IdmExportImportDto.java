package eu.bcvsolutions.idm.core.api.dto;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import org.springframework.hateoas.core.Relation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.domain.ExportImportType;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.ecm.api.dto.IdmAttachmentDto;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmLongRunningTaskDto;

/**
 * Export and import IdM data
 * - Data as zip object is stored in attachment manager
 * 
 * @author Vít Švanda
 *
 */
@Relation(collectionRelation = "exports")
public class IdmExportImportDto extends AbstractDto {

	private static final long serialVersionUID = 1L;
	//
	private ExportImportType type;
	private String name; // user friendly report name
	private String executorName; // executor's name
	@JsonProperty(access = Access.READ_ONLY)
	@Embedded(dtoClass = IdmAttachmentDto.class)
	private UUID data; // data are stored in attachment
	@JsonProperty(access = Access.READ_ONLY)
	private OperationResult result; // result from LRT
	@JsonProperty(access = Access.READ_ONLY)
	@Embedded(dtoClass = IdmLongRunningTaskDto.class)
	private UUID longRunningTask;
	@JsonIgnore
	private Path tempDirectory;
	@JsonIgnore
	private List<BaseDto> exportedDtos; // Only temp data for export (not for serializing)
	@JsonProperty(access = Access.READ_ONLY)
	// Deserialized importLog
	private ImportContext importContext;
	
	// Export descriptors. Keeps order of export and additional informations useful
	// for import. Are serialized in export batch (as manifest), but is not persist
	// in DB.
	private List<ExportDescriptorDto> exportOrder;

	public IdmExportImportDto() {
	}
	
	public IdmExportImportDto(UUID id) {
		super(id);
	}
	
	public ExportImportType getType() {
		return type;
	}

	public void setType(ExportImportType type) {
		this.type = type;
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

	public Path getTempDirectory() {
		return tempDirectory;
	}

	public void setTempDirectory(Path tempDirectory) {
		this.tempDirectory = tempDirectory;
	}

	public List<BaseDto> getExportedDtos() {
		if (exportedDtos == null) {
			exportedDtos = Lists.newArrayList();
		}
		return exportedDtos;
	}

	public List<ExportDescriptorDto> getExportOrder() {
		if (exportOrder == null) {
			exportOrder = Lists.newArrayList();
		}
		return exportOrder;
	}

	public ImportContext getImportContext() {
		return importContext;
	}

	public void setImportContext(ImportContext importContext) {
		this.importContext = importContext;
	}
	
}
