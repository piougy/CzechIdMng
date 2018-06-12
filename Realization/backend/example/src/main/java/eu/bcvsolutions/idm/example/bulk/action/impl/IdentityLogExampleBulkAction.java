package eu.bcvsolutions.idm.example.bulk.action.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.bulk.action.AbstractBulkAction;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityFilter;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.example.ExampleModuleDescriptor;

/**
 * Example bulk action. The action iterate over identities and log their
 * username.
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Enabled(ExampleModuleDescriptor.MODULE_ID)
@Component("identityLogExampleBulkAction")
@Description("Log idetity to info/warn log in bulk action.")
public class IdentityLogExampleBulkAction extends AbstractBulkAction<IdmIdentityDto, IdmIdentityFilter> {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(IdentityLogExampleBulkAction.class);

	public static final String BULK_ACTION_NAME = "identity-log-example-bulk-action";
	
	private static final String EXAMPLE_TEXT_CODE = "exampleText";
	private static final String EXAMPLE_WARNING_CODE = "exampleWarning";
	private static final String EXAMPLE_SLEEP_CODE = "sleep";
	private static final String EXAMPLE_FAIL_EVERY_N = "failEveryN";

	private int failCount = 1;
	
	@Autowired
	private IdmIdentityService identityService;
	
	@Override
	protected OperationResult processDto(IdmIdentityDto dto) {
		String textField = getTextField();
		Integer failEveryN = getFailEveryN();
		if (isWarning()) {
			LOG.warn("WARNING. Log identity with username: {}. User input: {}.", dto.getUsername(), textField);
		} else {
			LOG.info("Log identity with username: {}. User input: {}.", dto.getUsername(), textField);
		}
		if (getSleep() != null) {
			try {
				Thread.sleep(getSleep());
			} catch (InterruptedException e) {
				LOG.error("Isn't possible sleep thread.", e);
				return new OperationResult.Builder(OperationState.EXCEPTION).setCause(e).build();
			}
		}
		if (failEveryN != null && failEveryN == failCount) {
			failCount = 1;
			return new OperationResult.Builder(OperationState.NOT_EXECUTED).build();
		}
		failCount++;
		return new OperationResult.Builder(OperationState.EXECUTED).build();
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
		attributes.add(getSleepAttribute());
		attributes.add(getFailEveryNAttribute());
		return attributes;
	}

	/**
	 * Get {@link IdmFormAttributeDto} for required text field
	 *
	 * @return
	 */
	protected IdmFormAttributeDto getTextFieldAttribute() {
		IdmFormAttributeDto textField = new IdmFormAttributeDto(
				EXAMPLE_TEXT_CODE, 
				EXAMPLE_TEXT_CODE, 
				PersistentType.TEXT);
		return textField;
	}
	
	/**
	 * Get {@link IdmFormAttributeDto} for fail every n entity
	 *
	 * @return
	 */
	protected IdmFormAttributeDto getFailEveryNAttribute() {
		IdmFormAttributeDto failEveryN = new IdmFormAttributeDto(
				EXAMPLE_FAIL_EVERY_N,
				EXAMPLE_FAIL_EVERY_N,
				PersistentType.INT);
		return failEveryN;
	}

	/**
	 * Get {@link IdmFormAttributeDto} for log into warning
	 *
	 * @return
	 */
	protected IdmFormAttributeDto getWarningAttribute() {
		IdmFormAttributeDto warning = new IdmFormAttributeDto(
				EXAMPLE_WARNING_CODE, 
				EXAMPLE_WARNING_CODE, 
				PersistentType.BOOLEAN);
		return warning;
	}

	/**
	 * Get {@link IdmFormAttributeDto} for sleep attribute
	 *
	 * @return
	 */
	protected IdmFormAttributeDto getSleepAttribute() {
		IdmFormAttributeDto sleep = new IdmFormAttributeDto(
				EXAMPLE_SLEEP_CODE, 
				EXAMPLE_SLEEP_CODE, 
				PersistentType.INT);
		return sleep;
	}

	/**
	 * Get required text field
	 *
	 * @return
	 */
	private String getTextField() {
		return getParameterConverter().toString(getProperties(), EXAMPLE_TEXT_CODE);
	}

	/**
	 * Get sleep
	 *
	 * @return
	 */
	private Integer getSleep() {
		String sleepAsString = getParameterConverter().toString(getProperties(), EXAMPLE_SLEEP_CODE);
		if (sleepAsString == null) {
			return null;
		}
		return Integer.valueOf(sleepAsString);
	}

	/**
	 * Get fail every N
	 *
	 * @return
	 */
	private Integer getFailEveryN() {
		String integerAsString = getParameterConverter().toString(getProperties(), EXAMPLE_FAIL_EVERY_N);
		if (integerAsString == null) {
			return null;
		}
		return Integer.valueOf(integerAsString);
	}

	/**
	 * Is warning
	 *
	 * @return
	 */
	private boolean isWarning() {
		Boolean warning = this.getParameterConverter().toBoolean(getProperties(), EXAMPLE_WARNING_CODE);
		return warning != null ? warning.booleanValue() : false;
	}

	@Override
	protected BasePermission[] getPermissionForEntity() {
		BasePermission[] permissions= {
				IdmBasePermission.READ
		};
		return permissions;
	}

	@Override
	public int getOrder() {
		return super.getOrder() + 10000;
	}

	@Override
	public ReadWriteDtoService<IdmIdentityDto, IdmIdentityFilter> getService() {
		return identityService;
	}
}
