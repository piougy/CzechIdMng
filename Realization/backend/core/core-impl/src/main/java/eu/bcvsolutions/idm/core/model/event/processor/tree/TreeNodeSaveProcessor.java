package eu.bcvsolutions.idm.core.model.event.processor.tree;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeNodeDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.IdmTreeNodeService;
import eu.bcvsolutions.idm.core.model.event.TreeNodeEvent.TreeNodeEventType;

/**
 * Persists tree node.
 * 
 * @author Radek Tomiška
 *
 */
@Component
@Description("Persists tree node with forest index")
public class TreeNodeSaveProcessor extends CoreEventProcessor<IdmTreeNodeDto> {
	
	public static final String PROCESSOR_NAME = "tree-node-save-processor";
	private final IdmTreeNodeService service;
	
	@Autowired
	public TreeNodeSaveProcessor(IdmTreeNodeService service) {
		super(TreeNodeEventType.UPDATE, TreeNodeEventType.CREATE);
		//
		this.service = service;
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmTreeNodeDto> process(EntityEvent<IdmTreeNodeDto> event) {
		IdmTreeNodeDto dto = event.getContent();

		// Check if is code empty or null
		if (StringUtils.isBlank(dto.getCode())) {
			throw new ResultCodeException(CoreResultCode.TREE_NODE_EMPTY_CODE);
		}

		dto = service.saveInternal(dto);
		event.setContent(dto);
		//
		return new DefaultEventResult<>(event, this);
	}
}
