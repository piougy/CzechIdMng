package eu.bcvsolutions.idm.example.bulk.action.impl;

import java.util.List;

import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.bulk.action.impl.AbstractIdentityBulkAction;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;

/**
 * Example bulk action. The action iterate over identities and log their
 * username.
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Component("identityLogExampleBulkAction")
@Description("Log idetity to info/warn log in bulk action.")
public class IdentityLogExampleBulkAction extends AbstractIdentityBulkAction {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(IdentityLogExampleBulkAction.class);

	public static final String BULK_ACTION_NAME = "identity-log-example-bulk-action";
	
	private static final String EXAMPLE_TEXT_CODE = "exampleText";
	private static final String EXAMPLE_WARNING_CODE = "exampleWarning";

	@Override
	protected void processIdentity(IdmIdentityDto dto) {
		String textField = getTextField();
		if (isWarning()) {
			LOG.warn("WARNING. Log identity with username: {}. User input: {}.", dto.getUsername(), textField);
		} else {
			LOG.info("Log identity with username: {}. User input: {}.", dto.getUsername(), textField);
		}
	}

	@Override
	public String getName() {
		return BULK_ACTION_NAME;
	}

	@Override
	public List<IdmFormAttributeDto> getFormAttributes() {
		List<IdmFormAttributeDto> attributes = super.getFormAttributes();
		attributes.add(getTextFieldAttribute());
		attributes.add(getWarningAttribute());
		return attributes;
	}

	protected IdmFormAttributeDto getTextFieldAttribute() {
		IdmFormAttributeDto textField = new IdmFormAttributeDto(
				EXAMPLE_TEXT_CODE, 
				EXAMPLE_TEXT_CODE, 
				PersistentType.TEXT);
		textField.setRequired(true);
		return textField;
	}
	
	protected IdmFormAttributeDto getWarningAttribute() {
		IdmFormAttributeDto warning = new IdmFormAttributeDto(
				EXAMPLE_WARNING_CODE, 
				EXAMPLE_WARNING_CODE, 
				PersistentType.BOOLEAN);
		return warning;
	}
	
	private String getTextField() {
		return getParameterConverter().toString(getProperties(), EXAMPLE_TEXT_CODE);
	}
	
	private boolean isWarning() {
		Boolean warning = this.getParameterConverter().toBoolean(getProperties(), EXAMPLE_WARNING_CODE);
		return warning != null ? warning.booleanValue() : false;
	}
}
