package eu.bcvsolutions.idm.vs.service.impl;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.entity.SysSystem_;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.SynchronizationEntityExecutor;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestState;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.OperationResultDto;
import eu.bcvsolutions.idm.core.api.event.EventContext;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.config.domain.DynamicCorsConfiguration;
import eu.bcvsolutions.idm.core.notification.api.domain.NotificationLevel;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmMessageDto;
import eu.bcvsolutions.idm.core.notification.api.service.NotificationManager;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;
import eu.bcvsolutions.idm.ic.api.IcAttribute;
import eu.bcvsolutions.idm.ic.api.IcAttributeInfo;
import eu.bcvsolutions.idm.ic.api.IcConnectorObject;
import eu.bcvsolutions.idm.ic.api.IcUidAttribute;
import eu.bcvsolutions.idm.ic.czechidm.service.impl.CzechIdMIcConfigurationService;
import eu.bcvsolutions.idm.ic.czechidm.service.impl.CzechIdMIcConnectorService;
import eu.bcvsolutions.idm.ic.exception.IcException;
import eu.bcvsolutions.idm.ic.impl.IcConnectorObjectImpl;
import eu.bcvsolutions.idm.ic.impl.IcUidAttributeImpl;
import eu.bcvsolutions.idm.vs.VirtualSystemModuleDescriptor;
import eu.bcvsolutions.idm.vs.connector.api.VsVirtualConnector;
import eu.bcvsolutions.idm.vs.connector.basic.BasicVirtualConfiguration;
import eu.bcvsolutions.idm.vs.domain.VirtualSystemGroupPermission;
import eu.bcvsolutions.idm.vs.domain.VsOperationType;
import eu.bcvsolutions.idm.vs.domain.VsRequestState;
import eu.bcvsolutions.idm.vs.domain.VsValueChangeType;
import eu.bcvsolutions.idm.vs.dto.VsAccountDto;
import eu.bcvsolutions.idm.vs.dto.VsAttributeDto;
import eu.bcvsolutions.idm.vs.dto.VsAttributeValueDto;
import eu.bcvsolutions.idm.vs.dto.VsConnectorObjectDto;
import eu.bcvsolutions.idm.vs.dto.VsRequestDto;
import eu.bcvsolutions.idm.vs.dto.filter.VsRequestFilter;
import eu.bcvsolutions.idm.vs.entity.VsRequest;
import eu.bcvsolutions.idm.vs.entity.VsRequest_;
import eu.bcvsolutions.idm.vs.event.VsRequestEvent;
import eu.bcvsolutions.idm.vs.event.VsRequestEvent.VsRequestEventType;
import eu.bcvsolutions.idm.vs.event.processor.VsRequestRealizationProcessor;
import eu.bcvsolutions.idm.vs.exception.VsException;
import eu.bcvsolutions.idm.vs.exception.VsResultCode;
import eu.bcvsolutions.idm.vs.repository.VsRequestRepository;
import eu.bcvsolutions.idm.vs.service.api.VsAccountService;
import eu.bcvsolutions.idm.vs.service.api.VsRequestService;
import eu.bcvsolutions.idm.vs.service.api.VsSystemImplementerService;
import eu.bcvsolutions.idm.vs.service.api.VsSystemService;

/**
 * Service for request in virtual system
 * 
 * @author Svanda
 *
 */
