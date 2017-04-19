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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.utils.RepositoryUtils;
import eu.bcvsolutions.idm.core.eav.service.api.FormService;
import eu.bcvsolutions.idm.core.eav.service.impl.AbstractFormableService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.model.dto.filter.IdentityFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleGuarantee;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeType;
import eu.bcvsolutions.idm.core.model.event.IdentityEvent;
import eu.bcvsolutions.idm.core.model.event.IdentityEvent.IdentityEventType;
import eu.bcvsolutions.idm.core.model.event.processor.identity.IdentityDeleteProcessor;
import eu.bcvsolutions.idm.core.model.event.processor.identity.IdentityPasswordProcessor;
import eu.bcvsolutions.idm.core.model.event.processor.identity.IdentitySaveProcessor;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmRoleRepository;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityService;
import eu.bcvsolutions.idm.core.model.service.api.SubordinatesCriteriaBuilder;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;

/**
 * Operations with IdmIdentity
 * - supports {@link IdentityEvent}
 * 
 * @author Radek Tomiška
 *
 */
@Service("identityService")
public class DefaultIdmIdentityService extends AbstractFormableService<IdmIdentity, IdentityFilter> implements IdmIdentityService {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultIdmIdentityService.class);

	private final IdmIdentityRepository repository;
	private final IdmRoleRepository roleRepository;
	private final EntityEventManager entityEventManager;
	private final SubordinatesCriteriaBuilder subordinatesCriteriaBuilder;
	
	@Autowired
	public DefaultIdmIdentityService(
			IdmIdentityRepository repository,
			FormService formService,
			IdmRoleRepository roleRepository,
			EntityEventManager entityEventManager,
			SubordinatesCriteriaBuilder subordinatesCriteriaBuilder) {
		super(repository, formService);
		//
		Assert.notNull(roleRepository);
		Assert.notNull(entityEventManager);
		Assert.notNull(subordinatesCriteriaBuilder);
		//
		this.repository = repository;
		this.roleRepository = roleRepository;
		this.entityEventManager = entityEventManager;
		this.subordinatesCriteriaBuilder = subordinatesCriteriaBuilder;
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
				Predicate predicate = DefaultIdmIdentityService.this.toPredicate(filter, root, query, builder);
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
					DefaultIdmIdentityService.this.toPredicate(filter, root, query, builder),
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
	private Predicate toPredicate(IdentityFilter filter, Root<IdmIdentity> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
		List<Predicate> predicates = new ArrayList<>();
		// id
		if (filter.getId() != null) {
			predicates.add(builder.equal(root.get("id"), filter.getId()));
		}
		// quick - "fulltext"
		if (StringUtils.isNotEmpty(filter.getText())) {
			predicates.add(builder.or(
					builder.like(builder.lower(root.get("username")), "%" + filter.getText().toLowerCase() + "%"),
					builder.like(builder.lower(root.get("firstName")), "%" + filter.getText().toLowerCase() + "%"),
					builder.like(builder.lower(root.get("lastName")), "%" + filter.getText().toLowerCase() + "%"),
					builder.like(builder.lower(root.get("email")), "%" + filter.getText().toLowerCase() + "%"),
					builder.like(builder.lower(root.get("description")), "%" + filter.getText().toLowerCase() + "%")					
					));
		}
		// managers by tree node (working position)
		if (filter.getManagersByTreeNode() != null) {
			Subquery<IdmIdentityContract> subquery = query.subquery(IdmIdentityContract.class);
			Root<IdmIdentityContract> subRoot = subquery.from(IdmIdentityContract.class);
			subquery.select(subRoot);
			//
			Subquery<IdmIdentityContract> subqueryWp = query.subquery(IdmIdentityContract.class);
			Root<IdmIdentityContract> subqueryWpRoot = subqueryWp.from(IdmIdentityContract.class);
			subqueryWp.select(subqueryWpRoot.get("workPosition").get("parent"));
			subqueryWp.where(builder.and(
					builder.equal(subqueryWpRoot.get("workPosition"), filter.getManagersByTreeNode())
					));
			//
			subquery.where(
                    builder.and(
                    		builder.equal(subRoot.get("identity"), root), // correlation attr
                    		subRoot.get("workPosition").in(subqueryWp)
                    		)
            );			
			predicates.add(builder.exists(subquery));
		}
		// identity with any of given role (OR)
		if (!filter.getRoles().isEmpty()) {
			Subquery<IdmIdentityRole> subquery = query.subquery(IdmIdentityRole.class);
			Root<IdmIdentityRole> subRoot = subquery.from(IdmIdentityRole.class);
			subquery.select(subRoot);
			subquery.where(
                    builder.and(
                    		builder.equal(subRoot.get("identityContract").get("identity"), root), // correlation attr
                    		subRoot.get("role").get("id").in(RepositoryUtils.queryEntityIds(filter.getRoles()))
                    		)
            );			
			predicates.add(builder.exists(subquery));
		}
		// property
		if (StringUtils.equals("username", filter.getProperty())) {
			predicates.add(builder.equal(root.get("username"), filter.getValue()));
		}
		if (StringUtils.equals("firstName", filter.getProperty())) {
			predicates.add(builder.equal(root.get("firstName"), filter.getValue()));
		}
		if (StringUtils.equals("lastName", filter.getProperty())) {
			predicates.add(builder.equal(root.get("lastName"), filter.getValue()));
		}
		if (StringUtils.equals("email", filter.getProperty())) {
			predicates.add(builder.equal(root.get("email"), filter.getValue()));
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
	                    		builder.equal(subRoot.get("identity"), root), // correlation attr
	                    		builder.between(subRoot.get("workPosition").get("forestIndex").get("lft"), filter.getTreeNode().getLft(), filter.getTreeNode().getRgt())
	                    		)
	            );
			} else {
				subquery.where(
	                    builder.and(
	                    		builder.equal(subRoot.get("identity"), root), // correlation attr
	                    		builder.equal(subRoot.get("workPosition"), filter.getTreeNode())
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
                    		builder.equal(subRoot.get("identity"), root), // correlation attr
                    		builder.equal(subRoot.get("workPosition").get("treeType").get("id"), filter.getTreeTypeId())
                    		)
            );			
			predicates.add(builder.exists(subquery));
		}
		// TODO: dynamic filters (added, overriden by module)
		//
		// subordinates
		if (filter.getSubordinatesFor() != null) {
			predicates.add(subordinatesCriteriaBuilder.getSubordinatesPredicate(root, query, builder, filter.getSubordinatesFor().getUsername(), filter.getSubordinatesByTreeType()));
		}
		// managers
		if (filter.getManagersFor() != null) {
			predicates.add(subordinatesCriteriaBuilder.getSubordinatesPredicate(root, query, builder, filter.getManagersFor().getUsername(), filter.getManagersByTreeType()));
		}
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
		Page<IdmIdentity> managers = repository.find(filter, new PageRequest(0, 50, Sort.Direction.ASC, "username"));
		results.addAll(managers.getContent());
		while (managers.hasNext()) {
			managers = repository.find(filter, managers.nextPageable());
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
}
