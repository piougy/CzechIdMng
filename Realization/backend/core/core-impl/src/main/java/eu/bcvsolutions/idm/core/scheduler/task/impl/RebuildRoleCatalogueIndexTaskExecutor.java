package eu.bcvsolutions.idm.core.scheduler.task.impl;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.forest.index.service.api.ForestIndexService;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.model.entity.IdmForestIndexEntity;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleCatalogue;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleCatalogueService;
import eu.bcvsolutions.idm.core.model.service.impl.DefaultForestIndexService;
import eu.bcvsolutions.idm.core.scheduler.service.impl.AbstractSchedulableTaskExecutor;

/**
 * Rebuild forest index for role catalogue
 * 
 * @author Radek Tomiška
 *
 */
@Service
@Description("Rebuild role catalogue index")
public class RebuildRoleCatalogueIndexTaskExecutor extends AbstractSchedulableTaskExecutor<Boolean> {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(RebuildRoleCatalogueIndexTaskExecutor.class);
	//
	@Autowired
	private IdmRoleCatalogueService roleCatalogueService;
	@Autowired
	private ForestIndexService<IdmForestIndexEntity, UUID> forestIndexService;
	@Autowired
	private ConfigurationService configurationService;
	
	@Override
	public Boolean process() {
		if (!configurationService.getBooleanValue(DefaultForestIndexService.PROPERTY_INDEX_ENABLED, true)) {
			throw new ResultCodeException(CoreResultCode.FOREST_INDEX_DISABLED, ImmutableMap.of("property", DefaultForestIndexService.PROPERTY_INDEX_ENABLED));
		}
		String longRunningTaskId = configurationService.getValue(roleCatalogueService.getConfigurationPropertyName(IdmRoleCatalogueService.CONFIGURATION_PROPERTY_REBUILD));
		if (StringUtils.hasLength(longRunningTaskId) && !longRunningTaskId.equals(getLongRunningTaskId().toString())) {
			throw new ResultCodeException(CoreResultCode.FOREST_INDEX_RUNNING, ImmutableMap.of("type", IdmRoleCatalogue.FOREST_TREE_TYPE));
		}
		//
		LOG.info("Starting rebuilding index for role catalogue.");
		//
		// clear all rgt, lft
		try {
			forestIndexService.dropIndexes(IdmRoleCatalogue.FOREST_TREE_TYPE);
		} finally {
			configurationService.setBooleanValue(roleCatalogueService.getConfigurationPropertyName(IdmRoleCatalogueService.CONFIGURATION_PROPERTY_VALID), false);
		}
		try {
			configurationService.setValue(roleCatalogueService.getConfigurationPropertyName(IdmRoleCatalogueService.CONFIGURATION_PROPERTY_REBUILD), getLongRunningTaskId().toString());
			//
			Page<IdmRoleCatalogue> nodes = roleCatalogueService.find(new PageRequest(0, 100, new Sort("id")));
			count = nodes.getTotalElements();
			counter = 0L;
			boolean canContinue = true;
			while (canContinue) {
				for(IdmRoleCatalogue node : nodes) {
					if (node.getForestIndex() == null) {
						forestIndexService.index(node);
					}
					counter++;	
					canContinue = updateState();
					if (!canContinue) {
						break;
					}
				};
				if (!nodes.hasNext()) {
					break;
				}
				nodes = roleCatalogueService.find(nodes.nextPageable());
			}
			//
			if (count.equals(counter)) {
				configurationService.deleteValue(roleCatalogueService.getConfigurationPropertyName(IdmRoleCatalogueService.CONFIGURATION_PROPERTY_VALID));
				LOG.info("Forest index for role catalogue was successfully rebuilt (index size [{}]).", counter);
				return true;
			} 
			//
			LOG.warn("Forest index for role catalogue rebuild was canceled (index size [{}]).", counter);
			return false;
		} finally {
			configurationService.deleteValue(roleCatalogueService.getConfigurationPropertyName(IdmRoleCatalogueService.CONFIGURATION_PROPERTY_REBUILD));
		}
	}
}
