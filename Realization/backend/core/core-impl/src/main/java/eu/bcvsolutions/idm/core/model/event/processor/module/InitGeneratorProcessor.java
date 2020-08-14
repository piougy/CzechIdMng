package eu.bcvsolutions.idm.core.model.event.processor.module;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmGenerateValueDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.ModuleDescriptorDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.AbstractInitApplicationProcessor;
import eu.bcvsolutions.idm.core.api.service.IdmGenerateValueService;
import eu.bcvsolutions.idm.core.generator.identity.IdentityFormDefaultValueGenerator;
import eu.bcvsolutions.idm.core.generator.role.ConceptRoleRequestFormDefaultValueGenerator;
import eu.bcvsolutions.idm.core.generator.role.IdentityRoleFormDefaultValueGenerator;

/**
 * Init system value generators.
 * 
 * TODO: role, tree node, contract, slices
 * 
 * @author Ondřej Kopr
 * @author Radek Tomiška
 * @since 10.5.0
 */
@Component(InitGeneratorProcessor.PROCESSOR_NAME)
@Description("Init value generators for set default values of extended form attributes "
		+ "(for identity, role request concepts and assigned role attributes).")
public class InitGeneratorProcessor extends AbstractInitApplicationProcessor {

	public static final String PROCESSOR_NAME = "core-init-generator-processor";
	// FIXME: redesign - the same mechanism as for policy
	private static final UUID DEFAULT_FORM_GENERATE_VALUE_ID = UUID.fromString("61ae4b97-421d-4075-8911-8003989f30df"); // static system generate value uuid
	private static final UUID DEFAULT_CONCEPT_ROLE_REQUEST_FORM_GENERATE_VALUE_ID = UUID.fromString("f1752a83-c496-4f94-8e5d-e1705cbd76ee"); // static system generate value uuid
	private static final UUID DEFAULT_IDENTITY_ROLE_FORM_GENERATE_VALUE_ID = UUID.fromString("a5239276-c538-4da7-9b83-30e370a0e8a5"); // static system generate value uuid
	//
	@Autowired private IdmGenerateValueService generateValueService;
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
	
	@Override
	public EventResult<ModuleDescriptorDto> process(EntityEvent<ModuleDescriptorDto> event) {
		//
		// register default value generators
		if (generateValueService.get(DEFAULT_FORM_GENERATE_VALUE_ID) == null) {
			IdmGenerateValueDto generateValue = new IdmGenerateValueDto(DEFAULT_FORM_GENERATE_VALUE_ID);
			generateValue.setDtoType(IdmIdentityDto.class.getCanonicalName());
			generateValue.setGeneratorType(IdentityFormDefaultValueGenerator.class.getCanonicalName());
			generateValue.setSeq((short) 100);
			generateValue.setUnmodifiable(true);
			generateValueService.save(generateValue);
		}
		if (generateValueService.get(DEFAULT_CONCEPT_ROLE_REQUEST_FORM_GENERATE_VALUE_ID) == null) {
			IdmGenerateValueDto generateValue = new IdmGenerateValueDto(DEFAULT_CONCEPT_ROLE_REQUEST_FORM_GENERATE_VALUE_ID);
			generateValue.setDtoType(IdmConceptRoleRequestDto.class.getCanonicalName());
			generateValue.setGeneratorType(ConceptRoleRequestFormDefaultValueGenerator.class.getCanonicalName());
			generateValue.setSeq((short) 100);
			generateValue.setUnmodifiable(true);
			generateValueService.save(generateValue);
		}
		if (generateValueService.get(DEFAULT_IDENTITY_ROLE_FORM_GENERATE_VALUE_ID) == null) {
			IdmGenerateValueDto generateValue = new IdmGenerateValueDto(DEFAULT_IDENTITY_ROLE_FORM_GENERATE_VALUE_ID);
			generateValue.setDtoType(IdmIdentityRoleDto.class.getCanonicalName());
			generateValue.setGeneratorType(IdentityRoleFormDefaultValueGenerator.class.getCanonicalName());
			generateValue.setSeq((short) 100);
			generateValue.setUnmodifiable(true);
			generateValueService.save(generateValue);
		}
		//
		return new DefaultEventResult<>(event, this);
	}
	
	@Override
	public int getOrder() {
		// before 0 => generate default eav values.
		return CoreEvent.DEFAULT_ORDER - 100;
	}
}
