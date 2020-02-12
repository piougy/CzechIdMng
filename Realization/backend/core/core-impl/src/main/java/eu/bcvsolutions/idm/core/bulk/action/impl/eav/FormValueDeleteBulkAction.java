package eu.bcvsolutions.idm.core.bulk.action.impl.eav;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.bulk.action.AbstractRemoveBulkAction;
import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.DefaultResultModel;
import eu.bcvsolutions.idm.core.api.dto.ResultModel;
import eu.bcvsolutions.idm.core.api.dto.ResultModels;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.eav.api.dto.filter.IdmFormValueFilter;
import eu.bcvsolutions.idm.core.eav.api.entity.FormableEntity;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.eav.api.service.FormValueService;
import eu.bcvsolutions.idm.core.eav.entity.IdmForm;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormAttribute_;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormValue;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormValue_;
import eu.bcvsolutions.idm.core.eav.service.impl.AbstractFormValueService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmLongRunningTaskDto;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;

/**
 * Delete form values.
 * 
 * @author Radek Tomiška
 * @since 10.1.0
 */
@Component(FormValueDeleteBulkAction.NAME)
@Description("Delete form values.")
public class FormValueDeleteBulkAction extends AbstractRemoveBulkAction<IdmFormValueDto, IdmFormValueFilter<IdmForm>> {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(FormValueDeleteBulkAction.class);
	public static final String NAME = "core-form-value-delete-bulk-action";
	//
	@Autowired private AbstractFormValueService<IdmForm, IdmFormValue> commonService; // only for automatic action registration
	@Autowired private FormService formService;
	
	@Override
	public String getName() {
		return NAME;
	}
	
	@Override
	protected List<String> getAuthoritiesForEntity() {
		return Lists.newArrayList(CoreGroupPermission.FORM_VALUE_DELETE);
	}

	@Override
	public ReadWriteDtoService<IdmFormValueDto, IdmFormValueFilter<IdmForm>> getService() {
		// artificial common service
		return commonService;
	}
	
	/**
	 * Return proper form service.
	 * 
	 * @param formValue
	 * @return
	 */
	public FormValueService<FormableEntity> getFormValueService(IdmFormValueDto formValue) {
		FormValueService<FormableEntity> formValueService = formService.getFormValueService(formValue.getOwnerType());
		//
		return formValueService;
	}
	
	@Override
	public ResultModels prevalidate() {
		IdmBulkActionDto action = getAction();
		List<IdmFormValueDto> values = getValues(action, null);
		ResultModels result = new ResultModels();

		Map<UUID, IdmFormAttributeDto> attributes = new HashMap<>();
		Map<UUID, Long> requiredAttributes = new HashMap<>();
		values.forEach(value -> {
			IdmFormAttributeDto attribute;
			UUID attributeId = value.getFormAttribute();
			if (attributes.containsKey(attributeId)) {
				attribute = attributes.get(attributeId);
			} else {
				attribute = getAttribute(value);
				// prevent to load attributes repetitively, if is not given in dto embedded
				attributes.put(attribute.getId(), attribute);
			}
			if (attribute.isRequired()) {
				Long count;
				if (requiredAttributes.containsKey(attributeId)) {
					count = requiredAttributes.get(attributeId) + 1;
				} else {
					count = 1L;
				}
				requiredAttributes.put(attributeId, count);
			}			
		});

		// Sort by count
		requiredAttributes //
				.entrySet() //
				.stream() //
				.sorted(Collections.reverseOrder(Map.Entry.comparingByValue())) //
				.forEach(entry -> {
					IdmFormAttributeDto attribute = attributes.get(entry.getKey());
					ResultModel model = new DefaultResultModel(
							CoreResultCode.FORM_VALUE_DELETE_FAILED_IS_REQUIRED,
							ImmutableMap.of(
									"count", entry.getValue(),
									"attribute", attribute.getCode()
							)
					);
					//
					result.addInfo(model);
				});
		//
		return result;
	}
	
	@Override
	public OperationResult process() {
		IdmBulkActionDto action = this.getAction();
		Assert.notNull(action, "Bulk action is required.");
		//
		StringBuilder description = new StringBuilder();
		IdmLongRunningTaskDto longRunningTask = this.getLongRunningTaskService().get(this.getLongRunningTaskId());
		description.append(longRunningTask.getTaskDescription());
		//
		List<IdmFormValueDto> values = getValues(action, description);
		//
		this.count = Long.valueOf(values.size());
		this.counter = 0l;
		//
		// update description
		longRunningTask.setTaskDescription(description.toString());
		this.getLongRunningTaskService().save(longRunningTask);
		//
		return processValues(values);
	}
	
