package eu.bcvsolutions.idm.acc.service.impl.mock;

import eu.bcvsolutions.idm.acc.event.processor.IdentityInitCommonPasswordProcessor;
import eu.bcvsolutions.idm.core.api.domain.TransactionContextHolder;
import eu.bcvsolutions.idm.core.api.dto.IdmEntityStateDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.security.api.service.CommonPasswordManager;
import java.util.UUID;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

/**
 * Mock test - Init common password for identity. Used in the DefaultCommonPasswordManagerIntegrationTest.
 *
 * @author Vít Švanda
 * @since 11.0.0
 */
@Component
@Description("Mock test - Init common password for identity. Used in the DefaultCommonPasswordManagerIntegrationTest.")
public class MockIdentityInitCommonPasswordProcessor extends IdentityInitCommonPasswordProcessor {

	public static final String PROCESSOR_NAME = "test-mock-acc-identity-init-common-password-processor";

	@Autowired
	private CommonPasswordManager commonPasswordManager;
	private UUID enableTestForTransaction;

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmIdentityDto> process(EntityEvent<IdmIdentityDto> event) {
		IdmIdentityDto identityDto = event.getContent();
		IdmEntityStateDto entityStateDto = commonPasswordManager.getEntityState(identityDto.getId(), IdmIdentityDto.class, getEnableTestForTransaction());
		Assert.assertNotNull(entityStateDto);
		
		return new DefaultEventResult<>(event, this);
	}

	public UUID getEnableTestForTransaction() {
		return enableTestForTransaction;
	}

	public void setEnableTestForTransaction(UUID enableTestForTransaction) {
		this.enableTestForTransaction = enableTestForTransaction;
	}

	@Override
	public boolean conditional(EntityEvent<IdmIdentityDto> event) {
		// Same conditional as in IdentityInitCommonPasswordProcessor + manual enable from test.
		UUID transactionId = TransactionContextHolder.getContext().getTransactionId();
		return getEnableTestForTransaction() != null && getEnableTestForTransaction().equals(transactionId) && super.conditional(event);
	}

	@Override
	// One after original IdentityInitCommonPasswordProcessor.
	public int getOrder() {
		return super.getOrder() + 1;
	}
}
