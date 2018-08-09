package eu.bcvsolutions.idm.core.model.service.impl;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.MethodDescriptor;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
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

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.domain.Identifiable;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.domain.RequestOperationType;
import eu.bcvsolutions.idm.core.api.domain.RequestState;
import eu.bcvsolutions.idm.core.api.domain.Requestable;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRequestDto;
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
public class DefaultRequestManager implements RequestManager {

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

	private RequestManager requestManager;

	@Override
	@Transactional
	public IdmRequestDto startRequest(UUID requestId, boolean checkRight) {
		IdmRequestDto request = requestService.get(requestId);
		Assert.notNull(request, "Request is required!");
		return request;

		// // Validation on exist some rule
		// if (RequestType.ATTRIBUTE == request.getRequestType()
		// && RequestOperationType.REMOVE != request.getOperation()) {
		// IdmAutomaticRoleAttributeRuleRequestFilter ruleFilter = new
		// IdmAutomaticRoleAttributeRuleRequestFilter();
		// ruleFilter.setRoleRequestId(requestId);
		//
		// List<IdmAutomaticRoleAttributeRuleRequestDto> ruleConcepts =
		// automaticRoleRuleRequestService
		// .find(ruleFilter, null).getContent();
		// if (ruleConcepts.isEmpty()) {
		// throw new
		// RoleRequestException(CoreResultCode.AUTOMATIC_ROLE_REQUEST_START_WITHOUT_RULE,
		// ImmutableMap.of("request", request.getName()));
		// }
		// }
		//
		// try {
		// IdmRequestService service = this.getIdmRequestService();
		// if (!(service instanceof DefaultIdmRequestService)) {
		// throw new CoreException("We expects instace of DefaultIdmRequestService!");
		// }
		// return ((DefaultIdmRequestService)
		// service).startRequestNewTransactional(requestId,
		// checkRight);
		// } catch (Exception ex) {
		// LOG.error(ex.getLocalizedMessage(), ex);
		// request = get(requestId);
		// Throwable exceptionToLog = resolveException(ex);
		//
		// // TODO: I set only cause of exception, not code and properties. If are
		// // properties set, then request cannot be save!
		// request.setResult(
		// new
		// OperationResultDto.Builder(OperationState.EXCEPTION).setCause(exceptionToLog).build());
		// request.setState(RequestState.EXCEPTION);
		// return save(request);
		// }
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
		return this.getRequestManager().startRequestInternal(requestId, checkRight);
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
			boolean haveRightExecuteImmediately = securityService
					.hasAnyAuthority(CoreGroupPermission.AUTOMATIC_ROLE_REQUEST_ADMIN);

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
			variables.put("approvalForAutomaticRole", Boolean.TRUE);

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

		// UUID automaticRoleId = request.getAutomaticRole();
		//
		// if (RequestType.ATTRIBUTE == request.getRequestType()) {
		// // Automatic role by attributes
		// if (RequestOperationType.REMOVE == request.getOperation()) {
		// // Remove automatic role by attributes
		// Assert.notNull(automaticRoleId, "Id of automatic role in the request (for
		// delete) is required!");
		// automaticRoleAttributeService.delete(automaticRoleAttributeService.get(automaticRoleId));
		// request.setAutomaticRole(null);
		// } else {
		// // Add new or update (rules) for automatic role by attributes
		// IdmAutomaticRoleAttributeDto automaticRole = null;
		// if (automaticRoleId != null) {
		// automaticRole = automaticRoleAttributeService.get(automaticRoleId);
		// } else {
		// automaticRole = new IdmAutomaticRoleAttributeDto();
		// automaticRole = initAttributeAutomaticRole(request, automaticRole);
		// automaticRole = automaticRoleAttributeService.save(automaticRole);
		// request.setAutomaticRole(automaticRole.getId());
		// }
		// UUID roleId = automaticRole.getRole() != null ? automaticRole.getRole() :
		// request.getRole();
		// Assert.notNull(roleId, "Id of role is required in the automatic role
		// request!");
		//
		// IdmRoleDto role = roleService.get(request.getRole());
		// Assert.notNull(role, "Role is required in the automatic role request!");
		//
		// // Before we do any change, we have to sets the automatic role to concept
		// state
		// automaticRole.setConcept(true);
		// automaticRoleAttributeService.save(automaticRole);
		//
		// // Realize changes for rules
		// realizeAttributeRules(request, automaticRole, ruleConcepts);
		//
		// // Sets automatic role as no concept -> execute recalculation this role
		// automaticRole.setConcept(false);
		// automaticRoleAttributeService.recalculate(automaticRoleAttributeService.save(automaticRole).getId());
		//
		// }
		// } else if (RequestType.TREE == request.getRequestType()) {
		// // Automatic role by node in a tree
		// if (RequestOperationType.REMOVE == request.getOperation()) {
		// // Remove tree automatic role
		// Assert.notNull(automaticRoleId, "Id of automatic role in the request (for
		// delete) is required!");
		// // Recount (remove) assigned roles ensures LRT during delete
		// automaticRoleTreeService.delete(automaticRoleTreeService.get(automaticRoleId));
		// request.setAutomaticRole(null);
		//
		// } else if (RequestOperationType.ADD == request.getOperation()) {
		// // Create new tree automatic role
		// IdmRoleTreeNodeDto treeAutomaticRole = new IdmRoleTreeNodeDto();
		// treeAutomaticRole = initTreeAutomaticRole(request, treeAutomaticRole);
		// // Recount of assigned roles ensures LRT after save
		// treeAutomaticRole = automaticRoleTreeService.save(treeAutomaticRole);
		// request.setAutomaticRole(treeAutomaticRole.getId());
		// } else {
		// // Update is not supported
		// throw new ResultCodeException(CoreResultCode.METHOD_NOT_ALLOWED,
		// "Tree automatic role update is not supported");
		// }
		// }

		request.setState(RequestState.EXECUTED);
		request.setResult(new OperationResultDto.Builder(OperationState.EXECUTED).build());
		return requestService.save(request);
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
	public Requestable post(Serializable requestId, Requestable dto) {
		ReadDtoService<Requestable, ?> dtoReadService = getDtoService(dto);
		boolean isNew = dtoReadService.isNew(dto);
		return this.post(requestId, dto, isNew);
	}

	private Requestable post(Serializable requestId, Requestable dto, boolean isNew) {
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
				item.setOriginalOwnerId((UUID) dto.getId());
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
	public Requestable delete(Serializable requestId, Requestable dto) {
		Assert.notNull(dto, "DTO is required!");
		Assert.notNull(requestId, "Request ID is required!");
		// TODO: Rights!

		IdmRequestDto request = requestService.get(requestId);
		// Exists item for same original owner?
		IdmRequestItemDto item = this.findRequestItem(request.getId(), dto);
		// If this item already exists for ADD or UPDATE, then we want to delete him.
		if (item != null && RequestOperationType.REMOVE != item.getOperation()) {
			requestItemService.delete(item);
			return null;
		}
		item = createRequestItem(request.getId(), dto);
		item.setOriginalOwnerId((UUID) dto.getId());
		item.setOperation(RequestOperationType.REMOVE);
		item.setData(null);
		// Update or create new request item
		item = requestItemService.save(item);
		// Set ID of request item to result DTO
		dto.setRequestItem(item.getId());

		return dto;
	}

	@Override
	public Requestable get(Serializable requestId, Requestable dto) {
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
			BaseDto requestedDto = this.convertStringToDto(item.getData(), dto.getClass());
			addRequestItemToDto((Requestable) requestedDto, item);
			this.addEmbedded((AbstractDto) requestedDto, request.getId());
			return (Requestable) requestedDto;

		} catch (IOException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| IntrospectionException | InstantiationException e) {
			throw new ResultCodeException(CoreResultCode.JSON_CANNOT_BE_CONVERT_TO_DTO,
					ImmutableMap.of("json", item.getData()), e);
		}
	}

	@Override
	public Page<Requestable> find(Class<? extends Requestable> dtoClass, Serializable requestId, BaseFilter filter,
			Pageable pageable, IdmBasePermission permission) {
		ReadDtoService<Requestable, BaseFilter> dtoReadService = getDtoService(dtoClass);
		Page<Requestable> originalPage = dtoReadService.find(filter, pageable, permission);
		List<Requestable> originals = originalPage.getContent();
		List<Requestable> results = new ArrayList<>();

		IdmRequestDto request = requestService.get(requestId);
		List<IdmRequestItemDto> items = this.findRequestItems(request.getId(), dtoClass);

		originals.stream().forEach(dto -> {
			IdmRequestItemDto item = items.stream() //
					.filter(i -> dto.getId().equals(i.getOriginalOwnerId())) //
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
				BaseDto requestedDto = this.convertStringToDto(item.getData(), dtoClass);
				addEmbedded((AbstractDto) requestedDto, request.getId());
				addRequestItemToDto((Requestable) requestedDto, item);
				results.add((Requestable) requestedDto);
				return;

			} catch (IOException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
					| InstantiationException | IntrospectionException e) {
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
	public IdmRequestDto createRequest(Requestable dto) {
		Assert.notNull(dto, "DTO is required!");
		// TODO: Rights!

		if (dto.getId() == null) {
			dto.setId(UUID.randomUUID());
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
		this.post(request.getId(), dto);

		return request;
	}

	@Override
	@Transactional
	public IdmFormInstanceDto saveFormInstance(UUID requestId, Requestable owner, IdmFormDefinitionDto formDefinition,
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
	public IdmFormInstanceDto getFormInstance(UUID requestId, Requestable owner, IdmFormDefinitionDto formDefinition,
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
				requestValues.add((IdmFormValueDto) this.get(requestId, value));
			});
		}

		IdmRequestDto request = requestService.get(requestId);
		// Load all added items for that request
		List<IdmRequestItemDto> addedItems = this.findRequestItems(request.getId(), IdmFormValueDto.class,
				RequestOperationType.ADD);
		// Find added items for that owner ID
		List<Requestable> relatedAddedItems = this.findRelatedAddedItems(request,
				ImmutableList.of((UUID) owner.getId()), addedItems, IdmFormValueDto.class);

		requestValues.addAll((Collection<? extends IdmFormValueDto>) relatedAddedItems);

		return new IdmFormInstanceDto(owner, formDefinition, requestValues);
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
	private List<Requestable> findRelatedAddedItems(IdmRequestDto request, List<UUID> potencialParents,
			List<IdmRequestItemDto> items, Class<? extends Requestable> dtoClass) {
		List<Requestable> results = new ArrayList<>();
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
				BaseDto requestedDto = this.convertStringToDto(item.getData(), dtoClass);
				AbstractDto requested = (AbstractDto) requestedDto;
				addEmbedded(requested, request.getId());
				addRequestItemToDto((Requestable) requested, item);
				results.add((Requestable) requestedDto);
				return;

			} catch (IOException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
					| IntrospectionException | InstantiationException e) {
				throw new ResultCodeException(CoreResultCode.JSON_CANNOT_BE_CONVERT_TO_DTO,
						ImmutableMap.of("json", item.getData()), e);
			}
		});

		return results;
	}

	private String convertDtoToString(BaseDto dto) throws JsonProcessingException {
		return mapper.writerFor(dto.getClass()).writeValueAsString(dto);
	}

	private BaseDto convertStringToDto(String data, Class<? extends BaseDto> type)
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
		itemFilter.setOriginalOwnerId((UUID) dto.getId());
		itemFilter.setOriginalType(dto.getClass().getName());
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
		itemFilter.setOriginalType(dtoClass.getName());
		itemFilter.setOperationType(operation);

		return requestItemService.find(itemFilter, null).getContent();
	}

