package eu.bcvsolutions.idm.core.audit.service.impl;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.IdmLoggingEventExceptionDto;
import eu.bcvsolutions.idm.core.api.dto.filter.LoggingEventExceptionFilter;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.audit.entity.IdmLoggingEventException;
import eu.bcvsolutions.idm.core.audit.entity.IdmLoggingEventException_;
import eu.bcvsolutions.idm.core.audit.repository.IdmLoggingEventExceptionRepository;
import eu.bcvsolutions.idm.core.audit.service.api.IdmLoggingEventExceptionService;
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
public class DefaultIdmLoggingEventExceptionService extends
		AbstractReadWriteDtoService<IdmLoggingEventExceptionDto, IdmLoggingEventException, LoggingEventExceptionFilter>
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
			CriteriaBuilder builder, LoggingEventExceptionFilter filter) {
		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);
		
		if (filter.getEvent() != null) {
			predicates.add(
					builder.equal(root.get(IdmLoggingEventException_.event), filter.getEvent()));
		}
		
		return predicates;
	}

	@Override
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(CoreGroupPermission.AUDIT, getEntityClass());
	}

	@Override
	public Page<IdmLoggingEventExceptionDto> findAllByEvent(Long event, Pageable pageable) {
		return toDtoPage(repository.findAllByEvent(event, pageable));
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
	public void deleteAll(List<IdmLoggingEventExceptionDto> eventExceptions, BasePermission... permission) {
		for (IdmLoggingEventExceptionDto exception : eventExceptions) {
			this.delete(exception, permission);
		}
	}
}
