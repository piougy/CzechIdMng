package eu.bcvsolutions.idm.vs.event.processor;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.dto.SysConnectorKeyDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.event.SystemEvent.SystemEventType;
import eu.bcvsolutions.idm.core.api.event.AbstractEntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.ic.api.IcConnector;
import eu.bcvsolutions.idm.ic.api.IcConnectorInfo;
import eu.bcvsolutions.idm.vs.connector.api.VsVirtualConnector;
import eu.bcvsolutions.idm.vs.service.api.VsSystemService;

/**
 * Ensures update configuration of virtual system (form definition,
 * implementers). Is invoke after SysSystem save.
 * 
 * @author svandav
 *
 */
@Component("vsSystemSaveProcessor")
@Description("Ensures update configuration of virtual system (form definition, implementers). Is invoke after SysSystem save.")
public class SystemSaveProcessor extends AbstractEntityEventProcessor<SysSystemDto> {

	public static final String PROCESSOR_NAME = "system-save-processor";
	@Autowired
	private VsSystemService vsSystemService;

	public SystemSaveProcessor() {
		super(SystemEventType.UPDATE, SystemEventType.CREATE, SystemEventType.EAV_SAVE);
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public boolean conditional(EntityEvent<SysSystemDto> event) {
		// We want execute this processor only for virtual system
		SysSystemDto system = event.getContent();
		Assert.notNull(system);
		Assert.notNull(system.getId());
		
		if(system.getConnectorKey() == null) {
			return false;
		}
		
		String connectorKey = system.getConnectorKey().getFullName();
		IcConnectorInfo connectorInfo = vsSystemService.getConnectorInfo(connectorKey);
		if (connectorInfo == null) {
			return false;
		}

		IcConnector connectorInstance = vsSystemService.getConnectorInstance(system.getId(), connectorInfo);
		if (connectorInstance instanceof VsVirtualConnector) {
			return true;
		}
		return false;
	}

	@Override
	public EventResult<SysSystemDto> process(EntityEvent<SysSystemDto> event) {

		SysSystemDto system = event.getContent();
		Assert.notNull(system);
		UUID systemId = system.getId();
		Assert.notNull(systemId);
		SysConnectorKeyDto connectorKey = system.getConnectorKey();
		Assert.notNull(connectorKey);

		VsVirtualConnector virtualConnector = vsSystemService.getVirtualConnector(systemId, connectorKey.getFullName());
		Assert.notNull(virtualConnector);

		// Update configuration (implementers, definition)
		vsSystemService.updateSystemConfiguration(virtualConnector.getConfiguration(), virtualConnector.getClass());

		return new DefaultEventResult<>(event, this);
	}

	@Override
	public int getOrder() {
		// After system saved in the core module
		return CoreEvent.DEFAULT_ORDER + 10;
	}

	@Override
	public boolean isDisableable() {
		return true;
	}
}