	@SuppressWarnings("rawtypes")
	protected List<IdmFormValueDto> getValues(IdmBulkActionDto action, StringBuilder description) {
		List<IdmFormValueDto> values = null;
		if (!action.getIdentifiers().isEmpty()) {
			FormValueService<?> lastUsedFormValueService = null;
			values = new ArrayList<IdmFormValueDto>();
			for (UUID valueId : action.getIdentifiers()) {
				IdmFormValueDto formValue = null;
				// we have value identifier only => we need to iterate all registered services ... but we can to cache lastly used - will be same in 99%
				BasePermission[] permissionForEntity = getPermissionForEntity(); // we will not need to check permission manualy in next step.
				if (lastUsedFormValueService != null) {
					formValue = lastUsedFormValueService.get(valueId, permissionForEntity);
				}
				for (FormValueService<?> formValueService : formService.getAvailableFormValueServices()) {
					formValue = formValueService.get(valueId, permissionForEntity);
					if (formValue != null) {
						lastUsedFormValueService = formValueService;
						//
						break;
					}
				}
				//
				if (formValue != null) {
					values.add(formValue);
				} else {
					// TODO: not found / missing permission ... add operation result some how
					LOG.warn("Form value with id [{}] not found, cannot be removed", valueId);
					
				}
			}
			//
			if (description != null) {
				description.append(System.lineSeparator());
				description.append("For filtering is used list of ID's.");
			}
		} else if (action.getTransformedFilter() != null) {
			// is necessary find entities with given base permission			
			List<IdmFormValueDto> content = formService
					.findValues((IdmFormValueFilter) action.getTransformedFilter(), null, getPermissionForEntity())
					.getContent();
			// it is necessary create new arraylist because return list form find is unmodifiable
			values = new ArrayList<>(content);
			//
			if (description != null) {
				description.append(System.lineSeparator());
				description.append("For filtering is used filter:");
				description.append(System.lineSeparator());
				String filterAsString = Arrays.toString(action.getFilter().entrySet().toArray());
				description.append(filterAsString);
			}
		} else {
			throw new ResultCodeException(CoreResultCode.BULK_ACTION_ENTITIES_ARE_NOT_SPECIFIED);
		}
		//
		// remove given ids
		if (!action.getRemoveIdentifiers().isEmpty()) {
			values.removeIf(v -> {
				return action.getRemoveIdentifiers().contains(v.getId());
			});
		}
		return values;
	}
	
	private OperationResult processValues(List<IdmFormValueDto> values) {
		for (IdmFormValueDto value : values) {
			this.increaseCounter();
			//
			try {
				OperationResult result = processDto(value);
				this.logItemProcessed(value, result);
				//
				if (!updateState()) {
					return new OperationResult.Builder(OperationState.CANCELED).build();
				}
			} catch (Exception ex) {
				// log failed result and continue
				LOG.error("Processing of entity [{}] failed.", value.getId(), ex);
				//
				if (ex instanceof ResultCodeException) {
					this.logItemProcessed(value, new OperationResult.Builder(OperationState.EXCEPTION).setException((ResultCodeException) ex).build());
				} else {
					this.logItemProcessed(value, new OperationResult.Builder(OperationState.EXCEPTION).setCause(ex).build());
				}
				if (!updateState()) {
					return new OperationResult.Builder(OperationState.CANCELED).setCause(ex).build();
				}
			}
		}
		return new OperationResult.Builder(OperationState.EXECUTED).build();
	}
	
	/**
	 * Check permission is evaluated before this method id used.
	 */
	@Override
	protected OperationResult processDto(IdmFormValueDto dto) {
		// delete by form service usage (PATH + NOTIFY)
		dto.setValue(null);
		// get attribute form definition
		IdmFormAttributeDto formAttribute = getAttribute(dto);
		IdmFormDefinitionDto formDefinition = DtoUtils.getEmbedded(formAttribute, IdmFormAttribute_.formDefinition, (IdmFormDefinitionDto) null);
		if (formDefinition == null) {
			formDefinition = formService.getDefinition(formAttribute.getFormDefinition());
		}
		Assert.notNull(formDefinition, "Form definition is required.");
		//
		// by definition usage - notify event is needed
		UUID ownerId = DtoUtils.toUuid(dto.getOwnerId());
		Class<? extends FormableEntity> ownerType = dto.getOwnerType();
		List<IdmFormValueDto> newValues = Lists.newArrayList(dto); 
		if(formAttribute.isMultiple()) {
			// prevent to remove other values of the same attribute
			formService
				.getValues(ownerId, ownerType, formAttribute)
				.forEach(previousValue -> {
					if (!previousValue.getId().equals(dto.getId())) {
						newValues.add(previousValue);
					}
				});
		}
		// save at last - EAV_SAVE will be published
		formService.saveValues(
				ownerId, 
				ownerType, 
				formDefinition, 
				newValues
		);
		//
		return new OperationResult.Builder(OperationState.EXECUTED).build();
	}
	
	@Override
	protected boolean checkPermissionForEntity(BaseDto dto) {
		throw new UnsupportedOperationException("Permissions for form value entity is not supported (permissions are evaluated internaly in bulk action process).");
	}
	
	private IdmFormAttributeDto getAttribute(IdmFormValueDto formValue) {
		IdmFormAttributeDto formAttribute = DtoUtils.getEmbedded(formValue, IdmFormValue_.formAttribute, (IdmFormAttributeDto) null);
		if (formAttribute == null) {
			formAttribute = formService.getAttribute(formValue.getFormAttribute());
		}
		Assert.notNull(formAttribute, "Form attribute is required.");
		//
		return formAttribute;
	}
}
