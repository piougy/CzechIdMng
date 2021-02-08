package eu.bcvsolutions.idm.core.rest.lookup;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.IdmTreeNodeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeTypeDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmTreeNodeFilter;
import eu.bcvsolutions.idm.core.api.rest.lookup.AbstractDtoLookupByExample;
import eu.bcvsolutions.idm.core.api.service.IdmTreeNodeService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode_;

/**
 * Tree node lookup by example.
 * Tree node is not codeable - try to find by tree type + tree node code.
 * 
 * @author Radek Tomiška
 * @since 10.8.0
 */
@Component
public class IdmTreeNodeDtoLookupByExample extends AbstractDtoLookupByExample<IdmTreeNodeDto>{

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(IdmTreeNodeDtoLookupByExample.class);
	//
	@Autowired @Lazy private IdmTreeNodeService treeNodeService;

	@Override
	public IdmTreeNodeDto lookup(IdmTreeNodeDto example) {
		IdmTreeNodeFilter nodeFilter = new IdmTreeNodeFilter();
		nodeFilter.setCode(example.getCode());
		//
		IdmTreeTypeDto embeddedTreeType = DtoUtils.getEmbedded(example, IdmTreeNode_.treeType, IdmTreeTypeDto.class, null);
		if (embeddedTreeType != null) {
			// code has higher priority, if embedded object is available (can be used between different environments - e.g. in import)
			nodeFilter.setTreeTypeCode(embeddedTreeType.getCode());
		} else {
			nodeFilter.setTreeTypeId(example.getTreeType());
		}
		//
		Page<IdmTreeNodeDto> result = treeNodeService.find(nodeFilter, PageRequest.of(0, 1));
		long count = result.getTotalElements();
		// not found
		if (count == 0) {
			return null;
		}
		//
		if (count > 1) {
			LOG.trace("More result found [{}] for given example, returning null (~ not found by example).", count);
			return null;
		}
		//
		return result.getContent().get(0);
	}
}
