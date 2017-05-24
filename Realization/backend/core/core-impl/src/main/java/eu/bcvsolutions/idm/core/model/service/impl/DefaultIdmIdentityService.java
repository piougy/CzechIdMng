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
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.config.domain.RoleConfiguration;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdentityFilter;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.eav.service.api.FormService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmAuthorityChange;
import eu.bcvsolutions.idm.core.model.entity.IdmForestIndexEntity_;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract_;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole_;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity_;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleGuarantee;
import eu.bcvsolutions.idm.core.model.entity.IdmRole_;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode_;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeType_;
import eu.bcvsolutions.idm.core.model.event.IdentityEvent;
import eu.bcvsolutions.idm.core.model.event.IdentityEvent.IdentityEventType;
import eu.bcvsolutions.idm.core.model.event.processor.identity.IdentityDeleteProcessor;
import eu.bcvsolutions.idm.core.model.event.processor.identity.IdentityPasswordProcessor;
import eu.bcvsolutions.idm.core.model.event.processor.identity.IdentitySaveProcessor;
import eu.bcvsolutions.idm.core.model.repository.IdmAuthorityChangeRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRepository;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityService;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleService;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;

/**
 * Operations with IdmIdentity
 * - supports {@link IdentityEvent}
 * 
 * TODO: role dto
 * 
 * @author Radek Tomiška
 *
 */
