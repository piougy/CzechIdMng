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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.google.common.base.Strings;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.eav.service.api.FormService;
import eu.bcvsolutions.idm.core.eav.service.impl.AbstractFormableService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.dto.filter.RoleFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmForestIndexEntity_;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleCatalogueRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleCatalogueRole_;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleCatalogue_;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleGuarantee;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleGuarantee_;
import eu.bcvsolutions.idm.core.model.entity.IdmRole_;
import eu.bcvsolutions.idm.core.model.event.RoleEvent;
import eu.bcvsolutions.idm.core.model.event.RoleEvent.RoleEventType;
import eu.bcvsolutions.idm.core.model.event.processor.role.RoleDeleteProcessor;
import eu.bcvsolutions.idm.core.model.event.processor.role.RoleSaveProcessor;
import eu.bcvsolutions.idm.core.model.repository.IdmRoleRepository;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleService;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;

/**
 * Default role service
 * - supports {@link RoleEvent}
 * 
 * @author Radek Tomiška
 *
 */
public class DefaultIdmRoleService extends AbstractFormableService<IdmRole, RoleFilter> implements IdmRoleService {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultIdmRoleService.class);
	private final IdmRoleRepository repository;
	private final EntityEventManager entityEventManager;
	private final ConfigurationService configurationService;
	
	@Autowired
	public DefaultIdmRoleService(
			IdmRoleRepository repository,
			EntityEventManager entityEventManager,
			FormService formService,
			ConfigurationService configurationService) {
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
		return new AuthorizableType(CoreGroupPermission.ROLE, getEntityClass());
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
	@Transactional(readOnly = true)
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
	@Transactional(readOnly = true)
	public Page<IdmRole> findSecured(final RoleFilter filter, Pageable pageable, BasePermission permission) {
		// transform filter to criteria
		Specification<IdmRole> criteria = new Specification<IdmRole>() {
			public Predicate toPredicate(Root<IdmRole> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
				Predicate predicate = builder.and(
					DefaultIdmRoleService.this.toPredicate(filter, root, query, builder),
					getAuthorizationManager().getPredicate(root, query, builder, permission)
				);
				//
				return query.where(predicate).getRestriction();
			}
		};
		return getRepository().findAll(criteria, pageable);
	}
	
	/**
	 * Converts given filter to jpa predicate
	 * 
	 * @param filter
	 * @param root
	 * @param query
	 * @param builder
	 * @return
	 */
	private Predicate toPredicate(RoleFilter filter, Root<IdmRole> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
		List<Predicate> predicates = new ArrayList<>();
		// id
		if (filter.getId() != null) {
			predicates.add(builder.equal(root.get(IdmRole_.id), filter.getId()));
		}
		// quick
		if (StringUtils.isNotEmpty(filter.getText())) {
			predicates.add(builder.like(builder.lower(root.get(IdmRole_.name)), "%" + filter.getText().toLowerCase() + "%"));
		}
		// role type
		if (filter.getRoleType() != null) {
			predicates.add(builder.equal(root.get(IdmRole_.roleType), filter.getRoleType()));
		}
		// guarantee	
		if (filter.getGuarantee() != null) {
			Subquery<IdmRoleGuarantee> subquery = query.subquery(IdmRoleGuarantee.class);
			Root<IdmRoleGuarantee> subRoot = subquery.from(IdmRoleGuarantee.class);
			subquery.select(subRoot);
		
			subquery.where(
                    builder.and(
                    		builder.equal(subRoot.get(IdmRoleGuarantee_.role), root), // correlation attr
                    		builder.equal(subRoot.get(IdmRoleGuarantee_.guarantee), filter.getGuarantee())
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
                    		builder.equal(subRoot.get(IdmRoleCatalogueRole_.role), root), // correlation attr
                    		builder.between(subRoot.get(
                    				IdmRoleCatalogueRole_.roleCatalogue).get(IdmRoleCatalogue_.forestIndex).get(IdmForestIndexEntity_.lft), 
                    				filter.getRoleCatalogue().getLft(), filter.getRoleCatalogue().getRgt())
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

	@Override
	@Transactional(readOnly = true)
	public IdmRole getDefaultRole() {
		String roleName = configurationService.getValue(PROPERTY_DEFAULT_ROLE);
		if (StringUtils.isEmpty(roleName)) {
			LOG.debug("Default role is not configured. Change configuration [{}].", PROPERTY_DEFAULT_ROLE);
			return null;
		}
		IdmRole defaultRole = getByName(configurationService.getValue(PROPERTY_DEFAULT_ROLE));
		if (defaultRole == null) {
			LOG.warn("Default role [{}] not found. Change configuration [{}].", roleName, PROPERTY_DEFAULT_ROLE);
		}
		return defaultRole;
	}

	@Override
	public List<IdmRole> getSubroles(UUID roleId) {
		Assert.notNull(roleId);
		//
		return repository.getSubroles(roleId);
	}
	
}
