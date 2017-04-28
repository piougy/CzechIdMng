package eu.bcvsolutions.idm.acc.event.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.event.ProvisioningEvent;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningService;
import eu.bcvsolutions.idm.acc.service.api.TreeProvisioningService;
import eu.bcvsolutions.idm.core.api.event.AbstractEntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.CoreEvent.CoreEventType;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.event.TreeNodeEvent.TreeNodeEventType;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;

/**
 * Run provisioning after tree node was saved.
 * 
 * @author Svanda
 *
 */
@Component("accTreeNodeSaveProcessor")
@Enabled(AccModuleDescriptor.MODULE_ID)
@Description("Executes provisioing after tree node is saved.")
public class TreeNodeSaveProcessor extends AbstractEntityEventProcessor<IdmTreeNode> {

	public static final String PROCESSOR_NAME = "tree-node-save-processor";
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(TreeNodeSaveProcessor.class);
	private TreeProvisioningService provisioningService;
	private final ApplicationContext applicationContext;
	
	@Autowired
	public TreeNodeSaveProcessor(ApplicationContext applicationContext) {
		super(TreeNodeEventType.CREATE, TreeNodeEventType.UPDATE, CoreEventType.EAV_SAVE);
		//
		Assert.notNull(applicationContext);
		//
		this.applicationContext = applicationContext;
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmTreeNode> process(EntityEvent<IdmTreeNode> event) {
		doProvisioning(event.getContent());
		return new DefaultEventResult<>(event, this);
	}
	
	private void doProvisioning(IdmTreeNode node) {
		LOG.debug("Call account managment (create accounts for all systems) for tree node [{}]", node.getCode());
		getProvisioningService().createAccountsForAllSystems(node);
		LOG.debug("Call provisioning for tree node [{}]", node.getCode());
		getProvisioningService().doProvisioning(node);
	}

	@Override
	public int getOrder() {
		return ProvisioningEvent.DEFAULT_PROVISIONING_ORDER;
	}
	
	/**
	 * provisioningService has dependency everywhere - so we need lazy init ...
	 * 
	 * @return
	 */
	private TreeProvisioningService getProvisioningService() {
		if (provisioningService == null) {
			provisioningService = applicationContext.getBean(TreeProvisioningService.class);
		}
		return provisioningService;
	}
	
}