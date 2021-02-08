package eu.bcvsolutions.idm.acc.event.processor;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.domain.ProvisioningEventType;
import eu.bcvsolutions.idm.acc.dto.SysBlockedOperationDto;
import eu.bcvsolutions.idm.acc.dto.SysConnectorKeyDto;
import eu.bcvsolutions.idm.acc.dto.SysConnectorServerDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.event.SystemEvent.SystemEventType;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningBreakConfigService;
import eu.bcvsolutions.idm.acc.service.api.SysRemoteServerService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.service.ConfidentialStorage;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;

/**
 * Save target system, catch event UPDATE and CREATE.
 * 
 * @author svandav
 *
 */
@Component
@Description("Persists target system.")
public class SystemSaveProcessor extends CoreEventProcessor<SysSystemDto> {

	public static final String PROCESSOR_NAME = "system-save-processor";
	private final SysSystemService service;
	private final ConfidentialStorage confidentialStorage;
	private final SysProvisioningBreakConfigService provisioningBreakConfigService;
	//
	@Autowired private LookupService lookupService;
	@Autowired private SysRemoteServerService remoteServerService;
	
	@Autowired
	public SystemSaveProcessor(
			SysSystemService service,
			ConfidentialStorage confidentialStorage,
			SysProvisioningBreakConfigService provisioningBreakConfigService) {
		super(SystemEventType.UPDATE, SystemEventType.CREATE);
		//
		Assert.notNull(service, "Service is required.");
		Assert.notNull(confidentialStorage, "Confidential storage is required.");
		Assert.notNull(provisioningBreakConfigService, "Service is required.");
		//
		this.service = service;
		this.confidentialStorage = confidentialStorage;
		this.provisioningBreakConfigService = provisioningBreakConfigService;
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<SysSystemDto> process(EntityEvent<SysSystemDto> event) {
		SysSystemDto dto = event.getContent();
		SysSystemDto previousSystem = event.getOriginalSource();
		// resolve connector server
		UUID remoteServerId = dto.getRemoteServer();
		if (remoteServerId != null && (previousSystem == null || !remoteServerId.equals(previousSystem.getRemoteServer()))) {
			// fill remote system to system connector server (backward compatibility)
			SysConnectorServerDto remoteServer = lookupService.lookupEmbeddedDto(dto, SysSystemDto.PROPERTY_REMOTE_SERVER);
			dto.setConnectorServer(new SysConnectorServerDto(remoteServer));
			dto.getConnectorServer().setPassword(remoteServerService.getPassword(remoteServerId));
		} else if (dto.getConnectorServer() == null) {
			dto.setConnectorServer(new SysConnectorServerDto());
		}
		// create default connector key
		if (dto.getConnectorKey() == null) {
			dto.setConnectorKey(new SysConnectorKeyDto());
		}
		// create default blocked operations
		if (dto.getBlockedOperation() == null) {
			dto.setBlockedOperation(new SysBlockedOperationDto());
		}
		//
		if (previousSystem != null) {
			// Check if is connector changed
			if (!dto.getConnectorKey().equals(previousSystem.getConnectorKey())) {
				// If is connector changed, we set virtual to false. (Virtual
				// connectors set this attribute on true by themselves)
				dto.setVirtual(false);
			}
			// check blocked provisioning operation and clear provisioning break cache
			clearProvisionignBreakCache(dto, previousSystem);
		}
		SysSystemDto newSystem = service.saveInternal(dto);
		event.setContent(newSystem);
		//
		// after save entity save password to confidential storage
		// save password from remote connector server to confidential storage
		if (dto.getConnectorServer().getPassword() != null) {
			// save for newSystem
			confidentialStorage.save(newSystem.getId(), SysSystem.class, SysSystemService.REMOTE_SERVER_PASSWORD,
					dto.getConnectorServer().getPassword().asString());
			//
			// set asterix
			newSystem.getConnectorServer().setPassword(new GuardedString(GuardedString.SECRED_PROXY_STRING));
		}
		//
		return new DefaultEventResult<>(event, this);
	}
	
	/**
	 * Method check different between new and old system if some of blocked
	 * attribute change clear for them provisioning break configuration.
	 * 
	 * @param newSystem
	 * @param oldSystem
	 */
	private void clearProvisionignBreakCache(SysSystemDto newSystem, SysSystemDto oldSystem) {
		if (oldSystem == null) {
			return;
		}
		// check if attribute operation disable change from false to true - then clear cache
		if (!newSystem.getBlockedOperation().getCreateOperation()
				&& oldSystem.getBlockedOperation().getCreateOperation()) {
			provisioningBreakConfigService.clearCache(newSystem.getId(), ProvisioningEventType.CREATE);
		}
		if (!newSystem.getBlockedOperation().getUpdateOperation()
				&& oldSystem.getBlockedOperation().getUpdateOperation()) {
			provisioningBreakConfigService.clearCache(newSystem.getId(), ProvisioningEventType.UPDATE);
		}
		if (!newSystem.getBlockedOperation().getDeleteOperation()
				&& oldSystem.getBlockedOperation().getDeleteOperation()) {
			provisioningBreakConfigService.clearCache(newSystem.getId(), ProvisioningEventType.DELETE);
		}
	}
}