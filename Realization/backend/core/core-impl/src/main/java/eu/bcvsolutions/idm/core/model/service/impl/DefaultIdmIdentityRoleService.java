package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.ArrayList;
import java.util.List;
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

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity_;
import eu.bcvsolutions.idm.core.api.service.AbstractEventableDtoService;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
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
							builder.or(
									builder.lessThanOrEqualTo(root.get(IdmIdentityRole_.validFrom), today),
									builder.isNull(root.get(IdmIdentityRole_.validFrom))
									),
							builder.or(
									builder.greaterThanOrEqualTo(root.get(IdmIdentityRole_.validTill), today),
									builder.isNull(root.get(IdmIdentityRole_.validTill))
									)
							,
							builder.or(
									builder.lessThanOrEqualTo(root.get(IdmIdentityRole_.identityContract).get(IdmIdentityContract_.validFrom), today),
									builder.isNull(root.get(IdmIdentityRole_.identityContract).get(IdmIdentityContract_.validFrom))
									),
							builder.or(
									builder.greaterThanOrEqualTo(root.get(IdmIdentityRole_.identityContract).get(IdmIdentityContract_.validTill), today),
									builder.isNull(root.get(IdmIdentityRole_.identityContract).get(IdmIdentityContract_.validTill))
									)
							)
					);
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
	public Page<IdmIdentityRoleDto> findByAutomaticRole(UUID roleTreeNodeId, Pageable pageable) {
		return toDtoPage(repository.findByRoleTreeNode_Id(roleTreeNodeId, pageable));
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
