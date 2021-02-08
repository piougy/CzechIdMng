package eu.bcvsolutions.idm.acc.service.impl;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.acc.domain.AccGroupPermission;
import eu.bcvsolutions.idm.acc.dto.SysConnectorServerDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysRemoteServerFilter;
import eu.bcvsolutions.idm.acc.entity.SysRemoteServer;
import eu.bcvsolutions.idm.acc.entity.SysRemoteServer_;
import eu.bcvsolutions.idm.acc.repository.SysRemoteServerRepository;
import eu.bcvsolutions.idm.acc.service.api.SysRemoteServerService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.AbstractEventableDtoService;
import eu.bcvsolutions.idm.core.api.service.ConfidentialStorage;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;

/**
 * Remote server with connectors.
 *
 * @author Radek Tomiška
 * @since 10.8.0
 */
@Service
public class DefaultSysRemoteServerService
		extends AbstractEventableDtoService<SysConnectorServerDto, SysRemoteServer, SysRemoteServerFilter>
		implements SysRemoteServerService {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultSysRemoteServerService.class);
	//
	@Autowired private ConfidentialStorage confidentialStorage;

	@Autowired
	public DefaultSysRemoteServerService(
			SysRemoteServerRepository repository,
			EntityEventManager entityEventManager) {
		super(repository, entityEventManager);
	}

	@Override
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(AccGroupPermission.REMOTESERVER, getEntityClass());
	}

	@Override
	public boolean supportsToDtoWithFilter() {
		return true;
	}
	
	@Override
	protected SysConnectorServerDto toDto(SysRemoteServer entity, SysConnectorServerDto dto, SysRemoteServerFilter filter) {
		dto = super.toDto(entity, dto, filter);
		if (dto != null) {
			// remote connector server
			dto.setLocal(false);
		}
		//
		return dto;
	}
	
	@Override
	public SysConnectorServerDto get(Serializable id, SysRemoteServerFilter filter, BasePermission... permission) {
		SysConnectorServerDto entity = super.get(id, filter, permission);
		if (entity == null) {
			return null;
		}
		//
		// found if entity has filled password
		try {
			if (filter != null 
					&& BooleanUtils.isTrue(filter.getContainsPassword())
					&& confidentialStorage.exists(entity.getId(), SysRemoteServer.class, SysSystemService.REMOTE_SERVER_PASSWORD)) {
				entity.setPassword(new GuardedString(GuardedString.SECRED_PROXY_STRING));
			}
		} catch (ResultCodeException ex) {
			// decorator only - we has to log exception, because is not possible to change password, if error occurs in get ....
			LOG.error("Remote connector server pasword for system [{}] is wrong, fix remote server configuration.", entity.getFullServerName(), ex);
		}
		//
		return entity;
	}
	
	@Override
	protected SysConnectorServerDto internalExport(UUID id) {
		SysConnectorServerDto remoteServer = super.internalExport(id);
		// Set remote connector server password to the null. We don't want update password on target IdM.
		remoteServer.setPassword(null);
		//
		return remoteServer;
	}
	
	@Override
	public GuardedString getPassword(UUID remoteServerId) {
		return confidentialStorage.getGuardedString(remoteServerId, SysRemoteServer.class, SysSystemService.REMOTE_SERVER_PASSWORD);
	}
	
	@Override
	protected List<Predicate> toPredicates(Root<SysRemoteServer> root, CriteriaQuery<?> query, CriteriaBuilder builder, SysRemoteServerFilter filter) {
		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);
		// quick
		String text = filter.getText();
		if (StringUtils.isNotEmpty(text)) {
			text = text.toLowerCase();
			predicates.add(
					builder.or(
							builder.like(builder.lower(root.get(SysRemoteServer_.host)), "%" + text + "%"),
							builder.like(builder.lower(root.get(SysRemoteServer_.description)), "%" + text + "%")
					));

		}
		//
		return predicates;
	}	
}