@Service
public class DefaultVsRequestService extends AbstractReadWriteDtoService<VsRequestDto, VsRequest, VsRequestFilter>
		implements VsRequestService {

	private static final Logger LOG = LoggerFactory.getLogger(DefaultVsRequestService.class);

	private final EntityEventManager entityEventManager;
	private final VsSystemImplementerService requestImplementerService;
	private final SysSystemService systemService;
	private final NotificationManager notificationManager;
	private final IdmIdentityService identityService;
	private final VsAccountService accountService;
	private final ConfigurationService configurationService;
	@Autowired
	private VsSystemService vsSystemService;
	@Autowired
	private IdmRoleRequestService roleRequestService;
	@Autowired
	private AccAccountService accAccountService;

	@Autowired
	public DefaultVsRequestService(VsRequestRepository repository, EntityEventManager entityEventManager,
			VsSystemImplementerService requestImplementerService, CzechIdMIcConnectorService czechIdMConnectorService,
			CzechIdMIcConfigurationService czechIdMConfigurationService, SysSystemService systemService,
			NotificationManager notificationManager, IdmIdentityService identityService,
			VsAccountService accountService, ConfigurationService configurationService) {
		super(repository);
		//
		Assert.notNull(entityEventManager, "Manager is required.");
		Assert.notNull(requestImplementerService, "Service is required.");
		Assert.notNull(czechIdMConnectorService, "Service is required.");
		Assert.notNull(czechIdMConfigurationService, "Service is required.");
		Assert.notNull(systemService, "Service is required.");
		Assert.notNull(notificationManager, "Manager is required.");
		Assert.notNull(identityService, "Service is required.");
		Assert.notNull(accountService, "Service is required.");
		Assert.notNull(configurationService, "Service is required.");

		this.entityEventManager = entityEventManager;
		this.requestImplementerService = requestImplementerService;
		this.systemService = systemService;
		this.notificationManager = notificationManager;
		this.identityService = identityService;
		this.accountService = accountService;
		this.configurationService = configurationService;
	}
	
	@Override
	@Transactional
	public VsRequestDto realize(VsRequestDto request) {
		return this.realize(request, null);
	}

	@Override
	@Transactional
	public VsRequestDto realize(VsRequestDto request, String reason) {
		LOG.info(MessageFormat.format("Start realize virtual system request [{0}].", request));

		Assert.notNull(request, "VS request cannot be null!");
		this.checkAccess(request, IdmBasePermission.UPDATE);

		if (VsRequestState.IN_PROGRESS != request.getState()) {
			throw new VsException(VsResultCode.VS_REQUEST_REALIZE_WRONG_STATE,
					ImmutableMap.of("state", VsRequestState.IN_PROGRESS.name(), "currentState", request.getState()));
		}
		
		request.setState(VsRequestState.REALIZED);
		request.setReason(reason);
		// Realize request ... propagate change to VS account.
		IcUidAttribute uidAttribute = this.internalExecute(request);
		// Save realized request
		request = this.save(request);

		LOG.info(MessageFormat.format("Virtual system request [{0}] was realized. Output UID attribute: [{1}]", request,
				uidAttribute));
		// Refresh role-request state
		refreshRoleRequestState(request);
		
		return request;
	}

	@Override
	public VsRequestDto cancel(VsRequestDto request, String reason) {
		LOG.info(MessageFormat.format("Start cancel virtual system request [{0}].", request));

		Assert.notNull(reason, "Cancel reason cannot be null!");
		Assert.notNull(request, "VS request cannot be null!");
		this.checkAccess(request, IdmBasePermission.UPDATE);

		if (VsRequestState.IN_PROGRESS != request.getState()) {
			throw new VsException(VsResultCode.VS_REQUEST_CANCEL_WRONG_STATE,
					ImmutableMap.of("state", VsRequestState.IN_PROGRESS.name(), "currentState", request.getState()));
		}

		request.setState(VsRequestState.CANCELED);
		request.setReason(reason);
		// Save cancelled request
		request = this.save(request);

		LOG.info(MessageFormat.format("Virtual system request [{0}] for UID [{1}] was canceled.", request,
				request.getUid()));
		// Refresh role-request state
		refreshRoleRequestState(request);
		
		return request;
	}

	@Override
	@Transactional
	public IcUidAttribute execute(VsRequestDto request) {
		EventContext<VsRequestDto> event = entityEventManager
				.process(new VsRequestEvent(VsRequestEventType.EXCECUTE, request));
		return (IcUidAttribute) event.getLastResult().getEvent().getProperties()
				.get(VsRequestRealizationProcessor.RESULT_UID);
	}

	@Override
	@Transactional
	public VsRequestDto createRequest(VsRequestDto req) {
		Assert.notNull(req, "Request cannot be null!");

		// Save new request
		req.setState(VsRequestState.CONCEPT);
		VsRequestDto request = this.save(req);
		return this.get(request.getId());
	}

	@Override
	public IcUidAttribute internalStart(VsRequestDto request) {
		Assert.notNull(request, "Request cannot be null!");

		// Unfinished requests for same UID and system
		List<VsRequestDto> duplicities = this.findDuplicities(request.getUid(), request.getSystem());
		request.setState(VsRequestState.IN_PROGRESS);

		if (!CollectionUtils.isEmpty(duplicities)) {
			// Get the newest request (for same operation)
			VsRequestDto previousRequest = this.getPreviousRequest(request.getOperationType(), duplicities);
			// Load untrimed request
			if (previousRequest != null) {
				// previousRequest = this.get(previousRequest.getId());
				// Shows on previous request with same operation type. We need
				// this for create diff.
				request.setPreviousRequest(previousRequest.getId());

				if (this.isRequestSame(request, previousRequest)) {
					request.setDuplicateToRequest(previousRequest.getId());
					request.setState(VsRequestState.DUPLICATED);
				}
			}
		}
		request = this.save(request);

		if (VsRequestState.DUPLICATED == request.getState()) {
			return null;
		}

		if (request.isExecuteImmediately()) {
			// Request will be realized now
			return internalExecute(request);
		}

		// Find previous request ... not matter on operation type. Simple get
		// last request.
		VsRequestDto lastRequest = this.getPreviousRequest(null, duplicities);

		if (lastRequest != null) {
			// Send update message
			this.sendNotification(request, this.get(lastRequest.getId()));
		} else {
			// Send new message
			this.sendNotification(request, null);
		}
		return null;
	}

	@Override
	public boolean supportsToDtoWithFilter() {
		return true;
	}
	
	@Override
	protected VsRequestDto toDto(VsRequest entity, VsRequestDto dto, VsRequestFilter filter) {
		dto = super.toDto(entity, dto, filter);
		if (dto != null && dto.getSystem() != null && dto.getUid() != null && filter != null && filter.isIncludeOwner()) {
			// Load and set target entity. For loading a target entity is using sync
			// executor. Owner loading is processed only if filter "includeOwner" is present!
			AbstractDto targetEntity = findTargetEntity(dto);
			if (targetEntity != null) {
				dto.setTargetEntity(targetEntity);
				dto.setTargetEntityType(targetEntity.getClass().getName());
			}
		}

		return dto;
	}

	/**
	 * Find target entity for given request.
	 * For loading a target entity is using sync executor.
	 *
	 * @param dto
	 * @return
	 */
	private AbstractDto findTargetEntity(VsRequestDto dto) {
		if (dto.getUid() == null || dto.getSystem() == null) {
			return null;
		}
		AccAccountDto account = accAccountService.getAccount(dto.getUid(), dto.getSystem());
		if (account != null) {
			SystemEntityType entityType = account.getEntityType();
			if (entityType != null && entityType.isSupportsSync()) {
				SynchronizationEntityExecutor executor = accAccountService.getSyncExecutor(entityType);
				return executor.getDtoByAccount(null, account);
			}
		}
		return null;
	}

	@Override
	public IcUidAttribute internalExecute(VsRequestDto request) {
		request.setState(VsRequestState.REALIZED);
		Assert.notNull(request.getConfiguration(), "Request have to contains connector configuration!");
		Assert.notNull(request.getConnectorKey(), "Request have to contains connector key!");
		// Find connector by request
		VsVirtualConnector virtualConnector = getVirtualConnector(request);

		IcUidAttribute result = null;

		// Save the request
		this.save(request);
		switch (request.getOperationType()) {
		case CREATE: {
			result = virtualConnector.internalCreate(request.getConnectorObject().getObjectClass(),
					request.getConnectorObject().getAttributes());
			break;
		}
		case UPDATE: {
			VsAccountDto account = accountService.findByUidSystem(request.getUid(), request.getSystem());
			if (account == null) {
				throw new VsException(VsResultCode.VS_REQUEST_UPDATING_ACCOUNT_NOT_EXIST,
						ImmutableMap.of("uid", request.getUid()));
			}
			result = virtualConnector.internalUpdate(new IcUidAttributeImpl(null, request.getUid(), null),
					request.getConnectorObject().getObjectClass(), request.getConnectorObject().getAttributes());
			break;
		}
		case DELETE: {
			VsAccountDto account = accountService.findByUidSystem(request.getUid(), request.getSystem());
			if (account == null) {
				throw new VsException(VsResultCode.VS_REQUEST_DELETING_ACCOUNT_NOT_EXIST,
						ImmutableMap.of("uid", request.getUid()));
			}
			virtualConnector.internalDelete(new IcUidAttributeImpl(null, request.getUid(), null),
					request.getConnectorObject().getObjectClass());
			// All unresolved request created before this delete request will be
			// canceled
			VsRequestFilter filter = new VsRequestFilter();
			filter.setCreatedBefore(request.getCreated());
			filter.setUid(request.getUid());
			filter.setSystemId(request.getSystem());
			filter.setState(VsRequestState.IN_PROGRESS);
			// Unresolved request created before this request
			List<VsRequestDto> beforeRequests = this.find(filter, null).getContent();

			beforeRequests.forEach(beforeRequest -> {
				String reason = MessageFormat.format(
						"Request [{0}] was canceled (by SYSTEM), because 'after' delete request [{1}] was realized!",
						beforeRequest.getId(), request.getId());
				this.cancel(beforeRequest, reason);
				LOG.info(reason);

			});
			break;
		}

		default:
			throw new IcException(MessageFormat.format("Unsupported operation type [{0}]", request.getOperationType()));
		}
		return result;
	}

	@Override
	public IcConnectorObject getConnectorObject(VsRequestDto request) {
		LOG.info(MessageFormat.format("Start read connector object [{0}].", request));
		Assert.notNull(request, "VS request cannot be null!");
		Assert.notNull(request.getConnectorObject(), "Connector object in request cannot be null!");

		return this.systemService.readConnectorObject(request.getSystem(), request.getUid(),
				request.getConnectorObject().getObjectClass());
	}

	@Override
	public IcConnectorObject getVsConnectorObject(VsRequestDto request) {
		LOG.info(MessageFormat.format("Start read vs connector object [{0}].", request));
		Assert.notNull(request, "VS request cannot be null!");
		Assert.notNull(request.getConnectorObject(), "Connector object in request cannot be null!");

		// Find account by UID and System ID
		VsAccountDto account = accountService.findByUidSystem(request.getUid(), request.getSystem());
		if (account == null) {
			return null;
		}

		BasicVirtualConfiguration configuration = getVirtualConnector(request).getVirtualConfiguration();
		List<IcAttribute> attributes = accountService.getIcAttributes(account);
		// If system does not support enable, then enable attribute will be removed from
		// result list
		if (!configuration.isDisableSupported()) {
			IcAttribute enableAttribute = attributes.stream()
					.filter(attribute -> attribute.getName().equals(IcAttributeInfo.ENABLE)).findFirst().orElse(null);
			if (enableAttribute != null) {
				attributes.remove(enableAttribute);
			}
		}
		IcConnectorObjectImpl connectorObject = new IcConnectorObjectImpl();
		connectorObject.setUidValue(account.getUid());
		connectorObject.setObjectClass(request.getConnectorObject().getObjectClass());
		connectorObject.setAttributes(attributes);
		return connectorObject;
	}

	@Override
	public VsConnectorObjectDto getWishConnectorObject(VsRequestDto request) {
		LOG.info(MessageFormat.format("Start read wish connector object [{0}].", request));
		Assert.notNull(request, "VS request cannot be null!");

		List<VsAttributeDto> resultAttributes = new ArrayList<>();
		IcConnectorObject realConnectorObject = null;

		boolean isArchived = false;
		if (VsRequestState.REALIZED == request.getState() 
				|| VsRequestState.CANCELED == request.getState() 
				|| VsRequestState.DUPLICATED == request.getState() 
				|| VsRequestState.REJECTED == request.getState()) {
			isArchived = true;
		}
		
		// We don't want use current object, when request is archived. We want to show
		// only changes.
		if (!isArchived) {
			realConnectorObject = this.getVsConnectorObject(request);
		}

		IcConnectorObject currentObject = realConnectorObject != null ? realConnectorObject
				: new IcConnectorObjectImpl();
		IcConnectorObject changeObject = request.getConnectorObject() != null ? request.getConnectorObject()
				: new IcConnectorObjectImpl();
		List<IcAttribute> currentAttributes = currentObject.getAttributes();
		List<IcAttribute> changedAttributes = request.getConnectorObject().getAttributes();

		// First add all new attributes
		changedAttributes.forEach(changedAttribute -> {
			if (currentObject.getAttributeByName(changedAttribute.getName()) == null) {
				VsAttributeDto vsAttribute = new VsAttributeDto(changedAttribute.getName(),
						changedAttribute.isMultiValue(), true);
				if (changedAttribute.isMultiValue()) {
					if (changedAttribute.getValues() != null) {
						changedAttribute.getValues().forEach(value -> {
							vsAttribute.getValues().add(new VsAttributeValueDto(value, null, VsValueChangeType.ADDED));
						});
					}
				} else {
					vsAttribute.setValue(
							new VsAttributeValueDto(changedAttribute.getValue(), null, VsValueChangeType.ADDED));
				}
				resultAttributes.add(vsAttribute);
			}
		});

		// Second add all already exists attributes
		currentAttributes.forEach(currentAttribute -> {
			VsAttributeDto vsAttribute;
			// Attribute was changed
			if (changeObject.getAttributeByName(currentAttribute.getName()) != null) {
				vsAttribute = new VsAttributeDto(currentAttribute.getName(), currentAttribute.isMultiValue(), true);
				IcAttribute changedAttribute = changeObject.getAttributeByName(currentAttribute.getName());
				if (changedAttribute.isMultiValue()) {
					vsAttribute.setChanged(false);
					if (changedAttribute.getValues() != null) {
						changedAttribute.getValues().forEach(value -> {
							if (currentAttribute.getValues() != null && currentAttribute.getValues().contains(value)) {
								vsAttribute.getValues().add(new VsAttributeValueDto(value, value, null));
							} else {
								vsAttribute.setChanged(true);
								vsAttribute.getValues()
										.add(new VsAttributeValueDto(value, null, VsValueChangeType.ADDED));
							}
						});
					}
					if (currentAttribute.getValues() != null) {
						currentAttribute.getValues().forEach(value -> {
							if (changedAttribute.getValues() == null || !changedAttribute.getValues().contains(value)) {
								vsAttribute.setChanged(true);
								vsAttribute.getValues()
										.add(new VsAttributeValueDto(value, value, VsValueChangeType.REMOVED));
							}
						});
					}
				} else {
					Object changedValue = changedAttribute.getValue();
					Object currentValue = currentAttribute.getValue();
					if ((changedValue == null && currentValue == null)
							|| (changedValue != null && changedValue.equals(currentValue))
							|| (currentValue != null && currentValue.equals(changedValue))) {

						vsAttribute.setValue(new VsAttributeValueDto(changedValue, currentValue, null));
					} else {
						vsAttribute.setValue(
								new VsAttributeValueDto(changedValue, currentValue, VsValueChangeType.UPDATED));
					}
				}
			} else {
				// Attribute was not changed
				vsAttribute = new VsAttributeDto(currentAttribute.getName(), currentAttribute.isMultiValue(), false);
				if (currentAttribute.isMultiValue()) {
					if (currentAttribute.getValues() != null) {
						currentAttribute.getValues().forEach(value -> {
							vsAttribute.getValues().add(new VsAttributeValueDto(value, value, null));
						});
					}
				} else {
					vsAttribute.setValue(
							new VsAttributeValueDto(currentAttribute.getValue(), currentAttribute.getValue(), null));
				}
			}
			resultAttributes.add(vsAttribute);
		});

		BasicVirtualConfiguration configuration = getVirtualConnector(request).getVirtualConfiguration();
		// If system does not support enable, then enable attribute will be removed from
		// result list
		if (!configuration.isDisableSupported()) {
			VsAttributeDto enableAttribute = resultAttributes.stream()
					.filter(attribute -> attribute.getName().equals(IcAttributeInfo.ENABLE)).findFirst().orElse(null);
			if (enableAttribute != null) {
				resultAttributes.remove(enableAttribute);
			}
		}

		VsConnectorObjectDto wishObject = new VsConnectorObjectDto();
		wishObject.setUid(request.getUid());
		wishObject.setAttributes(resultAttributes);

		return wishObject;
	}

	/**
	 * Find duplicity requests. All request in state IN_PROGRESS for same UID and
	 * system. For all operation types.
	 * 
	 * @return
	 */
	@Override
	public List<VsRequestDto> findDuplicities(String uid, UUID systemId) {
		VsRequestFilter filter = new VsRequestFilter();
		filter.setUid(uid);
		filter.setSystemId(systemId);
		filter.setState(VsRequestState.IN_PROGRESS);
		Sort sort = new Sort(Direction.DESC, VsRequest_.created.getName());
		return this.find(filter, PageRequest.of(0, Integer.MAX_VALUE, sort)).getContent();
	}

	@Override
	protected List<Predicate> toPredicates(Root<VsRequest> root, CriteriaQuery<?> query, CriteriaBuilder builder,
			VsRequestFilter filter) {

		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);
		//
		// quick - "fulltext"
		if (StringUtils.isNotEmpty(filter.getText())) {
			predicates.add(builder.or(
					builder.like(builder.lower(root.get(VsRequest_.uid)), "%" + filter.getText().toLowerCase() + "%")));
		}

		// UID
		if (StringUtils.isNotEmpty(filter.getUid())) {
			predicates.add(builder.equal(root.get(VsRequest_.uid), filter.getUid()));
		}

		// System ID
		if (filter.getSystemId() != null) {
			predicates.add(builder.equal(root.get(VsRequest_.system).get(SysSystem_.id), filter.getSystemId()));
		}

		// State
		if (filter.getState() != null) {
			predicates.add(builder.equal(root.get(VsRequest_.state), filter.getState()));
		}

		// Operation type
		if (filter.getOperationType() != null) {
			predicates.add(builder.equal(root.get(VsRequest_.operationType), filter.getOperationType()));
		}

		// Created before
		if (filter.getCreatedBefore() != null) {
			predicates.add(builder.lessThan(root.get(VsRequest_.created), filter.getCreatedBefore()));
		}

		// Created after
		if (filter.getCreatedAfter() != null) {
			predicates.add(builder.greaterThan(root.get(VsRequest_.created), filter.getCreatedAfter()));
		}
		
		// Modified before
		if (filter.getModifiedBefore() != null) {
			predicates.add(builder.lessThan(root.get(VsRequest_.modified), filter.getModifiedBefore()));
		}

		// Modified after
		if (filter.getModifiedAfter() != null) {
			predicates.add(builder.greaterThan(root.get(VsRequest_.modified), filter.getModifiedAfter()));
		}

		// Only archived
		if (filter.getOnlyArchived() != null) {
			predicates.add(builder.or(//
					builder.equal(root.get(VsRequest_.state), VsRequestState.REALIZED), //
					builder.equal(root.get(VsRequest_.state), VsRequestState.CANCELED), //
					builder.equal(root.get(VsRequest_.state), VsRequestState.REJECTED), //
					builder.equal(root.get(VsRequest_.state), VsRequestState.DUPLICATED)));
		}
		
		// Role-request
		if (filter.getRoleRequestId() != null) {
			predicates.add(builder.equal(root.get(VsRequest_.roleRequestId), filter.getRoleRequestId()));
		}

		return predicates;
	}

	@Override
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(VirtualSystemGroupPermission.VSREQUEST, getEntityClass());
	}
	
	/**
	 * Optimize - system can be pre-loaded in DTO.
	 * 
	 * @param request
	 * @return
	 */
	@Override
	public SysSystemDto getSystem(VsRequestDto request) {
		SysSystemDto system = DtoUtils.getEmbedded(request, VsRequest_.system.getName(), (SysSystemDto) null);
		if (system == null) {
			// just for sure, self constructed operation can be given
			system = systemService.get(request.getSystem());
		}
		//
		return system;
	}

	/**
	 * Get virtual connector by vs request
	 * 
	 * @param request
	 * @return
	 */
	private VsVirtualConnector getVirtualConnector(VsRequestDto request) {
		Assert.notNull(request.getSystem(), "System is required.");
		return vsSystemService.getVirtualConnector(request.getSystem(), request.getConnectorKey());
	}

	private void sendNotification(VsRequestDto request, VsRequestDto previous) {
		Assert.notNull(request, "VS request cannot be null for send notification!");

		List<IdmIdentityDto> implementers = this.requestImplementerService.findRequestImplementers(request.getSystem(),
				50);
		if (implementers.isEmpty()) {
			// We do not have any implementers ... we don`t have anyone to send
			// notification
			LOG.warn(MessageFormat.format(
					"Notification cannot be send! We do not have any implementers in request [{0}].", request.getId()));
			return;
		}

		// Identity is find by target entity (entity connected to AccAccount).
		VsConnectorObjectDto wish = this.getWishConnectorObject(request);
		IdmIdentityDto identity = this.getIdentity(request);
		SysSystemDto system = systemService.get(request.getSystem());

		// For information about role request and role request applicant
		UUID roleRequestId = request.getRoleRequestId();
		IdmRoleRequestDto roleRequestDto = null;
		IdmIdentityDto roleRequestCreator = null;
		if (roleRequestId != null) {
			roleRequestDto = roleRequestService.get(roleRequestId);
			if (roleRequestDto != null) {
				UUID roleRequestCreatorId = roleRequestDto.getCreatorId();
				if (roleRequestCreatorId != null) {
					roleRequestCreator = identityService.get(roleRequestCreatorId);
				}
			}
		}
		String targetEntityUrl = null;
		if (identity != null) {
			targetEntityUrl = configurationService.getFrontendUrl(MessageFormat.format("identity/{0}/profile", identity.getId()));
		}
		
		// send create notification
		notificationManager.send(VirtualSystemModuleDescriptor.TOPIC_VS_REQUEST_CREATED, new IdmMessageDto.Builder()
				.setLevel(NotificationLevel.INFO)
				.addParameter("requestAttributes",
						request.getConnectorObject() != null ? request.getConnectorObject().getAttributes() : null)
				.addParameter("wishAttributes", wish.getAttributes())//
				.addParameter("fullName", identityService.getNiceLabel(identity))//
				.addParameter("identity", identity)//
				.addParameter("url", getUrl(request))//
				.addParameter("previousUrl", getUrl(previous))//
				.addParameter("targetEntityUrl", targetEntityUrl)//
				.addParameter("request", request)//
				.addParameter("roleRequestCreator", roleRequestCreator)//
				.addParameter("systemName", system.getName()).build(), implementers);

	}
	
	/**
	 * Construct URL to frontend for given request
	 * 
	 * @param request
	 * @return
	 */
	private String getUrl(VsRequestDto request) {
		if (request == null) {
			return null;
		}
		String origins = configurationService.getValue(DynamicCorsConfiguration.PROPERTY_ALLOWED_ORIGIN);
		//
		if (origins != null && !origins.isEmpty()) {
			String origin = origins.trim().split(DynamicCorsConfiguration.PROPERTY_ALLOWED_ORIGIN_SEPARATOR)[0];
			return MessageFormat.format("{0}/#/vs/request/{1}/detail", origin, request.getId());
		}
		return null;
	}

	private IdmIdentityDto getIdentity(VsRequestDto request) {
		AbstractDto targetEntity = findTargetEntity(request);
		if (targetEntity instanceof IdmIdentityDto) {
			return (IdmIdentityDto) targetEntity;
		}
		return null;
	}

	/**
	 * Check if is request same. Request are equals only if have same UID, system,
	 * operation type, all connector attributes (attributes may have not the same
	 * order).
	 * 
	 * @param request
	 * @param previousRequest
	 * @return
	 */
	private boolean isRequestSame(VsRequestDto request, VsRequestDto previousRequest) {
		Assert.notNull(request, "Request cannot be null!");
		Assert.notNull(previousRequest, "Previous request cannot be null!");
		Assert.notNull(request.getConnectorObject(), "Request connector object cannot be null!");
		Assert.notNull(previousRequest.getConnectorObject(), "Previous request connector object cannot be null!");

		if ((!request.getUid().equals(previousRequest.getUid()))
				|| (!request.getSystem().equals(previousRequest.getSystem()))
				|| request.getOperationType() != previousRequest.getOperationType()) {
			return false;
		}
		List<IcAttribute> requestAttributes = request.getConnectorObject().getAttributes();
		List<IcAttribute> previousRequestAttributes = previousRequest.getConnectorObject().getAttributes();

		return CollectionUtils.isEqualCollection(requestAttributes, previousRequestAttributes);
	}

	/**
	 * Find previous request for same operation type. Given duplicities must be DESC
	 * sorted by when was requests created.
	 * 
	 * @param operation
	 * @param duplicities
	 * @return
	 */
	private VsRequestDto getPreviousRequest(VsOperationType operation, List<VsRequestDto> duplicities) {
		if (CollectionUtils.isEmpty(duplicities)) {
			return null;
		}

		if (operation == null) {
			return duplicities.get(0);
		}
		VsRequestDto previousRequest = duplicities.stream()//
				.filter(duplicant -> operation == duplicant.getOperationType())//
				.findFirst()//
				.orElse(null);

		// If any previous request for update operation was not found, then we
		// try find request for Create operation.
		if (previousRequest == null && VsOperationType.UPDATE == operation) {
			previousRequest = duplicities.stream()//
					.filter(duplicant -> VsOperationType.CREATE == duplicant.getOperationType())//
					.findFirst()//
					.orElse(null);
		}
		return previousRequest;
	}
	
	/**
	 * Refresh state of role request
	 * 
	 * @param request
	 */
	private void refreshRoleRequestState(VsRequestDto request) {
		UUID requestId = request.getRoleRequestId();
		if (requestId != null) {
			IdmRoleRequestDto mockRequest = new IdmRoleRequestDto();
			mockRequest.setId(requestId);
			mockRequest.setState(RoleRequestState.EXECUTED);
			IdmRoleRequestDto returnedReqeust = roleRequestService.refreshSystemState(mockRequest);
			
			OperationResultDto systemState = returnedReqeust.getSystemState(); 
			if (systemState == null) {
				// State on system of request was not changed (may be not all provisioning operations are
				// resolved)
			} else {
				// We have final state on systems
				IdmRoleRequestDto requestDto = roleRequestService.get(requestId);
				if (requestDto != null) {
					requestDto.setSystemState(systemState);
					roleRequestService.save(requestDto);
				} else {
					LOG.info(MessageFormat.format(
							"Refresh role-request system state: Role-request with ID [{0}] was not found (maybe was deleted).",
							requestId));
				}
			}
		}
	}

}
