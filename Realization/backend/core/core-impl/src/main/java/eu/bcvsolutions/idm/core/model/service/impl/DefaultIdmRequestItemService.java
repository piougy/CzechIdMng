package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.domain.RequestState;
import eu.bcvsolutions.idm.core.api.dto.IdmRequestItemDto;
import eu.bcvsolutions.idm.core.api.dto.OperationResultDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRequestItemFilter;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.api.service.IdmRequestItemService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmAutomaticRoleAttributeRuleRequest_;
import eu.bcvsolutions.idm.core.model.entity.IdmAutomaticRoleRequest_;
import eu.bcvsolutions.idm.core.model.entity.IdmRequestItem;
import eu.bcvsolutions.idm.core.model.entity.IdmRequestItem_;
import eu.bcvsolutions.idm.core.model.repository.IdmRequestItemRepository;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;

/**
 * Default implementation of request's item service
 * 
 * @author svandav
 *
 */
@Service("requestItemService")
public class DefaultIdmRequestItemService extends
		AbstractReadWriteDtoService<IdmRequestItemDto, IdmRequestItem, IdmRequestItemFilter>
		implements IdmRequestItemService {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory
			.getLogger(DefaultIdmRequestItemService.class);

	@Autowired
	public DefaultIdmRequestItemService(IdmRequestItemRepository repository) {
		super(repository);
	}

	@Override
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(CoreGroupPermission.AUTOMATICROLEREQUEST, getEntityClass());
	}

	@Override
	public IdmRequestItemDto toDto(IdmRequestItem entity, IdmRequestItemDto dto) {
		IdmRequestItemDto requestDto = super.toDto(entity, dto);

		return requestDto;
	}

	@Override
	@Transactional
	public void deleteInternal(IdmRequestItemDto dto) {

		// First we have to delete all rule concepts for this request
		if (dto.getId() != null) {
//			IdmAutomaticRoleAttributeRuleRequestFilter ruleFilter = new IdmAutomaticRoleAttributeRuleRequestFilter();
//			ruleFilter.setRoleRequestId(dto.getId());
//			List<IdmAutomaticRoleAttributeRuleRequestDto> ruleConcepts = automaticRoleRuleRequestService
//					.find(ruleFilter, null).getContent();
//			ruleConcepts.forEach(concept -> {
//				automaticRoleRuleRequestService.delete(concept);
//			});
		}
		super.deleteInternal(dto);
	}

	@Override
	protected IdmRequestItem toEntity(IdmRequestItemDto dto, IdmRequestItem entity) {

		if (this.isNew(dto)) { 
			dto.setResult(new OperationResultDto(OperationState.CREATED));
		}
		IdmRequestItem requestEntity = super.toEntity(dto, entity);

		return requestEntity;
	}
	

	@Override
	protected List<Predicate> toPredicates(Root<IdmRequestItem> root, CriteriaQuery<?> query,
			CriteriaBuilder builder, IdmRequestItemFilter filter) {
		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);
		
		if (filter.getRequestId() != null) {
			predicates.add(builder.equal(
					root.get(IdmRequestItem_.request).get(IdmRequestItem_.id),
					filter.getRequestId()));
		}
		if (filter.getOriginalOwnerId() != null) {
			predicates.add(builder.equal(
					root.get(IdmRequestItem_.originalOwnerId),
					filter.getOriginalOwnerId()));
		}
		if (filter.getOriginalType() != null) {
			predicates.add(builder.equal(
					root.get(IdmRequestItem_.ownerType),
					filter.getOriginalType()));
		}

		return predicates;
	}


}
