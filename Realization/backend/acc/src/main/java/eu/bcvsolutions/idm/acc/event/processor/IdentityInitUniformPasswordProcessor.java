package eu.bcvsolutions.idm.acc.event.processor;

import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import eu.bcvsolutions.idm.acc.scheduler.task.impl.SynchronizationSchedulableTaskExecutor;
import eu.bcvsolutions.idm.core.api.domain.IdentityState;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.IdentityProcessor;
import eu.bcvsolutions.idm.core.model.event.IdentityEvent.IdentityEventType;
import eu.bcvsolutions.idm.core.scheduler.api.dto.filter.IdmLongRunningTaskFilter;
import eu.bcvsolutions.idm.core.scheduler.api.service.IdmLongRunningTaskService;
import eu.bcvsolutions.idm.core.security.api.service.UniformPasswordManager;

/**
 * Init uniform password for identity.
 *
 * @author Vít Švanda
 * @since 11.0.0
 */
@Component(IdentityInitUniformPasswordProcessor.PROCESSOR_NAME)
@Description("Init uniform password for identity.")
public class IdentityInitUniformPasswordProcessor
		extends CoreEventProcessor<IdmIdentityDto>
		implements IdentityProcessor {

	public static final String PROCESSOR_NAME = "acc-identity-init-common-password-processor";

	@Autowired
	private UniformPasswordManager uniformPasswordManager;
	@Autowired
	private IdmLongRunningTaskService longRunningTaskService;

	public IdentityInitUniformPasswordProcessor() {
		super(IdentityEventType.UPDATE);
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmIdentityDto> process(EntityEvent<IdmIdentityDto> event) {
		IdmIdentityDto previousIdentity = event.getOriginalSource();
		IdmIdentityDto newIdentity = event.getContent();
		if (stateStarting(previousIdentity, newIdentity)) {
			uniformPasswordManager.createEntityState(newIdentity);

			return new DefaultEventResult<>(event, this);
		}
		return new DefaultEventResult<>(event, this);
	}

	@Override
	public boolean conditional(EntityEvent<IdmIdentityDto> event) {
		UUID identityId = event.getContent().getId();
		UUID transactionId = event.getContent().getTransactionId();
		if( identityId == null || transactionId == null) {
			return false;
		}

		IdmLongRunningTaskFilter longRunningTaskFilter = new IdmLongRunningTaskFilter();
		longRunningTaskFilter.setTransactionId(transactionId);
		longRunningTaskFilter.setRunning(true);
		// TODO: Check only for contract sync?
		longRunningTaskFilter.setTaskType(SynchronizationSchedulableTaskExecutor.class.getCanonicalName());
		long count = longRunningTaskService.count(longRunningTaskFilter);

		return super.conditional(event)
				&& count > 0;
	}

	/**
	 * Return true, when identity starts to be valid or future contract.
	 */
	private boolean stateStarting(IdmIdentityDto previousIdentity, IdmIdentityDto newIdentity) {
		return (previousIdentity.getState() == IdentityState.CREATED
				|| previousIdentity.getState() == IdentityState.NO_CONTRACT
				|| previousIdentity.getState() == IdentityState.LEFT)
				&& (newIdentity.getState() == IdentityState.VALID || newIdentity.getState() == IdentityState.FUTURE_CONTRACT);
	}

	/**
	 * After update
	 */
	@Override
	public int getOrder() {
		return CoreEvent.DEFAULT_ORDER + 150;
	}
}
