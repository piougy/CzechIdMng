package eu.bcvsolutions.idm.core.generator.role;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.domain.ConceptRoleRequestOperation;
import eu.bcvsolutions.idm.core.api.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmGenerateValueDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.api.service.thin.IdmIdentityRoleThinService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormInstanceDto;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole_;

/**
 * Generator set default values to role concept eavs
 *
 * @author Ondrej Kopr
 * @since 9.4.0
 */
@Component(ConceptRoleRequestFormDefaultValueGenerator.GENERATOR_NAME)
@Description("Generator set default values to role concept EAV.")
public class ConceptRoleRequestFormDefaultValueGenerator
		extends AbstractRoleParametrizationFormDefaultValueGenerator<IdmConceptRoleRequestDto> {

	public static final String GENERATOR_NAME = "core-concept-role-request-form-default-value-generator";

	@Autowired
	private IdmRoleService roleService;
	@Autowired
	private IdmIdentityRoleThinService identityRoleService;

	@Override
	public String getName() {
		return GENERATOR_NAME;
	}

	@Override
	public IdmConceptRoleRequestDto generate(IdmConceptRoleRequestDto dto, IdmGenerateValueDto valueGenerator) {
		Assert.notNull(dto, "DTO is required.");
		Assert.notNull(valueGenerator, "Value generator is required.");

		// For remove concept is useless generate default values
		if (ConceptRoleRequestOperation.REMOVE == dto.getOperation()) {
			return dto;
		}

		UUID roleId = dto.getRole();
		IdmRoleDto roleDto = null;

		// Role id may not be set
		if (roleId == null) {
			IdmIdentityRoleDto identityRoleDto = identityRoleService.get(dto.getIdentityRole());
			roleDto = DtoUtils.getEmbedded(identityRoleDto, IdmIdentityRole_.role, IdmRoleDto.class);
		} else {
			roleDto = roleService.get(roleId);
		}

		// Defensive behavior if role doesn't exist
		if (roleDto == null) {
			return dto;
		}

		// TODO: now exists only one form instance for identity roles and conpcets
		List<IdmFormInstanceDto> existingEavs = dto.getEavs();
		IdmFormInstanceDto eavs = getDefaultValuesByRoleDefinition(roleDto, valueGenerator,
				existingEavs.isEmpty() ? null : existingEavs.get(0), dto);
		if (eavs == null) {
			return dto;
		}
		dto.getEavs().clear();
		dto.getEavs().add(eavs);
		return dto;
	}
}
