package eu.bcvsolutions.idm.vs.service.impl;

import java.io.Serializable;
import java.text.MessageFormat;
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
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.event.EventContext;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;
import eu.bcvsolutions.idm.ic.api.IcAttribute;
import eu.bcvsolutions.idm.ic.api.IcConnector;
import eu.bcvsolutions.idm.ic.api.IcConnectorInfo;
import eu.bcvsolutions.idm.ic.api.IcConnectorInstance;
import eu.bcvsolutions.idm.ic.api.IcUidAttribute;
import eu.bcvsolutions.idm.ic.czechidm.service.impl.CzechIdMIcConfigurationService;
import eu.bcvsolutions.idm.ic.czechidm.service.impl.CzechIdMIcConnectorService;
import eu.bcvsolutions.idm.ic.exception.IcException;
import eu.bcvsolutions.idm.ic.impl.IcConnectorInstanceImpl;
import eu.bcvsolutions.idm.ic.impl.IcUidAttributeImpl;
import eu.bcvsolutions.idm.vs.connector.api.VsVirtualConnector;
import eu.bcvsolutions.idm.vs.domain.VirtualSystemGroupPermission;
import eu.bcvsolutions.idm.vs.domain.VsOperationType;
import eu.bcvsolutions.idm.vs.domain.VsRequestState;
import eu.bcvsolutions.idm.vs.entity.VsRequest;
import eu.bcvsolutions.idm.vs.entity.VsRequest_;
import eu.bcvsolutions.idm.vs.event.VsRequestEvent;
import eu.bcvsolutions.idm.vs.event.VsRequestEvent.VsRequestEventType;
import eu.bcvsolutions.idm.vs.event.processor.VsRequestRealizationProcessor;
import eu.bcvsolutions.idm.vs.exception.VsException;
import eu.bcvsolutions.idm.vs.exception.VsResultCode;
import eu.bcvsolutions.idm.vs.repository.VsRequestRepository;
import eu.bcvsolutions.idm.vs.repository.filter.VsRequestFilter;
import eu.bcvsolutions.idm.vs.service.api.VsRequestImplementerService;
import eu.bcvsolutions.idm.vs.service.api.VsRequestService;
import eu.bcvsolutions.idm.vs.service.api.dto.VsRequestDto;
import eu.bcvsolutions.idm.vs.service.api.dto.VsRequestImplementerDto;

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
	private final VsRequestImplementerService requestImplementerService;
	private final CzechIdMIcConnectorService czechIdMConnectorService;
	private final CzechIdMIcConfigurationService czechIdMConfigurationService;
	private final ApplicationContext applicationContext;
	private VsRequestService self;

	@Autowired
	public DefaultVsRequestService(VsRequestRepository repository, EntityEventManager entityEventManager,
			VsRequestImplementerService requestImplementerService, CzechIdMIcConnectorService czechIdMConnectorService,
			CzechIdMIcConfigurationService czechIdMConfigurationService, ApplicationContext applicationContext) {
		super(repository);
		//
		Assert.notNull(entityEventManager);
		Assert.notNull(requestImplementerService);
		Assert.notNull(czechIdMConnectorService);
		Assert.notNull(czechIdMConfigurationService);
		Assert.notNull(applicationContext);

		this.entityEventManager = entityEventManager;
		this.requestImplementerService = requestImplementerService;
		this.czechIdMConnectorService = czechIdMConnectorService;
		this.czechIdMConfigurationService = czechIdMConfigurationService;
		this.applicationContext = applicationContext;
	}

	@Override
	@Transactional(readOnly = true)
	public VsRequestDto get(Serializable id, BasePermission... permission) {

		VsRequestDto request = super.get(id, permission);
		if (request == null) {
			return null;
		}

		// Add list of implementers
		List<IdmIdentityDto> implementers = requestImplementerService.findRequestImplementers(request);
		request.setImplementers(implementers);

		return request;

	}

	@Override
	@Transactional
	public VsRequestDto realize(UUID requestId) {
		LOG.info(MessageFormat.format("Start realize virtual system request [{0}].", requestId));

		Assert.notNull(requestId, "Id of VS request cannot be null!");
		VsRequestDto request = this.get(requestId, IdmBasePermission.READ);
		Assert.notNull(request, "VS request cannot be null!");
		this.checkAccess(request, IdmBasePermission.UPDATE);

		if (VsRequestState.IN_PROGRESS != request.getState()) {
			throw new VsException(VsResultCode.VS_REQUEST_REALIZE_WRONG_STATE,
					ImmutableMap.of("state", VsRequestState.IN_PROGRESS.name(), "currentState", request.getState()));
		}

		request.setState(VsRequestState.REALIZED);
		// Realize request ... propagate change to VS account.
		IcUidAttribute uidAttribute = this.internalExecute(request);
		// Save realized request
		request = this.save(request);

		LOG.info(MessageFormat.format("Virtual system request [{0}] was realized. Output UID attribute: [{1}]",
				requestId, uidAttribute));
		return request;
	}

	@Override
	public VsRequestDto cancel(UUID requestId) {
		LOG.info(MessageFormat.format("Start cancel virtual system request [{0}].", requestId));

		Assert.notNull(requestId, "Id of VS request cannot be null!");
		VsRequestDto request = this.get(requestId, IdmBasePermission.READ);
		Assert.notNull(request, "VS request cannot be null!");
		this.checkAccess(request, IdmBasePermission.UPDATE);

		if (VsRequestState.IN_PROGRESS != request.getState()) {
			throw new VsException(VsResultCode.VS_REQUEST_CANCEL_WRONG_STATE,
					ImmutableMap.of("state", VsRequestState.IN_PROGRESS.name(), "currentState", request.getState()));
		}

		request.setState(VsRequestState.CANCELED);
		// Save cancelled request
		request = this.save(request);

		LOG.info(MessageFormat.format("Virtual system request [{0}] for UID [{1}] was canceled.", requestId,
				request.getUid()));
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
		VsRequestDto request = this.save(req, IdmBasePermission.CREATE);
		if (req.getImplementers() != null) {
			req.getImplementers().forEach(identity -> {
				// We have some implementers to save
				VsRequestImplementerDto implementer = new VsRequestImplementerDto();
				implementer.setRequest(request.getId());
				implementer.setIdentity(identity.getId());
				requestImplementerService.save(implementer);
			});
		}
		return this.get(request.getId());
	}

	@Override
	public IcUidAttribute internalStart(VsRequestDto request) {
		Assert.notNull(request, "Request cannot be null!");
		// Unfinished requests for same UID and system

		List<VsRequestDto> duplicities = this.findDuplicities(request);
		request.setState(VsRequestState.IN_PROGRESS);

		if (!CollectionUtils.isEmpty(duplicities)) {
			// Get the newest request (for same operation)
			VsRequestDto previousRequest = this.getPreviousRequest(request.getOperationType(), duplicities);
			// Shows on previous request with same operation type. We need this
			// for create diff.
			request.setPreviousRequest(previousRequest.getId());
 
			if (this.isRequestSame(request, previousRequest)) {
				request.setDuplicateToRequest(previousRequest.getId());
				request.setState(VsRequestState.DUPLICATED);
			}
		}
		request = this.save(request);

		if (VsRequestState.DUPLICATED == request.getState()) {
			return null;
		}

		if (request.isExecuteImmediately()) {
			// Request will be realized now
			IcUidAttribute result = internalExecute(request);
			return result;
		}

		// Find previous request ... not matter on operation type. Simple get
		// last request.
		VsRequestDto lastRequest = this.getPreviousRequest(null, duplicities);

		if (lastRequest != null) {
			// Send update message
			// TODO: send message
			this.sendNotification(request, this.get(request.getPreviousRequest()));
		}
		{
			// Send new message
			// TODO: send message
			this.sendNotification(request, null); // TODO
		}
		return null;
	}

	@Override
	public IcUidAttribute internalExecute(VsRequestDto request) {
		request.setState(VsRequestState.REALIZED);
		Assert.notNull(request.getConfiguration(), "Request have to contains connector configuration!");
		Assert.notNull(request.getConnectorKey(), "Request have to contains connector key!");

		IcConnectorInfo connectorInfo = czechIdMConfigurationService.getAvailableLocalConnectors()//
				.stream()//
				.filter(info -> request.getConnectorKey().equals(info.getConnectorKey().getFullName()))//
				.findFirst()//
				.orElse(null);
		if (connectorInfo == null) {
			throw new IcException(MessageFormat.format(
					"We cannot found connector info by connector key [{0}] from virtual system request!",
					request.getConnectorKey()));
		}

		IcConnectorInstance connectorKeyInstance = new IcConnectorInstanceImpl(null, connectorInfo.getConnectorKey(),
				false);
		IcConnector connectorInstance = czechIdMConnectorService.getConnectorInstance(connectorKeyInstance,
				request.getConfiguration());
		if (!(connectorInstance instanceof VsVirtualConnector)) {
			throw new IcException("Found connector instance is not virtual system connector!");
		}
		VsVirtualConnector virtualConnector = (VsVirtualConnector) connectorInstance;

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
			result = virtualConnector.internalUpdate(new IcUidAttributeImpl(null, request.getUid(), null),
					request.getConnectorObject().getObjectClass(), request.getConnectorObject().getAttributes());
			break;
		}
		case DELETE: {
			virtualConnector.internalDelete(new IcUidAttributeImpl(null, request.getUid(), null),
					request.getConnectorObject().getObjectClass());
			break;
		}

		default:
			throw new IcException(MessageFormat.format("Unsupported operation type [{0}]", request.getOperationType()));
		}
		return result;
	}

	@Override
	protected List<Predicate> toPredicates(Root<VsRequest> root, CriteriaQuery<?> query, CriteriaBuilder builder,
			VsRequestFilter filter) {

		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);
		//
		// quick - "fulltext"
		if (StringUtils.isNotEmpty(filter.getText())) {
			predicates.add(builder.or(builder.equal(builder.lower(root.get(VsRequest_.uid)),
					"%" + filter.getText().toLowerCase() + "%")));
		}

		// UID
		if (StringUtils.isNotEmpty(filter.getUid())) {
			predicates.add(builder.equal(root.get(VsRequest_.uid), filter.getUid()));
		}

		// System ID
		if (filter.getSystemId() != null) {
			predicates.add(builder.equal(root.get(VsRequest_.systemId), filter.getSystemId()));
		}

		// State
		if (filter.getState() != null) {
			predicates.add(builder.equal(root.get(VsRequest_.state), filter.getState()));
		}

		// Operation type
		if (filter.getOperationType() != null) {
			predicates.add(builder.equal(root.get(VsRequest_.operationType), filter.getOperationType()));
		}

		return predicates;
	}

	@Override
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(VirtualSystemGroupPermission.VSREQUEST, getEntityClass());
	}

	private void sendNotification(VsRequestDto request, VsRequestDto vsRequestDto) {
		// TODO

	}

	/**
	 * Find duplicity requests. All request in state IN_PROGRESS for same UID
	 * and system. For all operation types.
	 * 
	 * @param request
	 * @return
	 */
	@Override
	public List<VsRequestDto> findDuplicities(VsRequestDto request) {
		VsRequestFilter filter = new VsRequestFilter();
		filter.setUid(request.getUid());
		filter.setSystemId(request.getSystemId());
		// filter.setOperationType(request.getOperationType());
		filter.setState(VsRequestState.IN_PROGRESS);
		Sort sort = new Sort(Direction.DESC, VsRequest_.created.getName());
		List<VsRequestDto> duplicities = this.find(filter, new PageRequest(0, Integer.MAX_VALUE, sort)).getContent();
		return duplicities;
	}

	/**
	 * Check if is request same. Request are equals only if have same UID,
	 * system, operation type, all connector attributes (attributes may have not
	 * the same order).
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
				|| (!request.getSystemId().equals(previousRequest.getSystemId()))
				|| request.getOperationType() != previousRequest.getOperationType()) {
			return false;
		}
		List<IcAttribute> requestAttributes = request.getConnectorObject().getAttributes();
		List<IcAttribute> previousRequestAttributes = previousRequest.getConnectorObject().getAttributes();

		return CollectionUtils.isEqualCollection(requestAttributes, previousRequestAttributes);
	}

	/**
	 * Find previous request for same operation type. Given duplicities must be
	 * DESC sorted by when was requests created.
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

	private VsRequestService getSelfProxy() {
		if (this.self == null) {
			this.self = this.applicationContext.getBean(VsRequestService.class);
		}
		return this.self;
	}

}
