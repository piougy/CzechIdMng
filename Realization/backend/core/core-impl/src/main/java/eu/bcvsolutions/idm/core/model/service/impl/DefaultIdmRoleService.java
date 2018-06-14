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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.google.common.base.Strings;

import eu.bcvsolutions.idm.core.api.config.domain.RoleConfiguration;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleFilter;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.eav.api.service.AbstractFormableService;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmForestIndexEntity_;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity_;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleCatalogue;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleCatalogueRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleCatalogueRole_;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleCatalogue_;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleComposition;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleGuarantee;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleGuarantee_;
import eu.bcvsolutions.idm.core.model.entity.IdmRole_;
import eu.bcvsolutions.idm.core.model.event.RoleEvent;
import eu.bcvsolutions.idm.core.model.repository.IdmRoleCatalogueRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmRoleCatalogueRoleRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmRoleRepository;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;

/**
 * Default role service
 * - supports {@link RoleEvent}
 * 
 * @author Radek Tomiška
 *
 */
public class DefaultIdmRoleService 
		extends AbstractFormableService<IdmRoleDto, IdmRole, IdmRoleFilter> 
		implements IdmRoleService {

	private final IdmRoleRepository repository;
	private final IdmRoleCatalogueRoleRepository roleCatalogueRoleRepository;
	private final ConfigurationService configurationService;
	private final RoleConfiguration roleConfiguration;
	//
	@Autowired private IdmRoleCatalogueRepository roleCatalogueRepository;
	
	@Autowired
	public DefaultIdmRoleService(
			IdmRoleRepository repository,
			IdmRoleCatalogueRoleRepository roleCatalogueRoleRepository,
			EntityEventManager entityEventManager,
			FormService formService,
			ConfigurationService configurationService,
			RoleConfiguration roleConfiguration) {
		super(repository, entityEventManager, formService);
		//
		Assert.notNull(configurationService);
		Assert.notNull(roleConfiguration);
		Assert.notNull(roleCatalogueRoleRepository);
		//
		this.repository = repository;
		this.configurationService = configurationService;
		this.roleConfiguration = roleConfiguration;
		this.roleCatalogueRoleRepository = roleCatalogueRoleRepository;
	}
	
	@Override
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(CoreGroupPermission.ROLE, getEntityClass());
	}
	
	@Override
	protected IdmRole toEntity(IdmRoleDto dto, IdmRole entity) {
		entity = super.toEntity(dto, entity);
		if (entity == null) {
			return null;
		}
		// fill lists references
		for (IdmRoleGuarantee guarantee : entity.getGuarantees()) {
			guarantee.setRole(entity);
		}
		for (IdmRoleCatalogueRole roleCatalogueRole : entity.getRoleCatalogues()) {
			roleCatalogueRole.setRole(entity);
		}
		for (IdmRoleComposition roleComposition : entity.getSubRoles()) {
			roleComposition.setSuperior(entity);
		}
		return entity;
	}

	/**
	 * @deprecated use {@link #getByCode(String)}
	 */
	@Override
	@Transactional(readOnly = true)
	@Deprecated
	public IdmRoleDto getByName(String name) {
		return getByCode(name);
	}
	
	@Override
	@Transactional(readOnly = true)
	public IdmRoleDto getByCode(String name) {
		return toDto(repository.findOneByCode(name));
	}
	
	@Override
	protected List<Predicate> toPredicates(Root<IdmRole> root, CriteriaQuery<?> query, CriteriaBuilder builder, IdmRoleFilter filter) {
		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);
		// quick
		if (StringUtils.isNotEmpty(filter.getText())) {
			predicates.add(
					builder.or(
							builder.like(builder.lower(root.get(IdmRole_.name)), "%" + filter.getText().toLowerCase() + "%"),
							builder.like(builder.lower(root.get(IdmRole_.description)), "%" + filter.getText().toLowerCase() + "%")
							));
		}
		// role type
		if (filter.getRoleType() != null) {
			predicates.add(builder.equal(root.get(IdmRole_.roleType), filter.getRoleType()));
		}
		// guarantee	
		if (filter.getGuaranteeId() != null) {
			Subquery<IdmRoleGuarantee> subquery = query.subquery(IdmRoleGuarantee.class);
			Root<IdmRoleGuarantee> subRoot = subquery.from(IdmRoleGuarantee.class);
			subquery.select(subRoot);
		
			subquery.where(
                    builder.and(
                    		builder.equal(subRoot.get(IdmRoleGuarantee_.role), root), // correlation attr
                    		builder.equal(subRoot.get(IdmRoleGuarantee_.guarantee).get(IdmIdentity_.id), filter.getGuaranteeId())
                    		)
            );
			predicates.add(builder.exists(subquery));
		}
		// role catalogue by forest index
		if (filter.getRoleCatalogueId() != null) {
			// TODO: use subquery - see DefaultIdmIdentityService#toPredicates
			IdmRoleCatalogue roleCatalogue = roleCatalogueRepository.findOne(filter.getRoleCatalogueId());
			Subquery<IdmRoleCatalogueRole> subquery = query.subquery(IdmRoleCatalogueRole.class);
			Root<IdmRoleCatalogueRole> subRoot = subquery.from(IdmRoleCatalogueRole.class);
			subquery.select(subRoot);
		
			subquery.where(
                    builder.and(
                    		builder.equal(subRoot.get(IdmRoleCatalogueRole_.role), root), // correlation attr
                    		builder.between(subRoot.get(
                    				IdmRoleCatalogueRole_.roleCatalogue).get(IdmRoleCatalogue_.forestIndex).get(IdmForestIndexEntity_.lft), 
                    				roleCatalogue.getLft(), roleCatalogue.getRgt())
                    		)
            );
			predicates.add(builder.exists(subquery));
		}
		//
		return predicates;
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<IdmRoleDto> getRolesByIds(String roles) {
		if (roles == null) {
			return null;
		}
		List<IdmRoleDto> idmRoles = new ArrayList<>();
		String[] rolesArray = roles.split(",");
		for (String id : rolesArray) {
			idmRoles.add(get(id));
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
	public IdmRoleDto getDefaultRole() {
		return roleConfiguration.getDefaultRole();
	}

	@Override
	@Transactional(readOnly = true)
	public IdmRoleDto getAdminRole() {
		return roleConfiguration.getAdminRole();
	}
	
	@Override
	public List<IdmRoleDto> getSubroles(UUID roleId) {
		Assert.notNull(roleId);
		//
		return toDtos(repository.getSubroles(roleId), false);
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<IdmRoleDto> findAllByRoleCatalogue(UUID roleCatalogueId) {
		List<IdmRole> roles = new ArrayList<>();
		for (IdmRoleCatalogueRole roleCatalogueRole : roleCatalogueRoleRepository.findAllByRoleCatalogue_Id(roleCatalogueId)) {
			roles.add(roleCatalogueRole.getRole());
		}
		return toDtos(roles, false);
	}	
}