public class DefaultIdmIdentityService
		extends AbstractReadWriteDtoService<IdmIdentityDto, IdmIdentity, IdentityFilter> 
		implements IdmIdentityService {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultIdmIdentityService.class);

	private final FormService formService;
	private final IdmIdentityRepository repository;
	private final IdmRoleService roleService;
	private final IdmAuthorityChangeRepository authChangeRepository;
	private final EntityEventManager entityEventManager;
	private final RoleConfiguration roleConfiguration; 
	
	@Autowired
	public DefaultIdmIdentityService(
			IdmIdentityRepository repository,
			FormService formService,
			IdmRoleService roleService,
			EntityEventManager entityEventManager,
			IdmAuthorityChangeRepository authChangeRepository,
			RoleConfiguration roleConfiguration) {
		super(repository);
		//
		Assert.notNull(formService);
		Assert.notNull(roleService);
		Assert.notNull(entityEventManager);
		Assert.notNull(authChangeRepository);
		Assert.notNull(roleConfiguration);
		//
		this.formService = formService;
		this.repository = repository;
		this.roleService = roleService;
		this.authChangeRepository = authChangeRepository;
		this.entityEventManager = entityEventManager;
		this.roleConfiguration = roleConfiguration;
	}
	
	@Override
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(CoreGroupPermission.IDENTITY, getEntityClass());
	}
	
	@Override
	@Transactional(readOnly = true)
	public IdmIdentityDto getByCode(String code) {
		return getByUsername(code);
	}
	
	@Override
	@Transactional
	@Deprecated
	public IdmIdentity saveIdentity(IdmIdentity identity) {
		return toEntity(save(toDto(identity)), null);
	}
	
	/**
	 * Publish {@link IdentityEvent} only.
	 * 
	 * @see {@link IdentitySaveProcessor}
	 */
	@Override
	@Transactional
	public IdmIdentityDto save(IdmIdentityDto identity, BasePermission... permission) {
		Assert.notNull(identity);
		checkAccess(toEntity(identity, null), permission);
		//
		LOG.debug("Saving identity [{}]", identity.getUsername());
		//
		if (isNew(identity)) { // create
			return entityEventManager.process(new IdentityEvent(IdentityEventType.CREATE, identity)).getContent();
		}
		return entityEventManager.process(new IdentityEvent(IdentityEventType.UPDATE, identity)).getContent();
	}
	
	/**
	 * Publish {@link IdentityEvent} only.
	 * 
	 * @see {@link IdentityDeleteProcessor}
	 */
	@Override
	@Transactional
	public void delete(IdmIdentityDto identity, BasePermission... permission) {
		Assert.notNull(identity);
		checkAccess(this.getEntity(identity.getId()), permission);
		//
		LOG.debug("Deleting identity [{}]", identity.getUsername());
		entityEventManager.process(new IdentityEvent(IdentityEventType.DELETE, identity));
	}
	
	@Override
	public void deleteInternal(IdmIdentityDto dto) {
		// TODO: eav dto
		formService.deleteValues(getRepository().findOne(dto.getId()));
		//
		super.deleteInternal(dto);
	}
	
	@Override
	protected List<Predicate> toPredicates(Root<IdmIdentity> root, CriteriaQuery<?> query, CriteriaBuilder builder, IdentityFilter filter) {
		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);
		//
		// quick - "fulltext"
		if (StringUtils.isNotEmpty(filter.getText())) {
			predicates.add(builder.or(
					builder.like(builder.lower(root.get(IdmIdentity_.username)), "%" + filter.getText().toLowerCase() + "%"),
					builder.like(builder.lower(root.get(IdmIdentity_.firstName)), "%" + filter.getText().toLowerCase() + "%"),
					builder.like(builder.lower(root.get(IdmIdentity_.lastName)), "%" + filter.getText().toLowerCase() + "%"),
					builder.like(builder.lower(root.get(IdmIdentity_.email)), "%" + filter.getText().toLowerCase() + "%"),
					builder.like(builder.lower(root.get(IdmIdentity_.description)), "%" + filter.getText().toLowerCase() + "%")					
					));
		}
		// identity with any of given role (OR)
		List<UUID> roles = filter.getRoles();
		if (!roles.isEmpty()) {
			Subquery<IdmIdentityRole> subquery = query.subquery(IdmIdentityRole.class);
			Root<IdmIdentityRole> subRoot = subquery.from(IdmIdentityRole.class);
			subquery.select(subRoot);
			subquery.where(
                    builder.and(
                    		builder.equal(subRoot.get(IdmIdentityRole_.identityContract).get(IdmIdentityContract_.identity), root), // correlation attr
                    		subRoot.get(IdmIdentityRole_.role).get(IdmRole_.id).in(roles)
                    		)
            );			
			predicates.add(builder.exists(subquery));
		}
		// property
		if (StringUtils.equals(IdmIdentity_.username.getName(), filter.getProperty())) {
			predicates.add(builder.equal(root.get(IdmIdentity_.username), filter.getValue()));
		}
		if (StringUtils.equals(IdmIdentity_.firstName.getName(), filter.getProperty())) {
			predicates.add(builder.equal(root.get(IdmIdentity_.firstName), filter.getValue()));
		}
		if (StringUtils.equals(IdmIdentity_.lastName.getName(), filter.getProperty())) {
			predicates.add(builder.equal(root.get(IdmIdentity_.lastName), filter.getValue()));
		}
		if (StringUtils.equals(IdmIdentity_.email.getName(), filter.getProperty())) {
			predicates.add(builder.equal(root.get(IdmIdentity_.email), filter.getValue()));
		}
		//
		// disabled
		if (filter.getDisabled() != null) {
			predicates.add(builder.equal(root.get(IdmIdentity_.disabled), filter.getDisabled()));
		}
		// treeNode
		if (filter.getTreeNode() != null) {
			Subquery<IdmIdentityContract> subquery = query.subquery(IdmIdentityContract.class);
			Root<IdmIdentityContract> subRoot = subquery.from(IdmIdentityContract.class);
			subquery.select(subRoot);
			//
			if (filter.isRecursively()) {
				Subquery<IdmTreeNode> subqueryTreeNode = query.subquery(IdmTreeNode.class);
				Root<IdmTreeNode> subqueryTreeNodeRoot = subqueryTreeNode.from(IdmTreeNode.class);
				subqueryTreeNode.select(subqueryTreeNodeRoot);
				subqueryTreeNode.where(
						builder.and(
								builder.equal(subqueryTreeNodeRoot.get(IdmTreeNode_.id), filter.getTreeNode()),
								builder.between(
	                    				subRoot.get(IdmIdentityContract_.workPosition).get(IdmTreeNode_.forestIndex).get(IdmForestIndexEntity_.lft), 
	                    				subqueryTreeNodeRoot.get(IdmTreeNode_.forestIndex).get(IdmForestIndexEntity_.lft),
	                    				subqueryTreeNodeRoot.get(IdmTreeNode_.forestIndex).get(IdmForestIndexEntity_.rgt)
	                    		)
						));				
	
				subquery.where(
	                    builder.and(
	                    		builder.equal(subRoot.get(IdmIdentityContract_.identity), root), // correlation attr
	                    		builder.exists(subqueryTreeNode)
	                    		)
	                    );
			} else {
				subquery.where(
	                    builder.and(
	                    		builder.equal(subRoot.get(IdmIdentityContract_.identity), root), // correlation attr
	                    		builder.equal(subRoot.get(IdmIdentityContract_.workPosition).get(IdmTreeNode_.id), filter.getTreeNode())
	                    		)
	                    );
			}
			predicates.add(builder.exists(subquery));
		}
		// treeType
		if (filter.getTreeType() != null) {
			Subquery<IdmIdentityContract> subquery = query.subquery(IdmIdentityContract.class);
			Root<IdmIdentityContract> subRoot = subquery.from(IdmIdentityContract.class);
			subquery.select(subRoot);
			subquery.where(
                    builder.and(
                    		builder.equal(subRoot.get(IdmIdentityContract_.identity), root), // correlation attr
                    		builder.equal(
                    				subRoot.get(IdmIdentityContract_.workPosition).get(IdmTreeNode_.treeType).get(IdmTreeType_.id), 
                    				filter.getTreeType())
                    		)
            );			
			predicates.add(builder.exists(subquery));
		}
		//
		return predicates;
	}

	@Override
	@Transactional(readOnly = true)
	public IdmIdentityDto getByUsername(String username) {
		return toDto(repository.findOneByUsername(username));
	}
	
	/**
	 * Changes given identity's password
	 * 
	 * @param identity
	 * @param passwordChangeDto
	 */
	@Override
	@Transactional
	public void passwordChange(IdmIdentityDto identity, PasswordChangeDto passwordChangeDto) {
		Assert.notNull(identity);
		//
		LOG.debug("Changing password for identity [{}]", identity.getUsername());
		entityEventManager.process(
				new IdentityEvent(
						IdentityEventType.PASSWORD,
						identity, 
						ImmutableMap.of(IdentityPasswordProcessor.PROPERTY_PASSWORD_CHANGE_DTO, passwordChangeDto)));	
	}
	
	@Override
	public String getNiceLabel(IdmIdentityDto identity) {
		if (identity == null) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		if (identity.getTitleBefore() != null) {
			sb.append(identity.getTitleBefore()).append(' ');
		}
		if (identity.getFirstName() != null) {
			sb.append(identity.getFirstName()).append(' ');
		}
		if (identity.getLastName() != null) {
			sb.append(identity.getLastName()).append(' ');
		}
		if (identity.getTitleAfter() != null) {
			sb.append(identity.getTitleAfter()).append(' ');
		}
		return sb.toString().trim();
	}

	/**
	 * Find all identities by assigned role name
	 * 
	 * @param roleName
	 * @return Identities with give role
	 */
	@Override
	@Transactional(readOnly = true)
	public List<IdmIdentityDto> findAllByRoleName(String roleName) {
		IdmRole role = roleService.getByCode(roleName);
		if(role == null){
			return new ArrayList<>();
		}
		
		return this.findAllByRole(role.getId());				
	}
	
	/**
	 * Find all identities by assigned role
	 * 
	 * @param roleId
	 * @return List of IdmIdentity with assigned role
	 */
	@Override
	@Transactional(readOnly = true)
	public List<IdmIdentityDto> findAllByRole(UUID roleId) {
		Assert.notNull(roleId, "Role is required");
		//
		return toDtos(repository.findAllByRole(roleId), false);
	}

	/**
	 * Method find all managers by identity contract and return manager's
	 * 
	 * @param identityId
	 * @return String - usernames separate by commas
	 */
	@Override
	@Transactional(readOnly = true)
	public List<IdmIdentityDto> findAllManagers(UUID forIdentity) {
		return this.findAllManagers(forIdentity, null);
	}

	/**
	 * Method finds all identity's managers by identity contract (guarantee or by assigned tree structure).
	 * 
	 * @param forIdentity
	 * @param byTreeType If optional tree type is given, then only managers defined with this type is returned
	 * @return
	 */
	@Override
	@Transactional(readOnly = true)
	public List<IdmIdentityDto> findAllManagers(UUID forIdentity, UUID byTreeType) {
		Assert.notNull(forIdentity, "Identity id is required.");
		//		
		IdentityFilter filter = new IdentityFilter();
		filter.setManagersFor(forIdentity);
		filter.setManagersByTreeType(byTreeType);
		//
		List<IdmIdentityDto> results = new ArrayList<>();
		Page<IdmIdentityDto> managers = find(filter, new PageRequest(0, 50, Sort.Direction.ASC, IdmIdentity_.username.getName()));
		results.addAll(managers.getContent());
		while (managers.hasNext()) {
			managers = find(filter, managers.nextPageable());
			results.addAll(managers.getContent());
		}
		//
		if (!results.isEmpty()) {
			return results;
		}
		// return all identities with admin role
		return this.findAllByRole(roleConfiguration.getAdminRoleId());
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<IdmIdentityDto> findAllGuaranteesByRoleId(UUID roleId) {
		IdmRole role = roleService.get(roleId);
		Assert.notNull(role, "Role is required. Role by name [" + roleId + "] not found.");
		return role.getGuarantees()
				.stream()
				.map(IdmRoleGuarantee::getGuarantee)
				.map(this::toDto)
				.collect(Collectors.toList());			
	}
	
	/**
	 * Contains list of identities some identity with given username.
	 * If yes, then return true.
	 * @param identities
	 * @param identifier
	 * @return
	 */
	@Override
	public boolean containsUser(List<IdmIdentityDto> identities, String identifier){
		return identities.stream().anyMatch(identity -> {
			return identity.getId().toString().equals(identifier);
		});
	}
	
	/**
	 * Convert given identities to string of user names separate with comma 
	 * @param identities
	 * @return
	 */
	@Override
	public String convertIdentitiesToString(List<IdmIdentityDto> identities) {
		if(identities == null){
			return "";
		}
		return identities
				.stream()
				.map(IdmIdentityDto::getUsername)
				.collect(Collectors.joining(","));
	}

	/**
	 * Update authority change timestamp for all given identities. The IdmAuthorityChange
	 * entity is either updated or created anew, if the original relation did not exist.
	 * @param identities identities to update
	 * @param changeTime authority change time
	 */
	@Transactional
	@Override
	public void updateAuthorityChange(List<UUID> identities, DateTime changeTime) {
		Assert.notNull(identities);
		//
		if (identities.isEmpty()) {
			return;
		}
		// handle identities without IdmAuthorityChange entity relation (auth. change is null)
		List<IdmIdentity> withoutAuthChangeRel = repository.findAllWithoutAuthorityChange(identities);
		if (!withoutAuthChangeRel.isEmpty()) {
			identities.removeAll(withoutAuthChangeRel);
			createAuthorityChange(withoutAuthChangeRel, changeTime);
		}
		// run update query on the rest of identities
		if (!identities.isEmpty()) {
			repository.setIdmAuthorityChangeForIdentity(identities, changeTime);
		}
	}

	private void createAuthorityChange(List<IdmIdentity> withoutAuthChangeRel, DateTime changeTime) {
		for (IdmIdentity identity : withoutAuthChangeRel) {
			IdmAuthorityChange ac = new IdmAuthorityChange();
			ac.setAuthChangeTimestamp(changeTime);
			ac.setIdentity(identity);
			authChangeRepository.save(ac);
		}
	}
}
