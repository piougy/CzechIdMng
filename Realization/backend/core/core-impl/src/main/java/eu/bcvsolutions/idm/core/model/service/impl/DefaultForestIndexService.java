package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.UUID;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.forest.index.domain.ForestContent;
import eu.bcvsolutions.forest.index.domain.ForestIndex;
import eu.bcvsolutions.forest.index.service.impl.AbstractForestIndexService;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.model.entity.IdmForestIndexEntity;
import eu.bcvsolutions.idm.core.model.repository.IdmForestIndexEntityRepository;

/**
 * Forest index service
 * * all abstract entities could use this index with different type
 * * indexing could be disabled (for test purposes etc.) by configuration property {@link DefaultForestIndexService#PROPERTY_INDEX_ENABLED}.
 *
 * @author Radek Tomiška
 * @see {@link ForestIndex}
 * @see {@link ConfigurationService}
 */
@Service("forestIndexService")
public class DefaultForestIndexService extends AbstractForestIndexService<IdmForestIndexEntity, UUID> {

	public static final String PROPERTY_INDEX_PREFIX = ConfigurationService.IDM_PRIVATE_PROPERTY_PREFIX + "core.forest.index.";
	public static final String PROPERTY_INDEX_ENABLED = PROPERTY_INDEX_PREFIX + "enabled";
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultForestIndexService.class);
	private final ConfigurationService configurationService;
	
	@Autowired
	public DefaultForestIndexService(
			IdmForestIndexEntityRepository repository, 
			EntityManager entityManager,
			ConfigurationService configurationService) {
		super(repository, entityManager);
		//
		Assert.notNull(configurationService);
		//
		this.configurationService = configurationService;
	}
	
	@Override
	@Transactional
	public <C extends ForestContent<C, IdmForestIndexEntity, UUID>> C index(C content) {
		if (!configurationService.getBooleanValue(PROPERTY_INDEX_ENABLED, true)) {
			LOG.debug("Forest index is disabled. Enable configuration property [{}] for creating index.", DefaultForestIndexService.PROPERTY_INDEX_ENABLED);
			return content;
		}
		return super.index(content);
	}
}
