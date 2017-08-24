package eu.bcvsolutions.idm.vs.service.impl;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.List;

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
import org.springframework.util.Assert;

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
import eu.bcvsolutions.idm.vs.repository.VsRequestRepository;
import eu.bcvsolutions.idm.vs.repository.filter.RequestFilter;
import eu.bcvsolutions.idm.vs.service.api.VsRequestImplementerService;
import eu.bcvsolutions.idm.vs.service.api.VsRequestService;
import eu.bcvsolutions.idm.vs.service.api.dto.VsRequestDto;

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
	public IcUidAttribute execute(VsRequestDto request) {
		EventContext<VsRequestDto> event = entityEventManager
				.process(new VsRequestEvent(VsRequestEventType.EXCECUTE, request));
		return (IcUidAttribute) event.getLastResult().getEvent().getProperties()
				.get(VsRequestRealizationProcessor.RESULT_UID);
	}

	@Override
	public VsRequestDto createRequest(VsRequestDto request) {
		Assert.notNull(request, "Request cannot be null!");

		List<VsRequestDto> duplicities = findDuplicities(request);
		if (CollectionUtils.isEmpty(duplicities)) {
			// We do not have any unfinished requests for this account.
			VsRequestDto savedRequest = this.save(request, IdmBasePermission.CREATE);
			return savedRequest;
		}
		return request;

	}

	@Override
	public IcUidAttribute internalExecute(VsRequestDto request) {
		Assert.notNull(request, "Request cannot be null!");
		if (request.isExecuteImmediately()) {
			// Request will be realized now
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

			IcConnectorInstance connectorKeyInstance = new IcConnectorInstanceImpl(null,
					connectorInfo.getConnectorKey(), false);
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
				throw new IcException(
						MessageFormat.format("Unsupported operation type [{0}]", request.getOperationType()));
			}
			return result;
		}
		return null;
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
		return predicates;
	}

	@Override
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(VirtualSystemGroupPermission.VSREQUEST, getEntityClass());
	}

}
