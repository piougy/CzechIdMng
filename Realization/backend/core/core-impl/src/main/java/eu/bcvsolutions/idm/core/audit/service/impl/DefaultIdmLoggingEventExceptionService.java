package eu.bcvsolutions.idm.core.audit.service.impl;

import java.io.Serializable;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.audit.dto.IdmLoggingEventExceptionDto;
import eu.bcvsolutions.idm.core.api.audit.dto.filter.IdmLoggingEventExceptionFilter;
import eu.bcvsolutions.idm.core.api.audit.service.IdmLoggingEventExceptionService;
import eu.bcvsolutions.idm.core.api.audit.service.IdmLoggingEventService;
import eu.bcvsolutions.idm.core.api.service.AbstractReadDtoService;
import eu.bcvsolutions.idm.core.audit.entity.IdmLoggingEventException;
import eu.bcvsolutions.idm.core.audit.entity.IdmLoggingEventException_;
import eu.bcvsolutions.idm.core.audit.repository.IdmLoggingEventExceptionRepository;
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
public class DefaultIdmLoggingEventExceptionService extends
		AbstractReadDtoService<IdmLoggingEventExceptionDto, IdmLoggingEventException, IdmLoggingEventExceptionFilter>
		implements IdmLoggingEventExceptionService {

	private final IdmLoggingEventExceptionRepository repository;
	
	@Autowired
	public DefaultIdmLoggingEventExceptionService(IdmLoggingEventExceptionRepository repository) {
		super(repository);
		//
		Assert.notNull(repository);
		//
		this.repository = repository;
	}

	@Override
	protected List<Predicate> toPredicates(Root<IdmLoggingEventException> root, CriteriaQuery<?> query,
			CriteriaBuilder builder, IdmLoggingEventExceptionFilter filter) {
		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);
		
		if (filter.getEvent() != null) {
			predicates.add(
					builder.equal(root.get(IdmLoggingEventException_.event), filter.getEvent()));
		}
		
		return predicates;
	}

	@Override
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(CoreGroupPermission.AUDIT, null);
	}
	
	@Override
	protected IdmLoggingEventExceptionDto toDto(IdmLoggingEventException entity, IdmLoggingEventExceptionDto dto) {
		if (entity == null) {
			return null;
		}
		if (dto == null) {
			dto = new IdmLoggingEventExceptionDto();
		}
		dto.setEvent((Long) entity.getEvent().getId());
		dto.setTraceLine(entity.getTraceLine());
		dto.setId(entity.getId());
		return dto;
	}
	
	@Override
	protected IdmLoggingEventException getEntity(Serializable id, BasePermission... permission) {
		Assert.notNull(id);
		IdmLoggingEventExceptionFilter filter = new IdmLoggingEventExceptionFilter();
		filter.setId(Long.valueOf(id.toString()));
		List<IdmLoggingEventExceptionDto> entities = this.find(filter, null, permission).getContent();
		if (entities.isEmpty()) {
			return null;
		}
		// for given id must found only one entity
		IdmLoggingEventException entity = this.toEntity(entities.get(0));
		return checkAccess(entity, permission);
	}

	@Override
	@Transactional
	public void deleteByEventId(Long eventId) {
		this.repository.deleteByEventId(eventId);
	}
}
