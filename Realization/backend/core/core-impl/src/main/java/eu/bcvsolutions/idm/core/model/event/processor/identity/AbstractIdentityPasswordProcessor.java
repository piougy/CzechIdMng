package eu.bcvsolutions.idm.core.model.event.processor.identity;

import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.EventType;
import eu.bcvsolutions.idm.core.api.service.IdmPasswordService;

/**
 * Implementation of password change logic. Purpose of this abstraction is that password can be now changed from various
 * concrete implementations
 *
 * @author Peter Sourek <peter.sourek@bcvsolutions.eu>
 */
public abstract class AbstractIdentityPasswordProcessor extends CoreEventProcessor<IdmIdentityDto> {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(IdentityPasswordProcessor.class);
	private final IdmPasswordService passwordService;
	public static final String PROPERTY_PASSWORD_CHANGE_DTO = "idm:password-change-dto";


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
		if (passwordChangeDto.isAll() || passwordChangeDto.isIdm()) { // change identity's password
			savePassword(identity, passwordChangeDto);
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
