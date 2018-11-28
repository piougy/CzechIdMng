package eu.bcvsolutions.idm.example.bulk.action.impl;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.bulk.action.AbstractBulkAction;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.IdentityState;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.DefaultResultModel;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityFilter;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.ecm.api.dto.IdmAttachmentDto;
import eu.bcvsolutions.idm.core.ecm.api.entity.AttachableEntity;
import eu.bcvsolutions.idm.core.ecm.api.service.AttachmentManager;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity_;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmLongRunningTaskDto;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;
import eu.bcvsolutions.idm.example.ExampleModuleDescriptor;

/**
 * Example export bulk action, that export username, personal number and state.
 * Result will be able to download via long running task controller.
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Enabled(ExampleModuleDescriptor.MODULE_ID)
@Component("identityUsernameExportBulkAction")
@Description("Example bulk action for export identity username state and personal number.")
public class IdentityUsernameExportBulkAction extends AbstractBulkAction<IdmIdentityDto, IdmIdentityFilter> {

	public static final String BULK_ACTION_NAME = "identity-username-export-bulk-action";

	private static final String SPLIT_CHARACTER_CODE = "splitCharacter";
	private static final String SPLIT_CHARACTER_DEFAULT_VALUE = ";";

	private StringBuilder result;
	
	@Autowired
	private IdmIdentityService identityService;
	@Autowired
	private AttachmentManager attachmentManager;

	@Override
	protected OperationResult processEntities(Collection<UUID> entitiesId) {
		initStringBuilder();

		return super.processEntities(entitiesId);
	}

	@Override
	protected OperationResult processDto(IdmIdentityDto dto) {
		String splitCharacter = getSplitCharacter();
		StringBuilder line = new StringBuilder();

		// Username
		line.append(StringUtils.trimToEmpty(dto.getUsername()));
		line.append(splitCharacter);

		// External code / personal number
		line.append(StringUtils.trimToEmpty(dto.getExternalCode()));
		line.append(splitCharacter);

		// Identity state
		IdentityState state = dto.getState();
		line.append(StringUtils.trimToEmpty(state != null ? state.name() : null));

		result.append(line.toString());
		result.append(System.lineSeparator());

		return new OperationResult.Builder(OperationState.EXECUTED).build();
	}

	@Override
	protected OperationResult end(OperationResult result, Exception ex) {
		if (ex != null) {
			return super.end(result, ex);
		}

		IdmLongRunningTaskDto runningTaskDto = this.getLongRunningTaskService().get(this.getLongRunningTaskId());
		IdmAttachmentDto attachmentDto = new IdmAttachmentDto();
		ByteArrayInputStream stream;
		try {
			stream = new ByteArrayInputStream(this.result.toString().getBytes(StandardCharsets.UTF_8.toString()));
		} catch (UnsupportedEncodingException e) {
			return new OperationResult.Builder(OperationState.EXCEPTION).setCause(e).build();
		}

		attachmentDto.setAttachmentType("csv");
		attachmentDto.setInputData(stream);
		attachmentDto.setEncoding(AttachableEntity.DEFAULT_ENCODING);
		attachmentDto.setMimetype(AttachableEntity.DEFAULT_MIMETYPE);
		attachmentDto.setName(getName());
		attachmentDto = attachmentManager.saveAttachment(runningTaskDto, attachmentDto);

		Map<String, Object> parameters = new HashMap<>();
		parameters.put(AttachableEntity.PARAMETER_ATTACHMENT_ID, attachmentDto.getId());
	
		DefaultResultModel resultModel = new DefaultResultModel(CoreResultCode.LONG_RUNNING_TASK_PARTITIAL_DOWNLOAD, parameters);
		OperationResult operationResult = new OperationResult.Builder(OperationState.EXECUTED).setModel(resultModel).build();

		super.end(operationResult, ex);

		// Save LRT with given operation result, because in parent end is resaved
		runningTaskDto.setRunning(false);
		runningTaskDto.setResult(operationResult);
		runningTaskDto = this.getLongRunningTaskService().save(runningTaskDto);

		return operationResult;
	}

	@Override
	public List<IdmFormAttributeDto> getFormAttributes() {
		List<IdmFormAttributeDto> attributes = super.getFormAttributes();
		attributes.add(getSplitCharacterdAttribute());
		return attributes;
	}

	/**
	 * Get {@link IdmFormAttributeDto} with split character
	 *
	 * @return
	 */
	protected IdmFormAttributeDto getSplitCharacterdAttribute() {
		IdmFormAttributeDto textField = new IdmFormAttributeDto(
				SPLIT_CHARACTER_CODE, 
				SPLIT_CHARACTER_CODE, 
				PersistentType.CHAR);
		textField.setDefaultValue(SPLIT_CHARACTER_DEFAULT_VALUE);
		return textField;
	}

	/**
	 * Method init string builder and also init HEADER line
	 */
	private void initStringBuilder() {
		this.result = new StringBuilder();
		String splitCharacter = getSplitCharacter();
		result.append(IdmIdentity_.username.getName().toUpperCase());
		result.append(splitCharacter);
		result.append(IdmIdentity_.externalCode.getName().toUpperCase());
		result.append(splitCharacter);
		result.append(IdmIdentity_.state.getName().toUpperCase());
		result.append(System.lineSeparator());
	}

	/**
	 * Return split character
	 *
	 * @return
	 */
	private String getSplitCharacter() {
		String result = getParameterConverter().toString(getProperties(), SPLIT_CHARACTER_CODE);
		return result != null ? result : SPLIT_CHARACTER_DEFAULT_VALUE;
	}

	@Override
	public int getOrder() {
		return super.getOrder() + 40001;
	}

	@Override
	public String getName() {
		return BULK_ACTION_NAME;
	}

	@Override
	public ReadWriteDtoService<IdmIdentityDto, IdmIdentityFilter> getService() {
		return identityService;
	}
}
