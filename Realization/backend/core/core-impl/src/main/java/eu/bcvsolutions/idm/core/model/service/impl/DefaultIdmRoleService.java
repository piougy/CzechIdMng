package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
import eu.bcvsolutions.idm.core.api.dto.IdmRoleCatalogueRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleCatalogueRoleFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleFilter;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.IdmRoleCatalogueRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleCompositionService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.api.utils.RepositoryUtils;
import eu.bcvsolutions.idm.core.eav.api.service.AbstractFormableService;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmForestIndexEntity_;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract_;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole_;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity_;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleCatalogue;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleCatalogueRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleCatalogueRole_;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleCatalogue_;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleComposition_;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleGuarantee;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleGuaranteeRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleGuaranteeRole_;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleGuarantee_;
import eu.bcvsolutions.idm.core.model.entity.IdmRole_;
import eu.bcvsolutions.idm.core.model.event.RoleEvent;
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
	private final ConfigurationService configurationService;
	private final RoleConfiguration roleConfiguration;
	//
	@Autowired private IdmRoleCatalogueRoleService roleCatalogueRoleService;
	@Autowired private IdmRoleCompositionService roleCompositionService;
	
	
	@Autowired
	public DefaultIdmRoleService(
			IdmRoleRepository repository,
			EntityEventManager entityEventManager,
			FormService formService,
			ConfigurationService configurationService,
			RoleConfiguration roleConfiguration) {
		super(repository, entityEventManager, formService);
		//
		Assert.notNull(configurationService);
		Assert.notNull(roleConfiguration);
		//
		this.repository = repository;
		this.configurationService = configurationService;
		this.roleConfiguration = roleConfiguration;
	}
	
	@Override
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(CoreGroupPermission.ROLE, getEntityClass());
	}
	
	@Override
	@Transactional
	public IdmRoleDto saveInternal(IdmRoleDto dto) {
		if (StringUtils.isEmpty(dto.getName())) {
			dto.setName(dto.getCode());
		} else if (StringUtils.isEmpty(dto.getCode())) {
			dto.setCode(dto.getName());
		} 
		//
		return super.saveInternal(dto);
	}
	
	@Override
	@Transactional(readOnly = true)
	public IdmRoleDto getByCode(String code) {
		return toDto(repository.findOneByCode(code));
	}
	
	@Override
	protected List<Predicate> toPredicates(Root<IdmRole> root, CriteriaQuery<?> query, CriteriaBuilder builder, IdmRoleFilter filter) {
		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);
		// quick
		String text = filter.getText();
		if (StringUtils.isNotEmpty(text)) {
			text = text.toLowerCase();
			predicates.add(
					builder.or(
							builder.like(builder.lower(root.get(IdmRole_.code)), "%" + text + "%"),
							builder.like(builder.lower(root.get(IdmRole_.name)), "%" + text + "%"),
							builder.like(builder.lower(root.get(IdmRole_.description)), "%" + text + "%")
							));
		}
		// role type
		if (filter.getRoleType() != null) {
			predicates.add(builder.equal(root.get(IdmRole_.roleType), filter.getRoleType()));
		}
		// guarantee	
		if (filter.getGuaranteeId() != null) {
			// guarante by identity
			Subquery<IdmRoleGuarantee> subquery = query.subquery(IdmRoleGuarantee.class);
			Root<IdmRoleGuarantee> subRoot = subquery.from(IdmRoleGuarantee.class);
			subquery.select(subRoot);
		
			subquery.where(
                    builder.and(
                    		builder.equal(subRoot.get(IdmRoleGuarantee_.role), root), // correlation attr
                    		builder.equal(subRoot.get(IdmRoleGuarantee_.guarantee).get(IdmIdentity_.id), filter.getGuaranteeId())
                    		)
            );
			// guarantee by role - identity has assigned role
			Subquery<UUID> subqueryIdentityRole = query.subquery(UUID.class);
			Root<IdmIdentityRole> subRootIdentityRole = subqueryIdentityRole.from(IdmIdentityRole.class);
			subqueryIdentityRole.select(subRootIdentityRole.get(IdmIdentityRole_.role).get(IdmRole_.id));
			subqueryIdentityRole.where(
                    builder.and(
                    		builder.equal(subRootIdentityRole.get(IdmIdentityRole_.identityContract).get(IdmIdentityContract_.identity).get(IdmIdentity_.id), filter.getGuaranteeId()),
                    		RepositoryUtils.getValidPredicate(subRootIdentityRole, builder)
                    		)
            );
			//
			Subquery<IdmRoleGuaranteeRole> subqueryRole = query.subquery(IdmRoleGuaranteeRole.class);
			Root<IdmRoleGuaranteeRole> subRootRole = subqueryRole.from(IdmRoleGuaranteeRole.class);
			subqueryRole.select(subRootRole);
		
			subqueryRole.where(
                    builder.and(
                    		builder.equal(subRootRole.get(IdmRoleGuaranteeRole_.role), root), // correlation attr
                    		subRootRole.get(IdmRoleGuaranteeRole_.guaranteeRole).get(IdmRole_.id).in(subqueryIdentityRole)
                    		)
            );
			predicates.add(builder.or(
					builder.exists(subquery),
					builder.exists(subqueryRole)
					));
		}
		// role catalogue by forest index
		if (filter.getRoleCatalogueId() != null) {
			Subquery<IdmRoleCatalogueRole> subquery = query.subquery(IdmRoleCatalogueRole.class);
			Root<IdmRoleCatalogueRole> subRoot = subquery.from(IdmRoleCatalogueRole.class);
			subquery.select(subRoot);
			
			Subquery<IdmRoleCatalogue> subqueryCatalogue = query.subquery(IdmRoleCatalogue.class);
			Root<IdmRoleCatalogue> subRootCatalogue = subqueryCatalogue.from(IdmRoleCatalogue.class);
			subqueryCatalogue.select(subRootCatalogue);
			subqueryCatalogue.where(
					builder.and(
							builder.equal(subRootCatalogue.get(IdmRoleCatalogue_.id), filter.getRoleCatalogueId()),
							builder.between(
                    				subRoot.get(IdmRoleCatalogueRole_.roleCatalogue).get(IdmRoleCatalogue_.forestIndex).get(IdmForestIndexEntity_.lft), 
                    				subRootCatalogue.get(IdmRoleCatalogue_.forestIndex).get(IdmForestIndexEntity_.lft),
                    				subRootCatalogue.get(IdmRoleCatalogue_.forestIndex).get(IdmForestIndexEntity_.rgt)
                    		)
					));				

			subquery.where(
                    builder.and(
                    		builder.equal(subRoot.get(IdmRoleCatalogueRole_.role), root), // correlation attr
                    		builder.exists(subqueryCatalogue)
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
	@Deprecated
	public List<IdmRoleDto> getSubroles(UUID roleId) {
		return roleCompositionService
				.findDirectSubRoles(roleId)
				.stream()
				.map(roleComposition -> {
					return DtoUtils.getEmbedded(roleComposition, IdmRoleComposition_.sub, IdmRoleDto.class);
				})
				.collect(Collectors.toList());
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<IdmRoleDto> findAllByRoleCatalogue(UUID roleCatalogueId) {
		Assert.notNull(roleCatalogueId);
		//
		IdmRoleCatalogueRoleFilter filter = new IdmRoleCatalogueRoleFilter();
		filter.setRoleCatalogueId(roleCatalogueId);
		//
		List<IdmRoleDto> roles = new ArrayList<>();
		for (IdmRoleCatalogueRoleDto roleCatalogueRole : roleCatalogueRoleService.find(filter, null).getContent()) {
			IdmRoleDto role = DtoUtils.getEmbedded(roleCatalogueRole, IdmRoleCatalogueRole_.role);
			roles.add(role);
		}
		return roles;
	}	
}
