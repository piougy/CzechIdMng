package eu.bcvsolutions.idm.acc.service.impl;

import java.text.MessageFormat;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.plugin.core.OrderAwarePluginRegistry;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.acc.domain.AccGroupPermission;
import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.domain.SystemOperationType;
import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.AccIdentityAccountDto;
import eu.bcvsolutions.idm.acc.dto.SysSchemaAttributeDto;
import eu.bcvsolutions.idm.acc.dto.SysSchemaObjectClassDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemEntityDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccAccountFilter;
import eu.bcvsolutions.idm.acc.dto.filter.AccIdentityAccountFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSchemaAttributeFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSchemaObjectClassFilter;
import eu.bcvsolutions.idm.acc.entity.AccAccount;
import eu.bcvsolutions.idm.acc.entity.AccAccount_;
import eu.bcvsolutions.idm.acc.entity.AccIdentityAccount;
import eu.bcvsolutions.idm.acc.entity.AccIdentityAccount_;
import eu.bcvsolutions.idm.acc.entity.SysSchemaAttribute_;
import eu.bcvsolutions.idm.acc.entity.SysSchemaObjectClass_;
import eu.bcvsolutions.idm.acc.entity.SysSystemAttributeMapping;
import eu.bcvsolutions.idm.acc.entity.SysSystemAttributeMapping_;
import eu.bcvsolutions.idm.acc.entity.SysSystemEntity_;
import eu.bcvsolutions.idm.acc.entity.SysSystemMapping_;
import eu.bcvsolutions.idm.acc.entity.SysSystem_;
import eu.bcvsolutions.idm.acc.event.AccountEvent;
import eu.bcvsolutions.idm.acc.event.AccountEvent.AccountEventType;
import eu.bcvsolutions.idm.acc.repository.AccAccountRepository;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.AccIdentityAccountService;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningService;
import eu.bcvsolutions.idm.acc.service.api.SynchronizationEntityExecutor;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaObjectClassService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.AbstractEventableDtoService;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity_;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;
import eu.bcvsolutions.idm.ic.api.IcAttribute;
import eu.bcvsolutions.idm.ic.api.IcConnectorObject;
import eu.bcvsolutions.idm.ic.api.IcObjectClassInfo;
import eu.bcvsolutions.idm.ic.impl.IcConnectorObjectImpl;

/**
 * Accounts on target system
 * 
 * @author Radek Tomiška
 * @author svandav
 *
 */
