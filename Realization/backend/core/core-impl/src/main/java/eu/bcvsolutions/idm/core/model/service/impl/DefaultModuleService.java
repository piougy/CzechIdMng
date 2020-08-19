package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.plugin.core.Plugin;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.domain.ModuleDescriptor;
import eu.bcvsolutions.idm.core.api.dto.ModuleDescriptorDto;
import eu.bcvsolutions.idm.core.api.event.ModuleDescriptorEvent;
import eu.bcvsolutions.idm.core.api.event.ModuleDescriptorEvent.ModuleDescriptorEventType;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.ModuleService;
import eu.bcvsolutions.idm.core.security.api.domain.GroupPermission;

/**
 * Default implementation for {@link ModuleDescriptor} administrative.
 * 
 * @author Radek Tomiška
 *
 * @see ModuleDescriptor
 * @see Plugin
 * @see PluginRegistry
 */
public class DefaultModuleService implements ModuleService {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultModuleService.class);
	
	private final PluginRegistry<ModuleDescriptor, String> moduleDescriptorRegistry;
	private final ConfigurationService configurationService;
	//
	@Autowired private ModelMapper mapper;
	@Autowired private EntityEventManager entityEventManager;

	public DefaultModuleService(
			PluginRegistry<ModuleDescriptor, String> moduleDescriptorRegistry,
			ConfigurationService configurationService) {
		Assert.notNull(moduleDescriptorRegistry, "Module registry is required!");
		Assert.notNull(configurationService, "ConfigurationService is required!");
		//
		this.moduleDescriptorRegistry = moduleDescriptorRegistry;
		this.configurationService = configurationService;
	}

	@Override
	public List<ModuleDescriptor> getInstalledModules() {
		List<ModuleDescriptor> registeredModules = new ArrayList<>();
		moduleDescriptorRegistry.forEach(moduleDescriptor -> {
			registeredModules.add(moduleDescriptor);
		});
		return Collections.unmodifiableList(registeredModules);
	}
	
	public ModuleDescriptor getModule(String moduleId) {
		return moduleDescriptorRegistry.getPluginFor(moduleId);
	}

	@Override
	public List<ModuleDescriptor> getEnabledModules() {
		return Collections.unmodifiableList( //
				getInstalledModules() //
				.stream() //
				.filter(moduleDescriptor -> isEnabled(moduleDescriptor)) //
				.collect(Collectors.toList()));
	}

	@Override
	public boolean isEnabled(String moduleId) {
		return isEnabled(moduleDescriptorRegistry.getPluginFor(moduleId));
	}
	
	@Override
	public boolean isEnabled(ModuleDescriptor moduleDescriptor) {
		// if module not exists, then is disabled by default
		if (moduleDescriptor == null) {
			return false;
		}
		if (!moduleDescriptor.isDisableable()) {
			return true;
		}
		return configurationService.getBooleanValue(
				getModuleConfigurationProperty(moduleDescriptor.getId(), ConfigurationService.PROPERTY_ENABLED), false);
	}

	@Override
	public void enable(String moduleId) {
		setEnabled(moduleId, true);
	}

	@Override
	public void disable(String moduleId) {
		setEnabled(moduleId, false);
	}

	@Override
	public void setEnabled(String moduleId, boolean enabled) {
		ModuleDescriptor moduleDescriptor = moduleDescriptorRegistry.getPluginFor(moduleId);
		ModuleDescriptorDto moduleDescriptorDto = null;
		if (moduleDescriptor == null) {
			LOG.info("Frontend module [{}] will be enabled [{}].", moduleId, enabled);
			// FE module - create basic descriptor
			moduleDescriptorDto = new ModuleDescriptorDto(moduleId);
			moduleDescriptorDto.setDisableable(true);
			moduleDescriptorDto.setDisabled(!configurationService.getBooleanValue(getModuleConfigurationProperty(moduleId, ConfigurationService.PROPERTY_ENABLED), false));
		} else {
			LOG.info("Backend module [{}] will be enabled [{}].", moduleId, enabled);
			//
			moduleDescriptorDto = toDto(moduleDescriptor);
		}
		//
		ModuleDescriptorEvent event = new ModuleDescriptorEvent(
				enabled ? ModuleDescriptorEventType.ENABLE : ModuleDescriptorEventType.DISABLE,
				moduleDescriptorDto
		);
		entityEventManager.process(event);
	}
	
	@Override
	public List<GroupPermission> getAvailablePermissions() {
		List<GroupPermission> permissions = new ArrayList<>();
		getEnabledModules().forEach(moduleDescriptor -> {
			permissions.addAll(moduleDescriptor.getPermissions());
		});
		LOG.debug("Loaded available groupPermissions [size:{}]", permissions.size());
		return Collections.unmodifiableList(permissions);
	}
	
	@Override
	public List<GroupPermission> getAllPermissions() {
		List<GroupPermission> permissions = new ArrayList<>();
		getInstalledModules().forEach(moduleDescriptor -> {
			permissions.addAll(moduleDescriptor.getPermissions());
		});
		LOG.debug("Loaded available groupPermissions [size:{}]", permissions.size());
		return Collections.unmodifiableList(permissions);
	}

	/**
	 * Returns module property by {@link eu.bcvsolutions.idm.core.api.service.IdmConfigurationService} conventions.
	 * 
	 * @param moduleId
	 * @param property
	 * @return
	 */
	@Override
	public String getModuleConfigurationProperty(String moduleId, String property) {
		return ConfigurationService.IDM_PUBLIC_PROPERTY_PREFIX //
				+ moduleId + ConfigurationService.PROPERTY_SEPARATOR + property;
	}
	
	/**
	 * FIXME: DRY ModuleController#toResource => redesign to work with dto?
	 */
	private ModuleDescriptorDto toDto(ModuleDescriptor moduleDescriptor) {
		ModuleDescriptorDto dto = mapper.map(moduleDescriptor,  ModuleDescriptorDto.class);
		//
		dto.setId(moduleDescriptor.getId());
		dto.setDisabled(!isEnabled(moduleDescriptor));
		return dto;
	}

}
