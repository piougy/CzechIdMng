package eu.bcvsolutions.idm.core.eav.service.impl;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.api.service.AbstractEventableDtoService;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormProjectionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.filter.IdmFormProjectionFilter;
import eu.bcvsolutions.idm.core.eav.api.service.IdmFormProjectionService;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormProjection;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormProjection_;
import eu.bcvsolutions.idm.core.eav.repository.IdmFormProjectionRepository;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;
	
/**
 * CRUD form projections.
 * 
 * @author Radek Tomiška
 * @since 10.2.0
 */
public class DefaultIdmFormProjectionService
		extends AbstractEventableDtoService<IdmFormProjectionDto, IdmFormProjection, IdmFormProjectionFilter> 
		implements IdmFormProjectionService {
	
	private final IdmFormProjectionRepository repository;
	
	@Autowired
	public DefaultIdmFormProjectionService(
			IdmFormProjectionRepository repository,
			EntityEventManager entityEventManager) {
		super(repository, entityEventManager);
		//
		this.repository = repository;
	}
	
	@Override
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(CoreGroupPermission.FORMPROJECTION, getEntityClass());
	}
	
	@Override
	@Transactional(readOnly = true)
	public IdmFormProjectionDto getByCode(String code) {
		return toDto(repository.findOneByCode(code));
	}
	
	@Override
	protected List<Predicate> toPredicates(Root<IdmFormProjection> root, CriteriaQuery<?> query, CriteriaBuilder builder, IdmFormProjectionFilter filter) {
		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);
		// fulltext
		String text = filter.getText();
		if (!StringUtils.isEmpty(text)) {
			text = text.toLowerCase();
			predicates.add(builder.or(
				builder.like(builder.lower(root.get(IdmFormProjection_.code)), "%" + text + "%"),
				builder.like(builder.lower(root.get(IdmFormProjection_.ownerType)), "%" + text + "%")
			));
		}
		// code
		String ownerType = filter.getOwnerType();
		if (StringUtils.isNotEmpty(ownerType)) {
			predicates.add(builder.equal(root.get(IdmFormProjection_.ownerType), ownerType));
		}
		//
		return predicates;
	}
}
