package eu.bcvsolutions.idm.core.model.service.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleCompositionDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleCompositionFilter;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.service.AbstractEventableDtoService;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleCompositionService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleComposition;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleComposition_;
import eu.bcvsolutions.idm.core.model.entity.IdmRole_;
import eu.bcvsolutions.idm.core.model.event.IdentityRoleEvent;
import eu.bcvsolutions.idm.core.model.event.IdentityRoleEvent.IdentityRoleEventType;
import eu.bcvsolutions.idm.core.model.repository.IdmRoleCompositionRepository;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;

/**
 * Role composition
 * 
 * @author Radek Tomiška
 * @since 9.0.0
 *
 */
public class DefaultIdmRoleCompositionService 
		extends AbstractEventableDtoService<IdmRoleCompositionDto, IdmRoleComposition, IdmRoleCompositionFilter> 
		implements IdmRoleCompositionService {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultIdmRoleCompositionService.class);
	//
	@Autowired private IdmIdentityRoleService identityRoleService;
	
	@Autowired
	public DefaultIdmRoleCompositionService(IdmRoleCompositionRepository repository, EntityEventManager entityEventManager) {
		super(repository, entityEventManager);
	}

	@Override
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(CoreGroupPermission.ROLECOMPOSITION, getEntityClass());
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<IdmRoleCompositionDto> findDirectSubRoles(UUID superiorId, BasePermission... permission) {
		Assert.notNull(superiorId);
		//
		IdmRoleCompositionFilter filter = new IdmRoleCompositionFilter();
		filter.setSuperiorId(superiorId);
		//
		return find(filter, null, permission).getContent();
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<IdmRoleCompositionDto> findAllSuperiorRoles(UUID subId, BasePermission... permission) {
		Assert.notNull(subId);
		//
		IdmRoleCompositionFilter filter = new IdmRoleCompositionFilter();
		filter.setSubId(subId);
		List<IdmRoleCompositionDto> results = new ArrayList<>();
		//
		findAllSuperiorRoles(results, subId, permission);
		//
		return results;
	}
	
	private void findAllSuperiorRoles(List<IdmRoleCompositionDto> results, UUID subId, BasePermission... permission) {
		IdmRoleCompositionFilter filter = new IdmRoleCompositionFilter();
		filter.setSubId(subId);
		//
		find(filter, null, permission)
			.forEach(superiorRole -> {
				if (!results.contains(superiorRole)) {
					results.add(superiorRole);
					//
					findAllSuperiorRoles(results, superiorRole.getSuperior(), permission);
				}				
			});
	}
	
	/**
	 * @Transactional is not needed - (asynchronous) events is thrown for every sub role anyway ...
	 * Can be called repetitively for given identity role => checks or creates missing sub roles  by composition.
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void assignSubRoles(EntityEvent<IdmIdentityRoleDto> event, BasePermission... permission) {
		Assert.notNull(event);
		IdmIdentityRoleDto identityRole = event.getContent();
		Assert.notNull(identityRole.getId());
		//
		List<IdmRoleCompositionDto> directSubRoles = findDirectSubRoles(identityRole.getRole());
		LOG.debug("Assign sub roles [{}] for identity role [{}], role []",
				directSubRoles.size(), identityRole.getId(), identityRole.getRole());
		//
		Map<String, Serializable> props = resolveProperties(event);
		Set<UUID> processedRoles = (HashSet<UUID>) props.get(IdentityRoleEvent.PROPERTY_PROCESSED_ROLES);
		processedRoles.add(identityRole.getRole());
		//
		directSubRoles
			.forEach(subRoleComposition -> {
				IdmRoleDto subRole = DtoUtils.getEmbedded(subRoleComposition, IdmRoleComposition_.sub);
				if (processedRoles.contains(subRole.getId())) {
					LOG.debug("Role [{}] was already processed by business role composition - skipping", subRole.getCode());
				} else {
					// try to find currently assigned subrole by this configuration (rerun operation)
					IdmIdentityRoleFilter filter = new IdmIdentityRoleFilter();
					filter.setRoleCompositionId(subRoleComposition.getId());
					filter.setDirectRoleId(identityRole.getDirectRole() == null ? identityRole.getId() : identityRole.getDirectRole());
					if (identityRoleService.find(filter, null).getTotalElements() > 0) {
						LOG.debug("Role [{}] was already processed by business role composition - skipping", subRole.getCode());
					} else {
						//
						IdmIdentityRoleDto subIdentityRole = new IdmIdentityRoleDto();
						subIdentityRole.setRole(subRole.getId());
						subIdentityRole.getEmbedded().put(IdmIdentityRoleDto.PROPERTY_ROLE, subRole);
						subIdentityRole.setIdentityContract(identityRole.getIdentityContract());
						subIdentityRole.getEmbedded().put(
								IdmIdentityRoleDto.PROPERTY_IDENTITY_CONTRACT, 
								identityRole.getEmbedded().get(IdmIdentityRoleDto.PROPERTY_IDENTITY_CONTRACT));
						subIdentityRole.setValidFrom(identityRole.getValidFrom());
						subIdentityRole.setValidTill(identityRole.getValidTill());
						subIdentityRole.setDirectRole(identityRole.getDirectRole() == null ? identityRole.getId() : identityRole.getDirectRole());
						subIdentityRole.setRoleComposition(subRoleComposition.getId());
						//
						processedRoles.add(subRole.getId());
						IdentityRoleEvent subEvent = new IdentityRoleEvent(IdentityRoleEventType.CREATE, subIdentityRole, props);
						// 
						identityRoleService.publish(subEvent, event, permission);
					}
				}
			});
	}
	
	@Override
	@Transactional
	public void removeSubRoles(EntityEvent<IdmIdentityRoleDto> event, BasePermission... permission) {
		Assert.notNull(event);
		IdmIdentityRoleDto directRole = event.getContent();
		Assert.notNull(directRole);
		Assert.notNull(directRole.getId());
		//
		IdmIdentityRoleFilter filter = new IdmIdentityRoleFilter();
		filter.setDirectRoleId(directRole.getId());
		//
		identityRoleService
			.find(filter, null)
			.forEach(subIdentityRole -> {
				IdentityRoleEvent subEvent = new IdentityRoleEvent(IdentityRoleEventType.DELETE, subIdentityRole);
				//
				identityRoleService.publish(subEvent, event, permission);
			});
	}
	
	@Override
	@Transactional
	public void updateSubRoles(EntityEvent<IdmIdentityRoleDto> event, BasePermission... permission) {
		Assert.notNull(event);
		IdmIdentityRoleDto identityRole = event.getContent();
		Assert.notNull(identityRole.getId());
		//
		IdmIdentityRoleFilter filter = new IdmIdentityRoleFilter();
		filter.setDirectRoleId(identityRole.getId());
		//
		identityRoleService
			.find(filter, null)
			.forEach(subIdentityRole -> {
				subIdentityRole.setIdentityContract(identityRole.getIdentityContract());
				subIdentityRole.getEmbedded().put(
						IdmIdentityRoleDto.PROPERTY_IDENTITY_CONTRACT, 
						identityRole.getEmbedded().get(IdmIdentityRoleDto.PROPERTY_IDENTITY_CONTRACT));
				subIdentityRole.setValidFrom(identityRole.getValidFrom());
				subIdentityRole.setValidTill(identityRole.getValidTill());
				//
				IdentityRoleEvent subEvent = new IdentityRoleEvent(IdentityRoleEventType.UPDATE, subIdentityRole);
				//
				identityRoleService.publish(subEvent, event, permission);
			});
	}
	
	@Override
	protected List<Predicate> toPredicates(Root<IdmRoleComposition> root, CriteriaQuery<?> query, CriteriaBuilder builder,
			IdmRoleCompositionFilter filter) {
		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);
		//
		// superior role
		UUID superior = filter.getSuperiorId();
		if (superior != null) {
			predicates.add(builder.equal(root.get(IdmRoleComposition_.superior).get(IdmRole_.id), superior));
		}
		//
		// sub role
		UUID sub = filter.getSubId();
		if (sub != null) {
			predicates.add(builder.equal(root.get(IdmRoleComposition_.sub).get(IdmRole_.id), sub));
		}		
		//
		return predicates;
	}
	
	/**
	 * Creates or reassign processed identity roles @Set of @UUID into event properties.
	 * 
	 * @param event
	 * @return
	 */
	private Map<String, Serializable> resolveProperties(EntityEvent<IdmIdentityRoleDto> event) {
		Map<String, Serializable> props = new HashMap<>();
		//
		if (event.getProperties().containsKey(IdentityRoleEvent.PROPERTY_PROCESSED_ROLES)) {
			props.put(IdentityRoleEvent.PROPERTY_PROCESSED_ROLES, event.getProperties().get(IdentityRoleEvent.PROPERTY_PROCESSED_ROLES));
		} else {
			props.put(IdentityRoleEvent.PROPERTY_PROCESSED_ROLES, new HashSet<UUID>());
		}
		//
		return props;
	}
}
