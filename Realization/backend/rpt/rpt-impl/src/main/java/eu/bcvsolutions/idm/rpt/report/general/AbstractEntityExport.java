package eu.bcvsolutions.idm.rpt.report.general;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.MediaType;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.bulk.action.AbstractBulkAction;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.DefaultResultModel;
import eu.bcvsolutions.idm.core.api.dto.ResultModel;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.ecm.api.dto.IdmAttachmentDto;
import eu.bcvsolutions.idm.core.ecm.api.entity.AttachableEntity;
import eu.bcvsolutions.idm.core.ecm.api.service.AttachmentManager;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmLongRunningTaskDto;
import eu.bcvsolutions.idm.rpt.api.dto.RptReportDto;
import eu.bcvsolutions.idm.rpt.api.service.RptReportService;
import java.text.MessageFormat;

/**
 * Base implementation for reporting dto attributes to json. It uses standard bulk action interface to provide this functionality
 * for all entities which support it. It exports all dto attributes except for those, which are prefixed with undescore '_'.
 *
 * @author Peter Štrunc <peter.strunc@bcvsolutions.eu>
 *
 * @param <D> Type of supported dto
 * @param <F> Type of supported filter
 */
public class AbstractEntityExport <D extends AbstractDto, F extends BaseFilter> extends AbstractBulkAction<D, F> {

	public static final String  REPORT_NAME = "generic-entity-report";

	private final RptReportService reportService;
	private final AttachmentManager attachmentManager;
	private final ObjectMapper mapper;
	//
	private File tempFile;
	private JsonGenerator jsonGenerator;
	private UUID relatedReport;

	public AbstractEntityExport(RptReportService reportService,
								AttachmentManager attachmentManager, ObjectMapper mapper) {
		this.reportService = reportService;
		this.attachmentManager = attachmentManager;
		this.mapper = mapper;
	}
	
	@Override
	public String getName() {
		return REPORT_NAME;
	}

	@Override
	protected boolean start() {
		boolean start = super.start();
		createReport();
		return  start && relatedReport != null;
	}

	@Override
	protected OperationResult processDto(D dto) {
		final OperationResult result = new OperationResult();
		//
		Map<String, String> attributes = tramsformToMap(dto);
		//
		try {
			final JsonGenerator jGen = getJsonGenerator();
			jGen.writeObject(attributes);
			result.setState(OperationState.EXECUTED);
		} catch (IOException e) {
			result.setState(OperationState.EXCEPTION);
			result.setCause(e.getMessage());
		}
		//
		return result;
	}

	@SuppressWarnings("unchecked")
	protected Map<String, String> tramsformToMap(D dto) {
		Map<String, String> map = getMapper().convertValue(dto, Map.class);

		// remove all underscore attributes
		List<String> toRemove = map.keySet().stream().filter(key -> key.startsWith("_")).collect(Collectors.toList());
		toRemove.forEach(map::remove);
		//
		return map;
	}

	private JsonGenerator getJsonGenerator() throws IOException {
		if (jsonGenerator == null) {
			FileOutputStream outputStream = new FileOutputStream(getTempFile());
			jsonGenerator = getMapper().getFactory().createGenerator(outputStream, JsonEncoding.UTF8);
			jsonGenerator.writeStartArray();
		}
		return jsonGenerator;
	}

	@Override
	public ReadWriteDtoService<D, F> getService() {
		return null;
	}

	private RptReportDto finishReport(OperationResult result) throws FileNotFoundException {
		RptReportDto report = reportService.get(relatedReport);
		//
		FileInputStream fis = new FileInputStream(tempFile);
		IdmAttachmentDto attachment = createAttachment(report, fis);
		report.setData(attachment.getId());
		report.setResult(result);
		//
		return reportService.save(report);
	}

	protected void createReport() {
		RptReportDto report = new RptReportDto();
		report.setLongRunningTask(getLongRunningTaskId());
		report.setExecutorName(FormableEntityReportExecutor.NAME);
		report.setName(REPORT_NAME);

		this.relatedReport = reportService.save(report).getId();
	}

	private boolean close() {
		try {
			if (this.jsonGenerator != null && !jsonGenerator.isClosed()) {
				getJsonGenerator().writeEndArray();
				this.jsonGenerator.close();
			}
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	private ObjectMapper getMapper() {
		return this.mapper;
	}

	private File getTempFile() {
		if (this.tempFile == null) {
			tempFile = attachmentManager.createTempFile();
		}
		return this.tempFile;
	}

	protected IdmAttachmentDto createAttachment(RptReportDto report, InputStream jsonData) {
		IdmAttachmentDto attachmentDto = new IdmAttachmentDto();
		attachmentDto.setDescription(getDescription());
		attachmentDto.setName(getName());
		attachmentDto.setMimetype(MediaType.APPLICATION_JSON_UTF8.toString());
		attachmentDto.setInputData(jsonData);
		return attachmentManager.saveAttachment(report, attachmentDto);
	}

	@Override
	protected OperationResult end(OperationResult result, Exception ex) {
		final OperationResult superResult = super.end(result, ex);
		if (!close()) {
			superResult.setState(OperationState.EXCEPTION);
			superResult.setCause("Cannot close temp file");
		}
		//
		try {
			RptReportDto report = finishReport(superResult);
			// Adds attachment metadata to the operation result (for download attachment
			// directly from bulk action modal dialog).
			addAttachmentMetadata(result, report);
		} catch (FileNotFoundException e) {
			superResult.setState(OperationState.EXCEPTION);
			superResult.setCause(e.getMessage());
		}
		//
		return superResult;
	}
	
	/**
	 * Adds attachment metadata to the operation result (for download attachment
	 * directly from bulk action modal dialog).
	 * 
	 * @param result
	 */
	private void addAttachmentMetadata(OperationResult result, RptReportDto report) {

		IdmLongRunningTaskDto task = getLongRunningTaskService().get(getLongRunningTaskId());
		OperationResult taskResult = task.getResult();

		if (OperationState.EXECUTED == taskResult.getState()) {
			ResultModel model = new DefaultResultModel(CoreResultCode.LONG_RUNNING_TASK_PARTITIAL_DOWNLOAD,
					ImmutableMap.of(//
							AttachableEntity.PARAMETER_DOWNLOAD_URL,
							MessageFormat.format("rpt/reports/{0}/render?renderer=formable-entity-xlsx-renderer", report.getId()),
							AttachableEntity.PARAMETER_OWNER_ID, report.getId(), //
							AttachableEntity.PARAMETER_OWNER_TYPE, report.getClass().getName()//
					));//

			taskResult.setModel(model);
			getLongRunningTaskService().save(task);
		}
	}
	
	@Override
	public int getOrder() {
		return DEFAULT_ORDER + 5000;
	}
}
