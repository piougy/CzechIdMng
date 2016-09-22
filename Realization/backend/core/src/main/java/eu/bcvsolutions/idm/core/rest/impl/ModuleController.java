package eu.bcvsolutions.idm.core.rest.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.core.exception.CoreResultCode;
import eu.bcvsolutions.idm.core.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.model.dto.ModuleDescriptorDto;
import eu.bcvsolutions.idm.core.model.service.ModuleService;
import eu.bcvsolutions.idm.core.rest.BaseEntityController;

@RestController
@RequestMapping(value = BaseEntityController.BASE_PATH + "/modules")
public class ModuleController {
	
	private final ModuleService moduleService;
	
	@Autowired
	public ModuleController(ModuleService moduleService) {
		Assert.notNull(moduleService, "ModuleService is required");
		
		this.moduleService = moduleService;
	}

	@RequestMapping(method = RequestMethod.GET)
	public List<ModuleDescriptorDto> getAll() {
		// todo assembler
		return moduleService.getRegisteredModules()
				.stream()
				.map(moduleDescriptor -> {
					ModuleDescriptorDto dto = new ModuleDescriptorDto();
					
					try {
						BeanUtils.copyProperties(dto, moduleDescriptor);
					} catch (Exception ex) {
						throw new ResultCodeException(CoreResultCode.INTERNAL_SERVER_ERROR, ex);
					}
					
					dto.setDisabled(!moduleService.isEnabled(moduleDescriptor));
					
					return dto;
					})
				.collect(Collectors.toList());
	}
}
