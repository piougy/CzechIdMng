package eu.bcvsolutions.idm.rpt.report.identity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonGenerator;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityFilter;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.eav.api.domain.BaseFaceType;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormInstanceDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.ecm.api.dto.IdmAttachmentDto;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.rpt.api.dto.RptReportDto;
import eu.bcvsolutions.idm.rpt.api.exception.ReportGenerateException;
import eu.bcvsolutions.idm.rpt.api.executor.AbstractReportExecutor;

@Component("identityEavExecutor")
@Description("Identities - report EAV attribute")
public class IdentityEavReportExecutor extends AbstractReportExecutor {

	public static final String REPORT_NAME = "identity-eav-report";
	//
	public static final String ATTRIBUTE_FNAME = "firstName";
	public static final String ATTRIBUTE_LNAME = "lastName";
	public static final String ATTRIBUTE_USERNAME = "username";
	public static final String ATTRIBUTE_DISABLED = "disabled";
	public static final String ATTRIBUTE_BTITLE = "titleBefore";
	public static final String ATTRIBUTE_ATITLE = "titleAfter";
	public static final String ATTRIBUTE_EXTERNAL_CODE = "personalNumber";
	//
	public static final String EAV_VALUE = "EAV-VALUE";
	public static final String FORM_DEFINITION = "FORM-DEFINITION";
	public static final String EAV_CODE = "EAV";
	//
	private String eavName;

	@Autowired
	private IdmIdentityService identityService;
	@Autowired
	private FormService formService;

	@Override
	protected IdmAttachmentDto generateData(RptReportDto report) {
		File temp = null;
		FileOutputStream outputStream = null;
		try {
			// prepare temp file for json stream
			temp = getAttachmentManager().createTempFile();
			outputStream = new FileOutputStream(temp);
			// write into json stream
			JsonGenerator jGenerator = getMapper().getFactory().createGenerator(outputStream, JsonEncoding.UTF8);
			try {
				jGenerator.writeStartArray();
				IdmFormInstanceDto formInstance = new IdmFormInstanceDto(report, getFormDefinition(),
						report.getFilter());
				counter = 0L;
				// take configuration
				Serializable eavNameSerializable = formInstance.toSinglePersistentValue(EAV_CODE);
				eavName = eavNameSerializable != null ? eavNameSerializable.toString() : null;

				Serializable eavValueSerializable = formInstance.toSinglePersistentValue(EAV_VALUE);
				String eavValue = eavValueSerializable != null ? eavValueSerializable.toString() : null;

				Serializable disabledSerializable =
						formInstance.toSinglePersistentValue(IdmIdentityFilter.PARAMETER_DISABLED);
				String disabled = disabledSerializable != null ? disabledSerializable.toString() : null;

				UUID definitionUUID =
						UUID.fromString(formInstance.toSinglePersistentValue(FORM_DEFINITION).toString());
				IdmFormDefinitionDto definition = formService.getDefinition(definitionUUID);

				IdmIdentityFilter fltr = new IdmIdentityFilter();
				if (disabled != null) {
					fltr.setDisabled(Boolean.valueOf(disabled));
				}
				List<IdmIdentityDto> identities =
						identityService.find(fltr, null, IdmBasePermission.READ).getContent();
				//
				if (count == null) {
					// report extends long running task - show progress by count and counter lrt attributes
					count = (long) identities.size();
				}
				//
				identities.forEach(identity -> {
					try {
						writeValues(identity, eavName, eavValue, jGenerator, definition);
					} catch (IOException e) {
						e.printStackTrace();
					}
				});

				jGenerator.writeEndArray();
			} finally {
				// close json stream
				jGenerator.close();
			}
			// save create temp file with array of identities in json as attachment
			return createAttachment(report, new FileInputStream(temp));
		} catch (IOException ex) {
			throw new ReportGenerateException(report.getName(), ex);
		} finally {
			IOUtils.closeQuietly(outputStream); // just for sure - jGenerator should close stream itself
			FileUtils.deleteQuietly(temp);
		}
	}

