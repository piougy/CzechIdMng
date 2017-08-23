package eu.bcvsolutions.idm.core.script.evaluator;

import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.core.api.domain.IdmScriptCategory;
import eu.bcvsolutions.idm.core.api.dto.IdmScriptDto;

/**
 * Default script evaluator for {@link IdmScriptCategory} DEFAULT
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Service("defaultScriptEvaluator")
public class DefaultScriptEvaluator extends AbstractScriptEvaluator {

	@Override
	public boolean supports(IdmScriptCategory category) {
		return category == IdmScriptCategory.DEFAULT;
	}

	@Override
	public String generateTemplate(IdmScriptDto script) {
		StringBuilder example = new StringBuilder();
		example.append("// Inserted script: ");
		example.append(script.getCode());
		example.append('\n');
		//
		example.append("/* Description:\n");
		example.append(script.getDescription());
		example.append('\n');
		//
		example.append("*/\n");
		example.append(SCRIPT_EVALUATOR);
		example.append(".evaluate(\n");
		example.append("    ");
		example.append(SCRIPT_EVALUATOR);
		example.append(".newBuilder()\n");
		//
		example.append("        .setScriptCode('");
		example.append(script.getCode());
		example.append("')\n");
		//
		example.append("        .addParameter('");
		example.append(SCRIPT_EVALUATOR);
		example.append("', ");
		example.append(SCRIPT_EVALUATOR);
		example.append(")\n");
		//
		example.append("	.build());\n");
		return example.toString();
	}
}
