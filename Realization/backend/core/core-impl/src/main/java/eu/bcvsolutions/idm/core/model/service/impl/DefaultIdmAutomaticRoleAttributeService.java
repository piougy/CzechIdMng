package eu.bcvsolutions.idm.core.model.service.impl;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.SingularAttribute;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.domain.AutomaticRoleAttributeRuleComparison;
import eu.bcvsolutions.idm.core.api.domain.AutomaticRoleAttributeRuleType;
import eu.bcvsolutions.idm.core.api.domain.ConceptRoleRequestOperation;
import eu.bcvsolutions.idm.core.api.domain.ContractState;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.PriorityType;
import eu.bcvsolutions.idm.core.api.dto.AbstractIdmAutomaticRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmAutomaticRoleAttributeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmAutomaticRoleAttributeRuleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmContractPositionDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmAutomaticRoleFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity_;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventContext;
import eu.bcvsolutions.idm.core.api.exception.AcceptedException;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.IdmAutomaticRoleAttributeRuleService;
import eu.bcvsolutions.idm.core.api.service.IdmAutomaticRoleAttributeService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.api.utils.AutowireHelper;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.service.IdmFormAttributeService;
import eu.bcvsolutions.idm.core.eav.entity.AbstractFormValue;
import eu.bcvsolutions.idm.core.eav.entity.AbstractFormValue_;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmAutomaticRoleAttribute;
import eu.bcvsolutions.idm.core.model.entity.IdmAutomaticRoleAttributeRule;
import eu.bcvsolutions.idm.core.model.entity.IdmAutomaticRoleAttributeRule_;
import eu.bcvsolutions.idm.core.model.entity.IdmAutomaticRoleAttribute_;
import eu.bcvsolutions.idm.core.model.entity.IdmAutomaticRole_;
import eu.bcvsolutions.idm.core.model.entity.IdmContractPosition_;
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
import eu.bcvsolutions.idm.core.model.event.IdentityRoleEvent;
import eu.bcvsolutions.idm.core.model.event.IdentityRoleEvent.IdentityRoleEventType;
import eu.bcvsolutions.idm.core.model.event.processor.role.AutomaticRoleAttributeDeleteProcessor;
import eu.bcvsolutions.idm.core.model.repository.IdmAutomaticRoleAttributeRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityContractRepository;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskManager;
import eu.bcvsolutions.idm.core.scheduler.task.impl.ProcessAutomaticRoleByAttributeTaskExecutor;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;

/**
 * Automatic role by attribute service
 * 
 * @author Radek Tomiška
 * @author Ondrej Kopr
 *
 */
