package eu.bcvsolutions.idm.rpt.report.general;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.MediaType;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.bcvsolutions.idm.core.api.bulk.action.AbstractBulkAction;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.FormableDto;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormInstanceDto;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.ecm.api.dto.IdmAttachmentDto;
import eu.bcvsolutions.idm.core.ecm.api.service.AttachmentManager;
import eu.bcvsolutions.idm.rpt.api.dto.RptReportDto;
import eu.bcvsolutions.idm.rpt.api.service.RptReportService;

public abstract class AbstractFormableEntityExport<D extends FormableDto, F extends BaseFilter> extends AbstractBulkAction<D, F> {

	public static final String  REPORT_NAME = "generic-formable-entity-report";

	private final ReadWriteDtoService<D, F> service;

	private final RptReportService reportService;
	private final AttachmentManager attachmentManager;
	private final ObjectMapper mapper;
	private final FormService formService;

	//
	private File tempFile;
	private JsonGenerator jsonGenerator;
	private UUID relatedReport;

	public AbstractFormableEntityExport(ReadWriteDtoService<D, F> service, RptReportService reportService,
										AttachmentManager attachmentManager, ObjectMapper mapper, FormService formService) {
		this.service = service;
		this.reportService = reportService;
		this.attachmentManager = attachmentManager;
		this.mapper = mapper;
		this.formService = formService;
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
		List<IdmFormDefinitionDto> definitions = formService.getDefinitions(dto);
		List<IdmFormInstanceDto> formInstances = definitions.stream().map(d -> formService.getFormInstance(dto, d)).collect(Collectors.toList());
		dto.setEavs(formInstances);
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

	private Map<String, String> tramsformToMap(D dto) {

		Map<String, String> map = getMapper().convertValue(dto, Map.class);

		// remove all underscore attributes
		List<String> toRemove = map.keySet().stream().filter(key -> key.startsWith("_")).collect(Collectors.toList());
		toRemove.forEach(map::remove);
		//
		dto.getEavs().forEach(formInstance -> processFormInstance(map, formInstance, dto.getEavs().size() > 1));
		return map;
	}

	private void processFormInstance(Map<String, String> resultMap, IdmFormInstanceDto formInstance, boolean prefixEavsWithDefinitionCode) {
		Map<String, List<Serializable>> eavsWithValues = new HashMap<>();

		// fill existing values
		formInstance.getValues().forEach(val -> {
			final String eavName = getEavName(formInstance.getMappedAttribute(val.getFormAttribute()), formInstance, prefixEavsWithDefinitionCode);
			if (!eavsWithValues.containsKey(eavName)) {
				eavsWithValues.put(eavName, new ArrayList<>());
			}
			eavsWithValues.get(eavName).add(val.getValue());
		});

		//fill other attributes with empty values
		formInstance.getFormDefinition().getFormAttributes().stream()
				.map(attr -> getEavName(attr, formInstance, prefixEavsWithDefinitionCode))
				.filter(attr -> !eavsWithValues.containsKey(attr))
				.forEach(attr -> eavsWithValues.put(attr, new ArrayList<>()));

		// transform values to result
		eavsWithValues.keySet().forEach(attr -> {
			List<Serializable> values = eavsWithValues.get(attr);
			if (values.isEmpty()) {
				resultMap.put(attr, "");
			} else {
				resultMap.put(attr, String.valueOf(values.size() > 1 ? values : values.get(0)));
			}
		});
	}

	protected String getEavName(IdmFormAttributeDto mappedAttribute,IdmFormInstanceDto formInstance, boolean prefixEavsWithDefinitionCode) {
		return prefixEavsWithDefinitionCode ? formInstance.getFormDefinition().getCode() + "_" + mappedAttribute.getCode() : mappedAttribute.getCode();
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
		return service;
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
			finishReport(superResult);
		} catch (FileNotFoundException e) {
			superResult.setState(OperationState.EXCEPTION);
			superResult.setCause(e.getMessage());
		}
		//
		return superResult;
	}

	private void finishReport(OperationResult result) throws FileNotFoundException {
		RptReportDto report = reportService.get(relatedReport);
		//
		FileInputStream fis = new FileInputStream(tempFile);
		IdmAttachmentDto attachment = createAttachment(report, fis);
		report.setData(attachment.getId());
		report.setResult(result);
		//
		reportService.save(report);
	}

	protected void createReport() {
		RptReportDto report = new RptReportDto();
		report.setLongRunningTask(getLongRunningTaskId());
		report.setExecutorName(FromableEntityReportExecutor.NAME);
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
	public String getName() {
		return REPORT_NAME;
	}
}
