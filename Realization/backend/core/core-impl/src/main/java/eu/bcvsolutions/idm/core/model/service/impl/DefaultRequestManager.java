package eu.bcvsolutions.idm.core.model.service.impl;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.MethodDescriptor;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.stream.Collectors;

import org.activiti.engine.runtime.ProcessInstance;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.domain.Codeable;
import eu.bcvsolutions.idm.core.api.domain.ConfigurationMap;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.domain.Identifiable;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.domain.RequestOperationType;
import eu.bcvsolutions.idm.core.api.domain.RequestState;
import eu.bcvsolutions.idm.core.api.domain.Requestable;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRequestAttributeValueDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRequestItemAttributeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRequestItemChangesDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRequestItemDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.OperationResultDto;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRequestItemFilter;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.exception.CoreException;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.exception.RoleRequestException;
import eu.bcvsolutions.idm.core.api.service.ConfidentialStorage;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.IdmRequestItemService;
import eu.bcvsolutions.idm.core.api.service.IdmRequestService;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.api.service.ReadDtoService;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.api.service.RequestManager;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.api.utils.EntityUtils;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormInstanceDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.eav.api.entity.FormableEntity;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.eav.api.service.FormValueService;
import eu.bcvsolutions.idm.core.eav.api.service.IdmFormDefinitionService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmRequestItem;
import eu.bcvsolutions.idm.core.model.event.RequestEvent;
import eu.bcvsolutions.idm.core.model.event.RequestEvent.RequestEventType;
import eu.bcvsolutions.idm.core.model.event.processor.role.RoleRequestApprovalProcessor;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowFilterDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowProcessInstanceDto;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowProcessInstanceService;

/**
 * Default implementation of universal request manager
 *
 * @author svandav
 */
