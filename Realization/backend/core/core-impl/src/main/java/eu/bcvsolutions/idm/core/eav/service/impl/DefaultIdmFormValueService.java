package eu.bcvsolutions.idm.core.eav.service.impl;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.api.service.AbstractReadDtoService;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.eav.api.dto.filter.IdmFormValueFilter;
import eu.bcvsolutions.idm.core.eav.api.service.FormValueService;
import eu.bcvsolutions.idm.core.eav.api.service.IdmFormValueService;
import eu.bcvsolutions.idm.core.eav.entity.AbstractFormValue;
import eu.bcvsolutions.idm.core.eav.entity.AbstractFormValue_;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormAttribute_;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormDefinition_;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormValue;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormValue_;
import eu.bcvsolutions.idm.core.eav.repository.AbstractFormValueRepository;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;

/**
 * Form definition attributes values service
 *
 * @author Roman Kučera
 */
//TODO extend AbstractFormValueService and use toPredicete there.
public class DefaultIdmFormValueService
		extends AbstractReadDtoService<IdmFormValueDto, AbstractFormValue, IdmFormValueFilter>
		implements IdmFormValueService {

	private final AbstractFormValueRepository repository;

	@Autowired
	public DefaultIdmFormValueService(
			AbstractFormValueRepository repository) {
		super(repository);

		this.repository = repository;
	}

	@Override
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(CoreGroupPermission.FORMVALUE, getEntityClass());
	}

	@Override
	protected List<Predicate> toPredicates(Root<AbstractFormValue> root, CriteriaQuery<?> query, CriteriaBuilder builder, IdmFormValueFilter filter) {
		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);
		//
		if (StringUtils.isNotEmpty(filter.getText())) {
			predicates.add(builder.or(
					builder.like(builder.lower(root.get(AbstractFormValue_.formAttribute).get(IdmFormAttribute_.code)), "%" + filter.getText().toLowerCase() + "%"),
					builder.like(builder.lower(root.get(AbstractFormValue_.formAttribute).get(IdmFormAttribute_.name)), "%" + filter.getText().toLowerCase() + "%")
			));
		}
		//
		if (filter.getPersistentType() != null) {
			predicates.add(builder.equal(root.get(AbstractFormValue_.persistentType), filter.getPersistentType()));
		}
		//
		if (filter.getDefinitionId() != null) {
			predicates.add(builder.equal(root.get(AbstractFormValue_.formAttribute).get(IdmFormAttribute_.formDefinition).get(IdmFormDefinition_.id), filter.getDefinitionId()));
		}
		//
		if (filter.getAttributeId() != null) {
			predicates.add(builder.equal(root.get(AbstractFormValue_.formAttribute).get(IdmFormAttribute_.id), filter.getAttributeId()));
		}
		//
		if (filter.getOwner() != null) {
			// by id - owner doesn't need to be persisted
			predicates.add(builder.equal(root.get(FormValueService.PROPERTY_OWNER).get(BaseEntity.PROPERTY_ID), filter.getOwner().getId()));
		}
		//
		return predicates;
	}
}
