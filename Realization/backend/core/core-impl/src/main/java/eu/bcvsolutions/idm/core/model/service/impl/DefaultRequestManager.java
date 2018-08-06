package eu.bcvsolutions.idm.core.model.service.impl;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.MethodDescriptor;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.activiti.engine.runtime.ProcessInstance;
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
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.Embedded;
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
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.exception.CoreException;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.exception.RoleRequestException;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.IdmRequestItemService;
import eu.bcvsolutions.idm.core.api.service.IdmRequestService;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.api.service.ReadDtoService;
import eu.bcvsolutions.idm.core.api.service.RequestManager;
import eu.bcvsolutions.idm.core.api.utils.EntityUtils;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.event.RequestEvent;
import eu.bcvsolutions.idm.core.model.event.RequestEvent.RequestEventType;
import eu.bcvsolutions.idm.core.model.event.processor.role.RoleRequestApprovalProcessor;
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

	@Override
	public Requestable post(Serializable requestId, Requestable dto) {
		Assert.notNull(dto, "DTO is required!");
		Assert.notNull(requestId, "Request ID is required!");
		// TODO: Rights!

		ReadDtoService<Requestable, ?> dtoReadService = getDtoService(dto);

		IdmRequestDto request = requestService.get(requestId);
		boolean isNew = dtoReadService.isNew(dto);
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
	public Requestable delete(Serializable requestId, Requestable dto) {
		Assert.notNull(dto, "DTO is required!");
		Assert.notNull(requestId, "Request ID is required!");
		// TODO: Rights!

		IdmRequestDto request = requestService.get(requestId);
		// Exists item for same original owner?
		IdmRequestItemDto item = this.findRequestItem(request.getId(), dto);
		if (item == null) {
			item = createRequestItem(request.getId(), dto);
			item.setOriginalOwnerId((UUID) dto.getId());
		}
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

		// Set all results as trimmed = true. Frontend expects trimmed value in the
		// table.
		results.forEach(result -> ((AbstractDto) result).setTrimmed(true));

		return new PageImpl<>(results, pageable, originalPage.getTotalElements());
	}

	@Override
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

		return requestService.save(request);
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
		IdmRequestItemFilter itemFilter = new IdmRequestItemFilter();
		itemFilter.setRequestId(requestId);
		itemFilter.setOriginalType(dtoClass.getName());
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
}
