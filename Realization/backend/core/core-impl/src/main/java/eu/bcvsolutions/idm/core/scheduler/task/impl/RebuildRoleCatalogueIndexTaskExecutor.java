package eu.bcvsolutions.idm.core.scheduler.task.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.forest.index.service.api.ForestIndexService;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleCatalogueService;
import eu.bcvsolutions.idm.core.model.entity.IdmForestIndexEntity;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleCatalogue;
import eu.bcvsolutions.idm.core.model.repository.IdmRoleCatalogueRepository;
import eu.bcvsolutions.idm.core.model.service.impl.DefaultForestIndexService;
import eu.bcvsolutions.idm.core.scheduler.api.service.AbstractSchedulableTaskExecutor;

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
	@Autowired private IdmRoleCatalogueRepository roleCatalogueRepository;
	@Autowired private IdmRoleCatalogueService roleCatalogueService;
	@Autowired private ForestIndexService<IdmForestIndexEntity, UUID> forestIndexService;
	@Autowired private ConfigurationService configurationService;
	
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
			count = roleCatalogueRepository.findAll(new PageRequest(0, 1)).getTotalElements();
			counter = 0L;
			boolean canContinue = true;
			Page<IdmRoleCatalogue> roots = roleCatalogueRepository.findRoots(new PageRequest(0, 100));
			while (canContinue) {
				canContinue = processChildren(roots.getContent());
				if (!canContinue) {
					break;
				}
				if (!roots.hasNext()) {
					break;
				}
				roots = roleCatalogueRepository.findRoots(roots.nextPageable());
			}
			//
			if (count.equals(counter)) {
				configurationService.deleteValue(roleCatalogueService.getConfigurationPropertyName(IdmRoleCatalogueService.CONFIGURATION_PROPERTY_VALID));
				LOG.info("Forest index for role catalogue was successfully rebuilt (index size [{}]).", counter);
				return Boolean.TRUE;
			} 
			//
			LOG.warn("Forest index for role catalogue rebuild was canceled (index size [{}]).", counter);
			return Boolean.FALSE;
		} finally {
			configurationService.deleteValue(roleCatalogueService.getConfigurationPropertyName(IdmRoleCatalogueService.CONFIGURATION_PROPERTY_REBUILD));
		}
	}
	
	private boolean processChildren(List<IdmRoleCatalogue> nodes) {
		boolean canContinue = true;
		for(IdmRoleCatalogue node : nodes) {
			if (node.getForestIndex() == null) {
				forestIndexService.index(node.getForestTreeType(), node.getId(), node.getParentId());
			}
			counter++;
			canContinue = updateState();
			if (!canContinue) {
				break;
			}
			// proces nodes childred
			canContinue = processChildren(roleCatalogueRepository.findDirectChildren(node, null).getContent());
			if (!canContinue) {
				break;
			}
		};
		return canContinue;
	}
}
