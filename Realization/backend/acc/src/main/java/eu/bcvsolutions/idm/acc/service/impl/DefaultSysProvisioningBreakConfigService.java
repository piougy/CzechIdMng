package eu.bcvsolutions.idm.acc.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.domain.ProvisioningEventType;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningBreakConfigDto;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningBreakItems;
import eu.bcvsolutions.idm.acc.dto.filter.SysProvisioningBreakConfigFilter;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningBreakConfig;
import eu.bcvsolutions.idm.acc.repository.SysProvisioningBreakConfigReposiotry;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningBreakConfigService;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningBreakRecipientService;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;

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

	@Autowired
	public DefaultSysProvisioningBreakConfigService(SysProvisioningBreakConfigReposiotry repository,
			SysProvisioningBreakRecipientService breakRecipientService,
			CacheManager cacheManager) {
		super(repository);
		//
		Assert.notNull(breakRecipientService);
		Assert.notNull(cacheManager);
		//
		this.breakRecipientService = breakRecipientService;
		this.cacheManager = cacheManager;
	}
	
	@Override
	public void delete(SysProvisioningBreakConfigDto dto, BasePermission... permission) {
		Assert.notNull(dto);
		breakRecipientService.deleteAllByBreakConfig(dto.getId());
		super.delete(dto, permission);
	}
	
	@Override
	public SysProvisioningBreakItems getCacheProcessedItems(UUID systemId) {
		SysProvisioningBreakItems cache = (SysProvisioningBreakItems) this.getCache().get(systemId);
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
}
