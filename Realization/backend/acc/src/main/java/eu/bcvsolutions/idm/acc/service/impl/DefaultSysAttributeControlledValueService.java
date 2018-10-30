package eu.bcvsolutions.idm.acc.service.impl;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.dto.SysAttributeControlledValueDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemAttributeMappingDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysAttributeControlledValueFilter;
import eu.bcvsolutions.idm.acc.entity.SysAttributeControlledValue;
import eu.bcvsolutions.idm.acc.repository.SysAttributeControlledValueRepository;
import eu.bcvsolutions.idm.acc.service.api.SysAttributeControlledValueService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.core.api.service.AbstractEventableDtoService;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;

/**
 * Service - Controlled value for attribute DTO. Is using in the provisioning
 * merge.
 * 
 * @author Vít Švanda
 *
 */
@Service("sysAttributeControlledValueService")
public class DefaultSysAttributeControlledValueService extends
		AbstractEventableDtoService<SysAttributeControlledValueDto, SysAttributeControlledValue, SysAttributeControlledValueFilter>
		implements SysAttributeControlledValueService {

	@Autowired
	private SysSystemAttributeMappingService systemAttributeMappingService;
	
	@Autowired
	public DefaultSysAttributeControlledValueService(SysAttributeControlledValueRepository repository,
			EntityEventManager entityEventManager) {
		super(repository, entityEventManager);
	}

	@Override
	public void setControlledValues(SysSystemAttributeMappingDto attributeMapping,
			List<Serializable> controlledAttributeValues) {
		Assert.notNull(attributeMapping);
		Assert.notNull(controlledAttributeValues);
		Assert.isTrue(attributeMapping.isEvictControlledValuesCache(), "Attribute must be setts as evict cache!");

		SysAttributeControlledValueFilter attributeControlledValueFilter = new SysAttributeControlledValueFilter();
		attributeControlledValueFilter.setAttributeMappingId(attributeMapping.getId());
		attributeControlledValueFilter.setHistoricValue(Boolean.FALSE);

		// Search controlled values for that attribute
		List<SysAttributeControlledValueDto> controlledValues = this.find(attributeControlledValueFilter, null) //
				.getContent();

		// Search values for which does not exists same controlled value (for given attribute)
		List<Serializable> valuesToAdd = controlledAttributeValues.stream()
				.filter(newValue -> { //
					return !controlledValues.stream() //
							.filter(controlledValue -> controlledValue.getValue().equals(newValue)) //
							.findFirst() //
							.isPresent(); //
				}) //
				.collect(Collectors.toList());
		
		// Search old controlled values which does not exists in new definition
		List<SysAttributeControlledValueDto> controlledValuesToDelete = controlledValues.stream()
				.filter(controlledValue -> { //
					return !controlledAttributeValues.stream() //
							.filter(newValue -> controlledValue.getValue().equals(newValue)) //
							.findFirst() //
							.isPresent(); //
				}) //
				.collect(Collectors.toList());
		
		// Delete old values
		controlledValuesToDelete.forEach(controlledValue -> this.delete(controlledValue));
		
		// Persists new controlled values
		valuesToAdd.forEach(valueToAdd -> {
			SysAttributeControlledValueDto controlledValue = new SysAttributeControlledValueDto();
			controlledValue.setAttributeMapping(attributeMapping.getId());
			controlledValue.setHistoricValue(false);
			controlledValue.setValue(valueToAdd);
			this.save(controlledValue);
		});
		
		// Controlled values are synchronized now, so we can set evict to false
		attributeMapping.setEvictControlledValuesCache(false);
		systemAttributeMappingService.save(attributeMapping);
		

	}

//	@Override
//	protected List<Predicate> toPredicates(Root<SysAttributeControlledValue> root, CriteriaQuery<?> query, CriteriaBuilder builder,
//			SysAttributeControlledValueFilter filter) {
//		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);
//		// full search
//		if (StringUtils.isNotEmpty(filter.getText())) {
//			predicates.add(builder.or(//
//					builder.like(builder.lower(root.get(SysAttributeControlledValue_.uid)), "%" + filter.getText().toLowerCase() + "%"),
//					builder.like(builder.lower(root.get(SysAttributeControlledValue_.systemEntity).get(SysSystemEntity_.uid)),
//							"%" + filter.getText().toLowerCase() + "%")));
//		}
//		if (filter.getSystemId() != null) {
//			predicates.add(builder.equal(root.get(SysAttributeControlledValue_.system).get(SysSystem_.id), filter.getSystemId()));
//		}
//		if (filter.getSystemEntityId() != null) {
//			predicates.add(builder.equal(root.get(SysAttributeControlledValue_.systemEntity).get(SysSystemEntity_.id),
//					filter.getSystemEntityId()));
//		}
//		if (filter.getUid() != null) {
//			predicates.add(builder.equal(root.get(SysAttributeControlledValue_.uid), filter.getUid()));
//		}
//		if (filter.getIdentityId() != null || filter.getOwnership() != null) {
//			Subquery<AccIdentityAccount> identityAccountSubquery = query.subquery(AccIdentityAccount.class);
//			Root<AccIdentityAccount> subRootIdentityAccount = identityAccountSubquery.from(AccIdentityAccount.class);
//			identityAccountSubquery.select(subRootIdentityAccount);
//
//			Predicate predicate = builder
//					.and(builder.equal(subRootIdentityAccount.get(AccIdentityAccount_.account), root));
//			Predicate identityPredicate = builder.equal(
//					subRootIdentityAccount.get(AccIdentityAccount_.identity).get(IdmIdentity_.id),
//					filter.getIdentityId());
//			Predicate ownerPredicate = builder.equal(subRootIdentityAccount.get(AccIdentityAccount_.ownership),
//					filter.getOwnership());
//
//			if (filter.getIdentityId() != null && filter.getOwnership() == null) {
//				predicate = builder.and(predicate, identityPredicate);
//			} else if (filter.getOwnership() != null && filter.getIdentityId() == null) {
//				predicate = builder.and(predicate, ownerPredicate);
//			} else {
//				predicate = builder.and(predicate, identityPredicate, ownerPredicate);
//			}
//
//			identityAccountSubquery.where(predicate);
//			predicates.add(builder.exists(identityAccountSubquery));
//		}
//		if (filter.getAccountType() != null) {
//			predicates.add(builder.equal(root.get(SysAttributeControlledValue_.accountType), filter.getAccountType()));
//		}
//
//		if (filter.getSupportChangePassword() != null && filter.getSupportChangePassword()) {
//			Subquery<SysSystemAttributeMapping> systemAttributeMappingSubquery = query
//					.subquery(SysSystemAttributeMapping.class);
//			Root<SysSystemAttributeMapping> subRootSystemAttributeMapping = systemAttributeMappingSubquery
//					.from(SysSystemAttributeMapping.class);
//			systemAttributeMappingSubquery.select(subRootSystemAttributeMapping);
//
//			Predicate predicate = builder.and(builder.equal(subRootSystemAttributeMapping//
//					.get(SysSystemAttributeMapping_.schemaAttribute)//
//					.get(SysSchemaAttribute_.objectClass)//
//					.get(SysSchemaObjectClass_.system), //
//					root.get(SysAttributeControlledValue_.system)),
//					builder.equal(subRootSystemAttributeMapping//
//							.get(SysSystemAttributeMapping_.systemMapping)//
//							.get(SysSystemMapping_.operationType), SystemOperationType.PROVISIONING),
//					builder.equal(subRootSystemAttributeMapping//
//							.get(SysSystemAttributeMapping_.schemaAttribute)//
//							.get(SysSchemaAttribute_.name), ProvisioningService.PASSWORD_SCHEMA_PROPERTY_NAME));
//
//			systemAttributeMappingSubquery.where(predicate);
//			predicates.add(builder.exists(systemAttributeMappingSubquery));
//		}
//
//		if (filter.getEntityType() != null) {
//			predicates.add(builder.equal(root.get(SysAttributeControlledValue_.entityType), filter.getEntityType()));
//		}
//
//		//
//		return predicates;
//
//	}
}
