package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.IdmAutomaticRoleAttributeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleTreeNodeDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity_;
import eu.bcvsolutions.idm.core.api.service.AbstractEventableDtoService;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.api.utils.RepositoryUtils;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmAutomaticRole;
import eu.bcvsolutions.idm.core.model.entity.IdmAutomaticRoleAttribute;
import eu.bcvsolutions.idm.core.model.entity.IdmAutomaticRole_;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract_;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole_;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity_;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleCatalogueRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleCatalogueRole_;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleComposition_;
import eu.bcvsolutions.idm.core.model.entity.IdmRole_;
import eu.bcvsolutions.idm.core.model.repository.IdmAutomaticRoleRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRoleRepository;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;

/**
 * Operations with identity roles - usable in wf
 * 
 * @author svanda
 * @author Radek Tomiška
 *
 */
public class DefaultIdmIdentityRoleService 
		extends AbstractEventableDtoService<IdmIdentityRoleDto, IdmIdentityRole, IdmIdentityRoleFilter>
		implements IdmIdentityRoleService {

	private final IdmIdentityRoleRepository repository;
	//
	@Autowired private LookupService lookupService;
	@Autowired private IdmAutomaticRoleRepository automaticRoleRepository;

	@Autowired
	public DefaultIdmIdentityRoleService(
			IdmIdentityRoleRepository repository,
			EntityEventManager entityEventManager) {
		super(repository, entityEventManager);
		//
		this.repository = repository;
	}
	
	@Override
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(CoreGroupPermission.IDENTITYROLE, getEntityClass());
	}
	
	@Override
	protected IdmIdentityRoleDto toDto(IdmIdentityRole entity, IdmIdentityRoleDto dto) {
		dto = super.toDto(entity, dto);
		if (dto == null) {
			return null;
		}
		//
		IdmAutomaticRole automaticRole = entity.getAutomaticRole();
		if (automaticRole != null) {
			dto.setAutomaticRole(automaticRole.getId());
			BaseDto baseDto = null;
			Map<String, BaseDto> embedded = dto.getEmbedded();
			if (automaticRole instanceof IdmAutomaticRoleAttribute) {
				baseDto = lookupService.getDtoService(IdmAutomaticRoleAttributeDto.class).get(automaticRole.getId());
			} else {
				baseDto = lookupService.getDtoService(IdmRoleTreeNodeDto.class).get(automaticRole.getId());
			}
			embedded.put(IdmIdentityRole_.automaticRole.getName(), baseDto);
			dto.setEmbedded(embedded);
		}
		//
		return dto;
	}
	
	@Override
	protected IdmIdentityRole toEntity(IdmIdentityRoleDto dto, IdmIdentityRole entity) {
		IdmIdentityRole resultEntity = super.toEntity(dto, entity);
		// set additional automatic role
		if (resultEntity != null && dto.getAutomaticRole() != null) {
			// it isn't possible use lookupService entity lookup
			IdmAutomaticRole automaticRole = automaticRoleRepository.findOne(dto.getAutomaticRole());
			resultEntity.setAutomaticRole(automaticRole);
		}
		return resultEntity;
	}
	
	@Override
	protected List<Predicate> toPredicates(Root<IdmIdentityRole> root, CriteriaQuery<?> query, CriteriaBuilder builder, IdmIdentityRoleFilter filter) {
		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);
		// id
		if (filter.getId() != null) {
			predicates.add(builder.equal(root.get(AbstractEntity_.id), filter.getId()));
		}
		// quick - by identity's username
		String text = filter.getText();
		if (StringUtils.isNotEmpty(text)) {
			text = text.toLowerCase();
			predicates.add(builder.like(
					builder.lower(root.get(IdmIdentityRole_.identityContract).get(IdmIdentityContract_.identity).get(IdmIdentity_.username)),
					"%" + text + "%")
					);
		}
		UUID identityId = filter.getIdentityId();
		if (identityId != null) {
			predicates.add(builder.equal(
					root.get(IdmIdentityRole_.identityContract).get(IdmIdentityContract_.identity).get(IdmIdentity_.id), 
					identityId)
					);
		}
		UUID roleId = filter.getRoleId();
		if (roleId != null) {
			predicates.add(builder.equal(
					root.get(IdmIdentityRole_.role).get(IdmRole_.id), 
					roleId)
					);
		}
		UUID roleCatalogueId = filter.getRoleCatalogueId();
		if (roleCatalogueId != null) {
			Subquery<IdmRoleCatalogueRole> roleCatalogueRoleSubquery = query.subquery(IdmRoleCatalogueRole.class);
			Root<IdmRoleCatalogueRole> subRootRoleCatalogueRole = roleCatalogueRoleSubquery.from(IdmRoleCatalogueRole.class);
			roleCatalogueRoleSubquery.select(subRootRoleCatalogueRole);
			
			roleCatalogueRoleSubquery.where(
                    builder.and(
                    		builder.equal(subRootRoleCatalogueRole.get(IdmRoleCatalogueRole_.role), root.get(IdmIdentityRole_.role)),
                    		builder.equal(subRootRoleCatalogueRole.get(IdmRoleCatalogueRole_.roleCatalogue).get(AbstractEntity_.id), roleCatalogueId)
                    		));
			predicates.add(builder.exists(roleCatalogueRoleSubquery));
		}
		//
		Boolean valid = filter.getValid();
		if (valid != null) {
			// Only valid identity-role include check on contract validity too
			if (valid) {
				final LocalDate today = LocalDate.now();
				predicates.add(
						builder.and(
								RepositoryUtils.getValidPredicate(root, builder, today),
								RepositoryUtils.getValidPredicate(root.get(IdmIdentityRole_.identityContract), builder, today),
								builder.equal(root.get(IdmIdentityRole_.identityContract).get(IdmIdentityContract_.disabled), Boolean.FALSE)
						));
			}
			// Only invalid identity-role
			if (!valid) {
				final LocalDate today = LocalDate.now();
				predicates.add(
						builder.or(
								builder.not(RepositoryUtils.getValidPredicate(root, builder, today)),
								builder.not(RepositoryUtils.getValidPredicate(root.get(IdmIdentityRole_.identityContract), builder, today)),
								builder.equal(root.get(IdmIdentityRole_.identityContract).get(IdmIdentityContract_.disabled), Boolean.TRUE)
								)
						);
			}
		}
		//
		// is automatic role
		Boolean automaticRole = filter.getAutomaticRole();
		if (automaticRole != null) {
			if (automaticRole) {
				predicates.add(builder.isNotNull(root.get(IdmIdentityRole_.automaticRole)));
			} else {
				predicates.add(builder.isNull(root.get(IdmIdentityRole_.automaticRole)));
			}
		}
		//
		UUID automaticRoleId = filter.getAutomaticRoleId();
		if (automaticRoleId != null) {
			predicates.add(builder.equal(
					root.get(IdmIdentityRole_.automaticRole).get(IdmAutomaticRole_.id), 
					automaticRoleId)
					);
		}
		//
		UUID identityContractId = filter.getIdentityContractId();
		if (identityContractId != null) {
			predicates.add(builder.equal(
					root.get(IdmIdentityRole_.identityContract).get(AbstractEntity_.id), 
					identityContractId)
					);
		}
		//
		UUID directRoleId = filter.getDirectRoleId();
		if (directRoleId != null) {
			predicates.add(builder.equal(root.get(IdmIdentityRole_.directRole).get(IdmIdentityRole_.id), directRoleId));
		}
		//
		UUID roleCompositionId = filter.getRoleCompositionId();
		if (roleCompositionId != null) {
			predicates.add(builder.equal(root.get(IdmIdentityRole_.roleComposition).get(IdmRoleComposition_.id), roleCompositionId));
		}
		//
		// is automatic role
		Boolean directRole = filter.getDirectRole();
		if (directRole != null) {
			if (directRole) {
				predicates.add(builder.isNull(root.get(IdmIdentityRole_.directRole)));
			} else {
				predicates.add(builder.isNotNull(root.get(IdmIdentityRole_.directRole)));
			}
		}
		//
		return predicates;
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<IdmIdentityRoleDto> findAllByIdentity(UUID identityId) {
		return toDtos(repository.findAllByIdentityContract_Identity_Id(identityId, getDefaultSort()), false);
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<IdmIdentityRoleDto> findAllByContract(UUID identityContractId) {
		Assert.notNull(identityContractId);
		//
		return toDtos(repository.findAllByIdentityContract_Id(identityContractId, getDefaultSort()), false);
	}
	
	@Override
	@Transactional(readOnly = true)
	public Page<IdmIdentityRoleDto> findByAutomaticRole(UUID automaticRoleId, Pageable pageable) {
		return toDtoPage(repository.findByAutomaticRole_Id(automaticRoleId, pageable));
	}
	
	@Override
	@Deprecated
	@Transactional(readOnly = true)
	public Page<IdmIdentityRoleDto> findValidRole(UUID identityId, Pageable pageable) {
		return this.findValidRoles(identityId, pageable);
	}
	
	@Override
	@Transactional(readOnly = true)
	public Page<IdmIdentityRoleDto> findValidRoles(UUID identityId, Pageable pageable) {
		IdmIdentityRoleFilter identityRoleFilter = new IdmIdentityRoleFilter();
		identityRoleFilter.setValid(Boolean.TRUE);
		identityRoleFilter.setIdentityId(identityId);
		//
		return this.find(identityRoleFilter, pageable);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<IdmIdentityRoleDto> findExpiredRoles(LocalDate expirationDate, Pageable page) {
		Assert.notNull(expirationDate);
		//
		return toDtoPage(repository.findExpiredRoles(expirationDate, page));
	}

	/**
	 * Default sort by role's name
	 * 
	 * @return
	 */
	private Sort getDefaultSort() {
		return new Sort(IdmIdentityRole_.role.getName() + "." + IdmRole_.code.getName());
	}
}
