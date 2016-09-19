package eu.bcvsolutions.idm.core.rest.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.core.model.domain.IdmGroupPermission;
import eu.bcvsolutions.idm.core.model.dto.ConfigurationDto;
import eu.bcvsolutions.idm.core.model.dto.QuickFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmConfiguration;
import eu.bcvsolutions.idm.core.model.service.IdmConfigurationService;
import eu.bcvsolutions.idm.core.rest.BaseEntityController;;

/**
 * Configuration controller - add custom methods to configuration repository
 * 
 * @author Radek Tomiška <radek.tomiska@bcvsolutions.eu>
 *
 */
@RestController
@RequestMapping(value = BaseEntityController.BASE_PATH + "/configurations")
public class IdmConfigurationController extends DefaultReadWriteEntityController<IdmConfiguration, QuickFilter> {
	
	private final IdmConfigurationService configurationService;
	
	@Autowired
	public IdmConfigurationController(IdmConfigurationService configurationService) {
		super(configurationService);
		this.configurationService = configurationService;
	}
	
	/**
	 * Returns configurations from property files 
	 * 
	 * @return
	 */
	@PostFilter("filterObject.name.startsWith('idm.pub.') or hasAuthority('" + IdmGroupPermission.CONFIGURATIONSECURED_READ + "')")
	@RequestMapping(path = "/file", method = RequestMethod.GET)
	public List<ConfigurationDto> getAllConfigurationsFromFiles() {
		// TODO: resource wrapper + assembler
		return configurationService.getAllConfigurationsFromFiles();
	}
	
	/**
	 * Returns configurations from property files 
	 * 
	 * @return
	 */
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.CONFIGURATIONSECURED_READ + "')")
	@RequestMapping(path = "/environment", method = RequestMethod.GET)
	public List<ConfigurationDto> getAllConfigurationsFromEnvironment() {
		// TODO: resource wrapper + assembler
		return configurationService.getAllConfigurationsFromEnvironment();
	}
	
	@Override
	protected QuickFilter toFilter(MultiValueMap<String, Object> parameters) {
		QuickFilter filter = new QuickFilter();
		filter.setText((String)parameters.toSingleValueMap().get("text"));
		return filter;
	}
}
