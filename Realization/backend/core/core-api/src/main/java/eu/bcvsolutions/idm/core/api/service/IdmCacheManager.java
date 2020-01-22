package eu.bcvsolutions.idm.core.api.service;

import eu.bcvsolutions.idm.core.api.dto.IdmCacheDto;
import org.springframework.data.domain.Page;

/**
 * @author Peter Štrunc <peter.strunc@bcvsolutions.eu>
 */

public interface IdmCacheManager {

    public Page<IdmCacheDto> getAllAvailableCaches();

    void evictCache(String cacheId);
}
