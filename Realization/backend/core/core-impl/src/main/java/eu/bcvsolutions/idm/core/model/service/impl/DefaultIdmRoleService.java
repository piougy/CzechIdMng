package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.eav.service.api.FormService;
import eu.bcvsolutions.idm.core.eav.service.impl.AbstractFormableService;
import eu.bcvsolutions.idm.core.model.dto.filter.RoleFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleCatalogueRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleGuarantee;
import eu.bcvsolutions.idm.core.model.event.RoleEvent;
import eu.bcvsolutions.idm.core.model.event.RoleEvent.RoleEventType;
import eu.bcvsolutions.idm.core.model.event.processor.RoleDeleteProcessor;
import eu.bcvsolutions.idm.core.model.event.processor.RoleSaveProcessor;
import eu.bcvsolutions.idm.core.model.repository.IdmRoleRepository;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleService;

/**
 * Default role service
 * - supports {@link RoleEvent}
 * 
 * @author Radek Tomiška
 *
 */
@Service("roleService")
public class DefaultIdmRoleService extends AbstractFormableService<IdmRole, RoleFilter> implements IdmRoleService {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultIdmRoleService.class);
	private final IdmRoleRepository repository;
	private final EntityEventManager entityEventManager;
	
	@Autowired
	public DefaultIdmRoleService(
			IdmRoleRepository repository,
			EntityEventManager entityEventManager,
			FormService formService) {
		super(repository, formService);
		//
		Assert.notNull(entityEventManager);
		//
		this.repository = repository;
		this.entityEventManager = entityEventManager;
	}

	@Override
	@Transactional(readOnly = true)
	public IdmRole getByName(String name) {
		return repository.findOneByName(name);
	}
	
	/**
	 * Publish {@link RoleEvent} only.
	 * 
	 * @see {@link RoleSaveProcessor}
	 */
	@Override
	@Transactional
	public IdmRole save(IdmRole role) {
		Assert.notNull(role);
		//
		LOG.debug("Saving role [{}]", role.getName());
		//
		return entityEventManager.process(new RoleEvent(isNew(role) ? RoleEventType.CREATE : RoleEventType.UPDATE, role)).getContent();
	}
	
	/**
	 * Publish {@link RoleEvent} only.
	 * 
	 * @see {@link RoleDeleteProcessor}
	 */
	@Override
	@Transactional
	public void delete(IdmRole role) {
		Assert.notNull(role);
		//
		LOG.debug("Deleting role [{}]", role.getName());
		entityEventManager.process(new RoleEvent(RoleEventType.DELETE, role));
	}
	
	@Override
	public Page<IdmRole> find(final RoleFilter filter, Pageable pageable) {
		// transform filter to criteria
		Specification<IdmRole> criteria = new Specification<IdmRole>() {
			public Predicate toPredicate(Root<IdmRole> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
				List<Predicate> predicates = new ArrayList<>();
				// quick
				if (StringUtils.isNotEmpty(filter.getText())) {
					predicates.add(builder.like(builder.lower(root.get("name")), "%" + filter.getText().toLowerCase() + "%"));
				}
				// role type
				if (filter.getRoleType() != null) {
					predicates.add(builder.equal(root.get("roleType"), filter.getRoleType()));
				}
				// guarantee	
				if (filter.getGuarantee() != null) {
					Subquery<IdmRoleGuarantee> subquery = query.subquery(IdmRoleGuarantee.class);
					Root<IdmRoleGuarantee> subRoot = subquery.from(IdmRoleGuarantee.class);
					subquery.select(subRoot);
				
					subquery.where(
	                        builder.and(
	                        		builder.equal(subRoot.get("role"), root), // correlation attr
	                        		builder.equal(subRoot.get("guarantee"), filter.getGuarantee())
	                        		)
	                );
					predicates.add(builder.exists(subquery));
				}
				// role catalogue by forest index
				if (filter.getRoleCatalogue() != null) {
					Subquery<IdmRoleCatalogueRole> subquery = query.subquery(IdmRoleCatalogueRole.class);
					Root<IdmRoleCatalogueRole> subRoot = subquery.from(IdmRoleCatalogueRole.class);
					subquery.select(subRoot);
				
					subquery.where(
	                        builder.and(
	                        		builder.equal(subRoot.get("role"), root), // correlation attr
	                        		builder.between(subRoot.get("roleCatalogue").get("forestIndex").get("lft"), filter.getRoleCatalogue().getLft(), filter.getRoleCatalogue().getRgt())
	                        		)
	                );
					predicates.add(builder.exists(subquery));
				}
				//
				return query.where(predicates.toArray(new Predicate[predicates.size()])).getRestriction();
			}
		};
		return getRepository().findAll(criteria, pageable);
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<IdmRole> getRolesByIds(String roles) {
		if (roles == null) {
			return null;
		}
		List<IdmRole> idmRoles = new ArrayList<>();
		String[] rolesArray = roles.split(",");
		for (String id : rolesArray) {
			// TODO: try - catch ...
			idmRoles.add(get(UUID.fromString(id)));
		}
		return idmRoles;
	}
}