	public String getEavName() {
		return eavName;
	}

	private void writeValues(IdmIdentityDto identity, String eavName, String eavValue, JsonGenerator jGenerator,
							 IdmFormDefinitionDto definition) throws IOException {
		List<IdmFormValueDto> formValue = formService.getValues(identity, definition, eavName);
		//
		String value = null;
		if (eavValue == null) {
			if (formValue != null) {
				if (formValue.size() != 0) {
					createData(identity, jGenerator, formValue);
				} else {
					count--;
				}
			}
		} else {
			if (formValue != null) {
				if (formValue.size() != 0) {
					List<IdmFormValueDto> listOfValues = new LinkedList<>();
					for (IdmFormValueDto val : formValue) {
						if (val.getValue().toString().equals(eavValue)) {
							listOfValues.add(val);
						}
					}
					if (listOfValues.size() > 0) {
						createData(identity, jGenerator, listOfValues);
					} else {
						count--;
					}
				} else {
					count--;
				}
			}
		}
	}

	private void createData(IdmIdentityDto identity, JsonGenerator jGenerator, List<IdmFormValueDto> formValue) throws IOException {
		jGenerator.writeStartObject();
		jGenerator.writeObjectField(ATTRIBUTE_BTITLE, identity.getTitleBefore());
		jGenerator.writeObjectField(ATTRIBUTE_FNAME, identity.getFirstName());
		jGenerator.writeObjectField(ATTRIBUTE_LNAME, identity.getLastName());
		jGenerator.writeObjectField(ATTRIBUTE_ATITLE, identity.getTitleAfter());
		jGenerator.writeObjectField(ATTRIBUTE_USERNAME, identity.getUsername());
		jGenerator.writeObjectField(ATTRIBUTE_EXTERNAL_CODE, identity.getExternalCode());
		jGenerator.writeBooleanField(ATTRIBUTE_DISABLED, identity.isDisabled());
		//
		jGenerator.writeFieldName(eavName);
		jGenerator.writeStartArray();
		for (IdmFormValueDto idmFormValueDto : formValue) {
			jGenerator.writeObject(idmFormValueDto.getValue());
		}
		jGenerator.writeEndArray();
		jGenerator.writeEndObject();
		counter++;
	}


	/**
	 * Report ~ executor name
	 */
	@Override
	public String getName() {
		return REPORT_NAME;
	}

	@Override
	public List<IdmFormAttributeDto> getFormAttributes() {
		List<IdmFormAttributeDto> attributes = new LinkedList<>();
		// Disabled
		IdmFormAttributeDto disabled = new IdmFormAttributeDto(
				IdmIdentityFilter.PARAMETER_DISABLED,
				"Disabled identities",
				PersistentType.BOOLEAN);
		disabled.setFaceType(BaseFaceType.BOOLEAN_SELECT);
		disabled.setPlaceholder("All identities or select ...");
		attributes.add(disabled);
		// form definition
		IdmFormAttributeDto formDefinition = new IdmFormAttributeDto(
				FORM_DEFINITION,
				"Form definition",
				PersistentType.UUID);
		formDefinition.setFaceType(BaseFaceType.FORM_DEFINITION_SELECT);
		formDefinition.setPlaceholder("Select form definition...");
		formDefinition.setRequired(true);
		attributes.add(formDefinition);
		// eav code
		IdmFormAttributeDto eavName = new IdmFormAttributeDto(
				EAV_CODE,
				"EAV attribute name",
				PersistentType.SHORTTEXT);
		eavName.setPlaceholder("Name of EAV attribute to report...");
		eavName.setRequired(true);
		attributes.add(eavName);
		//
		IdmFormAttributeDto value = new IdmFormAttributeDto(
				EAV_VALUE,
				"Value of EAV attribute to report",
				PersistentType.SHORTTEXT);
		value.setPlaceholder("If not filled, all kinds of values will be in report...");
		attributes.add(value);
		return attributes;
	}
}
