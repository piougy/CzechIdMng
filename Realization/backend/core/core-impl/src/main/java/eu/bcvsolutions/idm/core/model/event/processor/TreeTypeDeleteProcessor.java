package eu.bcvsolutions.idm.core.model.event.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.exception.TreeTypeException;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeType;
import eu.bcvsolutions.idm.core.model.event.TreeTypeEvent.TreeTypeEventType;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityContractRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmTreeNodeRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmTreeTypeRepository;

/**
 * Deletes tree type - ensures referential integrity.
 * 
 * @author Svanda
 *
 */
@Component
@Description("Deletes tree type")
public class TreeTypeDeleteProcessor extends CoreEventProcessor<IdmTreeType> {

	public static final String PROCESSOR_NAME = "tree-node-delete-processor";
	private final IdmTreeTypeRepository repository;
	private final IdmTreeNodeRepository nodeRepository;
	private final IdmIdentityContractRepository identityContractRepository;
	
	@Autowired
	public TreeTypeDeleteProcessor(
			IdmTreeTypeRepository repository,
			IdmTreeNodeRepository nodeRepository,
			IdmIdentityContractRepository identityContractRepository) {
		super(TreeTypeEventType.DELETE);
		//
		Assert.notNull(repository);
		Assert.notNull(nodeRepository);
		Assert.notNull(identityContractRepository);
		//
		this.repository = repository;
		this.nodeRepository = nodeRepository;
		this.identityContractRepository = identityContractRepository;
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
	
	@Override
	public EventResult<IdmTreeType> process(EntityEvent<IdmTreeType> event) {
		IdmTreeType treeType = event.getContent();
		
		Assert.notNull(treeType);
		//	
		Page<IdmTreeNode> nodes = nodeRepository.findChildren(treeType.getId(), null, new PageRequest(0, 1));
		if (nodes.getTotalElements() > 0) {
			throw new TreeTypeException(CoreResultCode.TREE_TYPE_DELETE_FAILED_HAS_CHILDREN,  ImmutableMap.of("treeType", treeType.getName()));
		}		
		if (identityContractRepository.countByWorkPosition_TreeType(treeType) > 0) {
			throw new TreeTypeException(CoreResultCode.TREE_TYPE_DELETE_FAILED_HAS_CONTRACTS,  ImmutableMap.of("treeType", treeType.getName()));
		}		
		//
		repository.delete(treeType);
		//
		return new DefaultEventResult<>(event, this);
	}
}
