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
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.repository.filter.FilterManager;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.utils.RepositoryUtils;
import eu.bcvsolutions.idm.core.eav.service.api.FormService;
import eu.bcvsolutions.idm.core.eav.service.impl.AbstractFormableService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.model.dto.filter.IdentityFilter;
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
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode_;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeType;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeType_;
import eu.bcvsolutions.idm.core.model.event.IdentityEvent;
import eu.bcvsolutions.idm.core.model.event.IdentityEvent.IdentityEventType;
import eu.bcvsolutions.idm.core.model.event.processor.identity.IdentityDeleteProcessor;
import eu.bcvsolutions.idm.core.model.event.processor.identity.IdentityPasswordProcessor;
import eu.bcvsolutions.idm.core.model.event.processor.identity.IdentitySaveProcessor;
import eu.bcvsolutions.idm.core.model.repository.IdmAuthorityChangeRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmRoleRepository;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityService;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;

/**
 * Operations with IdmIdentity
 * - supports {@link IdentityEvent}
 * 
 * @author Radek Tomiška
 *
 */
public class DefaultIdmIdentityService extends AbstractFormableService<IdmIdentity, IdentityFilter> implements IdmIdentityService {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultIdmIdentityService.class);

	private final IdmIdentityRepository repository;
	private final IdmRoleRepository roleRepository;
	private final IdmAuthorityChangeRepository authChangeRepository;
	private final EntityEventManager entityEventManager;
	private final FilterManager filterManager;
	
	@Autowired
	public DefaultIdmIdentityService(
			IdmIdentityRepository repository,
			FormService formService,
			IdmRoleRepository roleRepository,
			EntityEventManager entityEventManager,
			FilterManager filterManager,
			IdmAuthorityChangeRepository authChangeRepository) {

		super(repository, formService);
		//
		Assert.notNull(roleRepository);
		Assert.notNull(entityEventManager);
		Assert.notNull(filterManager);
		Assert.notNull(authChangeRepository);
		//
		this.repository = repository;
		this.roleRepository = roleRepository;
		this.authChangeRepository = authChangeRepository;
		this.entityEventManager = entityEventManager;
		this.filterManager = filterManager;
	}
	
	@Override
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(CoreGroupPermission.IDENTITY, getEntityClass());
	}
	
	/**
	 * Publish {@link IdentityEvent} only.
	 * 
	 * @see {@link IdentitySaveProcessor}
	 */
	@Override
	@Transactional
	public IdmIdentity save(IdmIdentity identity) {
		Assert.notNull(identity);
		//
		LOG.debug("Saving identity [{}]", identity.getUsername());
		
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
	public void delete(IdmIdentity identity) {
		Assert.notNull(identity);
		//
		LOG.debug("Deleting identity [{}]", identity.getUsername());
		entityEventManager.process(new IdentityEvent(IdentityEventType.DELETE, identity));
	}
	
	@Override
	public Page<IdmIdentity> find(final IdentityFilter filter, Pageable pageable) {
		// transform filter to criteria
		Specification<IdmIdentity> criteria = new Specification<IdmIdentity>() {
			public Predicate toPredicate(Root<IdmIdentity> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
				Predicate predicate = DefaultIdmIdentityService.this.toPredicate(root, query, builder, filter);
				return query.where(predicate).getRestriction();
			}
		};
		return getRepository().findAll(criteria, pageable);
	}
	
	@Override
	public Page<IdmIdentity> findSecured(final IdentityFilter filter, Pageable pageable, BasePermission permission) {
		// transform filter to criteria
		Specification<IdmIdentity> criteria = new Specification<IdmIdentity>() {
			public Predicate toPredicate(Root<IdmIdentity> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
				Predicate predicate = builder.and(
					DefaultIdmIdentityService.this.toPredicate(root, query, builder, filter),
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
	private Predicate toPredicate(Root<IdmIdentity> root, CriteriaQuery<?> query, CriteriaBuilder builder, IdentityFilter filter) {
		List<Predicate> predicates = new ArrayList<>();
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
		List<IdmRole> roles = filter.getRoles();
		if (!roles.isEmpty()) {
			Subquery<IdmIdentityRole> subquery = query.subquery(IdmIdentityRole.class);
			Root<IdmIdentityRole> subRoot = subquery.from(IdmIdentityRole.class);
			subquery.select(subRoot);
			subquery.where(
                    builder.and(
                    		builder.equal(subRoot.get(IdmIdentityRole_.identityContract).get(IdmIdentityContract_.identity), root), // correlation attr
                    		subRoot.get(IdmIdentityRole_.role).get(IdmRole_.id).in(RepositoryUtils.queryEntityIds(roles))
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
		// treeNode
		if (filter.getTreeNode() != null) {
			Subquery<IdmIdentityContract> subquery = query.subquery(IdmIdentityContract.class);
			Root<IdmIdentityContract> subRoot = subquery.from(IdmIdentityContract.class);
			subquery.select(subRoot);
			//
			if (filter.isRecursively()) {
				subquery.where(
	                    builder.and(
	                    		builder.equal(subRoot.get(IdmIdentityContract_.identity), root), // correlation attr
	                    		builder.between(
	                    				subRoot.get(IdmIdentityContract_.workPosition).get(IdmTreeNode_.forestIndex).get(IdmForestIndexEntity_.lft), 
	                    				filter.getTreeNode().getLft(), 
	                    				filter.getTreeNode().getRgt())
	                    		)
	            );
			} else {
				subquery.where(
	                    builder.and(
	                    		builder.equal(subRoot.get(IdmIdentityContract_.identity), root), // correlation attr
	                    		builder.equal(subRoot.get(IdmIdentityContract_.workPosition), filter.getTreeNode())
	                    		)
	            );
			}
			predicates.add(builder.exists(subquery));
		}
		// treeType
		if (filter.getTreeTypeId() != null) {
			Subquery<IdmIdentityContract> subquery = query.subquery(IdmIdentityContract.class);
			Root<IdmIdentityContract> subRoot = subquery.from(IdmIdentityContract.class);
			subquery.select(subRoot);
			subquery.where(
                    builder.and(
                    		builder.equal(subRoot.get(IdmIdentityContract_.identity), root), // correlation attr
                    		builder.equal(
                    				subRoot.get(IdmIdentityContract_.workPosition).get(IdmTreeNode_.treeType).get(IdmTreeType_.id), 
                    				filter.getTreeTypeId())
                    		)
            );			
			predicates.add(builder.exists(subquery));
		}
		//
		// Dynamic filters (added, overriden by module)
		predicates.addAll(filterManager.toPredicates(root, query, builder, filter));
		//
		return builder.and(predicates.toArray(new Predicate[predicates.size()]));
	}

	@Override
	@Transactional(readOnly = true)
	public IdmIdentity getByUsername(String username) {
		return repository.findOneByUsername(username);
	}
	
	@Override
	@Transactional(readOnly = true)
	public IdmIdentity getByName(String username) {
		return this.getByUsername(username);
	}
	
	/**
	 * Changes given identity's password
	 * 
	 * @param identity
	 * @param passwordChangeDto
	 */
	@Override
	@Transactional
	public void passwordChange(IdmIdentity identity, PasswordChangeDto passwordChangeDto) {
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
	public String getNiceLabel(IdmIdentity identity) {
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
	 * @param Role name
	 * @return Identities with give role
	 */
	@Override
	@Transactional(readOnly = true)
	public List<IdmIdentity> findAllByRoleName(String roleName) {
		IdmRole role = roleRepository.findOneByName(roleName);
		if(role == null){
			return new ArrayList<>();
		}
		
		return this.findAllByRole(role);				
	}
	
	/**
	 * Find all identities by assigned role
	 * 
	 * @param role
	 * @return List of IdmIdentity with assigned role
	 */
	@Override
	@Transactional(readOnly = true)
	public List<IdmIdentity> findAllByRole(IdmRole role) {
		Assert.notNull(role, "RoleIs required");
		//
		return repository.findAllByRole(role);
	}

	/**
	 * Method find all managers by identity contract and return manager's
	 * 
	 * @param identityId
	 * @return String - usernames separate by commas
	 */
	@Override
	@Transactional(readOnly = true)
	public List<IdmIdentity> findAllManagers(UUID identityId) {
		IdmIdentity identity = this.get(identityId);
		Assert.notNull(identity, "Identity is required. Identity by id [" + identityId + "] not found.");
		return this.findAllManagers(identity, null);
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
	public List<IdmIdentity> findAllManagers(IdmIdentity forIdentity, IdmTreeType byTreeType) {
		
		Assert.notNull(forIdentity, "Identity is required");
		//		
		IdentityFilter filter = new IdentityFilter();
		filter.setManagersFor(forIdentity);
		filter.setManagersByTreeType(byTreeType);
		//
		List<IdmIdentity> results = new ArrayList<IdmIdentity>();		
		Page<IdmIdentity> managers = find(filter, new PageRequest(0, 50, Sort.Direction.ASC, "username"));
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
		return this.findAllByRole(this.getAdminRole());
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<IdmIdentity> findAllGuaranteesByRoleId(UUID roleId) {
		IdmRole role = roleRepository.findOne(roleId);
		Assert.notNull(role, "Role is required. Role by name [" + roleId + "] not found.");
		return role.getGuarantees().stream().map(IdmRoleGuarantee::getGuarantee).collect(Collectors.toList());				
	}
	
	/**
	 * Contains list of identities some identity with given username.
	 * If yes, then return true.
	 * @param identities
	 * @param username
	 * @return
	 */
	@Override
	public boolean containsUser(List<IdmIdentity> identities, String username){
		return identities.stream().filter(identity -> {
			return identity.getUsername().equals(username);
		}).findFirst().isPresent();
	}
	
	/**
	 * Convert given identities to string of user names separate with comma 
	 * @param identities
	 * @return
	 */
	@Override
	public String convertIdentitiesToString(List<IdmIdentity> identities) {
		if(identities == null){
			return "";
		}
		List<String> list = identities.stream()
				.map(IdmIdentity::getUsername)
				.collect(Collectors.toList());
		return StringUtils.join(list, ',');
	}
	
	/**
	 * TODO: move to configuration service
	 * 
	 * @return
	 */
	private IdmRole getAdminRole() {
		return this.roleRepository.findOneByName(IdmRoleRepository.ADMIN_ROLE);
	}

	/**
	 * Update authority change timestamp for all given identities. The IdmAuthorityChange
	 * entity is either updated or created anew, if the original relation did not exist.
	 * @param identities identities to update
	 * @param changeTime authority change time
	 */
	@Transactional
	@Override
	public void updateAuthorityChange(List<IdmIdentity> identities, DateTime changeTime) {
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
