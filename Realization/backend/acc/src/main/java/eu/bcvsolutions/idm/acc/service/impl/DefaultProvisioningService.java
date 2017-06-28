package eu.bcvsolutions.idm.acc.service.impl;

import java.text.MessageFormat;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.plugin.core.OrderAwarePluginRegistry;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.domain.AttributeMapping;
import eu.bcvsolutions.idm.acc.domain.ProvisioningOperationType;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.entity.AccAccount;
import eu.bcvsolutions.idm.acc.entity.SysRoleSystemAttribute;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.entity.SysSystemEntity;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningEntityExecutor;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningService;
import eu.bcvsolutions.idm.core.api.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.ic.api.IcUidAttribute;

/**
 * Service for do provisioning
 * @author svandav
 *
 */
@Service
public class DefaultProvisioningService implements ProvisioningService {

	private final PluginRegistry<ProvisioningEntityExecutor<?>, SystemEntityType> pluginExecutors; 

	@Autowired
	public DefaultProvisioningService(List<ProvisioningEntityExecutor<?>>  executors) {
		Assert.notNull(executors);
		//
		this.pluginExecutors = OrderAwarePluginRegistry.create(executors);
	}

	@Override
	public void doProvisioning(AbstractEntity entity) {
		Assert.notNull(entity);
		this.getExecutor(SystemEntityType.getByClass(entity.getClass())).doProvisioning(entity);		
	}

	@Override
	public void doProvisioning(AccAccount account) {
		Assert.notNull(account);
		this.getExecutor(account.getSystemEntity().getEntityType()).doProvisioning(account);
	}

	@Override
	public void doProvisioning(AccAccount account, AbstractEntity entity) {
		Assert.notNull(account);
		Assert.notNull(entity);
		this.getExecutor(SystemEntityType.getByClass(entity.getClass())).doProvisioning(account, entity);
	}
	
	@Override
	public void doInternalProvisioning(AccAccount account, AbstractEntity entity) {
		Assert.notNull(account);
		Assert.notNull(entity);
		this.getExecutor(SystemEntityType.getByClass(entity.getClass())).doInternalProvisioning(account, entity);
	}

	@Override
	public void doDeleteProvisioning(AccAccount account, SystemEntityType entityType) {
		Assert.notNull(account);
		this.getExecutor(entityType).doDeleteProvisioning(account);
	}

	@Override
	public void changePassword(AbstractEntity entity, PasswordChangeDto passwordChange) {
		Assert.notNull(entity);
		//
		this.getExecutor(SystemEntityType.getByClass(entity.getClass())).changePassword(entity, passwordChange);
	}

	@Override
	public void doProvisioningForAttribute(SysSystemEntity systemEntity, AttributeMapping mappedAttribute, Object value,
			ProvisioningOperationType operationType, AbstractEntity entity) {
		Assert.notNull(entity);
		this.getExecutor(SystemEntityType.getByClass(entity.getClass())).doProvisioningForAttribute(systemEntity, mappedAttribute, value, operationType, entity);
	}

	@Override
	public IcUidAttribute authenticate(String username, GuardedString password, SysSystem system,
			SystemEntityType entityType) {
		Assert.notNull(entityType);
		return this.getExecutor(entityType).authenticate(username, password, system, entityType);
	}

	@Override
	public List<AttributeMapping> resolveMappedAttributes(String uid, AccAccount account, AbstractEntity entity,
			SysSystem system, SystemEntityType entityType) {
		Assert.notNull(entityType);
		return this.getExecutor(entityType).resolveMappedAttributes(uid, account, entity, system, entityType);
	}

	@Override
	public List<AttributeMapping> compileAttributes(List<? extends AttributeMapping> defaultAttributes,
			List<SysRoleSystemAttribute> overloadingAttributes, SystemEntityType entityType) {
		Assert.notNull(entityType);
		return this.getExecutor(entityType).compileAttributes(defaultAttributes, overloadingAttributes, entityType);
	}

	@Override
	public void createAccountsForAllSystems(AbstractEntity entity) {
		Assert.notNull(entity);
		this.getExecutor(SystemEntityType.getByClass(entity.getClass())).createAccountsForAllSystems(entity);
	}

	/**
	 * Find executor for synchronization given entity type
	 * @param entityType
	 * @return
	 */
	private ProvisioningEntityExecutor<AbstractEntity> getExecutor(SystemEntityType entityType){
		
		@SuppressWarnings("unchecked")
		ProvisioningEntityExecutor<AbstractEntity> executor =  (ProvisioningEntityExecutor<AbstractEntity>) pluginExecutors.getPluginFor(entityType);
		if (executor == null) {
			throw new UnsupportedOperationException(
					MessageFormat.format("Provisioning executor for SystemEntityType {0} is not supported!", entityType));
		}
		return executor;
	}
}
