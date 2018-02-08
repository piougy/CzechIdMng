package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.ArrayList;
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
import eu.bcvsolutions.idm.core.model.entity.IdmRole_;
import eu.bcvsolutions.idm.core.model.event.IdentityRoleEvent;
import eu.bcvsolutions.idm.core.model.event.processor.identity.IdentityRoleDeleteProcessor;
import eu.bcvsolutions.idm.core.model.event.processor.identity.IdentityRoleSaveProcessor;
import eu.bcvsolutions.idm.core.model.repository.IdmAutomaticRoleRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRoleRepository;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
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
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultIdmIdentityRoleService.class);

	private final IdmIdentityRoleRepository repository;
	private final LookupService lookupService;
	private final IdmAutomaticRoleRepository automaticRoleRepository;

	@Autowired
	public DefaultIdmIdentityRoleService(
			IdmIdentityRoleRepository repository,
			EntityEventManager entityEventManager,
			LookupService lookupService,
			IdmAutomaticRoleRepository automaticRoleRepository) {
		super(repository, entityEventManager);
		//
		Assert.notNull(lookupService);
		Assert.notNull(automaticRoleRepository);
		//
		this.repository = repository;
		this.lookupService = lookupService;
		this.automaticRoleRepository = automaticRoleRepository;
	}
	
	@Override
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(CoreGroupPermission.IDENTITYROLE, getEntityClass());
	}
	
	@Override
	protected IdmIdentityRoleDto toDto(IdmIdentityRole entity, IdmIdentityRoleDto dto) {
		if (entity == null) {
			return null;
		}
		//
		if (dto == null) {
			dto = modelMapper.map(entity, this.getDtoClass(entity));
		} else {
			modelMapper.map(entity, dto);
		}
		//
		IdmAutomaticRole automaticRole = entity.getAutomaticRole();
		if (automaticRole != null) {
			dto.setRoleTreeNode(automaticRole.getId());
			dto.setAutomaticRole(true);
			BaseDto baseDto = null;
			Map<String, BaseDto> embedded = dto.getEmbedded();
			if (automaticRole instanceof IdmAutomaticRoleAttribute) {
				baseDto = lookupService.getDtoService(IdmAutomaticRoleAttributeDto.class).get(automaticRole.getId());
			} else {
				baseDto = lookupService.getDtoService(IdmRoleTreeNodeDto.class).get(automaticRole.getId());
			}
			embedded.put("roleTreeNode", baseDto);
			dto.setEmbedded(embedded);
		}
		//
		return dto;
	}
	
	@Override
	protected IdmIdentityRole toEntity(IdmIdentityRoleDto dto, IdmIdentityRole entity) {
		IdmIdentityRole resultEntity = super.toEntity(dto, entity);
		// set additional automatic role
		if (resultEntity != null && dto.getRoleTreeNode() != null) {
			// it isn't possible use lookupService entity lookup
			IdmAutomaticRole automaticRole = automaticRoleRepository.findOne(dto.getRoleTreeNode());
			resultEntity.setAutomaticRole(automaticRole);
		}
		return resultEntity;
	}
	
	/**
	 * Publish {@link IdentityRoleEvent} only.
	 * 
	 * @see {@link IdentityRoleSaveProcessor}
	 */
	@Override
	@Transactional
	public IdmIdentityRoleDto save(IdmIdentityRoleDto dto, BasePermission... permission) {
		Assert.notNull(dto);
		Assert.notNull(dto.getRole());
		Assert.notNull(dto.getIdentityContract());
		//
		LOG.debug("Saving role [{}] for identity contract [{}]", dto.getRole(), dto.getIdentityContract());
		return super.save(dto, permission);
	}

	/**
	 * Publish {@link IdentityRoleEvent} only.
	 * 
	 * @see {@link IdentityRoleDeleteProcessor}
	 */
	@Override
	@Transactional
	public void delete(IdmIdentityRoleDto dto, BasePermission... permission) {
		Assert.notNull(dto);
		Assert.notNull(dto.getRole());
		Assert.notNull(dto.getIdentityContract());
		//
		LOG.debug("Deleting role [{}] for identity contract [{}]", dto.getRole(), dto.getIdentityContract());
		super.delete(dto, permission);
	}
	
	
	
	@Override
	protected List<Predicate> toPredicates(Root<IdmIdentityRole> root, CriteriaQuery<?> query, CriteriaBuilder builder, IdmIdentityRoleFilter filter) {
		List<Predicate> predicates = new ArrayList<>();
		// id
		if (filter.getId() != null) {
			predicates.add(builder.equal(root.get(AbstractEntity_.id), filter.getId()));
		}
		// quick - by identity's username
		if (StringUtils.isNotEmpty(filter.getText())) {
			predicates.add(builder.like(
					builder.lower(root.get(IdmIdentityRole_.identityContract).get(IdmIdentityContract_.identity).get(IdmIdentity_.username)),
					"%" + filter.getText().toLowerCase() + "%")
					);
		}
		if (filter.getIdentityId() != null) {
			predicates.add(builder.equal(
					root.get(IdmIdentityRole_.identityContract).get(IdmIdentityContract_.identity).get(IdmIdentity_.id), 
					filter.getIdentityId())
					);
		}
		if (filter.getRoleId() != null) {
			predicates.add(builder.equal(
					root.get(IdmIdentityRole_.role).get(IdmRole_.id), 
					filter.getRoleId())
					);
		}
		if (filter.getRoleCatalogueId() != null) {
			Subquery<IdmRoleCatalogueRole> roleCatalogueRoleSubquery = query.subquery(IdmRoleCatalogueRole.class);
			Root<IdmRoleCatalogueRole> subRootRoleCatalogueRole = roleCatalogueRoleSubquery.from(IdmRoleCatalogueRole.class);
			roleCatalogueRoleSubquery.select(subRootRoleCatalogueRole);
			
			roleCatalogueRoleSubquery.where(
                    builder.and(
                    		builder.equal(subRootRoleCatalogueRole.get(IdmRoleCatalogueRole_.role), root.get(IdmIdentityRole_.role)),
                    		builder.equal(subRootRoleCatalogueRole.get(IdmRoleCatalogueRole_.roleCatalogue).get(AbstractEntity_.id), filter.getRoleCatalogueId())
                    		));
			predicates.add(builder.exists(roleCatalogueRoleSubquery));
		}
		// Only valid identity-role include check on contract validity too
		if (filter.getValid() != null && filter.getValid()) {
			final LocalDate today = LocalDate.now();
			predicates.add(
					builder.and(
							RepositoryUtils.getValidPredicate(root, builder, today),
							RepositoryUtils.getValidPredicate(root.get(IdmIdentityRole_.identityContract), builder, today)
					));
		}
		// Only unvalid identity-role
		if (filter.getValid() != null && !filter.getValid()) {
			final LocalDate today = LocalDate.now();
			predicates.add(
					builder.or(
							builder.lessThan(root.get(IdmIdentityRole_.validTill), today),
							builder.greaterThan(root.get(IdmIdentityRole_.validFrom), today)
							)
					);
		}
		//
		// is automatic role
		if (filter.getAutomaticRole() != null) {
			predicates.add(builder.isNotNull(root.get(IdmIdentityRole_.automaticRole)));
		}
		//
		if (filter.getAutomaticRoleId() != null) {
			predicates.add(builder.equal(
					root.get(IdmIdentityRole_.automaticRole).get(IdmAutomaticRole_.id), 
					filter.getAutomaticRoleId())
					);
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
	@Transactional(readOnly = true)
	public Page<IdmIdentityRoleDto> findValidRole(UUID identityId, Pageable pageable) {
		IdmIdentityRoleFilter identityRoleFilter = new IdmIdentityRoleFilter();
		identityRoleFilter.setValid(Boolean.TRUE);
		identityRoleFilter.setIdentityId(identityId);
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
		return new Sort(IdmIdentityRole_.role.getName() + "." + IdmRole_.name.getName());
	}
}
