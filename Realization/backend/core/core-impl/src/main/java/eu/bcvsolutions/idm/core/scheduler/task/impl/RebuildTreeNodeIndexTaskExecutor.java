package eu.bcvsolutions.idm.core.scheduler.task.impl;

import java.util.List;
import java.util.Map;
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
import eu.bcvsolutions.idm.core.api.dto.IdmTreeTypeDto;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.model.entity.IdmForestIndexEntity;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.repository.IdmTreeNodeRepository;
import eu.bcvsolutions.idm.core.model.service.api.IdmTreeTypeService;
import eu.bcvsolutions.idm.core.model.service.impl.DefaultForestIndexService;
import eu.bcvsolutions.idm.core.scheduler.service.impl.AbstractSchedulableTaskExecutor;

/**
 * Rebuild forest index for tree type
 * 
 * @author Radek Tomiška
 *
 */
@Service
@Description("Rebuild tree node index")
public class RebuildTreeNodeIndexTaskExecutor extends AbstractSchedulableTaskExecutor<Boolean> {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(RebuildTreeNodeIndexTaskExecutor.class);
	private static final String PARAMETER_TREE_TYPE = "Tree type code";
	//
	@Autowired private IdmTreeTypeService treeTypeService;
	@Autowired private IdmTreeNodeRepository treeNodeRepository;
	@Autowired private ForestIndexService<IdmForestIndexEntity, UUID> forestIndexService;
	@Autowired private ConfigurationService configurationService;
	//
	private String treeTypeCode;
	
	@Override
	public void init(Map<String, Object> properties) {
		super.init(properties);
		//
		treeTypeCode = getParameterConverter().toString(properties, PARAMETER_TREE_TYPE);
		// validation
		getTreeType();
	}
	
	@Override
	public Boolean process() {
		if (!configurationService.getBooleanValue(DefaultForestIndexService.PROPERTY_INDEX_ENABLED, true)) {
			throw new ResultCodeException(CoreResultCode.FOREST_INDEX_DISABLED, ImmutableMap.of("property", DefaultForestIndexService.PROPERTY_INDEX_ENABLED));
		}
		IdmTreeTypeDto treeType = getTreeType();
		String longRunningTaskId = configurationService.getValue(treeTypeService.getConfigurationPropertyName(treeTypeCode, IdmTreeTypeService.CONFIGURATION_PROPERTY_REBUILD));
		if (StringUtils.hasLength(longRunningTaskId) && !longRunningTaskId.equals(getLongRunningTaskId().toString())) {
			throw new ResultCodeException(CoreResultCode.FOREST_INDEX_RUNNING, ImmutableMap.of("type", IdmTreeNode.toForestTreeType(treeType.getId())));
		}
		//
		LOG.info("Starting rebuilding tree node index for tree type code [{}].", treeTypeCode);
		//
		// clear all rgt, lft
		try {
			forestIndexService.dropIndexes(IdmTreeNode.toForestTreeType(treeType.getId()));
		} finally {
			configurationService.setBooleanValue(treeTypeService.getConfigurationPropertyName(treeTypeCode, IdmTreeTypeService.CONFIGURATION_PROPERTY_VALID), false);
		}
		try {
			configurationService.setValue(treeTypeService.getConfigurationPropertyName(treeTypeCode, IdmTreeTypeService.CONFIGURATION_PROPERTY_REBUILD), getLongRunningTaskId().toString());
			//
			Page<IdmTreeNode> nodes = treeNodeRepository.findByTreeType_Id(treeType.getId(), new PageRequest(0, 100, new Sort("id")));
			count = nodes.getTotalElements();
			counter = 0L;
			boolean canContinue = true;
			while (canContinue) {
				for(IdmTreeNode node : nodes) {
					if (node.getForestIndex() == null) {
						forestIndexService.index(node.getForestTreeType(), node.getId(), node.getParentId());
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
				nodes = treeNodeRepository.findByTreeType_Id(treeType.getId(), nodes.nextPageable());
			}
			//
			if (count.equals(counter)) {
				configurationService.deleteValue(treeTypeService.getConfigurationPropertyName(treeTypeCode, IdmTreeTypeService.CONFIGURATION_PROPERTY_VALID));
				LOG.info("Tree node index for tree type code [{}] was successfully rebuilt (index size [{}]).", treeTypeCode, counter);
				return Boolean.TRUE;
			} 
			//
			LOG.warn("Tree node index for tree type code [{}] rebuild was canceled (index size [{}]).", treeTypeCode, counter);
			return Boolean.FALSE;
		} finally {
			configurationService.deleteValue(treeTypeService.getConfigurationPropertyName(treeTypeCode, IdmTreeTypeService.CONFIGURATION_PROPERTY_REBUILD));
		}
	}
	
	private IdmTreeTypeDto getTreeType() {
		IdmTreeTypeDto treeType = treeTypeService.getByCode(treeTypeCode);
		if(treeType == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND,
					ImmutableMap.of("entity", treeTypeCode));
		}
		return treeType;
	}
	
	public void setTreeTypeCode(String treeTypeCode) {
		this.treeTypeCode = treeTypeCode;
	}
	
	public String getTreeTypeCode() {
		return treeTypeCode;
	}
	
	@Override
	public List<String> getParameterNames() {
		List<String> parameters = super.getPropertyNames();
		parameters.add(PARAMETER_TREE_TYPE);
		return parameters;
	}
}