public class DefaultIdmAutomaticRoleAttributeService
	extends AbstractReadWriteDtoService<IdmAutomaticRoleAttributeDto, IdmAutomaticRoleAttribute, IdmAutomaticRoleFilter>
	implements IdmAutomaticRoleAttributeService {
	
	/*
	 * Uses in page request
	 */
	private int PROCESS_ROLE_SIZE = 10;

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory
			.getLogger(DefaultIdmAutomaticRoleAttributeService.class);
	
	private final IdmIdentityContractService identityContractService;
	private final IdmFormAttributeService formAttributeService;
	private final IdmAutomaticRoleAttributeRuleService automaticRoleAttributeRuleService;
	private final IdmIdentityRoleService identityRoleService;	
	private final EntityEventManager entityEventManager;
	private final EntityManager entityManager;
	private final IdmIdentityContractRepository identityContractRepository;
	private final LongRunningTaskManager longRunningTaskManager;
	@Autowired
	private IdmRoleRequestService roleRequestService;
	
	@Autowired
	public DefaultIdmAutomaticRoleAttributeService(
			IdmAutomaticRoleAttributeRepository repository,
			IdmIdentityContractService identityContractService,
			EntityEventManager entityEventManager,
			IdmFormAttributeService formAttributeService,
			IdmAutomaticRoleAttributeRuleService automaticRoleAttributeRuleService,
			IdmIdentityRoleService identityRoleService,
			EntityManager entityManager,
			LongRunningTaskManager longRunningTaskManager,
			IdmIdentityContractRepository identityContractRepository) {
		super(repository);
		//
		Assert.notNull(identityContractService, "Service is required.");
		Assert.notNull(formAttributeService, "Service is required.");
		Assert.notNull(automaticRoleAttributeRuleService, "Service is required.");
		Assert.notNull(identityRoleService, "Service is required.");
		Assert.notNull(entityEventManager, "Manager is required.");
		Assert.notNull(entityManager, "Manager is required.");
		Assert.notNull(longRunningTaskManager, "Manager is required.");
		Assert.notNull(identityContractRepository, "Repository is required.");
		//
		this.identityContractService = identityContractService;
		this.formAttributeService = formAttributeService;
		this.automaticRoleAttributeRuleService = automaticRoleAttributeRuleService;
		this.identityRoleService = identityRoleService;
		this.entityEventManager = entityEventManager;
		this.entityManager = entityManager;
		this.longRunningTaskManager = longRunningTaskManager;
		this.identityContractRepository = identityContractRepository;
	}
	
	/**
	 * Publish {@link AutomaticRoleAttributeEvent} only.
	 * 
	 * @see {@link AutomaticRoleAttributeDeleteProcessor}
	 */
	@Override
	@Transactional(noRollbackFor = AcceptedException.class)
	public void delete(IdmAutomaticRoleAttributeDto dto, BasePermission... permission) {
		Assert.notNull(dto, "DTO is required.");
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
	@Transactional(noRollbackFor = AcceptedException.class)
	public IdmAutomaticRoleAttributeDto save(IdmAutomaticRoleAttributeDto dto, BasePermission... permission) {
		if (isNew(dto)) { // create
			EventContext<IdmAutomaticRoleAttributeDto> context = entityEventManager.process(new AutomaticRoleAttributeEvent(AutomaticRoleAttributeEventType.CREATE, dto));
			if (context.isSuspended()) {
				throw new AcceptedException();
			}
			return context.getContent();
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
	public void removeAutomaticRoles(IdmIdentityRoleDto identityRole) {
		Assert.notNull(identityRole.getAutomaticRole(), "Automatic role is required.");
		// skip check granted authorities
		IdentityRoleEvent event = new IdentityRoleEvent(IdentityRoleEventType.DELETE, identityRole);
		event.getProperties().put(IdmIdentityRoleService.SKIP_CHECK_AUTHORITIES, Boolean.TRUE);
		identityRoleService.publish(event);
	}
	
	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void removeAutomaticRoles(UUID contractId, Set<AbstractIdmAutomaticRoleDto> automaticRoles) {
		removeAutomaticRolesInternal(contractId, automaticRoles);
	}
	
	@Override
	@Transactional
	public void removeAutomaticRolesInternal(UUID contractId, Set<AbstractIdmAutomaticRoleDto> automaticRoles) {
		List<IdmConceptRoleRequestDto> concepts = new ArrayList<IdmConceptRoleRequestDto>();

		// Identity id is get from embedded identity role. This is little speedup.
		UUID identityId = null;

		for (AbstractIdmAutomaticRoleDto autoRole : automaticRoles) {
			IdmIdentityRoleFilter identityRoleFilter = new IdmIdentityRoleFilter();
			identityRoleFilter.setIdentityContractId(contractId);
			identityRoleFilter.setAutomaticRoleId(autoRole.getId());
			//
			// TODO: possible performance update with pageable
			for (IdmIdentityRoleDto identityRole : identityRoleService.find(identityRoleFilter, null).getContent()) {
				IdmConceptRoleRequestDto concept = new IdmConceptRoleRequestDto();
				concept.setIdentityContract(contractId);
				concept.setRole(autoRole.getRole());
				concept.setAutomaticRole(autoRole.getId());
				concept.setIdentityRole(identityRole.getId());
				concept.setOperation(ConceptRoleRequestOperation.REMOVE);
				concepts.add(concept);

				if (identityId == null) {
					IdmIdentityContractDto contractDto = DtoUtils.getEmbedded(identityRole, IdmIdentityRole_.identityContract, IdmIdentityContractDto.class);
					identityId = contractDto.getIdentity();
				}
			}
		}
		
		// Execute concepts
		roleRequestService.executeConceptsImmediate(identityId, concepts);
	}
	
	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void addAutomaticRoles(IdmIdentityContractDto contract, Set<AbstractIdmAutomaticRoleDto> automaticRoles) {		
		addAutomaticRolesInternal(contract, automaticRoles);
	}
	
	@Override
	@Transactional
	public void addAutomaticRolesInternal(IdmIdentityContractDto contract,
			Set<AbstractIdmAutomaticRoleDto> automaticRoles) {
		createIdentityRoles(contract, null, automaticRoles);
	}
	
	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void addAutomaticRoles(IdmContractPositionDto contractPosition, Set<AbstractIdmAutomaticRoleDto> automaticRoles) {		
		IdmIdentityContractDto contract = DtoUtils.getEmbedded(contractPosition, IdmContractPosition_.identityContract);
		createIdentityRoles(contract, contractPosition, automaticRoles);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void processAutomaticRolesForContract(UUID contractId, Set<AbstractIdmAutomaticRoleDto> passedAutomaticRoles, Set<AbstractIdmAutomaticRoleDto> notPassedAutomaticRoles) {
		// Just call internal method, transaction is wrapped by this method.
		processAutomaticRolesForContractInternal(contractId, passedAutomaticRoles, notPassedAutomaticRoles);
	}

	@Override
	@Transactional
	public void processAutomaticRolesForContractInternal(UUID contractId,
			Set<AbstractIdmAutomaticRoleDto> passedAutomaticRoles,
			Set<AbstractIdmAutomaticRoleDto> notPassedAutomaticRoles) {
		// Assign new passed automatic roles (assign to default contract)
		IdmIdentityContractDto contract = identityContractService.get(contractId);
		//
		if (contract == null) {
			LOG.debug(MessageFormat.format("Contract id [{0}] not found.", contractId));
			return;
		}
		// check contract validity for newly add roles
		// TODO: this behavior can be optimalized by add it into query
		if (!contract.isValidNowOrInFuture() || contract.getState() == ContractState.DISABLED) {
			// null all new passed automatic roles
			passedAutomaticRoles = null;
		}
		//
		if (passedAutomaticRoles != null && !passedAutomaticRoles.isEmpty()) {
			this.addAutomaticRoles(contract, passedAutomaticRoles);
		}
		//
		if (notPassedAutomaticRoles != null && !notPassedAutomaticRoles.isEmpty()) {
			this.removeAutomaticRoles(contract.getId(), notPassedAutomaticRoles);
		}
	}

	@Override
	public Page<IdmAutomaticRoleAttributeDto> findAllToProcess(AutomaticRoleAttributeRuleType type, Pageable page) {
		IdmAutomaticRoleFilter filter = new IdmAutomaticRoleFilter();
		filter.setConcept(Boolean.FALSE);
		filter.setRuleType(type);
		filter.setHasRules(Boolean.TRUE);
		return this.find(filter, page);
	}
	
	@Override
	public Page<UUID> getContractsForAutomaticRole(UUID automaticRoleId, boolean passed, Pageable pageable) {
		List<IdmAutomaticRoleAttributeRuleDto> rulesForContracts = automaticRoleAttributeRuleService.findAllRulesForAutomaticRole(automaticRoleId);
		//
		if (rulesForContracts.isEmpty()) {
			return new PageImpl<>(Collections.emptyList(), pageable, 0);
		}
		//
		Specification<IdmIdentityContract> criteria = this.getCriteriaForRulesByContract(automaticRoleId, rulesForContracts, passed, null);
		Page<IdmIdentityContract> contracts = identityContractRepository.findAll(criteria, pageable);
		//
		// transform to page uuid
		List<UUID> dtos = contracts.getContent().stream().map(IdmIdentityContract::getId).collect(Collectors.toList());
		return new PageImpl<>(dtos, pageable, contracts.getTotalElements());
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
		String text = filter.getText();
		if (StringUtils.isNotEmpty(text)) {
			text = text.toLowerCase();
			predicates.add(builder.or(
				builder.like(builder.lower(root.get(IdmAutomaticRoleAttribute_.name)), "%" + text + "%"),
				builder.like(builder.lower(root.get(IdmAutomaticRoleAttribute_.role).get(IdmRole_.name)), "%" + text + "%"),
				builder.like(builder.lower(root.get(IdmAutomaticRoleAttribute_.role).get(IdmRole_.code)), "%" + text + "%")
			));
		}
		//
		if (filter.getConcept() != null) {
			predicates.add(builder.equal(root.get(IdmAutomaticRoleAttribute_.concept), filter.getConcept()));
		}
		//
		if (filter.getHasRules() != null) {
			Subquery<IdmAutomaticRoleAttributeRule> subquery = query.subquery(IdmAutomaticRoleAttributeRule.class);
			Root<IdmAutomaticRoleAttributeRule> subRoot = subquery.from(IdmAutomaticRoleAttributeRule.class);
			subquery.select(subRoot);
			
			subquery.where(builder.equal(subRoot.get(IdmAutomaticRoleAttributeRule_.automaticRoleAttribute), root)); // correlation attr only
			
			if (BooleanUtils.isTrue(filter.getHasRules())) {
				predicates.add(builder.exists(subquery));
			} else {
				predicates.add(builder.isNull(subquery));
			}
		}
		//
		if (filter.getRuleType() != null) {
			Subquery<IdmAutomaticRoleAttributeRule> subquery = query.subquery(IdmAutomaticRoleAttributeRule.class);
			Root<IdmAutomaticRoleAttributeRule> subRoot = subquery.from(IdmAutomaticRoleAttributeRule.class);
			subquery.select(subRoot);
			
			subquery.where(
                    builder.and(
                    		builder.equal(subRoot.get(IdmAutomaticRoleAttributeRule_.automaticRoleAttribute), root), // correlation attr
                    		builder.equal(subRoot.get(IdmAutomaticRoleAttributeRule_.type), filter.getRuleType())
                    		)
            );
			predicates.add(builder.exists(subquery));
		}
		//
		return predicates;
	}
	
	@Override
	@Transactional
	public IdmAutomaticRoleAttributeDto recalculate(UUID automaticRoleId) {
		Assert.notNull(automaticRoleId, "Automatic role identifier is required.");
		IdmAutomaticRoleAttributeDto automaticRole = get(automaticRoleId);
		Assert.notNull(automaticRole, "Automatic role is required.");
		//
		EntityEvent<IdmAutomaticRoleAttributeDto> event = new AutomaticRoleAttributeEvent(AutomaticRoleAttributeEventType.UPDATE, automaticRole);
		event.setPriority(PriorityType.NORMAL);
		//
		return recalculate(event);
	}
	
	@Override
	@Transactional
	public IdmAutomaticRoleAttributeDto recalculate(EntityEvent<IdmAutomaticRoleAttributeDto> event) {
		Assert.notNull(event, "Event is required.");
		IdmAutomaticRoleAttributeDto automaticRoleAttributeDto = event.getContent();
		Assert.notNull(automaticRoleAttributeDto.getId(), "Automatic role identifier is required.");
		//
		// set concept to false before recalculation
		automaticRoleAttributeDto.setConcept(false);
		automaticRoleAttributeDto = this.save(automaticRoleAttributeDto);
		//
		ProcessAutomaticRoleByAttributeTaskExecutor automaticRoleTask = AutowireHelper.createBean(ProcessAutomaticRoleByAttributeTaskExecutor.class);
		automaticRoleTask.setAutomaticRoleId(automaticRoleAttributeDto.getId());
		if (event.getPriority() == PriorityType.IMMEDIATE) {
			automaticRoleTask.setAsync(false);
			longRunningTaskManager.executeSync(automaticRoleTask);
		} else {
			longRunningTaskManager.execute(automaticRoleTask);
		}
		//
		return automaticRoleAttributeDto;
	}
	
	@Override
	public Set<AbstractIdmAutomaticRoleDto> getRulesForContract(boolean pass, AutomaticRoleAttributeRuleType type, UUID contractId) {
		Set<AbstractIdmAutomaticRoleDto> automaticRoles = new HashSet<>();
		//
		// iterate trough all automatic role that has at least one rule and isn't in concept state
		Page<IdmAutomaticRoleAttributeDto> automaticRolesToProcess = this.findAllToProcess(type, PageRequest.of(0, PROCESS_ROLE_SIZE));
		while (automaticRolesToProcess.hasContent()) {
			// all found roles it will has rules and will not be in concept state
			for (IdmAutomaticRoleAttributeDto automaticRole : automaticRolesToProcess) {
				List<IdmAutomaticRoleAttributeRuleDto> allRulesForAutomaticRole = automaticRoleAttributeRuleService.findAllRulesForAutomaticRole(automaticRole.getId());
				//
				Specification<IdmIdentityContract> criteria = this.getCriteriaForRulesByContract(automaticRole.getId(), allRulesForAutomaticRole, pass, contractId);
				boolean result = !identityContractRepository.findAll(criteria).isEmpty();
				if (result) {
					automaticRoles.add(automaticRole);
				}
			}
			//
			if (automaticRolesToProcess.hasNext()) {
				automaticRolesToProcess = this.findAllToProcess(type, automaticRolesToProcess.nextPageable());
			} else {
				break;
			}
		}
		//
		return automaticRoles;
	}
	
	/**
	 * Return all criteria for given rules by contract
	 * Compose all specification for identity/contract and rules
	 * 
	 * @param automaticRoleId
	 * @param rules
	 * @param onlyNew
	 * @param passed
	 * @param identityId
	 * @param contractId
	 * @return
	 */
	private Specification<IdmIdentityContract> getCriteriaForRulesByContract(UUID automaticRoleId, List<IdmAutomaticRoleAttributeRuleDto> rules, boolean passed, UUID contractId) {
		Specification<IdmIdentityContract> criteria = new Specification<IdmIdentityContract>() {

			private static final long serialVersionUID = 1L;

			@Override
			public Predicate toPredicate(Root<IdmIdentityContract> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				List<Predicate> predicates = new ArrayList<>();
				//
				if (contractId != null) {
					predicates.add(cb.equal(root.get(AbstractEntity_.id), contractId));
				}
				//
				// If passed add condition for valid now or in future and disabled false
				if (passed) {
					predicates.add( //
								cb.and( //
									cb.or( //
										cb.greaterThanOrEqualTo(root.get(IdmIdentityContract_.validTill), LocalDate.now()), //
										cb.isNull(root.get(IdmIdentityContract_.validTill)) //
										), //
									cb.equal(root.get(IdmIdentityContract_.disabled), Boolean.FALSE) //
									)); //
				}
				//
				Subquery<IdmIdentityRole> subquery = query.subquery(IdmIdentityRole.class);
				Root<IdmIdentityRole> subRoot = subquery.from(IdmIdentityRole.class);
				subquery.select(subRoot);
				subquery.where(
						cb.and(
								cb.equal(subRoot.get(IdmIdentityRole_.identityContract), root), // correlation attr
								cb.equal(subRoot.get(IdmIdentityRole_.automaticRole).get(IdmAutomaticRole_.id), automaticRoleId)
								)
						);
				//
				if (passed) {
					predicates.add(cb.isNull(subquery));
				} else {
					predicates.add(cb.exists(subquery));
				}
				//
				List<Predicate> predicatesFromRules = new ArrayList<>(rules.size());
				for (IdmAutomaticRoleAttributeRuleDto rule : rules) {
					// compose all predicate from rules
					Predicate predicate = DefaultIdmAutomaticRoleAttributeService.this.getPredicateForRuleByContract(rule, root,
							query, cb, passed);
					predicatesFromRules.add(predicate);
				}
				//
				if (!predicatesFromRules.isEmpty()) {
					if (!passed) {
						// if we find all rules that not pass is necessary add 'or' statement between predicates from rules
						Predicate or = cb.or(predicatesFromRules.toArray(new Predicate[predicatesFromRules.size()]));
						predicates.add(or);
					} else {
						predicates.addAll(predicatesFromRules);
					}
				}
				return query.where(predicates.toArray(new Predicate[predicates.size()])).getRestriction();
			}
		};
		return criteria;
	}
	
	/**
	 * Return predicate for given rule by contract
	 * 
	 * @param rule
	 * @param root
	 * @param query
	 * @param cb
	 * @return
	 */
	private Predicate getPredicateForRuleByContract(IdmAutomaticRoleAttributeRuleDto rule, Root<IdmIdentityContract> root,
			CriteriaQuery<?> query, CriteriaBuilder cb, boolean pass) {
		//
		Metamodel metamodel = entityManager.getMetamodel();
		if (rule.getType() == AutomaticRoleAttributeRuleType.CONTRACT) {
			SingularAttribute<? super IdmIdentityContract, ?> singularAttribute = metamodel.entity(IdmIdentityContract.class)
					.getSingularAttribute(rule.getAttributeName());
			Path<Object> path = root.get(singularAttribute.getName());
			//
			// For contract is used direct query, because root for query is direct contract
			// Also for contract rules is used last parameter negation, this parameter control which
			// role will be added and wich roles will be removed.
			return getPredicateWithComparsion(
					path,
					castToType(singularAttribute, rule.getValue(), rule.getComparison()),
					cb,
					rule.getComparison(),
					!pass);
		} else if (rule.getType() == AutomaticRoleAttributeRuleType.CONTRACT_EAV) {
			IdmFormAttributeDto formAttributeDto = formAttributeService.get(rule.getFormAttribute());
			AutomaticRoleAttributeRuleComparison comparison = rule.getComparison();
			// Cast given value to specific persistent type
			// For is empty and is not empty comparison is returned null even if value exists
			Object value = getFormValue(rule.getValue(), formAttributeDto.getPersistentType(), comparison);
			//
			// For contract form attribute was composed only one subquery
			Subquery<IdmIdentityContractFormValue> subquery = query.subquery(IdmIdentityContractFormValue.class);
			Root<IdmIdentityContractFormValue> subRoot = subquery.from(IdmIdentityContractFormValue.class);
			subquery.select(subRoot);
			//
			Path<?> path = subRoot.get(getSingularAttributeForEav(formAttributeDto.getPersistentType()));
			//
			// Is empty comparison has specific behavior because form value isn't empty, but value doesn't exist 
			if (comparison == AutomaticRoleAttributeRuleComparison.IS_EMPTY) {
				subquery.where(
						cb.or(
							// Predicate for check if value exists
							getPredicateForNullFormAttributeIdentityContract(root, query, cb, formAttributeDto, pass),
							cb.and(
								cb.equal(subRoot.get(IdmIdentityContractFormValue_.owner), root),
								cb.equal(subRoot.get(IdmIdentityContractFormValue_.formAttribute).get(AbstractFormValue_.id), formAttributeDto.getId()),
								getPredicateWithComparsion(path, null, cb, rule.getComparison(), null)
								)
						));
				if(pass) {
					return cb.not(cb.exists(subquery));	
				}
				return cb.exists(subquery);
			}
			//
			subquery.where(
					cb.and(
						cb.equal(subRoot.get(IdmIdentityContractFormValue_.owner), root),
						cb.equal(subRoot.get(IdmIdentityContractFormValue_.formAttribute).get(AbstractFormValue_.id), formAttributeDto.getId()),
						getPredicateWithComparsion(path, value, cb, rule.getComparison(), null)
						)
					);
			//
			Predicate existsInEav = getPredicateForConnection(subquery, cb, pass, formAttributeDto.isMultiple());
			//
			return existsInEav;
		} else if (rule.getType() == AutomaticRoleAttributeRuleType.IDENTITY_EAV) {
			IdmFormAttributeDto formAttributeDto = formAttributeService.get(rule.getFormAttribute());
			AutomaticRoleAttributeRuleComparison comparison = rule.getComparison();
			// Cast given value to specific persistent type
						// For is empty and is not empty comparison is returned null even if value exists
			Object value = getFormValue(rule.getValue(), formAttributeDto.getPersistentType(), comparison);
			//
			// Rules for identity form values must contains two subquery identity -> identity eav
			Subquery<IdmIdentity> subquery = query.subquery(IdmIdentity.class);
			Root<IdmIdentity> subRoot = subquery.from(IdmIdentity.class);
			subquery.select(subRoot);
			//
			Subquery<IdmIdentityFormValue> subQueryIdentityEav = query.subquery(IdmIdentityFormValue.class);
			Root<IdmIdentityFormValue> subRootIdentityEav = subQueryIdentityEav.from(IdmIdentityFormValue.class);
			subQueryIdentityEav.select(subRootIdentityEav);
			//
			Path<?> path = subRootIdentityEav.get(getSingularAttributeForEav(formAttributeDto.getPersistentType()));
			//
			// Is empty comparison has specific behavior because form value isn't empty, but value doesn't exist 
			if (comparison == AutomaticRoleAttributeRuleComparison.IS_EMPTY) {
				subquery.where(
					cb.and(
						cb.equal(root.get(IdmIdentityContract_.identity), subRoot),
						cb.exists(
							subQueryIdentityEav.where(
								cb.or(
									cb.and(
										cb.equal(subRootIdentityEav.get(IdmIdentityFormValue_.owner), subRoot),
										cb.equal(subRootIdentityEav.get(IdmIdentityFormValue_.formAttribute).get(AbstractFormValue_.id), formAttributeDto.getId()),
										getPredicateWithComparsion(path, null, cb, rule.getComparison(), null)),
										// Predicate for check if value exists
										getPredicateForNullFormAttributeIdentity(subRoot, subquery, cb, formAttributeDto, pass)
									)
								)
							)
						)
					);
				//
				if(pass) {
					return cb.not(cb.exists(subquery));
				}
				return cb.exists(subquery);
			}
			//
			subQueryIdentityEav.where(
					cb.and(
							cb.equal(subRootIdentityEav.get(IdmIdentityFormValue_.owner), subRoot),
							cb.equal(root.get(IdmIdentityContract_.identity), subRoot),
							cb.equal(subRootIdentityEav.get(IdmIdentityFormValue_.formAttribute).get(AbstractFormValue_.id), formAttributeDto.getId()),
							getPredicateWithComparsion(path, value, cb, rule.getComparison(), null)
							));
			Predicate existsInEav = getPredicateForConnection(subQueryIdentityEav, cb, pass, formAttributeDto.isMultiple());
			//
			subquery.where(
					cb.and(
							cb.equal(subRoot.get(IdmIdentity_.id), root.get(IdmIdentityContract_.identity).get(AbstractEntity_.id)),
							existsInEav)
					);
			//
			return cb.exists(subquery);
		} else if (rule.getType() == AutomaticRoleAttributeRuleType.IDENTITY) {
			Subquery<IdmIdentity> subquery = query.subquery(IdmIdentity.class);
			Root<IdmIdentity> subRoot = subquery.from(IdmIdentity.class);
			subquery.select(subRoot);
			//
			SingularAttribute<? super IdmIdentity, ?> singularAttribute = metamodel
					.entity(IdmIdentity.class).getSingularAttribute(rule.getAttributeName());
			Path<Object> path = subRoot.get(singularAttribute.getName());
			//
			subquery.where(cb.and(cb.equal(subRoot.get(IdmIdentity_.id), root.get(IdmIdentityContract_.identity).get(AbstractEntity_.id)), // correlation attr
					getPredicateWithComparsion(path, castToType(singularAttribute, rule.getValue(), rule.getComparison()), cb,
							rule.getComparison(), null)));
			//
			return getPredicateForConnection(subquery, cb, pass, false);
		} else {
			throw new UnsupportedOperationException("Type: " + rule.getType().name() + ", isn't supported for contract rules!");
		}
	}

	/**
	 * Create subquery for check null form values for identity root
	 *
	 * @param root
	 * @param query
	 * @param cb
	 * @param formAttributeDto
	 * @param pass
	 * @return
	 */
	private Predicate getPredicateForNullFormAttributeIdentity(Root<IdmIdentity> root, Subquery<IdmIdentity> query, CriteriaBuilder cb, IdmFormAttributeDto formAttributeDto, boolean pass) {
		Subquery<IdmIdentityFormValue> subqueryNull = query.subquery(IdmIdentityFormValue.class);
		Root<IdmIdentityFormValue> subRootNull = subqueryNull.from(IdmIdentityFormValue.class);
		subqueryNull.select(subRootNull);
		
		subqueryNull.where(cb.and(
				cb.equal(subRootNull.get(IdmIdentityFormValue_.owner), root),
				cb.equal(subRootNull.get(IdmIdentityFormValue_.formAttribute).get(AbstractFormValue_.id), formAttributeDto.getId())
				));
		return cb.exists(subqueryNull);
	}

	/**
	 * Create subquery for check null form values for identity contract root
	 *
	 * @param root
	 * @param query
	 * @param cb
	 * @param formAttributeDto
	 * @param pass
	 * @return
	 */
	private Predicate getPredicateForNullFormAttributeIdentityContract(Root<IdmIdentityContract> root, CriteriaQuery<?> query,CriteriaBuilder cb, IdmFormAttributeDto formAttributeDto, boolean pass) {
		Subquery<IdmIdentityContractFormValue> subqueryNull = query.subquery(IdmIdentityContractFormValue.class);
		Root<IdmIdentityContractFormValue> subRootNull = subqueryNull.from(IdmIdentityContractFormValue.class);
		subqueryNull.select(subRootNull);
		
		subqueryNull.where(cb.and(
				cb.equal(subRootNull.get(IdmIdentityContractFormValue_.owner), root),
				cb.equal(subRootNull.get(IdmIdentityContractFormValue_.formAttribute).get(AbstractFormValue_.id), formAttributeDto.getId())
				));
		return cb.exists(subqueryNull);
	}

	/**
	 * Method is used for connect {@link Subquery} with outer query.
	 * In equal return exists otherwise return is null
	 * The method is used only for identity eav, contract eav and identity attributes.
	 *
	 * @param subQuery
	 * @param cb
	 * @param pass
	 * @param multivalued
	 * @return
	 */
	private Predicate getPredicateForConnection(Subquery<?> subQuery, CriteriaBuilder cb, boolean pass, boolean multivalued) {
		if (pass) {
			return cb.exists(subQuery);
		} else {
			if (multivalued) {
				return cb.not(cb.exists(subQuery));
			}
			return cb.isNull(subQuery);
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
			AutomaticRoleAttributeRuleComparison comparsion, Boolean negation) {
		Assert.notNull(comparsion, "Comparison operator is required.");
		Assert.notNull(path, "Path is required.");
		Assert.notNull(cb, "Criteria builder is required.");
		// TODO: now is implement only equals
		if (comparsion == AutomaticRoleAttributeRuleComparison.EQUALS) {
			//
			// is necessary explicit type to as String
			path.as(String.class);
			//
			if (BooleanUtils.isTrue(negation)) {
				Predicate predicate = null;
				//
				if (value instanceof Boolean) {
					// for boolean must be specific equals (isTrue or isFalse, notEqual doesn't works)
					Expression<Boolean> booleanAs = path.as(Boolean.class);
					if (BooleanUtils.isTrue((Boolean) value)) {
						predicate = cb.equal(booleanAs, cb.literal(Boolean.FALSE));
					} else {
						predicate = cb.equal(booleanAs, cb.literal(Boolean.TRUE));
					}
				} else {
					// for other type just classic negative compare
					predicate = cb.notEqual(path, value);
				}
				//
				return cb.or(
						predicate,
						cb.isNull(path));
			}
			return cb.equal(path, value);
		} else if (comparsion == AutomaticRoleAttributeRuleComparison.NOT_EQUALS) {
			//
			// is necessary explicit type to as String
			path.as(String.class);
			//
			if (BooleanUtils.isTrue(negation)) {
				Predicate predicate = null;
				//
				if (value instanceof Boolean) {
					// for boolean must be specific equals (isTrue or isFalse, notEqual doesn't works)
					Expression<Boolean> booleanAs = path.as(Boolean.class);
					if (BooleanUtils.isTrue((Boolean) value)) {
						predicate = cb.notEqual(booleanAs, cb.literal(Boolean.FALSE));
					} else {
						predicate = cb.notEqual(booleanAs, cb.literal(Boolean.TRUE));
					}
				} else {
					// for other type just classic negative compare
					predicate = cb.equal(path, value);
				}
				return predicate;
			}
			return cb.or(
					cb.notEqual(path, value),
					cb.isNull(path)
					);
		} else if (comparsion == AutomaticRoleAttributeRuleComparison.START_WITH) {
			StringBuilder likeExpression = new StringBuilder();
			likeExpression.append(String.valueOf(value)); // Beware with null values
			likeExpression.append('%');
			if (BooleanUtils.isTrue(negation)) {
				return cb.or(
						cb.notLike(path.as(String.class), likeExpression.toString()),
						cb.isNull(path)
						);
			}
			return cb.like(path.as(String.class), likeExpression.toString());
		} else if (comparsion == AutomaticRoleAttributeRuleComparison.NOT_START_WITH) {
			StringBuilder likeExpression = new StringBuilder();
			likeExpression.append(String.valueOf(value)); // Beware with null values
			likeExpression.append('%');
			if (BooleanUtils.isTrue(negation)) {
				return cb.like(path.as(String.class), likeExpression.toString());
			}
			return cb.or(
					cb.notLike(path.as(String.class), likeExpression.toString()),
					cb.isNull(path)
					);
		} else if (comparsion == AutomaticRoleAttributeRuleComparison.END_WITH) {
			StringBuilder likeExpression = new StringBuilder();
			likeExpression.append('%');
			likeExpression.append(String.valueOf(value)); // Beware with null values
			if (BooleanUtils.isTrue(negation)) {
				return cb.or(
						cb.notLike(path.as(String.class), likeExpression.toString()),
						cb.isNull(path)
						);
			}
			return cb.like(path.as(String.class), likeExpression.toString());
		} else if (comparsion == AutomaticRoleAttributeRuleComparison.NOT_END_WITH) {
			StringBuilder likeExpression = new StringBuilder();
			likeExpression.append('%');
			likeExpression.append(String.valueOf(value)); // Beware with null values
			if (BooleanUtils.isTrue(negation)) {
				return cb.like(path.as(String.class), likeExpression.toString());
			}
			return cb.or(
					cb.notLike(path.as(String.class), likeExpression.toString()),
					cb.isNull(path)
					);
		} else if (comparsion == AutomaticRoleAttributeRuleComparison.IS_EMPTY) {
			// For EAV is required also expression not exists with attribute
			if (BooleanUtils.isTrue(negation)) {
				return cb.isNotNull(path);
			}

			return cb.or(
					cb.equal(path.as(String.class), ""),
					cb.isNull(path)
					);
		} else if (comparsion == AutomaticRoleAttributeRuleComparison.IS_NOT_EMPTY) {
			// For EAV is required also expression not exists with attribute
			if (BooleanUtils.isTrue(negation)) {
				return cb.or(
						cb.equal(path.as(String.class), ""),
						cb.isNull(path)
						);
			}

			return cb.and(
					cb.isNotNull(path),
					cb.not(cb.equal(path.as(String.class), ""))
					);
		} else if (comparsion == AutomaticRoleAttributeRuleComparison.CONTAINS) {
			StringBuilder likeExpression = new StringBuilder();
			likeExpression.append('%');
			likeExpression.append(String.valueOf(value)); // Beware with null values
			likeExpression.append('%');
			if (BooleanUtils.isTrue(negation)) {
				return cb.or(
						cb.notLike(path.as(String.class), likeExpression.toString()),
						cb.isNull(path)
						);
			}
			return cb.like(path.as(String.class), likeExpression.toString());
		}  else if (comparsion == AutomaticRoleAttributeRuleComparison.NOT_CONTAINS) {
			StringBuilder likeExpression = new StringBuilder();
			likeExpression.append('%');
			likeExpression.append(String.valueOf(value)); // Beware with null values
			likeExpression.append('%');
			if (BooleanUtils.isTrue(negation)) {
				return cb.like(path.as(String.class), likeExpression.toString());
			}
			return cb.or(
					cb.notLike(path.as(String.class), likeExpression.toString()),
					cb.isNull(path)
					);
		} else if (comparsion == AutomaticRoleAttributeRuleComparison.LESS_THAN_OR_EQUAL) {
			BigDecimal valueAsNumber = new BigDecimal(String.valueOf(value));
			// Beware there CAN'T be used path.as(Number.class). DB cast column again into numeric and then throw error
			// path is already Number type by method getSingularAttributeForEav(
			Path<Number> numberPath = (Path<Number>) path;
			if (BooleanUtils.isTrue(negation)) {
				return cb.or(
						cb.gt(numberPath, valueAsNumber),
						cb.isNull(path)
						);
			}

			return cb.le(numberPath, valueAsNumber);
		} else if (comparsion == AutomaticRoleAttributeRuleComparison.GREATER_THAN_OR_EQUAL) {
			BigDecimal valueAsNumber = new BigDecimal(String.valueOf(value));
			// Beware there CAN'T be used path.as(Number.class). DB cast column again into numeric and then throw error
			// path is already Number type by method getSingularAttributeForEav(
			Path<Number> numberPath = (Path<Number>) path;
			if (BooleanUtils.isTrue(negation)) {
				return cb.or(
						cb.lt(numberPath, valueAsNumber),
						cb.isNull(path)
						);
			}
			return cb.ge(numberPath, valueAsNumber);
		}

		throw new UnsupportedOperationException("Operation: " + comparsion.name() + ", isn't supported for identity rules.");
	}
	
	/**
	 * Return singular attribute for {@link PersistentType}
	 * 
	 * @param persistentType
	 * @return
	 */
	@SuppressWarnings("rawtypes")
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
		case ATTACHMENT:
		case UUID: {
			return AbstractFormValue_.uuidValue;
		}
		case CODELIST:
		case ENUMERATION:
		case SHORTTEXT: {
			return AbstractFormValue_.shortTextValue;
		}
		default:
			return AbstractFormValue_.stringValue;
		}
	}
	
	/**
	 * Method cast input value by {@link SingularAttribute},
	 * method check if attribute is primitive and then cast
	 * as primitive or just use cast from {@link SingularAttribute}
	 * 
	 * @param singularAttribute
	 * @param value
	 * @param comparison
	 * @return
	 */
	private Object castToType(SingularAttribute<?, ?> singularAttribute, String value, AutomaticRoleAttributeRuleComparison comparison) {
		// For comparison empty or is not empty return null value
		if (comparison == AutomaticRoleAttributeRuleComparison.IS_EMPTY || comparison == AutomaticRoleAttributeRuleComparison.IS_NOT_EMPTY) {
			return null;
		}
		Class<?> javaType = singularAttribute.getJavaType();
		LOG.debug("Value: [{}], will be cast to java type: [{}].", value, javaType.getName());
		if (javaType.isPrimitive()) {
			if (javaType == boolean.class) {
				return Boolean.valueOf(value);
			} else if (javaType == double.class) {
				return Double.valueOf(value);
			} else if (javaType == int.class) {
				return Integer.valueOf(value);
			} else if (javaType == float.class) {
				return Float.valueOf(value);
			} else if (javaType == byte.class) {
				return Byte.valueOf(value);
			} else if (javaType == short.class) {
				return Short.valueOf(value);
			} else if (javaType == long.class) {
				return Long.valueOf(value);
			} else if (javaType == char.class) {
				char ch = value.charAt(0);
				return ch;
			} else {
				throw new UnsupportedOperationException("Primitive type :" + javaType.getName() + ", can't be cast!");
			}
		}
		// identity and contract specific enumeration
		/* TODO: RT: works, but why? Only usable state is future contract or created maybe? Other options except valid one cannot have roles.
		 * if (javaType.equals(ContractState.class)) {
		 *   return IdentityState.valueOf(value);
		 * }
		*/
		if (javaType.equals(ContractState.class)) {
			return ContractState.valueOf(value);
		}
		//
		return javaType.cast(value);
	}
	
	/**
	 * Cast value in string to given persistent type
	 *
	 * @param value
	 * @param persistentType
	 * @param comparison
	 * @return
	 */
	private Object getFormValue(String value, PersistentType persistentType, AutomaticRoleAttributeRuleComparison comparison) {
		// For comparison is empty and is not empty return null values
		if (comparison == AutomaticRoleAttributeRuleComparison.IS_EMPTY || comparison == AutomaticRoleAttributeRuleComparison.IS_NOT_EMPTY) {
			return null;
		}
		Assert.notNull(value, "Value is required.");
		switch (persistentType) {
		case INT:
		case LONG:
			return Long.valueOf(value);
		case BOOLEAN:
			return Boolean.valueOf(value);
		case DATE:
		case DATETIME:
			return ZonedDateTime.parse(value);
		case DOUBLE:
			return new BigDecimal(value);
		case BYTEARRAY: {
			return value.getBytes(StandardCharsets.UTF_8);
		}
		case ATTACHMENT:
		case UUID: {
			return UUID.fromString(value);
		}
		case SHORTTEXT: {
			return value;
		}
		default:
			return value;
		}
	}

	/**
	 * Create identity roles by request and concepts.
	 *
	 * @param contract
	 * @param contractPosition
	 * @param automaticRoles
	 */
	private void createIdentityRoles(IdmIdentityContractDto contract, IdmContractPositionDto contractPosition, Set<AbstractIdmAutomaticRoleDto> automaticRoles) {		
		List<IdmConceptRoleRequestDto> concepts = new ArrayList<>(automaticRoles.size());

		for (AbstractIdmAutomaticRoleDto autoRole : automaticRoles) {
			IdmConceptRoleRequestDto concept = new IdmConceptRoleRequestDto();
			concept.setIdentityContract(contract.getId());
			concept.setValidFrom(contract.getValidFrom());
			concept.setValidTill(contract.getValidTill());
			concept.setRole(autoRole.getRole());
			concept.setAutomaticRole(autoRole.getId());
			concept.setOperation(ConceptRoleRequestOperation.ADD);
			concepts.add(concept);
		}

		roleRequestService.executeConceptsImmediate(contract.getIdentity(), concepts);
	}
	
}