	private IdmRequestItemDto createRequestItem(UUID requestId, Requestable dto) {
		IdmRequestItemDto item = new IdmRequestItemDto();
		item.setRequest(requestId);
		item.setOwnerType(dto.getClass().getName());
		item.setResult(new OperationResultDto(OperationState.CREATED));
		item.setState(RequestState.CONCEPT);
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
					.filter(methodDescriptor -> methodDescriptor.getParameterDescriptors() == null
							|| methodDescriptor.getParameterDescriptors().length == 0) //
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
	 * Get read DTO service for given DTO
	 *
	 * @param dto
	 * @return
	 */
	private ReadDtoService<Requestable, BaseFilter> getDtoService(Requestable dto) {
		Class<? extends Requestable> dtoClass = dto.getClass();
		return this.getDtoService(dtoClass);
	}

	/**
	 * Get read DTO service for given DTO class
	 *
	 * @param dto
	 * @return
	 */
	private ReadDtoService<Requestable, BaseFilter> getDtoService(Class<? extends Requestable> dtoClass) {
		@SuppressWarnings("unchecked")
		ReadDtoService<Requestable, BaseFilter> dtoReadService = (ReadDtoService<Requestable, BaseFilter>) lookupService
				.getDtoService(dtoClass);
		return dtoReadService;
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
							Requestable originalEmbeddedDto = this.getDtoService((Requestable) embeddedDto)
									.get(embeddedDto.getId());
							if (originalEmbeddedDto != null) {
								// Call standard method for load request's DTO with original DTO
								embeddedDto = (AbstractDto) this.get(requestId, originalEmbeddedDto);
							} else {
								// Call standard method for load request's DTO with mock DTO (only with ID)
								embeddedDto = (AbstractDto) this.get(requestId, (Requestable) embeddedDto);
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
	private List<IdmFormValueDto> saveAttributeValues(UUID requestId, Requestable owner, IdmFormAttributeDto attribute,
			List<IdmFormValueDto> previousValues, List<IdmFormValueDto> newValues, BasePermission... permission) {

		ReadDtoService<Requestable, ?> dtoReadService = getDtoService(owner);
		Class<? extends BaseEntity> entityClass = dtoReadService.getEntityClass();
		if (!FormableEntity.class.isAssignableFrom(entityClass)) {
			throw new IllegalArgumentException(MessageFormat.format("Form owner [{0}] is not FormableEntity [{1}]!",
					owner.toString(), entityClass));
		}
		@SuppressWarnings("unchecked")
		Class<? extends FormableEntity> ownerClass = (Class<? extends FormableEntity>) entityClass;

		IdmFormDefinitionDto formDefinition = formDefinitionService.get(attribute.getFormDefinition());

		List<IdmFormValueDto> results = new ArrayList<>();
		Map<UUID, IdmFormValueDto> unprocessedPreviousValues = new LinkedHashMap<>(); // ordered by seq
		if (CollectionUtils.isNotEmpty(previousValues)) {
			previousValues.forEach(previousValue -> {
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
					results.add((IdmFormValueDto) this.delete(requestId, value));
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
					results.add((IdmFormValueDto) this.post(requestId, newValue, this.isFormValueNew(previousValue)));
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
							results.add((IdmFormValueDto) this.post(requestId, previousValue,
									this.isFormValueNew(previousValue)));
						}
					} else {
						results.add((IdmFormValueDto) this.delete(requestId, previousValue));
					}
				}
			}
		}
		// remove unprocessed values
		// confidential property will be removed too => none or all confidential values
		// have to be given for multiple attributes
		unprocessedPreviousValues.values().forEach(previousValue -> {
			results.add((IdmFormValueDto) this.delete(requestId, previousValue));
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
		Requestable persistedRequestDto = this.post(requestId, confidentialFormValue,
				this.isFormValueNew(confidentialFormValue));
		UUID requestItem = persistedRequestDto.getRequestItem();
		Assert.notNull(requestItem);

		// Save confidential value to ConfidentialStorage - owner is request item
		confidentialStorage.save(requestItem, IdmRequestItem.class, getConfidentialStorageKey(requestItem),
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

	private RequestManager getRequestManager() {
		if (this.requestManager == null) {
			this.requestManager = applicationContext.getBean(RequestManager.class);
		}
		return this.requestManager;
	}

	private String getConfidentialStorageKey(UUID itemId) {
		Assert.notNull(itemId);
		//
		return FormValueService.CONFIDENTIAL_STORAGE_VALUE_PREFIX + ":" + itemId;
	}

	private Serializable getConfidentialPersistentValue(IdmRequestItemDto confidentialItem) {
		Assert.notNull(confidentialItem);
		//
		return confidentialStorage.get(confidentialItem.getId(), IdmRequestItem.class,
				getConfidentialStorageKey(confidentialItem.getId()));
	}
}
