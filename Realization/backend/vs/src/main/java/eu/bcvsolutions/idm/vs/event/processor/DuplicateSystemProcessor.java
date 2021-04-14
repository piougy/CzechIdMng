package eu.bcvsolutions.idm.vs.event.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.event.SystemEvent.SystemEventType;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.event.AbstractEntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.utils.EntityUtils;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.vs.VirtualSystemModuleDescriptor;
import eu.bcvsolutions.idm.vs.entity.VsAccount;
import eu.bcvsolutions.idm.vs.service.api.VsSystemService;

/**
 * Duplicate system - virtual system duplication.
 * 
 *  
 * @author Ondrej Husnik
 * @since 11.0.0
 */
@Component(DuplicateSystemProcessor.PROCESSOR_NAME)
@Description("Duplicate system - virtual system duplication. ")
public class DuplicateSystemProcessor extends AbstractEntityEventProcessor<SysSystemDto> {
	
	public static final String PROCESSOR_NAME = "vs-duplicate-system-processor";
	
	@Autowired
	SysSystemService systemService;
	@Autowired
	VsSystemService vsSystemService;
	@Autowired
	private FormService formService;
	
	public DuplicateSystemProcessor() {
		super(SystemEventType.DUPLICATE);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
	
	@Override
	public boolean conditional(EntityEvent<SysSystemDto> event) {
		SysSystemDto system = event.getContent();
		Assert.notNull(system, "System is required.");
		return system.isVirtual();
	}

	@Override
	public EventResult<SysSystemDto> process(EntityEvent<SysSystemDto> event) {
		SysSystemDto system = event.getContent();
		SysSystemDto origSystem = event.getOriginalSource();
		
		duplicateFormDefinition(system, origSystem);
		
		event.setContent(system);
		return new DefaultEventResult<>(event, this);
	}
	
	@Override
	public int getOrder() {
		return 100;
	}
	
	/**
	 * Encapsulates duplication of VS form attributes 
	 * 
	 * @param clonedSystem
	 * @param originalSystem
	 */
	private void duplicateFormDefinition(SysSystemDto clonedSystem, SysSystemDto originalSystem) {
		String type = VsAccount.class.getName();
		// get original VS form definition
		String vsKeyOrig = vsSystemService.createVsFormDefinitionKey(originalSystem);
		IdmFormDefinitionDto origFormDef = formService.getDefinition(type, vsKeyOrig);
		List<IdmFormAttributeDto> origAttibutes = formService.getAttributes(origFormDef);

		// create new form attributes based on originalAttributes
		String vsKey = vsSystemService.createVsFormDefinitionKey(clonedSystem);
		IdmFormDefinitionDto existingDefinition = formService.getDefinition(type, vsKey);
		if (existingDefinition != null) {
			// clean old VS definition attributes if exist
			List<IdmFormAttributeDto> existingAttributes = formService.getAttributes(existingDefinition);
			existingAttributes.forEach(attr -> formService.deleteAttribute(attr));
			List<IdmFormAttributeDto> clonedAttributes = prepareFormAttributes(origAttibutes,
					existingDefinition.getId());
			clonedAttributes.forEach(attr -> formService.saveAttribute(attr));
		} else {
			List<IdmFormAttributeDto> clonedAttributes = prepareFormAttributes(origAttibutes, null);
			formService.createDefinition(type, vsKey, VirtualSystemModuleDescriptor.MODULE_ID, clonedAttributes);
		}
	}
	
	/**
	 * Prepares form attributes for duplication.
	 * 
	 * @param attributes
	 * @return
	 */
	private List<IdmFormAttributeDto> prepareFormAttributes (List<IdmFormAttributeDto> attributes, UUID formDefinition) {
		List<IdmFormAttributeDto> newAttrs = new ArrayList<IdmFormAttributeDto>();
		for (IdmFormAttributeDto attr : attributes) {
			attr.setId(null);
			attr.setFormDefinition(formDefinition);
			EntityUtils.clearAuditFields(attr);
			newAttrs.add(attr);
		}
		return newAttrs;
	}

}
