package eu.bcvsolutions.idm.core.model.service.impl;

import eu.bcvsolutions.idm.core.api.dto.DelegationTypeDto;
import java.util.List;
import java.util.UUID;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.core.api.dto.IdmDelegationDefinitionDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmDelegationDefinitionFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmDelegationFilter;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity_;
import eu.bcvsolutions.idm.core.api.service.AbstractEventableDtoService;
import eu.bcvsolutions.idm.core.api.service.DelegationManager;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.IdmDelegationDefinitionService;
import eu.bcvsolutions.idm.core.api.service.IdmDelegationService;
import eu.bcvsolutions.idm.core.api.utils.RepositoryUtils;
import eu.bcvsolutions.idm.core.eav.api.service.DelegationType;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmDelegationDefinition;
import eu.bcvsolutions.idm.core.model.entity.IdmDelegationDefinition_;
import eu.bcvsolutions.idm.core.model.repository.IdmDelegationDefinitionRepository;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;
import java.time.LocalDate;
import org.modelmapper.internal.util.Assert;
import org.springframework.transaction.annotation.Transactional;

/**
 * CRUD service for a definition of delegation.
 *
 * @author Vít Švanda
 * @since 10.4.0
 *
 */
@Service("delegationDefinitionService")
public class DefaultIdmDelegationDefinitionService extends
		AbstractEventableDtoService<IdmDelegationDefinitionDto, IdmDelegationDefinition, IdmDelegationDefinitionFilter> implements IdmDelegationDefinitionService {

	@Autowired
	private IdmDelegationService delegationService;
	@Autowired
	private DelegationManager delegationManager;

	@Autowired
	public DefaultIdmDelegationDefinitionService(IdmDelegationDefinitionRepository repository, EntityEventManager entityEventManager) {
		super(repository, entityEventManager);
	}

	@Override
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(CoreGroupPermission.DELEGATIONDEFINITION, getEntityClass());
	}

	@Override
	protected IdmDelegationDefinitionDto toDto(IdmDelegationDefinition entity, IdmDelegationDefinitionDto dto, IdmDelegationDefinitionFilter filter) {
		dto = super.toDto(entity, dto, filter);
		if (dto != null && dto.getType() != null) {
			// Add delegation type DTO to the delegation definition.
			DelegationType delegateType = delegationManager.getDelegateType(dto.getType());
			if (delegateType != null) {
				DelegationTypeDto delegationTypeDto = delegationManager.convertDelegationTypeToDto(delegateType);
				// I cannot use a key "type", because EnumSelect on FE try to use complex value from embedded.
				dto.getEmbedded().put(DelegationManager.WORKFLOW_DELEGATION_TYPE_KEY, delegationTypeDto);
			}
		}
		
		return dto;
	}

	@Override
	public boolean supportsToDtoWithFilter() {
		return true;
	}

	@Override
	@Transactional
	public void deleteInternal(IdmDelegationDefinitionDto dto) {
		Assert.notNull(dto.getId(), "ID cannot be null!");
		
		// Referential integrity - delete all delegations for that definition.
		IdmDelegationFilter delegationFilter = new IdmDelegationFilter();
		delegationFilter.setDelegationDefinitionId(dto.getId());
		
		delegationService.find(delegationFilter, null).getContent()
				.forEach(delegation -> delegationService.delete(delegation));
		
		super.deleteInternal(dto);
	}

	@Override
	protected List<Predicate> toPredicates(Root<IdmDelegationDefinition> root, CriteriaQuery<?> query, CriteriaBuilder builder,
			IdmDelegationDefinitionFilter filter) {
		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);

		UUID delegateId = filter.getDelegateId();
		if (delegateId != null) {
			predicates.add(builder.equal(root.get(IdmDelegationDefinition_.delegate).get(AbstractEntity_.id), delegateId));
		}

		UUID delegatorId = filter.getDelegatorId();
		if (delegatorId != null) {
			predicates.add(builder.equal(root.get(IdmDelegationDefinition_.delegator).get(AbstractEntity_.id), delegatorId));
		}

		UUID delegatorContractId = filter.getDelegatorContractId();
		if (delegatorContractId != null) {
			predicates.add(builder.equal(root.get(IdmDelegationDefinition_.delegatorContract).get(AbstractEntity_.id), delegatorContractId));
		}

		String type = filter.getType();
		if (type != null) {
			predicates.add(builder.equal(root.get(IdmDelegationDefinition_.type), type));
		}

		if (filter.getValid() != null) {
			final LocalDate today = LocalDate.now();
			//
			if (filter.getValid()) {
				predicates.add(
						builder.and(
								RepositoryUtils.getValidPredicate(root, builder, today)
						)
				);
			} else {
				throw new UnsupportedOperationException();
			}
		}

		return predicates;
	}

}
