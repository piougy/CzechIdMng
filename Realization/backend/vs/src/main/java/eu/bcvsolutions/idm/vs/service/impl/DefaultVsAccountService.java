package eu.bcvsolutions.idm.vs.service.impl;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.eav.api.service.IdmFormAttributeService;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;
import eu.bcvsolutions.idm.ic.api.IcAttribute;
import eu.bcvsolutions.idm.ic.api.IcAttributeInfo;
import eu.bcvsolutions.idm.ic.exception.IcException;
import eu.bcvsolutions.idm.ic.impl.IcAttributeImpl;
import eu.bcvsolutions.idm.vs.domain.VirtualSystemGroupPermission;
import eu.bcvsolutions.idm.vs.entity.VsAccount;
import eu.bcvsolutions.idm.vs.entity.VsAccount_;
import eu.bcvsolutions.idm.vs.repository.VsAccountRepository;
import eu.bcvsolutions.idm.vs.repository.filter.VsAccountFilter;
import eu.bcvsolutions.idm.vs.service.api.VsAccountService;
import eu.bcvsolutions.idm.vs.service.api.dto.VsAccountDto;
import net.bytebuddy.dynamic.loading.PackageDefinitionStrategy.Definition;

/**
 * Service for account in virtual system
 * 
 * @author Svanda
 *
 */
@Service
public class DefaultVsAccountService extends AbstractReadWriteDtoService<VsAccountDto, VsAccount, VsAccountFilter>
		implements VsAccountService {

	private static final Logger LOG = LoggerFactory.getLogger(DefaultVsAccountService.class);

	private final FormService formService;
	private final IdmFormAttributeService formAttributeService;

	@Autowired
	public DefaultVsAccountService(VsAccountRepository repository, EntityEventManager entityEventManager,
			FormService formService, IdmFormAttributeService formAttributeService) {
		super(repository);
		//
		Assert.notNull(formService);
		Assert.notNull(entityEventManager);
		Assert.notNull(formAttributeService);
		//
		this.formService = formService;
		this.formAttributeService = formAttributeService;
	}

	@Override
	public void deleteInternal(VsAccountDto dto) {
		// TODO: eav dto
		formService.deleteValues(getRepository().findOne(dto.getId()));
		//
		super.deleteInternal(dto);
	}

	@Override
	protected List<Predicate> toPredicates(Root<VsAccount> root, CriteriaQuery<?> query, CriteriaBuilder builder,
			VsAccountFilter filter) {
		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);
		//
		// quick - "fulltext"
		if (StringUtils.isNotEmpty(filter.getText())) {
			predicates.add(builder.or(builder.equal(builder.lower(root.get(VsAccount_.uid)),
					"%" + filter.getText().toLowerCase() + "%")));
		}

		// UID
		if (StringUtils.isNotEmpty(filter.getUid())) {
			predicates.add(builder.equal(root.get(VsAccount_.uid), filter.getUid()));
		}

		// System ID
		if (filter.getSystemId() != null) {
			predicates.add(builder.equal(root.get(VsAccount_.systemId), filter.getSystemId()));
		}
		return predicates;
	}

	@Override
	public VsAccountDto findByUidSystem(String uidValue, UUID systemId) {
		Assert.notNull(uidValue, "Uid value cannot be null!");
		Assert.notNull(systemId, "Id of CzechIdM system cannot be null!");

		VsAccountFilter filter = new VsAccountFilter();
		filter.setUid(uidValue);
		filter.setSystemId(systemId);
		List<VsAccountDto> accounts = this.find(filter, null).getContent();
		if (accounts.size() > 1) {
			throw new IcException(
					MessageFormat.format("To many vs accounts for uid [{0}] and system [{1}]!", uidValue, systemId));
		}

		return accounts.isEmpty() ? null : accounts.get(0);
	}

	/**
	 * Load data from extended attribute and create IcAttribute
	 * 
	 * @param accountId
	 * @param name
	 * @return
	 */
	@Override
	public IcAttribute loadIcAttribute(UUID accountId, String name, IdmFormDefinitionDto formDefinition) {
		IdmFormAttributeDto attributeDefinition = this.formAttributeService.findAttribute(formDefinition.getType(),
				formDefinition.getCode(), name);
		List<IdmFormValueDto> values = this.formService.getValues(accountId, VsAccount.class, formDefinition, name);
		if (CollectionUtils.isEmpty(values)) {
			return null;
		}

		List<Object> valuesObject = values.stream().map(IdmFormValueDto::getValue).collect(Collectors.toList());

		IcAttributeImpl attribute = new IcAttributeImpl();
		attribute.setMultiValue(attributeDefinition.isMultiple());
		attribute.setName(name);
		attribute.setValues(valuesObject);
		return attribute;
	}
	
	
	@Override
	public List<IcAttribute> loadIcAttributes(VsAccountDto account) {
		Assert.notNull(account);

		List<IcAttribute> attributes = new ArrayList<>();
		// Create uid attribute
		IcAttributeImpl uidAttribute = new IcAttributeImpl(IcAttributeInfo.NAME, account.getUid());
		attributes.add(uidAttribute);
		String connectorKey = account.getConnectorKey();
		String virtualSystemKey = MessageFormat.format("{0}:systemId={1}", connectorKey,
				account.getSystemId().toString());
		String type = VsAccount.class.getName();
		IdmFormDefinitionDto definition = this.formService.getDefinition(type, virtualSystemKey);
		if (definition == null) {
			return attributes;
		}
		definition.getFormAttributes().forEach(formAttribute -> {
			attributes.add(this.loadIcAttribute(account.getId(), formAttribute.getName(), definition));
		});
		return attributes;
	}

	@Override
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(VirtualSystemGroupPermission.VSACCOUNT, getEntityClass());
	}

}
