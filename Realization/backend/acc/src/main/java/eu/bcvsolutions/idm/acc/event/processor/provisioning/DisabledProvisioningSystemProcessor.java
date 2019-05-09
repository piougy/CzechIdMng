package eu.bcvsolutions.idm.acc.event.processor.provisioning;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.entity.AccAccount_;
import eu.bcvsolutions.idm.acc.event.ProvisioningEvent.ProvisioningEventType;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.dto.DefaultResultModel;
import eu.bcvsolutions.idm.core.api.dto.ResultModel;
import eu.bcvsolutions.idm.core.api.event.AbstractEntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;

/**
 * Check disabled provisioning on the system before provisioning process starts for given account.
 * 
 * @author Radek Tomiška
 * @since 9.6.0
 */
@Component(DisabledProvisioningSystemProcessor.PROCESSOR_NAME)
@Description("Check disabled provisioning on the system before provisioning process starts for given account.")
public class DisabledProvisioningSystemProcessor extends AbstractEntityEventProcessor<AccAccountDto> {

	public static final String PROCESSOR_NAME = "acc-disabled-provisioning-system-processor";
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DisabledProvisioningSystemProcessor.class);

	@Autowired private SysSystemService systemService;
	
	public DisabledProvisioningSystemProcessor() {
		super(ProvisioningEventType.START);
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<AccAccountDto> process(EntityEvent<AccAccountDto> event) {
		AccAccountDto account = event.getContent();
		//
		SysSystemDto system = DtoUtils.getEmbedded(account, AccAccount_.system, (SysSystemDto) null);
		if (system == null) {
			system = systemService.get(account.getSystem());
		}
		boolean closed = false;
		if (system.isDisabledProvisioning()) {
			ResultModel resultModel = new DefaultResultModel(AccResultCode.PROVISIONING_SYSTEM_DISABLED_PROVISIONING, 
					ImmutableMap.of("name", account.getRealUid(), "system", system.getName()));
			LOG.info(resultModel.toString());
			//
			closed = true;
		}
		//
		return new DefaultEventResult<>(event, this, closed);
	}

	@Override
	public int getOrder() {
		// before all
		return -5000;
	}
}