package eu.bcvsolutions.idm.core.model.service.impl;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.domain.AutomaticRoleAttributeRuleComparison;
import eu.bcvsolutions.idm.core.api.domain.AutomaticRoleAttributeRuleType;
import eu.bcvsolutions.idm.core.api.domain.ConceptRoleRequestOperation;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestedByType;
import eu.bcvsolutions.idm.core.api.dto.AbstractIdmAutomaticRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmAutomaticRoleAttributeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmAutomaticRoleAttributeRuleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmAutomaticRoleAttributeRuleFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmAutomaticRoleFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityContractFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity_;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.IdmAutomaticRoleAttributeRuleService;
import eu.bcvsolutions.idm.core.api.service.IdmAutomaticRoleAttributeService;
import eu.bcvsolutions.idm.core.api.service.IdmConceptRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.eav.api.service.IdmFormAttributeService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmAutomaticRoleAttribute;
import eu.bcvsolutions.idm.core.model.entity.IdmAutomaticRoleAttribute_;
import eu.bcvsolutions.idm.core.model.entity.IdmRole_;
import eu.bcvsolutions.idm.core.model.repository.IdmAutomaticRoleAttributeRepository;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;

/**
 * Automatic role by attribute
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
	private final IdmIdentityService identityService;
	private final FormService formService;
	private final IdmFormAttributeService formAttributeService;
	private final IdmAutomaticRoleAttributeRuleService automaticRoleAttributeRuleService;
	private final IdmIdentityRoleService identityRoleService;	
	
	@Autowired
	public DefaultIdmAutomaticRoleAttributeService(
			IdmAutomaticRoleAttributeRepository repository,
			IdmIdentityService identityService,
			IdmRoleRequestService roleRequestService,
			IdmIdentityContractService identityContractService,
			IdmConceptRoleRequestService conceptRoleRequestService,
			EntityEventManager entityEventManager,
			FormService formService,
			IdmFormAttributeService formAttributeService,
			IdmAutomaticRoleAttributeRuleService automaticRoleAttributeRuleService,
			IdmIdentityRoleService identityRoleService) {
		super(repository);
		//
		Assert.notNull(identityService);
		Assert.notNull(roleRequestService);
		Assert.notNull(identityContractService);
		Assert.notNull(conceptRoleRequestService);
		Assert.notNull(formService);
		Assert.notNull(formAttributeService);
		Assert.notNull(automaticRoleAttributeRuleService);
		Assert.notNull(identityRoleService);
		//
		this.formService = formService;
		this.identityService = identityService;
		this.roleRequestService = roleRequestService;
		this.identityContractService = identityContractService;
		this.conceptRoleRequestService = conceptRoleRequestService;
		this.formAttributeService = formAttributeService;
		this.automaticRoleAttributeRuleService = automaticRoleAttributeRuleService;
		this.identityRoleService = identityRoleService;
	}

	@Override
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(CoreGroupPermission.AUTOMATICROLEATTRIBUTE, getEntityClass());
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
	public void resolveAutomaticRolesByAttribute(UUID identityId) {
		Assert.notNull(identityId);
		// two sets for passed and not passed automatic roles by attributes
		Set<AbstractIdmAutomaticRoleDto> passedAutomaticRoles = new HashSet<>();
		Set<AbstractIdmAutomaticRoleDto> notPassedAutomaticRoles = new HashSet<>();
		//
		// algorithm will be iterate over all automatic roles by attribute in IdM
		List<IdmAutomaticRoleAttributeDto> automaticRoles = this.find(null).getContent();
		//
		for (IdmAutomaticRoleAttributeDto automaticRole : automaticRoles) {
			if (resolveAutomaticRoles(automaticRole, identityId)) {
				passedAutomaticRoles.add(automaticRole);
			} else {
				notPassedAutomaticRoles.add(automaticRole);
			}
		}
		//
		// find all automatic roles for identity
		IdmIdentityRoleFilter roleIdentityFilter = new IdmIdentityRoleFilter();
		roleIdentityFilter.setIdentityId(identityId);
		roleIdentityFilter.setAutomaticRole(Boolean.TRUE);
		List<IdmIdentityRoleDto> allAutomaticRolesByIdentity = identityRoleService.find(roleIdentityFilter, null).getContent();
		//
		// check duplicates in passed
		List<UUID> allAutomaticRolesByIdentityIds = allAutomaticRolesByIdentity.stream().map(IdmIdentityRoleDto::getRoleTreeNode).collect(Collectors.toList());
		passedAutomaticRoles = passedAutomaticRoles.stream().filter(passedAutoRole -> {
			return !allAutomaticRolesByIdentityIds.contains(passedAutoRole.getId());
		}).collect(Collectors.toSet());
		//
		// remove not passed automatic roles that pass another rule
		List<UUID> newRolesId = passedAutomaticRoles.stream().map(AbstractIdmAutomaticRoleDto::getRole).collect(Collectors.toList());
		notPassedAutomaticRoles = notPassedAutomaticRoles.stream().filter(notPassedAutoRole -> !newRolesId.contains(notPassedAutoRole.getRole())).collect(Collectors.toSet());
		//
		// Assign new passed automatic roles
		IdmIdentityContractDto primeContract = identityContractService.getPrimeContract(identityId);
		if (!passedAutomaticRoles.isEmpty()) {
			IdmRoleRequestDto roleRequest = processAutomaticRoles(primeContract, null, passedAutomaticRoles, ConceptRoleRequestOperation.ADD);
			roleRequestService.startRequestInternal(roleRequest.getId(), false);
		}
		//
		// remove not passed automatic roles that isn't in identity roles
		notPassedAutomaticRoles = notPassedAutomaticRoles.stream().filter(notPassedAutoRole -> {
			return allAutomaticRolesByIdentityIds.contains(notPassedAutoRole.getId());
		}).collect(Collectors.toSet());
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
	
	public boolean resolveAutomaticRoles(UUID attributeId, UUID identityId) {
		Assert.notNull(attributeId);
		//
		IdmAutomaticRoleAttributeDto attribute = this.get(attributeId);
		return this.resolveAutomaticRoles(attribute, identityId);
	}
	
	public boolean resolveAutomaticRoles(IdmAutomaticRoleAttributeDto attribute, UUID identityId) {
		Assert.notNull(attribute);
		Assert.notNull(identityId);
		// find all rules for attribute
		IdmAutomaticRoleAttributeRuleFilter ruleFilter = new IdmAutomaticRoleAttributeRuleFilter();
		ruleFilter.setAutomaticRoleAttributeId(attribute.getId());
		List<IdmAutomaticRoleAttributeRuleDto> rules = automaticRoleAttributeRuleService.find(ruleFilter , null).getContent();
		//
		// if rules are empty return false -> not passed
		if (rules.isEmpty()) {
			LOG.debug("Rules for automatic role by attribute [{}], are empty.", attribute.getId());
			return false;
		}
		//
		// resolve all rules, all rules must pass, TODO: add or and next comparison between rules
		for (IdmAutomaticRoleAttributeRuleDto rule : rules) {
			boolean passRule = resolveRule(rule, identityId);
			if (!passRule) {
				LOG.debug("Rule [{}] not passed.", rule.getId());
				return false;
			}
		}
		//
		return true;
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
	
	/**
	 * Resolve rule
	 * 
	 * @param rule
	 * @param attributeId
	 * @param identityId
	 * @return
	 */
	private boolean resolveRule(IdmAutomaticRoleAttributeRuleDto rule, UUID identityId) {
		// resolve rule by type
		if (rule.getType() == AutomaticRoleAttributeRuleType.CONTRACT) {
			if (!containsContractValue(identityId, rule.getValue(), rule.getAttributeName())) {
				return false;
			}
		} else if (rule.getType() == AutomaticRoleAttributeRuleType.CONTRACT_EAV) {
			if (!resolveContractEavValue(identityId, rule)) {
				return false;
			}
		} else if (rule.getType() == AutomaticRoleAttributeRuleType.IDENITITY_EAV) {
			if (!resolveIdentityEavValue(identityId, rule)) {
				return false;
			}
		} else if (rule.getType() == AutomaticRoleAttributeRuleType.IDENTITY) {
			if (!cointainsIdentityValue(identityId, rule.getValue(), rule.getAttributeName())) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Method try find first value defined in rule in identity eav attributes.
	 * 
	 * @param identityId
	 * @param rule
	 * @return
	 */
	private boolean resolveIdentityEavValue(UUID identityId, IdmAutomaticRoleAttributeRuleDto rule) {
		IdmFormAttributeDto idmFormAttributeDto = formAttributeService.get(rule.getFormAttribute());
		List<IdmFormValueDto> values = formService.getValues(identityId, IdmIdentityDto.class, idmFormAttributeDto);
		if (findEavAttribute(values, rule) != null) {
			return true;
		}
		return false;
	}
	
	/**
	 * Method try find first value defined in rule in contract eav attributes.
	 * 
	 * @param identityId
	 * @param rule
	 * @return
	 */
	private boolean resolveContractEavValue(UUID identityId, IdmAutomaticRoleAttributeRuleDto rule) {
		IdmFormAttributeDto idmFormAttributeDto = formAttributeService.get(rule.getFormAttribute());
		List<IdmIdentityContractDto> contracts = identityContractService.findAllValidForDate(identityId, new LocalDate(), null);
		for (IdmIdentityContractDto contract : contracts) {
			List<IdmFormValueDto> values = formService.getValues(contract.getId(), IdmIdentityDto.class, idmFormAttributeDto);
			if (findEavAttribute(values, rule) != null) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 
	 * @param identityId
	 * @param value
	 * @param property
	 * @return
	 */
	private boolean containsContractValue(UUID identityId, String value, String property) {
		IdmIdentityContractFilter filter = new IdmIdentityContractFilter();
		filter.setProperty(value);
		filter.setValue(property.toLowerCase());
		filter.setIdentity(identityId);
		filter.setValidNowOrInFuture(true);
		List<IdmIdentityContractDto> contracts = identityContractService.find(filter, null).getContent();
		if (contracts.isEmpty()) {
			return false;
		}
		return true;
	}
	
	/**
	 * Method try find {@link IdmIdentityDto} with given id, value and property. 
	 * TODO: only supported comparison is now equals.
	 * 
	 * @param identityId
	 * @param value
	 * @param property
	 * @return
	 */
	private boolean cointainsIdentityValue(UUID identityId, String value, String property) {
		IdmIdentityFilter filter = new IdmIdentityFilter();
		filter.setId(identityId);
		filter.setValue(value);
		filter.setProperty(property.toLowerCase());
		List<IdmIdentityDto> identity = identityService.find(filter, null).getContent();
		if (identity.isEmpty()) {
			return false;
		}
		return true;
	}
	
	/**
	 * Method compare value1 with value2 by comparsion
	 * 
	 * @param value1
	 * @param value2
	 * @param comparsion
	 * @return
	 */
	private boolean compareValue(Object value1, Object value2, AutomaticRoleAttributeRuleComparison comparsion) {
		if  (comparsion == AutomaticRoleAttributeRuleComparison.EQUALS) {
			// TODO: probably will be necessary create better comparator
			return ObjectUtils.equals(value1, value2);
		}
		throw new UnsupportedOperationException("Unsupported comparsion");
	}
	
	/**
	 * Method find {@link IdmFormValueDto} by value. Find first same value.
	 * 
	 * @param attributes
	 * @param value
	 * @return
	 */
	private IdmFormValueDto findEavAttribute(List<IdmFormValueDto> attributes, IdmAutomaticRoleAttributeRuleDto rule) {
		for (IdmFormValueDto attribute : attributes) {
			Serializable value = attribute.getValue();
			if (compareValue(value == null ? value : value.toString(), rule.getValue(), rule.getComparison())) {
				return attribute;
			}
		}
		return null;
	}
	
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
}