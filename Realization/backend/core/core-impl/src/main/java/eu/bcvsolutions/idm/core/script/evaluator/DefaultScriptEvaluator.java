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
	public boolean supports(IdmScriptCategory arg0) {
		return arg0 == IdmScriptCategory.DEFAULT;
	}

	@Override
	public String generateTemplate(IdmScriptDto script) {
		StringBuilder example = new StringBuilder();
		example.append("// Inserted script: " + script.getCode() + "\n");
		example.append("/* Description:\n");
		example.append(script.getDescription());
		example.append("\n");
		example.append("*/\n");
		example.append(SCRIPT_EVALUATOR + ".evaluate(\n");
		example.append("    " + SCRIPT_EVALUATOR + ".newBuilder()\n");
		example.append("        .setScriptCode('" + script.getCode() + "')\n");
		example.append("        .addParameter('" + SCRIPT_EVALUATOR + "', " + SCRIPT_EVALUATOR + ")\n");
		example.append("	.build());\n");
		return example.toString();
	}
}
