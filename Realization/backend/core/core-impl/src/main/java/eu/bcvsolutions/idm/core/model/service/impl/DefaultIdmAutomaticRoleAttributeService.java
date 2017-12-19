package eu.bcvsolutions.idm.core.model.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.SingularAttribute;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.domain.AutomaticRoleAttributeRuleComparison;
import eu.bcvsolutions.idm.core.api.domain.AutomaticRoleAttributeRuleType;
import eu.bcvsolutions.idm.core.api.domain.ConceptRoleRequestOperation;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestedByType;
import eu.bcvsolutions.idm.core.api.dto.AbstractIdmAutomaticRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmAutomaticRoleAttributeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmAutomaticRoleAttributeRuleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmAutomaticRoleFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity_;
import eu.bcvsolutions.idm.core.api.event.EventContext;
import eu.bcvsolutions.idm.core.api.exception.AcceptedException;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.IdmAutomaticRoleAttributeRuleService;
import eu.bcvsolutions.idm.core.api.service.IdmAutomaticRoleAttributeService;
import eu.bcvsolutions.idm.core.api.service.IdmConceptRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.api.utils.AutowireHelper;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.service.IdmFormAttributeService;
import eu.bcvsolutions.idm.core.eav.entity.AbstractFormValue;
import eu.bcvsolutions.idm.core.eav.entity.AbstractFormValue_;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmAutomaticRoleAttribute;
import eu.bcvsolutions.idm.core.model.entity.IdmAutomaticRoleAttribute_;
import eu.bcvsolutions.idm.core.model.entity.IdmAutomaticRole_;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract_;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole_;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity_;
import eu.bcvsolutions.idm.core.model.entity.IdmRole_;
import eu.bcvsolutions.idm.core.model.entity.eav.IdmIdentityContractFormValue;
import eu.bcvsolutions.idm.core.model.entity.eav.IdmIdentityContractFormValue_;
import eu.bcvsolutions.idm.core.model.entity.eav.IdmIdentityFormValue;
import eu.bcvsolutions.idm.core.model.entity.eav.IdmIdentityFormValue_;
import eu.bcvsolutions.idm.core.model.event.AutomaticRoleAttributeEvent;
import eu.bcvsolutions.idm.core.model.event.AutomaticRoleAttributeEvent.AutomaticRoleAttributeEventType;
import eu.bcvsolutions.idm.core.model.event.processor.role.AutomaticRoleAttributeDeleteProcessor;
import eu.bcvsolutions.idm.core.model.repository.IdmAutomaticRoleAttributeRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRepository;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskManager;
import eu.bcvsolutions.idm.core.scheduler.task.impl.ProcessAutomaticRoleByAttributeTaskExecutor;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;

