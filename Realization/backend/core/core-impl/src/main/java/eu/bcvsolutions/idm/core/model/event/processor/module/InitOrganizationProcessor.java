package eu.bcvsolutions.idm.core.model.event.processor.module;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.config.domain.TreeConfiguration;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeNodeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeTypeDto;
import eu.bcvsolutions.idm.core.api.dto.ModuleDescriptorDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.AbstractInitApplicationProcessor;
import eu.bcvsolutions.idm.core.api.service.IdmTreeNodeService;
import eu.bcvsolutions.idm.core.api.service.IdmTreeTypeService;

/**
 * Init default organization.
 * 
 * @author Radek Tomiška
 * @since 10.5.0
 */
@Component(InitOrganizationProcessor.PROCESSOR_NAME)
@Description("Init default organization type 'Organization structure' with one root tree node 'Root organization'. "
		+ "Tree type and node is created, if no other tree type exists.")
public class InitOrganizationProcessor extends AbstractInitApplicationProcessor {

	public static final String PROCESSOR_NAME = "core-init-organization-processor";
	public static final String DEFAULT_TREE_TYPE = "ORGANIZATIONS"; // FIXME: from configuration
	//
	@Autowired private TreeConfiguration treeConfiguration;
	@Autowired private IdmTreeNodeService treeNodeService;
	@Autowired private IdmTreeTypeService treeTypeService;
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
	
	@Override
	public boolean conditional(EntityEvent<ModuleDescriptorDto> event) {
		return super.conditional(event) && isInitDataEnabled();
	}
	
	@Override
	public EventResult<ModuleDescriptorDto> process(EntityEvent<ModuleDescriptorDto> event) {
		//
		// create Node type for organization
		IdmTreeTypeDto treeType = treeTypeService.getByCode(DEFAULT_TREE_TYPE);
		if (treeType == null && treeTypeService.find(PageRequest.of(0, 1)).getTotalElements() == 0) {
			treeType = new IdmTreeTypeDto();
			treeType.setCode(DEFAULT_TREE_TYPE);
			treeType.setName("Organization structure");
			treeType = treeTypeService.save(treeType);
			treeConfiguration.setDefaultType(treeType.getId());
			//
			// create organization root
			if (treeNodeService.findRoots(treeType.getId(), PageRequest.of(0, 1)).getTotalElements() == 0) {
				IdmTreeNodeDto organizationRoot = new IdmTreeNodeDto();
				organizationRoot.setCode("root");
				organizationRoot.setName("Root organization");
				organizationRoot.setTreeType(treeType.getId());
				organizationRoot = treeNodeService.save(organizationRoot);
			}
		}
		return new DefaultEventResult<>(event, this);
	}
	
	@Override
	public int getOrder() {
		// after admin is created
		return CoreEvent.DEFAULT_ORDER + 200;
	}
}
