package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.dto.IdmConfidentialStorageValueDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmConfidentialStorageValueFilter;
import eu.bcvsolutions.idm.core.api.service.AbstractReadDtoService;
import eu.bcvsolutions.idm.core.api.service.IdmConfidentialStorageValueService;
import eu.bcvsolutions.idm.core.model.entity.IdmConfidentialStorageValue;
import eu.bcvsolutions.idm.core.model.entity.IdmConfidentialStorageValue_;
import eu.bcvsolutions.idm.core.model.repository.IdmConfidentialStorageValueRepository;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;

/**
 * service for Confidential Storage Value Dto
 * 
 * @author Patrik Stloukal
 * @author Radek Tomiška
 */
public class DefaultIdmConfidentialStorageValueService extends
		AbstractReadDtoService<IdmConfidentialStorageValueDto, IdmConfidentialStorageValue, IdmConfidentialStorageValueFilter>
		implements IdmConfidentialStorageValueService {

	@Autowired
	public DefaultIdmConfidentialStorageValueService(IdmConfidentialStorageValueRepository repository) {
		super(repository);
	}

	@Override
	public AuthorizableType getAuthorizableType() {
		// configuration storage can be loaded externally and we will not be able to implement our authorization policies
		return null;
	}

	@Override
	protected IdmConfidentialStorageValueDto toDto(IdmConfidentialStorageValue entity,
			IdmConfidentialStorageValueDto dto) {
		IdmConfidentialStorageValueDto result = super.toDto(entity, dto);
		//
		// indicate, if value is filled only
		if (result.getValue() != null && result.getValue().length > 0) {
			result.setSerializableValue(GuardedString.SECRED_PROXY_STRING);
		}
		//
		return result;
	}
	
	@Override
	protected List<Predicate> toPredicates(Root<IdmConfidentialStorageValue> root, CriteriaQuery<?> query,
			CriteriaBuilder builder, IdmConfidentialStorageValueFilter filter) {
		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);
		// text - key, owner type
		if (StringUtils.isNotEmpty(filter.getText())) {
			predicates.add(builder.or(
					builder.like(builder.lower(root.get(IdmConfidentialStorageValue_.ownerType)),
							"%" + filter.getText().toLowerCase() + "%"),
					builder.like(builder.lower(root.get(IdmConfidentialStorageValue_.key)),
							"%" + filter.getText().toLowerCase() + "%")));
		}
		// owner id
		if (filter.getOwnerId() != null) {
			predicates.add(builder.equal(root.get(IdmConfidentialStorageValue_.ownerId), filter.getOwnerId()));
		}
		// owner type
		if (StringUtils.isNotEmpty(filter.getOwnerType())) {
			predicates.add(builder.equal(root.get(IdmConfidentialStorageValue_.ownerType), filter.getOwnerType()));
		}
		// key
		if (StringUtils.isNotEmpty(filter.getKey())) {
			predicates.add(builder.equal(root.get(IdmConfidentialStorageValue_.key), filter.getKey()));
		}
		return predicates;
	}

}
