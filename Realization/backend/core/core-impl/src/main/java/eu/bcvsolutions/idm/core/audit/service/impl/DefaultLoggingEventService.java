package eu.bcvsolutions.idm.core.audit.service.impl;

import java.io.Serializable;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.IdmLoggingEventDto;
import eu.bcvsolutions.idm.core.api.dto.filter.LoggingEventFilter;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.AbstractReadDtoService;
import eu.bcvsolutions.idm.core.audit.model.entity.IdmLoggingEvent;
import eu.bcvsolutions.idm.core.audit.repository.IdmLoggingEventRepository;
import eu.bcvsolutions.idm.core.audit.service.api.IdmLoggingEventService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;

/**
 * Default implementation of {@link IdmLoggingEventService}
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
@Service
public class DefaultLoggingEventService
		extends AbstractReadDtoService<IdmLoggingEventDto, IdmLoggingEvent, LoggingEventFilter>
		implements IdmLoggingEventService {

	private final IdmLoggingEventRepository repository;
	
	@Autowired
	public DefaultLoggingEventService(IdmLoggingEventRepository repository) {
		super(repository);
		//
		Assert.notNull(repository);
		//
		this.repository = repository;
	}

	@Override
	protected List<Predicate> toPredicates(Root<IdmLoggingEvent> root, CriteriaQuery<?> query, CriteriaBuilder builder,
			LoggingEventFilter filter) {
		return super.toPredicates(root, query, builder, filter);
	}
	
	
	@Override
	public IdmLoggingEventDto get(Serializable id, BasePermission... permission) {
		Assert.notNull(id, "Id is required");
		//
		IdmLoggingEvent entity = this.repository.findOneByEventId(Long.valueOf(id.toString()));
		//
		if (entity == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", id));
		}
		//
		return this.toDto(entity);
	}

	@Override
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(CoreGroupPermission.AUDIT, getEntityClass());
	}
}
