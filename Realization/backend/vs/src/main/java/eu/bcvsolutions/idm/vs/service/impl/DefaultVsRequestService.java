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
import org.springframework.beans.factory.annotation.Autowired;
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
import eu.bcvsolutions.idm.vs.domain.VsRequestState;
import eu.bcvsolutions.idm.vs.entity.VsRequest;
import eu.bcvsolutions.idm.vs.entity.VsRequest_;
import eu.bcvsolutions.idm.vs.event.VsRequestEvent;
import eu.bcvsolutions.idm.vs.event.VsRequestEvent.VsRequestEventType;
import eu.bcvsolutions.idm.vs.event.processor.VsRequestRealizationProcessor;
import eu.bcvsolutions.idm.vs.exception.VsException;
import eu.bcvsolutions.idm.vs.exception.VsResultCode;
import eu.bcvsolutions.idm.vs.repository.VsRequestRepository;
import eu.bcvsolutions.idm.vs.repository.filter.RequestFilter;
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
public class DefaultVsRequestService extends AbstractReadWriteDtoService<VsRequestDto, VsRequest, RequestFilter>
		implements VsRequestService {

	private static final Logger LOG = LoggerFactory.getLogger(DefaultVsRequestService.class);

	private final EntityEventManager entityEventManager;
	private final VsRequestImplementerService requestImplementerService;
	private final CzechIdMIcConnectorService czechIdMConnectorService;
	private final CzechIdMIcConfigurationService czechIdMConfigurationService;

	@Autowired
	public DefaultVsRequestService(VsRequestRepository repository, EntityEventManager entityEventManager,
			VsRequestImplementerService requestImplementerService, CzechIdMIcConnectorService czechIdMConnectorService,
			CzechIdMIcConfigurationService czechIdMConfigurationService) {
		super(repository);
		//
		Assert.notNull(entityEventManager);
		Assert.notNull(requestImplementerService);
		Assert.notNull(czechIdMConnectorService);
		Assert.notNull(czechIdMConfigurationService);

		this.entityEventManager = entityEventManager;
		this.requestImplementerService = requestImplementerService;
		this.czechIdMConnectorService = czechIdMConnectorService;
		this.czechIdMConfigurationService = czechIdMConfigurationService;
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
	@Transactional
	public IcUidAttribute execute(VsRequestDto request) {
		EventContext<VsRequestDto> event = entityEventManager
				.process(new VsRequestEvent(VsRequestEventType.EXCECUTE, request));
		return (IcUidAttribute) event.getLastResult().getEvent().getProperties()
				.get(VsRequestRealizationProcessor.RESULT_UID);
	}

	@Override
	@Transactional
	public VsRequestDto createRequest(VsRequestDto request) {
		Assert.notNull(request, "Request cannot be null!");

		List<VsRequestDto> duplicities = findDuplicities(request);
		if (CollectionUtils.isEmpty(duplicities)) {
			// We do not have any unfinished requests for this account.
			VsRequestDto savedRequest = this.save(request, IdmBasePermission.CREATE);
			if (request.getImplementers() != null) {
				request.getImplementers().forEach(identity -> {
					// We have some implementers to save
					VsRequestImplementerDto implementer = new VsRequestImplementerDto();
					implementer.setRequest(savedRequest.getId());
					implementer.setIdentity(identity.getId()); 
					requestImplementerService.save(implementer);
				});
			}

			return savedRequest;
		}
		return request;

	}

	@Override
	public IcUidAttribute internalStart(VsRequestDto request) {
		Assert.notNull(request, "Request cannot be null!");
		if (request.isExecuteImmediately()) {
			// Request will be realized now
			IcUidAttribute result = internalExecute(request);
			return result;
		}
		this.sendNotification(request); // TODO
		request.setState(VsRequestState.IN_PROGRESS);
		this.save(request);
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

	private List<VsRequestDto> findDuplicities(VsRequestDto request) {
		RequestFilter filter = new RequestFilter();
		filter.setUid(request.getUid());
		filter.setSystemId(request.getSystemId());
		// filter.setOperationType(request.getOperationType());
		filter.setUnfinished(Boolean.TRUE);

		List<VsRequestDto> duplicities = this.find(filter, null).getContent();
		return duplicities;
	}

	private void sendNotification(VsRequestDto request) {
		// TODO

	}

	@Override
	protected List<Predicate> toPredicates(Root<VsRequest> root, CriteriaQuery<?> query, CriteriaBuilder builder,
			RequestFilter filter) {
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

		return predicates;
	}

	@Override
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(VirtualSystemGroupPermission.VSREQUEST, getEntityClass());
	}

}
