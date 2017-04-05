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

import com.google.common.base.Strings;

import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.eav.service.api.FormService;
import eu.bcvsolutions.idm.core.eav.service.impl.AbstractFormableService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.dto.filter.RoleFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleCatalogueRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleGuarantee;
import eu.bcvsolutions.idm.core.model.event.RoleEvent;
import eu.bcvsolutions.idm.core.model.event.RoleEvent.RoleEventType;
import eu.bcvsolutions.idm.core.model.event.processor.RoleDeleteProcessor;
import eu.bcvsolutions.idm.core.model.event.processor.RoleSaveProcessor;
import eu.bcvsolutions.idm.core.model.repository.IdmRoleRepository;
import eu.bcvsolutions.idm.core.model.service.api.IdmConfigurationService;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleService;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizationManager;

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
	private final IdmConfigurationService configurationService;
	@Autowired
	private AuthorizationManager authorizationManager;
	
	@Autowired
	public DefaultIdmRoleService(
			IdmRoleRepository repository,
			EntityEventManager entityEventManager,
			FormService formService,
			IdmConfigurationService configurationService) {
		super(repository, formService);
		//
		Assert.notNull(entityEventManager);
		Assert.notNull(configurationService);
		//
		this.repository = repository;
		this.entityEventManager = entityEventManager;
		this.configurationService = configurationService;
	}
	
	@Override
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(getEntityClass(), CoreGroupPermission.ROLE);
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
				Predicate predicate = DefaultIdmRoleService.this.toPredicate(filter, root, query, builder);
				return query.where(predicate).getRestriction();
			}
		};
		return getRepository().findAll(criteria, pageable);
	}
	
	@Override
	public Page<IdmRole> findSecured(final RoleFilter filter, BasePermission permission, Pageable pageable) {
		// transform filter to criteria
		Specification<IdmRole> criteria = new Specification<IdmRole>() {
			public Predicate toPredicate(Root<IdmRole> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
				Predicate predicate = builder.and(
					DefaultIdmRoleService.this.toPredicate(filter, root, query, builder),
					authorizationManager.getPredicate(permission, root, query, builder)
				);
				//
				return query.where(predicate).getRestriction();
			}
		};
		return getRepository().findAll(criteria, pageable);
	}
	
	/**
	 * Converts given filter to jap predicate
	 * 
	 * @param filter
	 * @param root
	 * @param query
	 * @param builder
	 * @return
	 */
	private Predicate toPredicate(RoleFilter filter, Root<IdmRole> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
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
		return builder.and(predicates.toArray(new Predicate[predicates.size()]));
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
	
	@Override
	public String findAssignRoleWorkflowDefinition(UUID roleId){
		Assert.notNull(roleId, "Role ID is required!");
		
		String key =  configurationService.getValue(WF_BY_ROLE_PRIORITY_PREFIX + this.get(roleId).getPriority());
		return Strings.isNullOrEmpty(key) ? null : key;
	}

	@Override
	public String findChangeAssignRoleWorkflowDefinition(UUID roleId){
		Assert.notNull(roleId, "Role ID is required!");
	
		String key =  configurationService.getValue(WF_BY_ROLE_PRIORITY_PREFIX + this.get(roleId).getPriority());
		return Strings.isNullOrEmpty(key) ? null : key;
	}
	
	@Override
	public String findUnAssignRoleWorkflowDefinition(UUID roleId){
		Assert.notNull(roleId, "Role ID is required!");
		String key = null;
		if(this.get(roleId).isApproveRemove()){
			key =  configurationService.getValue(WF_BY_ROLE_PRIORITY_PREFIX + "remove");
		}
		return Strings.isNullOrEmpty(key) ? null : key;
	}
	
}
