package eu.bcvsolutions.idm.acc.event.processor.module;

import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.SerializationException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.acc.dto.SysConnectorServerDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemFilter;
import eu.bcvsolutions.idm.acc.entity.SysRemoteServer;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.service.api.SysRemoteServerService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.dto.ModuleDescriptorDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.AbstractInitApplicationProcessor;
import eu.bcvsolutions.idm.core.api.service.ConfidentialStorage;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;

/**
 * Init remote server agenda from remote systems connectors (versions <= 10.7.x).
 * Migrate configured remote servers from systems to standalone agenda.
 * 
 * @author Radek Tomiška
 * @since 10.8.0
 */
@Component(AccInitRemoteServerProcessor.PROCESSOR_NAME)
@Description("Cancel synchronizations after server is restarted.")
public class AccInitRemoteServerProcessor extends AbstractInitApplicationProcessor {
	
	public static final String PROCESSOR_NAME = "acc-init-remote-server-processor";
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AccInitRemoteServerProcessor.class);
	//
	@Autowired private SysSystemService systemService;
	@Autowired private SysRemoteServerService remoteServerService;
	@Autowired private ConfidentialStorage confidentialStorage;
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
	
	@Override
	public EventResult<ModuleDescriptorDto> process(EntityEvent<ModuleDescriptorDto> event) {
		// all remote systems => will be two at max
		List<SysConnectorServerDto> remoteServers = Lists.newArrayList(remoteServerService.find(null).getContent());
		// fill password
		remoteServers.forEach(remoteServer -> {
			remoteServer.setPassword(confidentialStorage.getGuardedString(
					remoteServer.getId(), 
					SysRemoteServer.class, 
					SysSystemService.REMOTE_SERVER_PASSWORD)
			);
		});
		//
		// find all systems with remote flag and empty related remoteServer 
		SysSystemFilter systemFilter = new SysSystemFilter();
		systemFilter.setRemote(Boolean.TRUE);
		systemService
			.find(systemFilter, null)
			.stream()
			.filter(system -> Objects.isNull(system.getRemoteServer())) // remote server is not referenced => old definition with remote flag
			.filter(system -> {
				// remote server is properly filled
				// cannot be filled from frontend, but just for sure
				SysConnectorServerDto connectorServer = system.getConnectorServer();
				if (connectorServer == null) {
					return false;
				}
				return StringUtils.isNotBlank(connectorServer.getHost());
			})
			.forEach(system -> {
				SysConnectorServerDto systemConnector = system.getConnectorServer();
				try {
					systemConnector.setPassword(confidentialStorage.getGuardedString(
							system.getId(),
							SysSystem.class, 
							SysSystemService.REMOTE_SERVER_PASSWORD
					));
				} catch (SerializationException ex) {
					LOG.error("Password for configured system [{}] is broken, will be ignored.", system.getCode());
				}
				// try to find remote system by all fields
				SysConnectorServerDto remoteServer = remoteServers
						.stream()
						.filter(r -> {
							return StringUtils.equals(r.getHost(), systemConnector.getHost())
									&& Integer.compare(r.getPort(), systemConnector.getPort()) == 0
									&& BooleanUtils.compare(r.isUseSsl(), systemConnector.isUseSsl()) == 0
									&& Integer.compare(r.getTimeout(), systemConnector.getTimeout()) == 0
									&& 
									(
										systemConnector.getPassword() == null // password is broken, e.g. when confidential storage was dropped
										|| 
										StringUtils.equals(r.getPassword().asString(), systemConnector.getPassword().asString())
									);
						})
						.findFirst()
						.orElse(null);
				//
				if (remoteServer != null) {
					LOG.info("Remote server [{}] will be used for configured system [{}].", 
							remoteServer.getFullServerName(), system.getCode());
					system.setRemoteServer(remoteServer.getId());
					systemService.save(system);
				} else {
					String systemCode = system.getCode();
					systemConnector.setDescription(
							String.format("Created automatically by upgrade to CzechIdM version 10.8.0 by target system [%s].", systemCode)
					);
					GuardedString password = systemConnector.getPassword();
					remoteServer = remoteServerService.save(systemConnector);
					remoteServer.setPassword(password); // preserve password 
					remoteServers.add(remoteServer);
					system.setRemoteServer(remoteServer.getId());
					systemService.save(system);
					LOG.info("New remote server [{}] was created and used for configured system [{}].", 
							remoteServer.getFullServerName(), systemCode);
				}
			});
		//
		// Turn off for next start => already processed
		getConfigurationService().setBooleanValue(getConfigurationPropertyName(ConfigurationService.PROPERTY_ENABLED), false);
		//
		return new DefaultEventResult<>(event, this);
	}
	
	@Override
	public int getOrder() {
		// before init sync with systems usage
		return CoreEvent.DEFAULT_ORDER - 10020;
	}
}
