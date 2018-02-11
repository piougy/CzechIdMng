package eu.bcvsolutions.idm.core.model.event.processor.identity;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.DefaultResultModel;
import eu.bcvsolutions.idm.core.api.dto.IdmAccountDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.EventType;
import eu.bcvsolutions.idm.core.api.event.processor.IdentityProcessor;
import eu.bcvsolutions.idm.core.api.service.IdmPasswordService;

/**
 * Implementation of password change logic. Purpose of this abstraction is that password can be now changed from various
 * concrete implementations
 *
 * @author Peter Sourek <peter.sourek@bcvsolutions.eu>
 */
public abstract class AbstractIdentityPasswordProcessor 
		extends CoreEventProcessor<IdmIdentityDto> 
		implements IdentityProcessor {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractIdentityPasswordProcessor.class);
	public static final String PROPERTY_PASSWORD_CHANGE_DTO = "idm:password-change-dto";
	//
	private final eu.bcvsolutions.idm.core.api.service.IdmPasswordService passwordService;

	public AbstractIdentityPasswordProcessor(IdmPasswordService passwordService, EventType... types) {
		super(types);
		//
		Assert.notNull(passwordService);
		//
		this.passwordService = passwordService;
	}

	@Override
	public EventResult<IdmIdentityDto> process(EntityEvent<IdmIdentityDto> event) {
		IdmIdentityDto identity = event.getContent();
		PasswordChangeDto passwordChangeDto = (PasswordChangeDto) event.getProperties().get(PROPERTY_PASSWORD_CHANGE_DTO);
		Assert.notNull(passwordChangeDto);
		//
		if (passwordChangeDto.isIdm()) { // change identity's password
			savePassword(identity, passwordChangeDto);
			Map<String, Object> parameters = new LinkedHashMap<>();
			parameters.put("account", new IdmAccountDto(
					identity.getId(), 
					true, 
					identity.getUsername()));
			return new DefaultEventResult.Builder<>(event, this).setResult(
					new OperationResult.Builder(OperationState.EXECUTED)
						.setModel(new DefaultResultModel(CoreResultCode.PASSWORD_CHANGE_ACCOUNT_SUCCESS, parameters))
						.build()
					).build();
		}
		return new DefaultEventResult<>(event, this);
	}

	/**
	 * Saves identity's password
	 *
	 * @param identity
	 * @param newPassword
	 */
	protected void savePassword(IdmIdentityDto identity, PasswordChangeDto newPassword) {
		LOG.debug("Saving password for identity [{}].", identity.getUsername());
		this.passwordService.save(identity, newPassword);
	}

	/**
	 * Delete identity's password from confidential storage
	 *
	 * @param identity
	 */
	protected void deletePassword(IdmIdentityDto identity) {
		LOG.debug("Deleting password for identity [{}]. ", identity.getUsername());
		this.passwordService.delete(identity);
	}

	@Override
	public int getOrder() {
		return super.getOrder() + 100;
	}
}
