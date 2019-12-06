package eu.bcvsolutions.idm.core.generator.treenode;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.IdmGenerateValueDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeNodeDto;
import eu.bcvsolutions.idm.core.api.generator.AbstractValueGenerator;

/**
 * Generate simple code for {@link IdmTreeNodeDto} from name
 *
 * @author Ondrej Kopr
 * @since 9.6.2
 */
@Component(TreeNodeCodeGenerator.GENERATOR_NAME)
@Description("Generate simple code from name.")
public class TreeNodeCodeGenerator extends AbstractValueGenerator<IdmTreeNodeDto> {

	public static final String GENERATOR_NAME = "core-tree-node-code-value-generator";

	@Override
	public String getName() {
		return GENERATOR_NAME;
	}

	@Override
	public IdmTreeNodeDto generate(IdmTreeNodeDto dto, IdmGenerateValueDto valueGenerator) {
		Assert.notNull(dto, "DTO is required.");
		Assert.notNull(valueGenerator, "Value generator is required.");

		// Code is based from name
		String code = dto.getName();

		if (code == null) {
			return dto;
		}
	
		// if exists email and configuration doesn't allow regenerate return dto
		if (!valueGenerator.isRegenerateValue() && StringUtils.isNotEmpty(dto.getCode())) {
			return dto;
		}
		
		code = StringUtils.stripAccents(code);
		code = StringUtils.deleteWhitespace(code);
		code = code.toLowerCase();

		dto.setCode(code);
		return dto;
	}
}