@Service("accAccountService")
public class DefaultAccAccountService extends AbstractEventableDtoService<AccAccountDto, AccAccount, AccAccountFilter>
		implements AccAccountService {

	private final AccAccountRepository accountRepository;
	private final AccIdentityAccountService identityAccountService;
	private final SysSystemService systemService;
	private final SysSchemaObjectClassService schemaObjectClassService;
	private final SysSchemaAttributeService schemaAttributeService;
	@Autowired
	private List<SynchronizationEntityExecutor> executors;
	private PluginRegistry<SynchronizationEntityExecutor, SystemEntityType> pluginExecutors;

	@Autowired
	public DefaultAccAccountService(AccAccountRepository accountRepository,
			AccIdentityAccountService identityAccountService, SysSystemService systemService,
			SysSchemaObjectClassService schemaObjectClassService, SysSchemaAttributeService schemaAttributeService,
			EntityEventManager entityEventManager) {
		super(accountRepository, entityEventManager);
		//
		Assert.notNull(identityAccountService);
		Assert.notNull(accountRepository);
		Assert.notNull(systemService);
		Assert.notNull(schemaObjectClassService);
		Assert.notNull(schemaAttributeService);

		//
		this.identityAccountService = identityAccountService;
		this.accountRepository = accountRepository;
		this.systemService = systemService;
		this.schemaAttributeService = schemaAttributeService;
		this.schemaObjectClassService = schemaObjectClassService;
	}

	@Override
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(AccGroupPermission.ACCOUNT, getEntityClass());
	}

	@Override
	protected AccAccountDto toDto(AccAccount entity, AccAccountDto dto) {
		AccAccountDto newDto = super.toDto(entity, dto);
		//
		// if dto exists add real uid
		if (newDto != null) {
			if (newDto.getSystemEntity() != null) {
				SysSystemEntityDto systemEntity = DtoUtils.getEmbedded(newDto, AccAccount_.systemEntity);
				newDto.setRealUid(systemEntity.getUid());
			} else {
				// If system entity do not exist, then return uid from account.
				newDto.setRealUid(newDto.getUid());
			}
			// Load and set target entity. For loading a target entity is using sync
			// executor.
			SystemEntityType entityType = newDto.getEntityType();
			if (entityType != null && entityType.isSupportsSync()) {
				SynchronizationEntityExecutor executor = this.getSyncExecutor(entityType);
				UUID targetEntity = executor.getEntityByAccount(newDto.getId());
				newDto.setTargetEntityType(entityType.getEntityType().getName());
				newDto.setTargetEntityId(targetEntity);
			}
		}
		return newDto;
	}

	@Override
	@Transactional
	public void delete(AccAccountDto account, BasePermission... permission) {
		Assert.notNull(account);
		// delete all identity accounts (call event)
		AccIdentityAccountFilter identityAccountFilter = new AccIdentityAccountFilter();
		identityAccountFilter.setAccountId(account.getId());
		List<AccIdentityAccountDto> identityAccounts = identityAccountService.find(identityAccountFilter, null)
				.getContent();
		identityAccounts.forEach(identityAccount -> {
			identityAccountService.delete(identityAccount);
		});

		AccAccountDto potentialProtectedAccount = get(account);

		// Account was already deleted during relations identity-accounts deletion
		if (potentialProtectedAccount == null) {
			return;
		}
		// If was account marked as protected (after we try delete all
		// identity-accounts), then we don't want delete account. We have to prevent
		// throw "account is protected" exception, because we need do commit (not
		// rollback).
		if (!account.isAccountProtectedAndValid() && potentialProtectedAccount.isAccountProtectedAndValid()) {
			return;
		}

		this.publish(new AccountEvent(AccountEventType.DELETE, account,
				ImmutableMap.of(AccAccountService.DELETE_TARGET_ACCOUNT_PROPERTY, Boolean.TRUE)));
	}

	@Override
	public List<AccAccountDto> getAccounts(UUID systemId, UUID identityId) {
		return toDtos(accountRepository.findAccountBySystemAndIdentity(identityId, systemId), true);
	}

	@Override
	public AccAccountDto getAccount(String uid, UUID systemId) {
		Assert.notNull(uid, "UID cannot be null!");

		AccAccountFilter filter = new AccAccountFilter();
		filter.setUid(uid);
		filter.setSystemId(systemId);

		List<AccAccountDto> accounts = this.find(filter, null).getContent();
		if (accounts.isEmpty()) {
			return null;
		}
		return accounts.get(0);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<AccAccountDto> findExpired(DateTime expirationDate, Pageable pageable) {
		Assert.notNull(expirationDate);
		//
		return toDtoPage(
				accountRepository.findByEndOfProtectionLessThanAndInProtectionIsTrue(expirationDate, pageable));
	}

	@Override
	public IcConnectorObject getConnectorObject(AccAccountDto account, BasePermission... permissions) {
		Assert.notNull(account, "Account cannot be null!");
		this.checkAccess(account, permissions);
		List<SysSchemaAttributeDto> schemaAttributes = this.getSchemaAttributes(account.getSystem(), null);
		if (schemaAttributes == null) {
			return null;
		}
		try {
			IcConnectorObject fullObject = this.systemService.readConnectorObject(account.getSystem(),
					account.getRealUid(), null);
			return this.getConnectorObjectForSchema(fullObject, schemaAttributes);
		} catch (Exception ex) {
			SysSystemDto system = DtoUtils.getEmbedded(account, AccAccount_.system, SysSystemDto.class);
			throw new ResultCodeException(AccResultCode.ACCOUNT_CANNOT_BE_READ_FROM_TARGET, ImmutableMap.of("account",
					account.getUid(), "system", system != null ? system.getName() : account.getSystem()), ex);
		}
	}

	/**
	 * Return only attributes for witch we have schema attribute definitions.
	 * 
	 * @param fullObject
	 * @param schemaAttributes
	 * @return
	 */
	private IcConnectorObject getConnectorObjectForSchema(IcConnectorObject fullObject,
			List<SysSchemaAttributeDto> schemaAttributes) {
		if (fullObject == null || schemaAttributes == null) {
			return null;
		}

		List<IcAttribute> allAttributes = fullObject.getAttributes();
		List<IcAttribute> resultAttributes = allAttributes.stream().filter(attribute -> {
			return schemaAttributes.stream()
					.filter(schemaAttribute -> attribute.getName().equals(schemaAttribute.getName())).findFirst()
					.isPresent();
		}).collect(Collectors.toList());
		return new IcConnectorObjectImpl(fullObject.getUidValue(), fullObject.getObjectClass(), resultAttributes);
	}

	/**
	 * Find schema's attributes for the system id and schema name.
	 * 
	 * @param systemId
	 * @param schema
	 *            - If is schema name null, then will used default '__ACCOUNT__'.
	 * @return
	 */
	private List<SysSchemaAttributeDto> getSchemaAttributes(UUID systemId, String schema) {
		SysSchemaObjectClassFilter schemaFilter = new SysSchemaObjectClassFilter();
		schemaFilter.setSystemId(systemId);
		schemaFilter.setObjectClassName(schema != null ? schema : IcObjectClassInfo.ACCOUNT);

		List<SysSchemaObjectClassDto> schemas = schemaObjectClassService.find(schemaFilter, null).getContent();
		if (schemas.size() != 1) {
			return null;
		}
		SysSchemaAttributeFilter schemaAttributeFilter = new SysSchemaAttributeFilter();
		schemaAttributeFilter.setObjectClassId(schemas.get(0).getId());
		schemaAttributeFilter.setSystemId(systemId);
		return schemaAttributeService.find(schemaAttributeFilter, null).getContent();
	}

	@Override
	protected List<Predicate> toPredicates(Root<AccAccount> root, CriteriaQuery<?> query, CriteriaBuilder builder,
			AccAccountFilter filter) {
		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);
		// full search
		if (StringUtils.isNotEmpty(filter.getText())) {
			predicates.add(builder.or(//
					builder.like(builder.lower(root.get(AccAccount_.uid)), "%" + filter.getText().toLowerCase() + "%"),
					builder.like(builder.lower(root.get(AccAccount_.systemEntity).get(SysSystemEntity_.uid)),
							"%" + filter.getText().toLowerCase() + "%")));
		}
		if (filter.getSystemId() != null) {
			predicates.add(builder.equal(root.get(AccAccount_.system).get(SysSystem_.id), filter.getSystemId()));
		}
		if (filter.getSystemEntityId() != null) {
			predicates.add(builder.equal(root.get(AccAccount_.systemEntity).get(SysSystemEntity_.id),
					filter.getSystemEntityId()));
		}
		if (filter.getUid() != null) {
			predicates.add(builder.equal(root.get(AccAccount_.uid), filter.getUid()));
		}
		if (filter.getIdentityId() != null || filter.getOwnership() != null) {
			Subquery<AccIdentityAccount> identityAccountSubquery = query.subquery(AccIdentityAccount.class);
			Root<AccIdentityAccount> subRootIdentityAccount = identityAccountSubquery.from(AccIdentityAccount.class);
			identityAccountSubquery.select(subRootIdentityAccount);

			Predicate predicate = builder
					.and(builder.equal(subRootIdentityAccount.get(AccIdentityAccount_.account), root));
			Predicate identityPredicate = builder.equal(
					subRootIdentityAccount.get(AccIdentityAccount_.identity).get(IdmIdentity_.id),
					filter.getIdentityId());
			Predicate ownerPredicate = builder.equal(subRootIdentityAccount.get(AccIdentityAccount_.ownership),
					filter.getOwnership());

			if (filter.getIdentityId() != null && filter.getOwnership() == null) {
				predicate = builder.and(predicate, identityPredicate);
			} else if (filter.getOwnership() != null && filter.getIdentityId() == null) {
				predicate = builder.and(predicate, ownerPredicate);
			} else {
				predicate = builder.and(predicate, identityPredicate, ownerPredicate);
			}

			identityAccountSubquery.where(predicate);
			predicates.add(builder.exists(identityAccountSubquery));
		}
		if (filter.getAccountType() != null) {
			predicates.add(builder.equal(root.get(AccAccount_.accountType), filter.getAccountType()));
		}

		if (filter.getSupportChangePassword() != null && filter.getSupportChangePassword()) {
			Subquery<SysSystemAttributeMapping> systemAttributeMappingSubquery = query
					.subquery(SysSystemAttributeMapping.class);
			Root<SysSystemAttributeMapping> subRootSystemAttributeMapping = systemAttributeMappingSubquery
					.from(SysSystemAttributeMapping.class);
			systemAttributeMappingSubquery.select(subRootSystemAttributeMapping);

			Predicate predicate = builder.and(builder.equal(subRootSystemAttributeMapping//
					.get(SysSystemAttributeMapping_.schemaAttribute)//
					.get(SysSchemaAttribute_.objectClass)//
					.get(SysSchemaObjectClass_.system), //
					root.get(AccAccount_.system)),
					builder.equal(subRootSystemAttributeMapping//
							.get(SysSystemAttributeMapping_.systemMapping)//
							.get(SysSystemMapping_.operationType), SystemOperationType.PROVISIONING),
					builder.equal(subRootSystemAttributeMapping//
							.get(SysSystemAttributeMapping_.schemaAttribute)//
							.get(SysSchemaAttribute_.name), ProvisioningService.PASSWORD_SCHEMA_PROPERTY_NAME));

			systemAttributeMappingSubquery.where(predicate);
			predicates.add(builder.exists(systemAttributeMappingSubquery));
		}

		if (filter.getEntityType() != null) {
			predicates.add(builder.equal(root.get(AccAccount_.entityType), filter.getEntityType()));
		}

		//
		return predicates;

	}

	/**
	 * Find executor for synchronization given entity type
	 * 
	 * @param entityType
	 * @return
	 */
	private SynchronizationEntityExecutor getSyncExecutor(SystemEntityType entityType) {

		if (this.pluginExecutors == null) {
			this.pluginExecutors = OrderAwarePluginRegistry.create(executors);
		}
		SynchronizationEntityExecutor executor = this.pluginExecutors.getPluginFor(entityType);
		if (executor == null) {
			throw new UnsupportedOperationException(MessageFormat
					.format("Synchronization executor for SystemEntityType {0} is not supported!", entityType));
		}
		return executor;
	}

}
