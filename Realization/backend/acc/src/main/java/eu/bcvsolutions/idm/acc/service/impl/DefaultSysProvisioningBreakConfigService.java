package eu.bcvsolutions.idm.acc.service.impl;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.SimpleValueWrapper;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.acc.domain.AccGroupPermission;
import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.domain.ProvisioningBreakConfiguration;
import eu.bcvsolutions.idm.acc.domain.ProvisioningEventType;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningBreakConfigDto;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningBreakItems;
import eu.bcvsolutions.idm.acc.dto.filter.SysProvisioningBreakConfigFilter;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningBreakConfig;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningBreakConfig_;
import eu.bcvsolutions.idm.acc.exception.ProvisioningException;
import eu.bcvsolutions.idm.acc.repository.SysProvisioningBreakConfigRepository;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningBreakConfigService;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningBreakRecipientService;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity_;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationTemplateDto;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;

/**
 * Default implementation for {@link SysProvisioningBreakConfigService}
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Service
public class DefaultSysProvisioningBreakConfigService extends
		AbstractReadWriteDtoService<SysProvisioningBreakConfigDto, SysProvisioningBreakConfig, SysProvisioningBreakConfigFilter>
		implements SysProvisioningBreakConfigService {

	private final String CACHE_NAME = "idm-provisioning-cache";
	
	private final SysProvisioningBreakRecipientService breakRecipientService;
	private final CacheManager cacheManager;
	private final ProvisioningBreakConfiguration provisioningBreakConfiguration;
	private final SysProvisioningBreakConfigRepository repository;

	@Autowired
	public DefaultSysProvisioningBreakConfigService(SysProvisioningBreakConfigRepository repository,
			SysProvisioningBreakRecipientService breakRecipientService,
			CacheManager cacheManager,
			ProvisioningBreakConfiguration provisioningBreakConfiguration) {
		super(repository);
		//
		Assert.notNull(breakRecipientService);
		Assert.notNull(cacheManager);
		Assert.notNull(provisioningBreakConfiguration);
		//
		this.breakRecipientService = breakRecipientService;
		this.cacheManager = cacheManager;
		this.provisioningBreakConfiguration = provisioningBreakConfiguration;
		this.repository = repository;
	}
	
	@Override
	public SysProvisioningBreakConfigDto get(Serializable id, BasePermission... permission) {
		// TODO Auto-generated method stub
		repository.findOne(UUID.fromString(id.toString()));
		return super.get(id, permission);
	}
	
	@Override
	public void delete(SysProvisioningBreakConfigDto dto, BasePermission... permission) {
		Assert.notNull(dto);
		breakRecipientService.deleteAllByBreakConfig(dto.getId());
		super.delete(dto, permission);
	}
	
	@Override
	public SysProvisioningBreakConfigDto save(SysProvisioningBreakConfigDto dto, BasePermission... permission) {
		// check if for same system doesn't exist same operation type
		SysProvisioningBreakConfigFilter filter = new SysProvisioningBreakConfigFilter();
		filter.setSystemId(dto.getSystem());
		filter.setOperationType(dto.getOperationType());
		List<SysProvisioningBreakConfigDto> similarConfigs = this.find(filter, null).getContent();
		boolean existSimilar = similarConfigs.stream().filter(config -> !config.getId().equals(dto.getId())).findFirst().isPresent();
		if (!existSimilar) {
			return super.save(dto, permission);			
		}
		throw new ProvisioningException(AccResultCode.PROVISIONING_BREAK_OPERATION_EXISTS,
				ImmutableMap.of("operationType", dto.getOperationType(), "systemId", dto.getSystem()));
	}
	
	@Override
	public SysProvisioningBreakItems getCacheProcessedItems(UUID systemId) {
		SimpleValueWrapper cachedValueWrapper = (SimpleValueWrapper) this.getCache().get(systemId);
		if (cachedValueWrapper == null) {
			return new SysProvisioningBreakItems();
		}
		SysProvisioningBreakItems cache = (SysProvisioningBreakItems) cachedValueWrapper.get();
		if (cache == null) {
			return new SysProvisioningBreakItems();
		}
		return cache;
	}

	@Override
	public void saveCacheProcessedItems(UUID systemId, SysProvisioningBreakItems cache) {
		this.getCache().put(systemId, cache);
	}

	private Cache getCache() {
		return this.cacheManager.getCache(CACHE_NAME);
	}

	@Override
	public SysProvisioningBreakConfigDto getConfig(ProvisioningEventType operationType, UUID systemId) {
		SysProvisioningBreakConfigFilter filter = new SysProvisioningBreakConfigFilter();
		filter.setOperationType(operationType);
		filter.setSystemId(systemId);
		List<SysProvisioningBreakConfigDto> configs = this.find(filter, null).getContent();
		//
		if (configs.isEmpty()) {
			return null;
		}
		// must exists only one configs for operation type and system id
		return configs.stream().findFirst().get();
	}

	@Override
	public SysProvisioningBreakConfigDto getGlobalBreakConfiguration() {
		SysProvisioningBreakConfigDto globalConfig = new SysProvisioningBreakConfigDto();
		globalConfig.setDisabled(provisioningBreakConfiguration.getDisabled());
		globalConfig.setDisableLimit(provisioningBreakConfiguration.getDisableLimit());
		//
		IdmNotificationTemplateDto disabledTemplate = provisioningBreakConfiguration.getDisableTemplate();
		if (disabledTemplate != null) {
			globalConfig.setDisableTemplate(disabledTemplate.getId());
			globalConfig.setDisableTemplateEmbedded(disabledTemplate);
		}
		//
		IdmNotificationTemplateDto warningTemplate = provisioningBreakConfiguration.getWarningTemplate();
		if (warningTemplate != null) {
			globalConfig.setWarningTemplate(warningTemplate.getId());
			globalConfig.setWarningTemplateEmbedded(warningTemplate);
		}
		//
		globalConfig.setOperationDisabled(provisioningBreakConfiguration.getOperationDisabled());
		globalConfig.setOperationType(null); // operation type for global provisioning break is not implemented yet
		globalConfig.setPeriod(provisioningBreakConfiguration.getPeriod());
		globalConfig.setSystem(null); // global provisioning break hasn't system id
		globalConfig.setWarningLimit(provisioningBreakConfiguration.getWarningLimit());
		return globalConfig;
	}

	@Override
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(AccGroupPermission.SYSTEM, getEntityClass());
	}
	
	@Override
	protected List<Predicate> toPredicates(Root<SysProvisioningBreakConfig> root, CriteriaQuery<?> query,
			CriteriaBuilder builder, SysProvisioningBreakConfigFilter filter) {
		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);
		//
		if (filter.getSystemId() != null) {
			predicates.add(builder.equal(root.get(SysProvisioningBreakConfig_.system).get(AbstractEntity_.id), filter.getSystemId()));
		}
		//
		if (filter.getPeriod() != null) {
			predicates.add(builder.equal(root.get(SysProvisioningBreakConfig_.period), filter.getPeriod()));
		}
		//
		if (filter.getWarningLimit() != null) {
			predicates.add(builder.equal(root.get(SysProvisioningBreakConfig_.warningLimit), filter.getWarningLimit()));
		}
		//
		if (filter.getDisableLimit() != null) {
			predicates.add(builder.equal(root.get(SysProvisioningBreakConfig_.disableLimit), filter.getDisableLimit()));
		}
		//
		if (filter.getOperationType() != null) {
			predicates.add(builder.equal(root.get(SysProvisioningBreakConfig_.operationType), filter.getOperationType()));
		}
		//
		return predicates;
	}
}
