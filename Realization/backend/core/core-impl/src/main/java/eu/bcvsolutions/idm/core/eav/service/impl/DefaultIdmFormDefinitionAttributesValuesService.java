package eu.bcvsolutions.idm.core.eav.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.eav.api.dto.filter.IdmFormValueFilter;
import eu.bcvsolutions.idm.core.eav.api.service.IdmFormDefinitionAttributesValuesService;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormAttribute;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormAttribute_;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormDefinition_;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.eav.IdmIdentityFormValue;
import eu.bcvsolutions.idm.core.model.entity.eav.IdmIdentityFormValue_;
import eu.bcvsolutions.idm.core.model.repository.eav.IdmIdentityFormValueRepository;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;

/**
 * Form definition attributes values service
 *
 * @author Roman Kučera
 */
public class DefaultIdmFormDefinitionAttributesValuesService
		extends AbstractReadWriteDtoService<IdmFormValueDto, IdmIdentityFormValue, IdmFormValueFilter>
		implements IdmFormDefinitionAttributesValuesService {

	private final IdmIdentityFormValueRepository repository;

	@Autowired
	public DefaultIdmFormDefinitionAttributesValuesService(IdmIdentityFormValueRepository repository) {
		super(repository);
		this.repository = repository;
	}

	@Override
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(CoreGroupPermission.FORMVALUE, getEntityClass());
	}

	@Override
	protected List<Predicate> toPredicates(Root<IdmIdentityFormValue> root, CriteriaQuery<?> query, CriteriaBuilder builder, IdmFormValueFilter filter) {
		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);

		if (filter.getDefinitionId() != null) {
			predicates.add(builder.equal(root.get(IdmIdentityFormValue_.formAttribute).get(IdmFormAttribute_.formDefinition).get(IdmFormDefinition_.id), filter.getDefinitionId()));
		}
		if (filter.getOwner() != null) {
			predicates.add(builder.equal(root.get(IdmIdentityFormValue_.owner), filter.getOwner()));
		}

		return predicates;
	}

//	@Override
//	public List<IdmFormValueDto> findDefinitionAttributesValues(String definitionId) {
//		Specification<IdmIdentityFormValue> criteria = (root, query, cb) -> {
//			List<Predicate> predicates = new ArrayList<>();
//			predicates.add(cb.equal(root.get(IdmIdentityFormValue_.formAttribute).get(IdmFormAttribute_.formDefinition).get(IdmFormDefinition_.id), UUID.fromString(definitionId)));
//			return query.where(predicates.toArray(new Predicate[predicates.size()])).getRestriction();
//		};
//		List result = repository.findAll(criteria);
//		return result;
//	}
}
