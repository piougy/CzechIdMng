package eu.bcvsolutions.idm.core.rest.impl;

import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.ModuleDescriptor;
import eu.bcvsolutions.idm.core.api.dto.ModuleDescriptorDto;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.BaseEntityController;
import eu.bcvsolutions.idm.core.api.rest.domain.RequestResourceResolver;
import eu.bcvsolutions.idm.core.api.service.ModuleService;
import eu.bcvsolutions.idm.core.model.domain.IdmGroupPermission;

/**
 * Module controler can enable / disable module etc.
 * 
 * @author Radek Tomiška
 */
@RestController
@RequestMapping(value = BaseEntityController.BASE_PATH + "/modules")
public class ModuleController {

	private final ModuleService moduleService;
	
	private final RequestResourceResolver requestResourceResolver;

	@Autowired
	public ModuleController(ModuleService moduleService, RequestResourceResolver requestResourceResolver) {
		Assert.notNull(moduleService, "ModuleService is required");
		Assert.notNull(requestResourceResolver, "PersistentEntityResolver is required");
		//
		this.moduleService = moduleService;
		this.requestResourceResolver = requestResourceResolver;
	}

	/**
	 * Returns all installed modules
	 * 
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.MODULE_READ + "')")
	public List<ModuleDescriptorDto> getInstalledModules() {
		return moduleService.getInstalledModules() //
				.stream() //
				.map(moduleDescriptor -> { //
					return toResource(moduleDescriptor);
				}) //
				.collect(Collectors.toList());
	}
	
	/**
	 * Returns selected module
	 * 
	 * @param moduleId
	 * @return
	 */	
	@RequestMapping(value = "/{moduleId}", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.MODULE_READ + "')")
	public ModuleDescriptorDto get(@PathVariable @NotNull String moduleId) {
		ModuleDescriptor moduleDescriptor = moduleService.getModule(moduleId);
		if (moduleDescriptor == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", moduleId));
		}
		return toResource(moduleDescriptor);
				
	}
	
	/**
	 * Supports enable / disable only 
	 * 
	 * @param moduleId
	 * @param nativeRequest
	 * @return
	 */
	@RequestMapping(value = "/{moduleId}", method = RequestMethod.PUT)
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.MODULE_WRITE + "')")
	public ModuleDescriptorDto put(@PathVariable @NotNull String moduleId, HttpServletRequest nativeRequest) {	
		ModuleDescriptor updatedModuleDescriptor = moduleService.getModule(moduleId);
		if (updatedModuleDescriptor == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", moduleId));
		}
		ModuleDescriptorDto md = (ModuleDescriptorDto)requestResourceResolver.resolve(nativeRequest, ModuleDescriptorDto.class, null);
		moduleService.setEnabled(moduleId, !md.isDisabled());	
		return get(moduleId);	
	}
	
	/**
	 * Supports enable / disable only 
	 * 
	 * @param moduleId
	 * @param nativeRequest
	 * @return
	 */
	@RequestMapping(value = "/{moduleId}", method = RequestMethod.PATCH)
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.MODULE_WRITE + "')")
	public ModuleDescriptorDto patch(@PathVariable @NotNull String moduleId, HttpServletRequest nativeRequest) {	
		ModuleDescriptor updatedModuleDescriptor = moduleService.getModule(moduleId);
		if (updatedModuleDescriptor == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", moduleId));
		}
		ModuleDescriptorDto md = (ModuleDescriptorDto)requestResourceResolver.resolve(nativeRequest, ModuleDescriptorDto.class, toResource(updatedModuleDescriptor));
		
		moduleService.setEnabled(moduleId, !md.isDisabled());		
		return get(moduleId);	
	}

	
	/**
	 * TODO: move to assembler + resource support + self link
	 * 
	 * @param moduleDescriptor
	 * @return
	 */
	protected ModuleDescriptorDto toResource(ModuleDescriptor moduleDescriptor) {
		ModuleDescriptorDto dto = new ModuleDescriptorDto();

		try {
			BeanUtils.copyProperties(dto, moduleDescriptor);
		} catch (Exception ex) {
			throw new ResultCodeException(CoreResultCode.INTERNAL_SERVER_ERROR, ex);
		}

		dto.setDisabled(!moduleService.isEnabled(moduleDescriptor));

		return dto;
	}
}