/**
 * Automatic role by attribute service
 * 
 * @author Radek Tomiška
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Service("automaticRoleAttributeService")
public class DefaultIdmAutomaticRoleAttributeService
	extends AbstractReadWriteDtoService<IdmAutomaticRoleAttributeDto, IdmAutomaticRoleAttribute, IdmAutomaticRoleFilter>
	implements IdmAutomaticRoleAttributeService {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory
			.getLogger(DefaultIdmAutomaticRoleAttributeService.class);
	
	private final IdmRoleRequestService roleRequestService;
	private final IdmIdentityContractService identityContractService;
	private final IdmConceptRoleRequestService conceptRoleRequestService;
	private final IdmFormAttributeService formAttributeService;
	private final IdmAutomaticRoleAttributeRuleService automaticRoleAttributeRuleService;
	private final IdmIdentityRoleService identityRoleService;	
	private final EntityEventManager entityEventManager;
	private final EntityManager entityManager;
	private final IdmIdentityRepository identityRepository;
	private final LongRunningTaskManager longRunningTaskManager;
	
	@Autowired
	public DefaultIdmAutomaticRoleAttributeService(
			IdmAutomaticRoleAttributeRepository repository,
			IdmRoleRequestService roleRequestService,
			IdmIdentityContractService identityContractService,
			IdmConceptRoleRequestService conceptRoleRequestService,
			EntityEventManager entityEventManager,
			IdmFormAttributeService formAttributeService,
			IdmAutomaticRoleAttributeRuleService automaticRoleAttributeRuleService,
			IdmIdentityRoleService identityRoleService,
			EntityManager entityManager,
			IdmIdentityRepository identityRepository,
			LongRunningTaskManager longRunningTaskManager) {
		super(repository);
		//
		Assert.notNull(roleRequestService);
		Assert.notNull(identityContractService);
		Assert.notNull(conceptRoleRequestService);
		Assert.notNull(formAttributeService);
		Assert.notNull(automaticRoleAttributeRuleService);
		Assert.notNull(identityRoleService);
		Assert.notNull(entityEventManager);
		Assert.notNull(entityManager);
		Assert.notNull(identityRepository);
		Assert.notNull(longRunningTaskManager);
		//
		this.roleRequestService = roleRequestService;
		this.identityContractService = identityContractService;
		this.conceptRoleRequestService = conceptRoleRequestService;
		this.formAttributeService = formAttributeService;
		this.automaticRoleAttributeRuleService = automaticRoleAttributeRuleService;
		this.identityRoleService = identityRoleService;
		this.entityEventManager = entityEventManager;
		this.entityManager = entityManager;
		this.identityRepository = identityRepository;
		this.longRunningTaskManager = longRunningTaskManager;
	}
	
	/**
	 * Publish {@link AutomaticRoleAttributeEvent} only.
	 * 
	 * @see {@link AutomaticRoleAttributeDeleteProcessor}
	 */
	@Override
	@Transactional(noRollbackFor = AcceptedException.class)
	public void delete(IdmAutomaticRoleAttributeDto dto, BasePermission... permission) {
		Assert.notNull(dto);
		checkAccess(this.getEntity(dto.getId()), permission);
		//
		LOG.debug("Deleting automatic role by attribute [{}]", dto.getRole());
		//
		EventContext<IdmAutomaticRoleAttributeDto> context = entityEventManager.process(new AutomaticRoleAttributeEvent(AutomaticRoleAttributeEventType.DELETE, dto));
		//
		if (context.isSuspended()) {
			throw new AcceptedException();
		}
	}

	@Override
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(CoreGroupPermission.AUTOMATICROLEATTRIBUTE, getEntityClass());
	}

	@Override
	public IdmAutomaticRoleAttributeDto save(IdmAutomaticRoleAttributeDto dto, BasePermission... permission) {
		if (isNew(dto)) { // create
			return super.save(dto, permission);
		}
		//
		// only attribute that can be changed is concept
		IdmAutomaticRoleAttributeDto oldDto = this.get(dto.getId());
		if (dto.getRole().equals(oldDto.getRole()) && dto.getName().equals(oldDto.getName())) {
			return super.save(dto, permission);
		}
		//
		throw new ResultCodeException(CoreResultCode.METHOD_NOT_ALLOWED, "Automatic role update is not supported");
	}
	
	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public IdmRoleRequestDto prepareRemoveAutomaticRoles(IdmIdentityRoleDto identityRole,
			Set<AbstractIdmAutomaticRoleDto> automaticRoles) {
		Assert.notNull(identityRole);
		//
		IdmIdentityContractDto dto = identityContractService.get(identityRole.getIdentityContract());
		return this.processAutomaticRoles(dto, identityRole.getId(), automaticRoles, ConceptRoleRequestOperation.REMOVE);
	}
	
	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public IdmRoleRequestDto prepareAddAutomaticRoles(IdmIdentityContractDto contract,
			Set<AbstractIdmAutomaticRoleDto> automaticRoles) {
		Assert.notNull(contract);
		//
		return this.processAutomaticRoles(contract, null, automaticRoles, ConceptRoleRequestOperation.ADD);
	}

	@Override
	public void processAutomaticRolesForIdentity(UUID identityId, Set<AbstractIdmAutomaticRoleDto> passedAutomaticRoles, Set<AbstractIdmAutomaticRoleDto> notPassedAutomaticRoles) {
		// find all automatic roles for identity
		IdmIdentityRoleFilter roleIdentityFilter = new IdmIdentityRoleFilter();
		roleIdentityFilter.setIdentityId(identityId);
		roleIdentityFilter.setAutomaticRole(Boolean.TRUE);
		List<IdmIdentityRoleDto> allAutomaticRolesByIdentity = identityRoleService.find(roleIdentityFilter, null).getContent();
		// Assign new passed automatic roles (assign to default contract)
		IdmIdentityContractDto primeContract = identityContractService.getPrimeContract(identityId);
		if (!passedAutomaticRoles.isEmpty()) {
			IdmRoleRequestDto roleRequest = processAutomaticRoles(primeContract, null, passedAutomaticRoles, ConceptRoleRequestOperation.ADD);
			roleRequestService.startRequestInternal(roleRequest.getId(), false);
		}
		//
		if (!notPassedAutomaticRoles.isEmpty()) {
			List<UUID> notPassedAutoRoleIds = notPassedAutomaticRoles.stream().map(AbstractIdmAutomaticRoleDto::getId).collect(Collectors.toList());
			allAutomaticRolesByIdentity = allAutomaticRolesByIdentity.stream().filter(autoRoleIdentity -> {
				return notPassedAutoRoleIds.contains(autoRoleIdentity.getRoleTreeNode());
			}).collect(Collectors.toList());
			// iterate over all identity roles
			for (IdmIdentityRoleDto identityRole : allAutomaticRolesByIdentity) {
				IdmIdentityContractDto dto = identityContractService.get(identityRole.getIdentityContract());
				IdmRoleRequestDto roleRequest = this.processAutomaticRoles(dto, identityRole.getId(), notPassedAutomaticRoles, ConceptRoleRequestOperation.REMOVE);
				roleRequest = roleRequestService.startRequestInternal(roleRequest.getId(), false);
			}
		}
	}
	
	@Override
	public Set<AbstractIdmAutomaticRoleDto> getAllNewPassedAutomaticRoleForIdentity(UUID identityId) {
		Set<AbstractIdmAutomaticRoleDto> automaticRoles = new HashSet<>();
		//
		// iterate trough all automatic role
		for (IdmAutomaticRoleAttributeDto automaticRole : this.find(null).getContent()) {
			List<IdmAutomaticRoleAttributeRuleDto> allRulesForAutomaticRole = automaticRoleAttributeRuleService.findAllRulesForAutomaticRoleAndType(automaticRole.getId(), null);
			Specification<IdmIdentity> criteria = this.getCriteriaForRules(automaticRole.getId(), allRulesForAutomaticRole, true, true, identityId);
			boolean pass = !identityRepository.findAll(criteria).isEmpty();
			if (pass) {
				automaticRoles.add(automaticRole);
			}
		}
		//
		return automaticRoles;
	}
	
	@Override
	public Set<AbstractIdmAutomaticRoleDto> getAllNotPassedAutomaticRoleForIdentity(UUID identityId) {
		Set<AbstractIdmAutomaticRoleDto> automaticRoles = new HashSet<>();
		//
		// iterate trough all automatic role
		for (IdmAutomaticRoleAttributeDto automaticRole : this.find(null).getContent()) {
			List<IdmAutomaticRoleAttributeRuleDto> allRulesForAutomaticRole = automaticRoleAttributeRuleService.findAllRulesForAutomaticRoleAndType(automaticRole.getId(), null);
			Specification<IdmIdentity> criteria = this.getCriteriaForRules(automaticRole.getId(), allRulesForAutomaticRole, false, false, identityId);
			// if identity is in list, is'nt pass by this automatic role
			boolean notPass = !identityRepository.findAll(criteria).isEmpty();
			if (notPass) {
				automaticRoles.add(automaticRole);
			}
		}
		//
		return automaticRoles;
	}
	
	@Override
	public Page<UUID> getNewPassedIdentitiesForAutomaticRole(UUID automaticRoleId, Pageable pageable) {
		return this.getIdentitiesForAutomaticRole(automaticRoleId, true, true, pageable);
	}
	
	@Override
	public Page<UUID> getNewNotPassedIdentitiesForAutomaticRole(UUID automaticRoleId, Pageable pageable) {
		return this.getIdentitiesForAutomaticRole(automaticRoleId, false, false, pageable);
	}
	
	@Override
	public Page<UUID> getIdentitiesForAutomaticRole(UUID automaticRoleId, boolean onlyNew, boolean passed, Pageable pageable) {
		List<IdmAutomaticRoleAttributeRuleDto> allRulesForAutomaticRole = automaticRoleAttributeRuleService.findAllRulesForAutomaticRoleAndType(automaticRoleId, null);
		Specification<IdmIdentity> criteria = this.getCriteriaForRules(automaticRoleId, allRulesForAutomaticRole, onlyNew, passed, null);
		Page<IdmIdentity> identities = identityRepository.findAll(criteria, pageable);

		// transform to page uuid
		List<UUID> dtos = identities.getContent().stream().map(IdmIdentity::getId).collect(Collectors.toList());
		PageRequest pageRequest = null;
		if (identities.getSize() > 0) {
			pageRequest = new PageRequest(identities.getNumber(), identities.getSize(), identities.getSort());
		}
		Page<UUID> dtoPage = new PageImpl<>(dtos, pageRequest, identities.getTotalElements());
		return dtoPage;
	}
	
	@Override
	protected List<Predicate> toPredicates(Root<IdmAutomaticRoleAttribute> root, CriteriaQuery<?> query,
			CriteriaBuilder builder, IdmAutomaticRoleFilter filter) {
		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);
		//
		if (StringUtils.isNotEmpty(filter.getName())) {
			predicates.add(builder.equal(root.get(IdmAutomaticRoleAttribute_.name), filter.getName()));
		}
		//
		if (filter.getRoleId() != null) {
			predicates.add(builder.equal(root.get(IdmAutomaticRoleAttribute_.role).get(AbstractEntity_.id), filter.getRoleId()));
		}
		//
		if (StringUtils.isNotEmpty(filter.getText())) {
			predicates.add(builder.or(
				builder.like(builder.lower(root.get(IdmAutomaticRoleAttribute_.name)), "%" + filter.getText().toLowerCase() + "%"),
				builder.like(builder.lower(root.get(IdmAutomaticRoleAttribute_.role).get(IdmRole_.name)), "%" + filter.getText().toLowerCase() + "%")
			));
		}
		//
		return predicates;
	}
	
	@Override
	public void recalculate(UUID automaticRoleId) {
		Assert.notNull(automaticRoleId);
		//
		ProcessAutomaticRoleByAttributeTaskExecutor automaticRoleTask = AutowireHelper.createBean(ProcessAutomaticRoleByAttributeTaskExecutor.class);
		automaticRoleTask.setAutomaticRoleId(automaticRoleId);
		longRunningTaskManager.execute(automaticRoleTask);
	}
	
	/**
	 * Process automatic roles, create concept and request
	 * 
	 * @param contract
	 * @param identityRoleId
	 * @param automaticRoles
	 * @param operation
	 * @return
	 */
	private IdmRoleRequestDto processAutomaticRoles(IdmIdentityContractDto contract, UUID identityRoleId,
			Set<AbstractIdmAutomaticRoleDto> automaticRoles, ConceptRoleRequestOperation operation) {
		Assert.notNull(automaticRoles);
		Assert.notNull(contract);
		Assert.notNull(operation);
		//
		if (automaticRoles.isEmpty()) {
			return null;
		}
		//
		// prepare request
		IdmRoleRequestDto roleRequest = new IdmRoleRequestDto();
		roleRequest.setApplicant(contract.getIdentity());
		roleRequest.setRequestedByType(RoleRequestedByType.AUTOMATICALLY);
		roleRequest.setExecuteImmediately(true); // TODO: by configuration
		roleRequest = roleRequestService.save(roleRequest);
		//
		for(AbstractIdmAutomaticRoleDto automaticRole : automaticRoles) {
			IdmConceptRoleRequestDto conceptRoleRequest = new IdmConceptRoleRequestDto();
			conceptRoleRequest.setRoleRequest(roleRequest.getId());
			conceptRoleRequest.setIdentityContract(contract.getId());
			conceptRoleRequest.setValidFrom(contract.getValidFrom());
			conceptRoleRequest.setIdentityRole(identityRoleId);
			conceptRoleRequest.setValidTill(contract.getValidTill());
			conceptRoleRequest.setRole(automaticRole.getRole());
			conceptRoleRequest.setAutomaticRole(automaticRole.getId());
			//
			conceptRoleRequest.setOperation(operation);
			//
			conceptRoleRequestService.save(conceptRoleRequest);
		};
		//
		return roleRequest;
	}
	
	/**
	 * Return all criteria for given rules
	 * 
	 * @param automaticRoleId
	 * @param rules
	 * @param onlyNew
	 * @param passed
	 * @param identityId
	 * @return
	 */
	private Specification<IdmIdentity> getCriteriaForRules(UUID automaticRoleId, List<IdmAutomaticRoleAttributeRuleDto> rules, boolean onlyNew, boolean passed, UUID identityId) {
		Specification<IdmIdentity> criteria = new Specification<IdmIdentity>() {
			@Override
			public Predicate toPredicate(Root<IdmIdentity> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				List<Predicate> predicates = new ArrayList<>();
				//
				if (onlyNew) {
					// we want only identities that don't already own this automatic role 
					Subquery<IdmIdentityRole> subquery = query.subquery(IdmIdentityRole.class);
					Root<IdmIdentityRole> subRoot = subquery.from(IdmIdentityRole.class);
					subquery.select(subRoot);
					subquery.where(
							cb.and(
									cb.equal(subRoot.get(IdmIdentityRole_.identityContract).get(IdmIdentityContract_.identity), root), // correlation attr
									cb.equal(subRoot.get(IdmIdentityRole_.automaticRole).get(IdmAutomaticRole_.id), automaticRoleId)
									)
							);			
					predicates.add(cb.not(cb.exists(subquery)));
				}
				//
				if (identityId != null) {
					// we want search only for this identity
					predicates.add(cb.equal(root.get(IdmIdentity_.id), identityId));
				}
				//
				List<Predicate> predicatesFromRules = new ArrayList<>();
				for (IdmAutomaticRoleAttributeRuleDto rule : rules) {
					Predicate predicate = DefaultIdmAutomaticRoleAttributeService.this.getPredicateForRule(rule, root,
							query, cb, passed);
					predicatesFromRules.add(predicate);
				}
				//
				if (!passed) {
					// if we find all rules that not pass is necessary add or between predicates from rules
					Predicate or = cb.or(predicatesFromRules.toArray(new Predicate[predicates.size()]));
					predicates.add(or);
				} else {
					predicates.addAll(predicatesFromRules);
				}
				return query.where(predicates.toArray(new Predicate[predicates.size()])).getRestriction();
			}
		};
		return criteria;
	}
	
	/**
	 * Return predicate for given rule
	 * 
	 * @param rule
	 * @param root
	 * @param query
	 * @param cb
	 * @return
	 */
	private Predicate getPredicateForRule(IdmAutomaticRoleAttributeRuleDto rule, Root<IdmIdentity> root,
			CriteriaQuery<?> query, CriteriaBuilder cb, boolean pass) {
		//
		Metamodel metamodel = entityManager.getMetamodel();
		if (rule.getType() == AutomaticRoleAttributeRuleType.IDENTITY) {
			SingularAttribute<? super IdmIdentity, ?> singularAttribute = metamodel.entity(IdmIdentity.class)
					.getSingularAttribute(rule.getAttributeName());
			Path<Object> path = root.get(singularAttribute.getName());
			return getPredicateWithComparsion(path, singularAttribute.getJavaType().cast(rule.getValue()), cb,
					rule.getComparison(), !pass);
		} else if (rule.getType() == AutomaticRoleAttributeRuleType.CONTRACT) {
			Subquery<IdmIdentityContract> subquery = query.subquery(IdmIdentityContract.class);
			Root<IdmIdentityContract> subRoot = subquery.from(IdmIdentityContract.class);
			subquery.select(subRoot);
			//
			SingularAttribute<? super IdmIdentityContract, ?> singularAttribute = metamodel
					.entity(IdmIdentityContract.class).getSingularAttribute(rule.getAttributeName());
			Path<Object> path = subRoot.get(singularAttribute.getName());
			//
			subquery.where(cb.and(cb.equal(subRoot.get(IdmIdentityContract_.identity), root), // correlation attr
					getPredicateWithComparsion(path, singularAttribute.getJavaType().cast(rule.getValue()), cb,
							rule.getComparison(), !pass)));
			return cb.exists(subquery);
		} else if (rule.getType() == AutomaticRoleAttributeRuleType.IDENITITY_EAV) {
			//
			IdmFormAttributeDto formAttributeDto = formAttributeService.get(rule.getFormAttribute());
			//
			Object value = ConvertUtils.convert(rule.getValue(), getEavType(formAttributeDto.getPersistentType()));
			//
			Subquery<IdmIdentityFormValue> subquery = query.subquery(IdmIdentityFormValue.class);
			Root<IdmIdentityFormValue> subRoot = subquery.from(IdmIdentityFormValue.class);
			subquery.select(subRoot);
			//
			Path<?> path = subRoot.get(getSingularAttributeForEav(formAttributeDto.getPersistentType()));
			subquery.where(
					cb.and(
							cb.equal(subRoot.get(IdmIdentityFormValue_.owner), root),
							cb.equal(subRoot.get(IdmIdentityFormValue_.formAttribute).get(AbstractFormValue_.id), formAttributeDto.getId()),
							getPredicateWithComparsion(path, value, cb, rule.getComparison(), !pass)
							)
            );
			return cb.exists(subquery);
		} else if (rule.getType() == AutomaticRoleAttributeRuleType.CONTRACT_EAV) {
			IdmFormAttributeDto formAttributeDto = formAttributeService.get(rule.getFormAttribute());
			//
			Object value = ConvertUtils.convert(rule.getValue(), getEavType(formAttributeDto.getPersistentType()));
			//
			Subquery<IdmIdentityContract> subquery = query.subquery(IdmIdentityContract.class);
			Root<IdmIdentityContract> subRoot = subquery.from(IdmIdentityContract.class);
			subquery.select(subRoot);
			//
			
			//
			Subquery<IdmIdentityContractFormValue> subQueryContractEav = query.subquery(IdmIdentityContractFormValue.class);
			Root<IdmIdentityContractFormValue> subRootContractEav = subQueryContractEav.from(IdmIdentityContractFormValue.class);
			subQueryContractEav.select(subRootContractEav);
			//
			Path<?> path = subRootContractEav.get(getSingularAttributeForEav(formAttributeDto.getPersistentType()));
			subQueryContractEav.where(
					cb.and(
							cb.equal(subRootContractEav.get(IdmIdentityContractFormValue_.owner), subRoot),
							cb.equal(subRootContractEav.get(IdmIdentityContractFormValue_.formAttribute).get(AbstractFormValue_.id), formAttributeDto.getId()),
							getPredicateWithComparsion(path, value, cb, rule.getComparison(), !pass)
							));
			//
			subquery.where(
					cb.and(
							cb.equal(subRoot.get(IdmIdentityContract_.identity), root),
							cb.exists(subQueryContractEav))
							);
			//
			return cb.exists(subquery);
		} else {
			throw new UnsupportedOperationException("Type: " + rule.getType().name() + ", isn't supported!");
		}
	}
	
	/**
	 * Return predicate with comparison. If Parameter negation is true comparison type will be negative ->
	 * equal == not equal.
	 * 
	 * @param path
	 * @param value
	 * @param cb
	 * @param comparsion
	 * @param neq
	 * @return
	 */
	private Predicate getPredicateWithComparsion(Path<?> path, Object value, CriteriaBuilder cb,
			AutomaticRoleAttributeRuleComparison comparsion, boolean negation) {
		Assert.notNull(comparsion);
		Assert.notNull(path);
		Assert.notNull(cb);
		// TODO: now is implement only equals
		if (comparsion == AutomaticRoleAttributeRuleComparison.EQUALS) {
			// for string type is necessary explicit type to as String
			if (value instanceof String) {
				path.as(String.class);
			}
			if (negation) {
				return cb.or(
						cb.notEqual(path, value),
						cb.isNull(path));
			}
			return cb.equal(path, value);
		}
		throw new UnsupportedOperationException("Operation: " + comparsion.name() + ", is not supported.");
	}
	
	/**
	 * Return singular attribute for {@link PersistentType}
	 * 
	 * @param persistentType
	 * @return
	 */
	private SingularAttribute<AbstractFormValue, ?> getSingularAttributeForEav(PersistentType persistentType) {
		switch (persistentType) {
		case INT:
		case LONG:
			return AbstractFormValue_.longValue;
		case BOOLEAN:
			return AbstractFormValue_.booleanValue;
		case DATE:
		case DATETIME:
			return AbstractFormValue_.dateValue;
		case DOUBLE:
			return AbstractFormValue_.doubleValue;
		case BYTEARRAY: {
			return AbstractFormValue_.byteValue;
		}
		case UUID: {
			return AbstractFormValue_.uuidValue;
		}
		default:
			return AbstractFormValue_.stringValue;
		}
	}
	
	/**
	 * Return class type for {@link PersistentType}
	 * 
	 * @param persistentType
	 * @return
	 */
	private Class<?> getEavType(PersistentType persistentType) {
		switch (persistentType) {
		case INT:
		case LONG:
			return Long.class;
		case BOOLEAN:
			return Boolean.class;
		case DATE:
		case DATETIME:
			return DateTime.class;
		case DOUBLE:
			return BigDecimal.class;
		case BYTEARRAY: {
			return byte[].class;
		}
		case UUID: {
			return UUID.class;
		}
		default:
			return String.class;
		}
	}
}