@Service("requestManager")
public class DefaultRequestManager<R extends Requestable> implements RequestManager<R> {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultRequestManager.class);

	@Autowired
	private SecurityService securityService;
	@Autowired
	private ApplicationContext applicationContext;
	@Autowired
	private WorkflowProcessInstanceService workflowProcessInstanceService;
	@Autowired
	private EntityEventManager entityEventManager;
	@Autowired
	private IdmRequestService requestService;
	@Autowired
	private IdmRequestItemService requestItemService;
	@Autowired
	private LookupService lookupService;
	@Autowired
	private FormService formService;
	@Autowired
	private IdmFormDefinitionService formDefinitionService;
	@Autowired
	private ConfidentialStorage confidentialStorage;
	@Autowired
	@Qualifier("objectMapper")
	private ObjectMapper mapper;
	private RequestManager<R> requestManager;

	private ConfigurationMap configurationMap;

	@Override
	@Transactional
	public IdmRequestDto startRequest(UUID requestId, boolean checkRight) {
		
		IdmRequestDto request = requestService.get(requestId);
		Assert.notNull(request, "Request is required!");

		try {
			RequestManager<R> service = this.getRequestManager();
			if (!(service instanceof DefaultRequestManager)) {
				throw new CoreException("We expects instace of DefaultRequestManager!");
			}
			return ((DefaultRequestManager<R>) service).startRequestNewTransactional(requestId, checkRight);
		} catch (Exception ex) {
			LOG.error(ex.getLocalizedMessage(), ex);
			request = requestService.get(requestId);
			Throwable exceptionToLog = resolveException(ex);

			if (exceptionToLog instanceof ResultCodeException) {
				request.setResult( //
						new OperationResultDto //
								.Builder(OperationState.EXCEPTION) //
										.setException((ResultCodeException) exceptionToLog) //
										.build()); //
			} else {
				request.setResult( //
						new OperationResultDto //
								.Builder(OperationState.EXCEPTION) //
										.setCause(exceptionToLog) //
										.build()); //
			}
			request.setState(RequestState.EXCEPTION);
			return requestService.save(request);
		}
	}

	/**
	 * Internal start request. Start in new transaction
	 *
	 * @param requestId
	 * @param checkRight
	 *            - If is true, then will be check right for immediately execution
	 *            (if is requires)
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public IdmRequestDto startRequestNewTransactional(UUID requestId, boolean checkRight) {
		return (IdmRequestDto) this.getRequestManager().startRequestInternal(requestId, checkRight);
	}

	@Override
	@Transactional
	public IdmRequestDto startRequestInternal(UUID requestId, boolean checkRight) {
		LOG.debug("Start role request [{}]", requestId);
		Assert.notNull(requestId, "Role request ID is required!");
		// Load request ... check right for read
		IdmRequestDto request = requestService.get(requestId);
		Assert.notNull(request, "Role request DTO is required!");
		Assert.isTrue(
				RequestState.CONCEPT == request.getState() || RequestState.DUPLICATED == request.getState()
						|| RequestState.EXCEPTION == request.getState(),
				"Only role request with CONCEPT or EXCEPTION or DUPLICATED state can be started!");

		// Request will be set on in progress state
		request.setState(RequestState.IN_PROGRESS);
		request.setResult(new OperationResultDto.Builder(OperationState.RUNNING).build());
		IdmRequestDto savedRequest = requestService.save(request);

		// Throw event
		Map<String, Serializable> variables = new HashMap<>();
		variables.put(RoleRequestApprovalProcessor.CHECK_RIGHT_PROPERTY, checkRight);
		return entityEventManager.process(new RequestEvent(RequestEventType.EXECUTE, savedRequest, variables))
				.getContent();
	}

	@Override
	@Transactional
	public boolean startApprovalProcess(IdmRequestDto request, boolean checkRight, EntityEvent<IdmRequestDto> event,
			String wfDefinition) {
		// If is request marked as executed immediately, then we will check right
		// and do realization immediately (without start approval process)
		if (request.isExecuteImmediately()) {
			boolean haveRightExecuteImmediately = securityService.hasAnyAuthority(CoreGroupPermission.REQUEST_ADMIN);

			if (checkRight && !haveRightExecuteImmediately) {
				throw new RoleRequestException(CoreResultCode.ROLE_REQUEST_NO_EXECUTE_IMMEDIATELY_RIGHT,
						ImmutableMap.of("new", request));
			}

			// Execute request immediately
			return true;
		} else {

			Map<String, Object> variables = new HashMap<>();
			// Minimize size of DTO persisting to WF
			IdmRequestDto eventRequest = event.getContent();
			eventRequest.setEmbedded(null);
			variables.put(EntityEvent.EVENT_PROPERTY, event);

			ProcessInstance processInstance = workflowProcessInstanceService.startProcess(wfDefinition,
					IdmRoleDto.class.getSimpleName(), request.getCreator(), request.getCreatorId().toString(),
					variables);
			// We have to refresh request (maybe was changed in wf process)
			request = requestService.get(request.getId());
			request.setWfProcessId(processInstance.getProcessInstanceId());
			requestService.save(request);
		}

		return false;
	}

	@Override
	@Transactional
	public IdmRequestDto executeRequest(UUID requestId) {
		// We can`t catch and log exception to request, because this transaction will be
		// marked as to rollback.
		// We can`t run this method in new transaction, because changes on request
		// (state modified in WF for example) is in uncommited transaction!
		return this.executeRequestInternal(requestId);
	}

	private IdmRequestDto executeRequestInternal(UUID requestId) {
		Assert.notNull(requestId, "Role request ID is required!");
		IdmRequestDto request = requestService.get(requestId);
		Assert.notNull(request, "Role request is required!");

		List<IdmRequestItemDto> items = this.findRequestItems(request.getId(), null);
		List<IdmRequestItemDto> sortedItems = items.stream().sorted(Comparator.comparing(IdmRequestItemDto::getCreated))
				.collect(Collectors.toList());

		// Validate items
		sortedItems.stream() //
				.filter(item -> RequestOperationType.ADD == item.getOperation()
						|| RequestOperationType.UPDATE == item.getOperation()) //
				.forEach(item -> { //
					// Get DTO service
					R dto = null;
					try {
						@SuppressWarnings("unchecked")
						Class<? extends R> dtoClass = (Class<? extends R>) Class.forName(item.getOwnerType());
						@SuppressWarnings("unchecked")
						ReadWriteDtoService<Requestable, BaseFilter> dtoService = (ReadWriteDtoService<Requestable, BaseFilter>) getServiceByItem(
								item, dtoClass);
						dto = this.convertItemToDto(item, dtoClass);
						dtoService.validateDto((Requestable) dto);
					} catch (Exception e) {
						throw new RoleRequestException(CoreResultCode.REQUEST_ITEM_IS_NOT_VALID,
								ImmutableMap.of("dto", dto != null ? dto.toString() : null, "item", item.toString()),
								e);
					}
				});

		sortedItems.forEach(item -> {
			try {
				this.resolveItem(item);
			} catch (ClassNotFoundException | IOException e) {
				throw new CoreException(e);
			}
		});

		request.setState(RequestState.EXECUTED);
		request.setResult(new OperationResultDto.Builder(OperationState.EXECUTED).build());
		return requestService.save(request);
	}

	private void resolveItem(IdmRequestItemDto item)
			throws ClassNotFoundException, JsonParseException, JsonMappingException, IOException {

		Assert.notNull(item, "Item is mandatory resolving!");

		RequestOperationType type = item.getOperation();
		// Get DTO service
		@SuppressWarnings("unchecked")
		Class<? extends R> dtoClass = (Class<? extends R>) Class.forName(item.getOwnerType());
		// Get service
		@SuppressWarnings("unchecked")
		ReadWriteDtoService<R, BaseFilter> dtoService = (ReadWriteDtoService<R, BaseFilter>) this.getServiceByItem(item,
				dtoClass);

		// Create or Update DTO
		if (RequestOperationType.ADD == type || RequestOperationType.UPDATE == type) {
			R dto = this.convertItemToDto(item, dtoClass);
			// If is DTO form value and confidential, then we need to load value form
			// confidential storage
			if (dto instanceof IdmFormValueDto) {
				IdmFormValueDto formValueDto = (IdmFormValueDto) dto;
				if (formValueDto.isConfidential()) {
					formValueDto.setValue(this.getConfidentialPersistentValue(item));
				}
			}
			// Save without check a permissions
			dto = dtoService.save(dto);
			item.setResult(new OperationResultDto(OperationState.EXECUTED));
			requestItemService.save(item);
			return;
		}
		// Delete DTO
		if (RequestOperationType.REMOVE == type) {
			Assert.notNull(item.getOwnerId(), "Id in item is required for delete!");

			Requestable dtoToDelete = dtoService.get(item.getOwnerId());
			if (dtoToDelete == null) {
				item.setResult(new OperationResultDto //
						.Builder(OperationState.NOT_EXECUTED) //
								.setException(new ResultCodeException(CoreResultCode.NOT_FOUND,
										ImmutableMap.of("entity", item.getOriginalCreatorId()))) //
								.build()); //
				requestItemService.save(item);
				return;
			}
			// Delete without check a permissions
			dtoService.deleteById(dtoToDelete.getId());
			item.setResult(new OperationResultDto(OperationState.EXECUTED));
			requestItemService.save(item);
			return;
		}

	}

	@Override
	@Transactional
	public void cancel(IdmRequestDto dto) {
		cancelWF(dto);
		dto.setState(RequestState.CANCELED);
		dto.setResult(new OperationResultDto(OperationState.CANCELED));
		requestService.save(dto);
	}

	@Override
	@Transactional
	public R post(Serializable requestId, R dto) {
		ReadDtoService<R, ?> dtoReadService = getDtoService(dto);
		boolean isNew = dtoReadService.isNew(dto);
		return this.post(requestId, dto, isNew);
	}

	private R post(Serializable requestId, R dto, boolean isNew) {
		Assert.notNull(dto, "DTO is required!");
		Assert.notNull(requestId, "Request ID is required!");
		// TODO: Rights!

		IdmRequestDto request = requestService.get(requestId);
		// Exists item for same original owner?
		IdmRequestItemDto item = this.findRequestItem(request.getId(), dto);
		try {
			if (dto.getId() == null) {
				dto.setId(UUID.randomUUID());
			}
			if (item == null) {
				item = createRequestItem(request.getId(), dto);
				item.setOperation(isNew ? RequestOperationType.ADD : RequestOperationType.UPDATE);
				item.setOwnerId((UUID) dto.getId());
			} else {
				item.setOperation(isNew ? RequestOperationType.ADD : RequestOperationType.UPDATE);
			}

			String dtoString = this.convertDtoToString(dto);
			item.setData(dtoString);
			// Update or create new request item
			item = requestItemService.save(item);
			// Set ID of request item to result DTO
			dto.setRequestItem(item.getId());
			return this.get(requestId, dto);

		} catch (JsonProcessingException e) {
			throw new ResultCodeException(CoreResultCode.DTO_CANNOT_BE_CONVERT_TO_JSON,
					ImmutableMap.of("dto", dto.toString()));
		}
	}

	@Override
	@Transactional
	public R delete(Serializable requestId, R dto) {
		Assert.notNull(dto, "DTO is required!");
		Assert.notNull(requestId, "Request ID is required!");
		// TODO: Rights!

		IdmRequestDto request = requestService.get(requestId);
		// Exists item for same original owner?
		IdmRequestItemDto item = this.findRequestItem(request.getId(), dto);
		// If this item already exists for ADD, then we want to delete him.
		if (item != null && RequestOperationType.ADD == item.getOperation()) {
			requestItemService.delete(item);
			return null;
		}
		if (item == null) {
			item = createRequestItem(request.getId(), dto);
		}
		item.setOwnerId((UUID) dto.getId());
		item.setOperation(RequestOperationType.REMOVE);
		item.setData(null);
		// Update or create new request item
		item = requestItemService.save(item);
		// Set ID of request item to result DTO
		dto.setRequestItem(item.getId());

		return dto;
	}

	@Override
	public R get(Serializable requestId, R dto) {
		Assert.notNull(dto, "DTO is required!");
		Assert.notNull(requestId, "Request ID is required!");
		// TODO: Rights!

		IdmRequestDto request = requestService.get(requestId);
		// Exists item for same original owner?
		IdmRequestItemDto item = this.findRequestItem(request.getId(), dto);

		if (item == null) {
			return dto;
		} else if (RequestOperationType.REMOVE == item.getOperation()) {
			addRequestItemToDto(dto, item);
			return dto;
		}

		try {
			@SuppressWarnings("unchecked")
			R requestedDto = this.convertItemToDto(item, (Class<? extends R>) dto.getClass());
			addRequestItemToDto((R) requestedDto, item);
			this.addEmbedded((AbstractDto) requestedDto, request.getId());
			return (R) requestedDto;

		} catch (IOException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| IntrospectionException | InstantiationException | ClassNotFoundException e) {
			throw new ResultCodeException(CoreResultCode.JSON_CANNOT_BE_CONVERT_TO_DTO,
					ImmutableMap.of("json", item.getData()), e);
		}
	}

	@Override
	public Page<R> find(Class<? extends R> dtoClass, Serializable requestId, BaseFilter filter, Pageable pageable,
			IdmBasePermission permission) {
		ReadDtoService<R, BaseFilter> dtoReadService = getDtoService(dtoClass);
		Page<R> originalPage = dtoReadService.find(filter, pageable, permission);
		List<R> originals = originalPage.getContent();
		List<R> results = new ArrayList<>();

		IdmRequestDto request = requestService.get(requestId);
		List<IdmRequestItemDto> items = this.findRequestItems(request.getId(), dtoClass);

		originals.stream().forEach(dto -> {
			IdmRequestItemDto item = items.stream() //
					.filter(i -> dto.getId().equals(i.getOwnerId())) //
					.findFirst() //
					.orElse(null); //
			if (item == null) {
				// None item found -> result is original DTO
				results.add(dto);
				return;
			}
			if (Strings.isNullOrEmpty(item.getData())) {
				// Item found, but does not contains any DTO. So original DTO will be result
				// (with connected item)
				addRequestItemToDto(dto, item);
				results.add(dto);
				return;
			}

			try {
				// Item with data found. Data in the request is result
				R requestedDto = this.convertItemToDto(item, dtoClass);
				addEmbedded((AbstractDto) requestedDto, request.getId());
				addRequestItemToDto((Requestable) requestedDto, item);
				results.add(requestedDto);
				return;

			} catch (IOException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
					| InstantiationException | IntrospectionException | ClassNotFoundException e) {
				throw new ResultCodeException(CoreResultCode.JSON_CANNOT_BE_CONVERT_TO_DTO,
						ImmutableMap.of("json", item.getData()));
			}
		});

		// !!Searching of added DTOs are very naive!!
		// We use all UUID value in the filter and try to find it in the DTOs. It means
		// only equals is implemented.

		// Find potential parents
		List<UUID> potencialParents = this.findPotencialParents(filter);

		results.addAll(this.findRelatedAddedItems(request, potencialParents, items, dtoClass));

		// Set all results as trimmed = true. FE expects trimmed value in the table.
		results.forEach(result -> ((AbstractDto) result).setTrimmed(true));

		return new PageImpl<>(results, pageable, originalPage.getTotalElements());
	}

	@Override
	@Transactional
	public IdmRequestDto createRequest(R dto) {
		Assert.notNull(dto, "DTO is required!");
		// TODO: Rights!

		boolean createNew = false;
		if (dto.getId() == null) {
			dto.setId(UUID.randomUUID());
			createNew = true;
		}
		IdmRequestDto request = new IdmRequestDto();
		request.setState(RequestState.CONCEPT);
		request.setOwnerId((UUID) dto.getId());
		request.setOwnerType(dto.getClass().getName());
		request.setExecuteImmediately(false);
		request.setRequestType(dto.getClass().getSimpleName());
		request.setResult(new OperationResultDto(OperationState.CREATED));
		// Create request
		request = requestService.save(request);
		// Create item
		if (createNew) {
			this.post(request.getId(), dto);
		}

		return request;
	}

	@Override
	@Transactional
	public IdmFormInstanceDto saveFormInstance(UUID requestId, R owner, IdmFormDefinitionDto formDefinition,
			List<IdmFormValueDto> newValues, BasePermission... permission) {

		IdmFormInstanceDto formInstance = new IdmFormInstanceDto(owner, formDefinition, newValues);

		// Load all current form values (includes requested items)
		IdmFormInstanceDto previousRequestFormInstance = getFormInstance(requestId, owner, formDefinition, permission);

		Map<UUID, Map<UUID, IdmFormValueDto>> previousValues = new HashMap<>(); // values by attributes
		previousRequestFormInstance.getValues().forEach(formValue -> {
			if (!previousValues.containsKey(formValue.getFormAttribute())) {
				previousValues.put(formValue.getFormAttribute(), new LinkedHashMap<>()); // sort by seq
			}
			previousValues.get(formValue.getFormAttribute()).put(formValue.getId(), formValue);
		});

		List<IdmFormValueDto> results = new ArrayList<>();
		for (Entry<String, List<IdmFormValueDto>> attributeEntry : formInstance.toValueMap().entrySet()) {
			IdmFormAttributeDto attribute = formInstance.getMappedAttributeByCode(attributeEntry.getKey());
			List<IdmFormValueDto> attributePreviousValues = new ArrayList<>();
			if (previousValues.containsKey(attribute.getId())) {
				attributePreviousValues.addAll(previousValues.get(attribute.getId()).values());
			}
			// Save attributes
			results.addAll(saveAttributeValues(requestId, owner, attribute, attributePreviousValues,
					attributeEntry.getValue(), permission));
		}

		return new IdmFormInstanceDto(owner, formDefinition, results);
	}

	@SuppressWarnings("unchecked")
	@Override
	public IdmFormInstanceDto getFormInstance(UUID requestId, R owner, IdmFormDefinitionDto formDefinition,
			BasePermission... permission) {

		Assert.notNull(requestId, "ID of request is required!");
		Assert.notNull(owner, "Owner is required!");
		Assert.notNull(formDefinition, "Form definition is required!");

		boolean isNew = getDtoService(owner).isNew(owner);
		List<IdmFormValueDto> requestValues = new ArrayList<>();

		// If owner does not exists in the DB. We cannot call form service - exception
		// (owner does not exist) would be throw.
		if (!isNew) {
			IdmFormInstanceDto previousFormInstance = formService.getFormInstance(owner, formDefinition, permission);
			List<IdmFormValueDto> originalValues = previousFormInstance.getValues();

			originalValues.forEach(value -> {
				requestValues.add((IdmFormValueDto) this.get(requestId, (R) value));
			});
		}

		IdmRequestDto request = requestService.get(requestId);
		// Load all added items for that request
		List<IdmRequestItemDto> addedItems = this.findRequestItems(request.getId(), IdmFormValueDto.class,
				RequestOperationType.ADD);
		// Find added items for that owner ID
		List<R> relatedAddedItems = this.findRelatedAddedItems(request, ImmutableList.of((UUID) owner.getId()),
				addedItems, (Class<? extends R>) IdmFormValueDto.class);

		requestValues.addAll((Collection<? extends IdmFormValueDto>) relatedAddedItems);

		return new IdmFormInstanceDto(owner, formDefinition, requestValues);
	}

	@SuppressWarnings("unchecked")
	@Override
	public IdmRequestItemChangesDto getChanges(IdmRequestItemDto item) {
		LOG.debug(MessageFormat.format("Start read request item with changes [{0}].", item));
		Assert.notNull(item, "Idm request item cannot be null!");
		if (Strings.isNullOrEmpty(item.getOwnerType()) || item.getOwnerId() == null) {
			return null;
		}

		List<IdmRequestItemAttributeDto> resultAttributes = new ArrayList<>();
		Class<? extends R> dtoClass;
		try {
			dtoClass = (Class<? extends R>) Class.forName(item.getOwnerType());
		} catch (ClassNotFoundException e) {
			throw new CoreException(e);
		}
		ReadDtoService<?, ?> readService = getServiceByItem(item, dtoClass);

		R currentDto = (R) readService.get(item.getOwnerId());
		if (currentDto == null) {
			try {
				currentDto = (R) dtoClass.newInstance();
				currentDto.setId(item.getOwnerId());
			} catch (InstantiationException | IllegalAccessException e) {
				throw new CoreException(e);
			}
		}
		R changedDto = this.get(item.getRequest(), currentDto);
		Map<String, Object> currentFieldsValues = this.dtoToMap((AbstractDto) currentDto);
		Map<String, Object> changedFieldsValues = this.dtoToMap((AbstractDto) changedDto);

		// First add all new attributes
		changedFieldsValues.keySet().stream().forEach(changedAttribute -> {
			if (!currentFieldsValues.containsKey(changedAttribute)) {
				Object value = changedFieldsValues.get(changedAttribute);
				IdmRequestItemAttributeDto attribute = new IdmRequestItemAttributeDto(changedAttribute,
						value instanceof List, true);
				if (attribute.isMultivalue()) {
					if (value != null && value instanceof List) {
						((List<?>) value).forEach(v -> {
							attribute.getValues()
									.add(new IdmRequestAttributeValueDto(v, null, RequestOperationType.ADD));
						});
					}
				} else {
					attribute.setValue(new IdmRequestAttributeValueDto(value, null, RequestOperationType.ADD));
				}
				resultAttributes.add(attribute);
			}
		});

		// Second add all already exists attributes
		currentFieldsValues.keySet().forEach(currentAttribute -> {
			Object changedValue = changedFieldsValues.get(currentAttribute);
			IdmRequestItemAttributeDto attribute;
			Object currentValue = currentFieldsValues.get(currentAttribute);
			attribute = new IdmRequestItemAttributeDto(currentAttribute, changedValue instanceof List, false);

			if (attribute.isMultivalue()) {
				if (changedValue != null && changedValue instanceof List) {
					((List<?>) changedValue).forEach(value -> {
						if (currentValue != null && currentValue instanceof List
								&& ((List<?>) currentValue).contains(value)) {
							attribute.getValues().add(new IdmRequestAttributeValueDto(value, value, null));
						} else {
							attribute.setChanged(true);
							attribute.getValues()
									.add(new IdmRequestAttributeValueDto(value, null, RequestOperationType.ADD));
						}
					});
				}
				if (currentValue != null && currentValue instanceof List) {
					((List<?>) currentValue).forEach(value -> {
						if (changedValue == null || !((List<?>) changedValue).contains(value)) {
							attribute.setChanged(true);
							attribute.getValues().add(
									new IdmRequestAttributeValueDto(value, value, RequestOperationType.REMOVE));
						}
					});
				}
			} else {
				if ((changedValue == null && currentValue == null)
						|| (changedValue != null && changedValue.equals(currentValue))
						|| (currentValue != null && currentValue.equals(changedValue))) {
					attribute.setChanged(RequestOperationType.UPDATE == item.getOperation() ? false : true);
					attribute.setValue(new IdmRequestAttributeValueDto(changedValue, currentValue,
							RequestOperationType.UPDATE == item.getOperation() ? null : item.getOperation()));
				} else {
					attribute.setChanged(true);
					attribute
							.setValue(new IdmRequestAttributeValueDto(changedValue, currentValue, item.getOperation()));
				}
			}
			resultAttributes.add(attribute);
		});

		// Make all values nicer
		resultAttributes.forEach(attribute -> {
			attribute.getValue().setValue(this.makeNiceValue(attribute.getValue().getValue()));
			attribute.getValue().setOldValue(this.makeNiceValue(attribute.getValue().getOldValue()));
			
			List<IdmRequestAttributeValueDto> attributeValues = attribute.getValues();
			attributeValues.forEach(attributeValue -> {
				attributeValue.setValue(this.makeNiceValue(attributeValue.getValue()));
				attributeValue.setOldValue(this.makeNiceValue(attributeValue.getOldValue()));
			});
		});
		
		IdmRequestItemChangesDto result = new IdmRequestItemChangesDto();
		result.setRequestItem(item);
		result.getAttributes().addAll(resultAttributes);

		LOG.debug(MessageFormat.format("End of reading the request item with changes [{0}].", item));
		return result;
	}
	
	private Object makeNiceValue(Object value) {
		if (value == null) {
			return null;
		}
		if (value instanceof Codeable) {
			Codeable codeable = (Codeable) value;
			return codeable.getCode();
		}
		if (value instanceof Identifiable) {
			Identifiable identifiable = (Identifiable) value;
			return identifiable.getId();
		}
		if (value instanceof ConfigurationMap) {
			configurationMap = (ConfigurationMap) value;
			Map<String, Serializable> map = configurationMap.toMap();
			return map.toString();
			
		}
		return value;
	}

	private Map<String, Object> dtoToMap(AbstractDto dto) {
		Map<String, Object> results = new HashMap<>();
		if (dto == null) {
			return results;
		}
		try {
			List<PropertyDescriptor> descriptors = Lists
					.newArrayList(Introspector.getBeanInfo(dto.getClass()).getPropertyDescriptors());
			List<Field> fields = Lists.newArrayList(dto.getClass().getDeclaredFields()) //
					.stream() //
					.filter(field -> !Requestable.REQUEST_ITEM_FIELD.equals(field.getName())) //
					.filter(field -> !Requestable.REQUEST_FIELD.equals(field.getName())) //
					.filter(field -> !field.isAnnotationPresent(JsonIgnore.class)) //
					.collect(Collectors.toList()); //
			// Embedded objects
			fields.stream() //
					.filter(field -> field.isAnnotationPresent(Embedded.class)) //
					.forEach(field -> {
						results.put(field.getName(), dto.getEmbedded().get(field.getName()));
					});

			// Others objects
			fields.stream() //
					.filter(field -> !field.isAnnotationPresent(Embedded.class)) //
					.forEach(field -> {
						try {
							PropertyDescriptor fieldDescriptor = descriptors.stream() //
									.filter(descriptor -> field.getName().equals(descriptor.getName())) //
									.findFirst() //
									.orElse(null); //
							if (fieldDescriptor != null) {
								Object value = fieldDescriptor.getReadMethod().invoke(dto);
								results.put(field.getName(), value);
							}
						} catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
							throw new CoreException(e);
						}
					});
		} catch (IntrospectionException e) {
			throw new CoreException(e);
		}

		return results;
	}

	/**
	 * Find related added DTOs by given parents. !!Searching of added DTOs are very
	 * naive!! We use all UUID value in the filter and try to find it in the DTOs.
	 * It means only equals is implemented.
	 * 
	 * @param request
	 * @param potencialParents
	 * @param items
	 * @param dtoClass
	 * @return
	 */
	private List<R> findRelatedAddedItems(IdmRequestDto request, List<UUID> potencialParents,
			List<IdmRequestItemDto> items, Class<? extends R> dtoClass) {
		List<R> results = new ArrayList<>();
		// Find items which should be added
		List<IdmRequestItemDto> itemsToAdd = items.stream() //
				.filter(i -> RequestOperationType.ADD == i.getOperation()) //
				.filter(i -> {
					return potencialParents.stream() //
							.filter(parentId -> i.getData().indexOf(parentId.toString()) != -1) //
							.findFirst() //
							.isPresent(); //
				}).collect(Collectors.toList()); //

		itemsToAdd.forEach(item -> {
			try {
				R requestedDto = this.convertItemToDto(item, dtoClass);
				AbstractDto requested = (AbstractDto) requestedDto;
				addEmbedded(requested, request.getId());
				addRequestItemToDto((Requestable) requested, item);
				results.add((R) requestedDto);
				return;

			} catch (IOException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
					| IntrospectionException | InstantiationException | ClassNotFoundException e) {
				throw new ResultCodeException(CoreResultCode.JSON_CANNOT_BE_CONVERT_TO_DTO,
						ImmutableMap.of("json", item.getData()), e);
			}
		});

		return results;
	}

	@SuppressWarnings("unchecked")
	/*
	 * Get DTO from the request item. Place of additional conversion (EAV attribute
	 * for example)
	 */
	private R convertItemToDto(IdmRequestItemDto item, Class<? extends R> type)
			throws JsonParseException, JsonMappingException, IOException, ClassNotFoundException {
		R dto = convertStringToDto(item.getData(), type);
		if (dto instanceof IdmFormValueDto) {
			IdmFormValueDto formValueDto = (IdmFormValueDto) dto;
			formValueDto.setOwnerType((Class<? extends FormableEntity>) Class.forName(item.getSuperOwnerType()));
			formValueDto.setOwnerId(item.getSuperOwnerId());
		}
		return dto;
	}

	private String convertDtoToString(BaseDto dto) throws JsonProcessingException {
		return mapper.writerFor(dto.getClass()).writeValueAsString(dto);
	}

	private R convertStringToDto(String data, Class<? extends R> type)
			throws JsonParseException, JsonMappingException, IOException {
		return mapper.readValue(data, type);
	}

	private IdmRequestItemDto findRequestItem(UUID requestId, Requestable dto) {
		Assert.notNull(dto, "DTO is required!");
		if (dto.getId() == null) {
			return null;
		}
		IdmRequestItemFilter itemFilter = new IdmRequestItemFilter();
		itemFilter.setRequestId(requestId);
		itemFilter.setOwnerId((UUID) dto.getId());
		itemFilter.setOwnerType(dto.getClass().getName());
		List<IdmRequestItemDto> items = requestItemService.find(itemFilter, null).getContent();
		if (items.size() > 0) {
			return items.get(0);
		}
		return null;
	}

	private List<IdmRequestItemDto> findRequestItems(UUID requestId, Class<? extends Requestable> dtoClass) {
		return this.findRequestItems(requestId, dtoClass, null);
	}

	private List<IdmRequestItemDto> findRequestItems(UUID requestId, Class<? extends Requestable> dtoClass,
			RequestOperationType operation) {
		IdmRequestItemFilter itemFilter = new IdmRequestItemFilter();
		itemFilter.setRequestId(requestId);
		itemFilter.setOwnerType(dtoClass != null ? dtoClass.getName() : null);
		itemFilter.setOperationType(operation);

		return requestItemService.find(itemFilter, null).getContent();
	}

	private IdmRequestItemDto createRequestItem(UUID requestId, Requestable dto) {
		IdmRequestItemDto item = new IdmRequestItemDto();
		if (dto instanceof IdmFormValueDto) {
			IdmFormValueDto formValueDto = (IdmFormValueDto) dto;
			item.setSuperOwnerType(formValueDto.getOwnerType().getName());
			item.setSuperOwnerId((UUID) formValueDto.getOwnerId());
		}
		item.setRequest(requestId);
		item.setOwnerType(dto.getClass().getName());
		item.setResult(new OperationResultDto(OperationState.CREATED));
		return item;
	}

	/**
	 * Find potential parents. Invokes all method with UUID return type and without
	 * input parameters.
	 * 
	 * @param filter
	 * @return
	 */
	private List<UUID> findPotencialParents(BaseFilter filter) {
		Assert.notNull(filter, "Filter is mandatory!");

		List<MethodDescriptor> descriptors;
		try {
			descriptors = Lists.newArrayList(Introspector.getBeanInfo(filter.getClass()).getMethodDescriptors()) //
					.stream() //
					.filter(methodDescriptor -> UUID.class.equals(methodDescriptor.getMethod().getReturnType())) //
					.filter(methodDescriptor -> methodDescriptor.getMethod().getParameterTypes() == null
							|| methodDescriptor.getMethod().getParameterTypes().length == 0) //
					.collect(Collectors.toList());
		} catch (IntrospectionException e) {
			throw new CoreException(e);
		} //

		List<UUID> results = new ArrayList<>();
		descriptors.stream().forEach(descriptor -> {
			try {
				Object value = descriptor.getMethod().invoke(filter, new Object[] {});
				if (value == null) {
					return;
				}
				results.add((UUID) value);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				throw new CoreException(e);
			}
		});
		return results;
	}

	/**
	 * Add request item to DTO and to embedded map
	 *
	 * @param dto
	 * @param item
	 */
	private void addRequestItemToDto(Requestable dto, IdmRequestItemDto item) {
		dto.setRequestItem(item.getId());
		if (dto instanceof AbstractDto) {
			((AbstractDto) dto).getEmbedded().put(Requestable.REQUEST_ITEM_FIELD, item);
		}
	}

	/**
	 * Get read form value service for given class
	 *
	 * @param dto
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private FormValueService<FormableEntity> getFormValueService(Class<? extends FormableEntity> formableEntityClass) {
		return formService.getFormValueService((Class<FormableEntity>) formableEntityClass);
	}

	/**
	 * Get read DTO service for given DTO
	 *
	 * @param dto
	 * @return
	 */
	private ReadDtoService<R, BaseFilter> getDtoService(R dto) {
		@SuppressWarnings("unchecked")
		Class<? extends R> dtoClass = (Class<? extends R>) dto.getClass();
		return this.getDtoService(dtoClass);
	}

	/**
	 * Get read DTO service for given DTO class
	 *
	 * @param dto
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private ReadDtoService<R, BaseFilter> getDtoService(Class<? extends R> dtoClass) {
		return (ReadDtoService<R, BaseFilter>) lookupService.getDtoService(dtoClass);
	}

	/**
	 * Get service by give item. If item solving form-value, then form value service
	 * is returned
	 * 
	 * @param item
	 * @param dtoClass
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private ReadDtoService<?, ?> getServiceByItem(IdmRequestItemDto item, Class<? extends R> dtoClass) {
		ReadDtoService<?, ?> readService = null;
		if (IdmFormValueDto.class.getName().equals(item.getOwnerType())) {
			// EAV value .. we need find form value service by super owner type
			String superOwnerType = item.getSuperOwnerType();
			Assert.notNull(superOwnerType, "Super owner type is mandatory for EAV value!");
			try {
				readService = this.getFormValueService((Class<? extends FormableEntity>) Class.forName(superOwnerType));
			} catch (ClassNotFoundException e) {
				throw new CoreException(e);
			}
		} else {
			// Standard DTO service
			readService = this.getDtoService(dtoClass);
		}
		return readService;
	}

	/**
	 * Loads and adds DTOs by embedded annotation
	 * 
	 * @param dto
	 * @param requestId
	 * 
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws IntrospectionException
	 * @throws InstantiationException
	 */
	@SuppressWarnings("unchecked")
	private void addEmbedded(AbstractDto dto, UUID requestId) throws IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, IntrospectionException, InstantiationException {
		Assert.notNull(dto, "DTO is required!");

		Field[] fields = dto.getClass().getDeclaredFields();
		for (Field field : fields) {
			if (field.isAnnotationPresent(Embedded.class)) {
				Embedded embeddedAnnotation = field.getAnnotation(Embedded.class);
				if (embeddedAnnotation.enabled()) {
					// If DTO class is abstract then continue
					if (Modifier.isAbstract(embeddedAnnotation.dtoClass().getModifiers())) {
						continue;
					}
					Object value = EntityUtils.getEntityValue(dto, field.getName());
					if (value instanceof UUID) {
						// Create mock instance of embedded DTO only with ID
						UUID id = (UUID) value;
						AbstractDto embeddedDto = null;
						if (Requestable.class.isAssignableFrom(embeddedAnnotation.dtoClass())) {
							embeddedDto = embeddedAnnotation.dtoClass().newInstance();
							embeddedDto.setId(id);
							R originalEmbeddedDto = this.getDtoService((R) embeddedDto).get(embeddedDto.getId());
							if (originalEmbeddedDto != null) {
								// Call standard method for load request's DTO with original DTO
								embeddedDto = (AbstractDto) this.get(requestId, originalEmbeddedDto);
							} else {
								// Call standard method for load request's DTO with mock DTO (only with ID)
								embeddedDto = (AbstractDto) this.get(requestId, (R) embeddedDto);
							}
						} else {
							// If embedded DTO is not Requestable, then standard service is using
							embeddedDto = (AbstractDto) lookupService.getDtoService(embeddedAnnotation.dtoClass())
									.get(id);
						}
						if (embeddedDto == null) {
							continue;
						}
						embeddedDto.setTrimmed(true);
						dto.getEmbedded().put(field.getName(), embeddedDto);
					}
				}
			}
		}
	}

	/**
	 * Save single attribute values
	 * 
	 * @param owner
	 * @param attribute
	 * @param previousValues
	 * @param newValues
	 * @param permission
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private List<IdmFormValueDto> saveAttributeValues(UUID requestId, R owner, IdmFormAttributeDto attribute,
			List<IdmFormValueDto> previousValues, List<IdmFormValueDto> newValues, BasePermission... permission) {

		ReadDtoService<R, ?> dtoReadService = getDtoService(owner);
		Class<? extends BaseEntity> entityClass = dtoReadService.getEntityClass();
		if (!FormableEntity.class.isAssignableFrom(entityClass)) {
			throw new IllegalArgumentException(MessageFormat.format("Form owner [{0}] is not FormableEntity [{1}]!",
					owner.toString(), entityClass));
		}
		Class<? extends FormableEntity> ownerClass = (Class<? extends FormableEntity>) entityClass;

		IdmFormDefinitionDto formDefinition = formDefinitionService.get(attribute.getFormDefinition());

		List<IdmFormValueDto> results = new ArrayList<>();
		Map<UUID, IdmFormValueDto> unprocessedPreviousValues = new LinkedHashMap<>(); // ordered by seq
		if (CollectionUtils.isNotEmpty(previousValues)) {
			previousValues.forEach(previousValue -> {
				// Set owner to the form value
				previousValue.setOwnerAndAttribute(null, attribute);
				previousValue.setOwnerId(owner.getId());
				previousValue.setOwnerType(ownerClass);

				unprocessedPreviousValues.put(previousValue.getId(), previousValue);
			});
		}
		//
		if (newValues == null || newValues.isEmpty()) {
			// confidential values has to removed directly, they could not be sent with form
			// (only changed values)
			if (!attribute.isConfidential()) {
				// delete previous attributes
				unprocessedPreviousValues.values().forEach(value -> {
					results.add((IdmFormValueDto) this.delete(requestId, (R) value));
				});
			}
			return results;
		}
		//
		if (!attribute.isMultiple() && newValues.size() > 1) {
			throw new IllegalArgumentException(
					MessageFormat.format("Form attribute [{0}:{1}] does not support multivalue, sent [{2}] values.",
							formDefinition.getCode(), attribute.getCode(), newValues.size()));
		}
		//
		// compare values
		IdmFormValueDto[] sortedPreviousValues = formService.resolvePreviousValues(unprocessedPreviousValues,
				newValues);
		for (short index = 0; index < newValues.size(); index++) {
			IdmFormValueDto previousValue = sortedPreviousValues[index];
			IdmFormValueDto newValue = newValues.get(index);
			newValue.setOwnerAndAttribute(null, attribute);
			newValue.setOwnerId(owner.getId());
			newValue.setOwnerType(ownerClass);
			newValue.setSeq(index);
			//
			if (previousValue == null) {
				if (!newValue.isNull()) { // null values are not saved
					results.add(
							(IdmFormValueDto) this.post(requestId, (R) newValue, this.isFormValueNew(previousValue)));
				}
			} else {
				//
				// we using filled value only and set her into previous value => value id is
				// preserved
				// the same value should not be updated
				// confidential value is always updated - only new values are sent from client
				if (newValue.isConfidential() || !previousValue.isEquals(newValue)) {
					// set value for the previous value
					previousValue.setValue(newValue.getValue());
					// attribute persistent type could be changed
					previousValue.setOwnerAndAttribute(null, attribute);
					previousValue.setOwnerId(owner.getId());
					previousValue.setOwnerType(ownerClass);
					previousValue.setSeq(index);
					if (!previousValue.isNull()) { // null values are not saved

						if (previousValue.isConfidential()) {
							// Confidential value has to be persisted in the confidential storage
							results.add(saveConfidentialEavValue(requestId, previousValue));
						} else {
							results.add((IdmFormValueDto) this.post(requestId, (R) previousValue,
									this.isFormValueNew(previousValue)));
						}
					} else {
						results.add((IdmFormValueDto) this.delete(requestId, (R) previousValue));
					}
				}
			}
		}
		// remove unprocessed values
		// confidential property will be removed too => none or all confidential values
		// have to be given for multiple attributes
		unprocessedPreviousValues.values().forEach(previousValue -> {
			results.add((IdmFormValueDto) this.delete(requestId, (R) previousValue));
		});

		return results;
	}

	/**
	 * Save confidential FormValueDto. Value is persists to the confidential
	 * storage. DTO persisted in the request item contains 'asterixed' value only.
	 * 
	 * @param requestId
	 * @param confidentialFormValue
	 * @return
	 */
	private IdmFormValueDto saveConfidentialEavValue(UUID requestId, IdmFormValueDto confidentialFormValue) {
		// check, if value has to be persisted in confidential storage
		Serializable confidentialValue = confidentialFormValue.getValue();
		if (confidentialFormValue.isConfidential()) {
			confidentialFormValue.clearValues();
			if (confidentialValue != null) {
				// we need only to know, if value was filled
				confidentialFormValue.setStringValue(GuardedString.SECRED_PROXY_STRING);
				confidentialFormValue.setShortTextValue(GuardedString.SECRED_PROXY_STRING);
			}
		}
		Assert.notNull(confidentialFormValue);
		// Save DTO without confidential value
		@SuppressWarnings("unchecked")
		Requestable persistedRequestDto = this.post(requestId, (R) confidentialFormValue,
				this.isFormValueNew(confidentialFormValue));
		UUID requestItem = persistedRequestDto.getRequestItem();
		Assert.notNull(requestItem);

		// Save confidential value to ConfidentialStorage - owner is request item
		confidentialStorage.save(requestItem, IdmRequestItem.class, RequestManager.getConfidentialStorageKey(requestItem),
				confidentialValue);
		LOG.debug("Confidential FormValue [{}]  is persisted in RequestItem [{}] and value in the confidential storage",
				confidentialFormValue.getId(), requestItem);

		return (IdmFormValueDto) persistedRequestDto;
	}

	/**
	 * Check if given FormValue is new.
	 * 
	 * @param formValue
	 * @return
	 */
	private boolean isFormValueNew(IdmFormValueDto formValue) {
		if (formValue == null) {
			return true;
		}
		if (formValue.getId() == null) {
			return true;
		}
		if (formValue.getRequestItem() != null) {
			IdmRequestItemDto requestItemDto = DtoUtils.getEmbedded(formValue, Requestable.REQUEST_ITEM_FIELD,
					IdmRequestItemDto.class, null);
			if (requestItemDto == null) {
				requestItemDto = requestItemService.get(formValue.getRequestItem());
			}
			if (requestItemDto != null && RequestOperationType.ADD == requestItemDto.getOperation()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Cancel unfinished workflow process for this automatic role.
	 *
	 * @param dto
	 */
	private void cancelWF(IdmRequestDto dto) {
		if (!Strings.isNullOrEmpty(dto.getWfProcessId())) {
			WorkflowFilterDto filter = new WorkflowFilterDto();
			filter.setProcessInstanceId(dto.getWfProcessId());

			Collection<WorkflowProcessInstanceDto> resources = workflowProcessInstanceService.find(filter, null)
					.getContent();
			if (resources.isEmpty()) {
				// Process with this ID not exist ... maybe was ended
				return;
			}

			workflowProcessInstanceService.delete(dto.getWfProcessId(),
					"Role request use this WF, was deleted. This WF was deleted too.");
		}
	}

	@SuppressWarnings("unchecked")
	private RequestManager<R> getRequestManager() {
		if (this.requestManager == null) {
			this.requestManager = applicationContext.getBean(RequestManager.class);
		}
		return this.requestManager;
	}

	private Serializable getConfidentialPersistentValue(IdmRequestItemDto confidentialItem) {
		Assert.notNull(confidentialItem);
		//
		return confidentialStorage.get(confidentialItem.getId(), IdmRequestItem.class,
				RequestManager.getConfidentialStorageKey(confidentialItem.getId()));
	}

	/**
	 * If exception causal chain contains cause instance of ResultCodeException,
	 * then is return primary.
	 * 
	 * @param ex
	 * @return
	 */
	private Throwable resolveException(Exception ex) {
		Assert.notNull(ex);
		Throwable exceptionToLog = null;
		List<Throwable> causes = Throwables.getCausalChain(ex);
		// If is some cause instance of ResultCodeException, then we will use only it
		// (for better show on frontend)
		Throwable resultCodeException = causes.stream().filter(cause -> {
			if (cause instanceof ResultCodeException) {
				return true;
			}
			return false;
		}).findFirst().orElse(null);

		exceptionToLog = resultCodeException != null ? resultCodeException : ex;
		return exceptionToLog;
	}
}
