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
import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityFilter;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.eav.api.domain.BaseFaceType;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormInstanceDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.ecm.api.dto.IdmAttachmentDto;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.rpt.api.domain.RptResultCode;
import eu.bcvsolutions.idm.rpt.api.dto.RptReportDto;
import eu.bcvsolutions.idm.rpt.api.exception.ReportGenerateException;
import eu.bcvsolutions.idm.rpt.api.executor.AbstractReportExecutor;
import eu.bcvsolutions.idm.rpt.dto.RptIdentityWithFormValueDto;

/**
 * Report for identity with chosen eav
 *
 * @author Marek Klement
 */
@Component("identityEavExecutor")
@Description("Identities - report EAV attribute")
public class IdentityEavReportExecutor extends AbstractReportExecutor {

	public static final String REPORT_NAME = "identity-eav-report";
	//
	public static final String PARAMETER_EAV_VALUE = "EAV-VALUE";
	public static final String PARAMETER_FORM_DEFINITION = "FORM-DEFINITION";
	public static final String PARAMETER_EAV_CODE = "EAV";
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
				Serializable eavNameSerializable = formInstance.toSinglePersistentValue(PARAMETER_EAV_CODE);
				eavName = eavNameSerializable != null ? eavNameSerializable.toString() : null;

				Serializable eavValueSerializable = formInstance.toSinglePersistentValue(PARAMETER_EAV_VALUE);
				String eavValue = eavValueSerializable != null ? eavValueSerializable.toString() : null;

				Serializable disabledSerializable =
						formInstance.toSinglePersistentValue(IdmIdentityFilter.PARAMETER_DISABLED);
				String disabled = disabledSerializable != null ? disabledSerializable.toString() : null;

				UUID definitionUUID =
						UUID.fromString(formInstance.toSinglePersistentValue(PARAMETER_FORM_DEFINITION).toString());
				IdmFormDefinitionDto definition = formService.getDefinition(definitionUUID);

				if(!definition.getType().equals(IdmIdentity.class.getName())){
					throw new ResultCodeException(RptResultCode.REPORT_WRONG_DEFINITION,
							ImmutableMap.of("firstType", definition.getType(),"secondType", IdmIdentity.class.getName()));
				}
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
				boolean canContinue;
				for (IdmIdentityDto identity : identities) {
					try {
						writeValues(identity, eavName, eavValue, jGenerator, definition);
						canContinue = updateState();
						if (!canContinue) {
							break;
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

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

	private boolean writeValues(IdmIdentityDto identity, String eavName, String eavValue, JsonGenerator jGenerator,
							 IdmFormDefinitionDto definition) throws IOException {
		boolean ret = true;
		List<IdmFormValueDto> formValue;
		try {
			formValue = formService.getValues(identity, definition, eavName);
		} catch (Exception e){
			throw new ResultCodeException(RptResultCode.REPORT_NO_FORM_ATTRIBUTE, ImmutableMap.of("code",
					eavName));
		}
		//
		if (eavValue == null) {
			if (formValue != null) {
				if (formValue.size() != 0) {
					createData(identity, jGenerator, formValue);
					ret = updateState();
					counter++;
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
						ret = updateState();
						counter++;
					} else {
						count--;
					}
				} else {
					count--;
				}
			}
		}
		return ret;
	}

	private void createData(IdmIdentityDto identity, JsonGenerator jGenerator, List<IdmFormValueDto> formValue) throws IOException {
		RptIdentityWithFormValueDto row = new RptIdentityWithFormValueDto();
		row.setFirstName(identity.getFirstName());
		row.setDisabled(identity.isDisabled());
		row.setLastName(identity.getLastName());
		row.setTitleBefore(identity.getTitleBefore());
		row.setTitleAfter(identity.getTitleAfter());
		row.setUsername(identity.getUsername());
		row.setExternalCode(identity.getExternalCode());
		//
		List<String> formValues = new LinkedList<>();
		for (IdmFormValueDto idmFormValueDto : formValue) {
			formValues.add(idmFormValueDto.getValue().toString());
		}
		row.setFormValue(formValues);
		getMapper().writeValue(jGenerator, row);
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
				PARAMETER_FORM_DEFINITION,
				"Form definition",
				PersistentType.UUID);
		formDefinition.setFaceType(BaseFaceType.FORM_DEFINITION_SELECT);
		formDefinition.setPlaceholder("Select form definition...");
		formDefinition.setRequired(true);
		attributes.add(formDefinition);
		// eav code
		IdmFormAttributeDto eavName = new IdmFormAttributeDto(
				PARAMETER_EAV_CODE,
				"EAV attribute name",
				PersistentType.SHORTTEXT);
		eavName.setPlaceholder("Name of EAV attribute to report...");
		eavName.setRequired(true);
		attributes.add(eavName);
		//
		IdmFormAttributeDto value = new IdmFormAttributeDto(
				PARAMETER_EAV_VALUE,
				"Value of EAV attribute to report",
				PersistentType.SHORTTEXT);
		value.setPlaceholder("If not filled, all kinds of values will be in report...");
		attributes.add(value);
		return attributes;
	}
}
