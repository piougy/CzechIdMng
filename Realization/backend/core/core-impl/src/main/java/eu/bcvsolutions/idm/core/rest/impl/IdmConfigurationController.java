package eu.bcvsolutions.idm.core.rest.impl;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.core.api.dto.IdmConfigurationDto;
import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.service.api.IdmConfigurationService;
import eu.bcvsolutions.idm.core.security.api.domain.IdmGroupPermission;

/**
 * Configuration controller - add custom methods to configuration repository
 * 
 * @author Radek Tomiška 
 *
 */
@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/configurations")
public class IdmConfigurationController extends AbstractReadWriteDtoController<IdmConfigurationDto, DataFilter> {
	
	private final IdmConfigurationService configurationService;
	
	@Autowired
	public IdmConfigurationController(IdmConfigurationService configurationService) {
		super(configurationService);
		//
		this.configurationService = configurationService;
	}
	
	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.CONFIGURATION_READ + "')")
	public Resources<?> find(@RequestParam MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}

	@ResponseBody
	@RequestMapping(value = "/search/quick", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.CONFIGURATION_READ + "')")
	public Resources<?> findQuick(@RequestParam MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.CONFIGURATION_READ + "')")
	public ResponseEntity<?> get(@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}

	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.CONFIGURATION_CREATE + "') or hasAuthority('" + CoreGroupPermission.CONFIGURATION_UPDATE + "')")
	public ResponseEntity<?> post(@Valid @RequestBody IdmConfigurationDto dto) {
		return super.post(dto);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PUT)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.CONFIGURATION_UPDATE + "')")
	public ResponseEntity<?> put(@PathVariable @NotNull String backendId, @Valid @RequestBody IdmConfigurationDto dto) {
		return super.put(backendId, dto);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PATCH)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.CONFIGURATION_UPDATE + "')")
	public ResponseEntity<?> patch(@PathVariable @NotNull String backendId, HttpServletRequest nativeRequest)
			throws HttpMessageNotReadableException {
		return super.patch(backendId, nativeRequest);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.DELETE)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.CONFIGURATION_DELETE + "')")
	public ResponseEntity<?> delete(@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}/permissions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.CONFIGURATION_READ + "')")
	public Set<String> getPermissions(@PathVariable @NotNull String backendId) {
		return super.getPermissions(backendId);
	}
	
	/**
	 * Returns configurations from property files 
	 * 
	 * @return
	 */
	@ResponseBody
	@PostFilter("hasAuthority('" + CoreGroupPermission.CONFIGURATION_READ + "')")
	@RequestMapping(path = "/all/file", method = RequestMethod.GET)
	public List<IdmConfigurationDto> getAllConfigurationsFromFiles() {
		// TODO: resource wrapper + assembler
		return configurationService.getAllConfigurationsFromFiles();
	}
	
	/**
	 * Returns configurations from property files 
	 * 
	 * @return
	 */
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.CONFIGURATION_ADMIN + "')")
	@RequestMapping(path = "/all/environment", method = RequestMethod.GET)
	public List<IdmConfigurationDto> getAllConfigurationsFromEnvironment() {
		// TODO: resource wrapper + assembler + hateoas links
		return configurationService.getAllConfigurationsFromEnvironment();
	}
	
	/**
	 * Bulk configuration save
	 * 
	 * @param configuration
	 * @return
	 * @throws IOException 
	 */
	@ResponseStatus(code = HttpStatus.ACCEPTED)
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.APP_ADMIN + "')")
	@RequestMapping(value = "/bulk/save", method = RequestMethod.PUT, consumes = MediaType.TEXT_PLAIN_VALUE)
	public void saveProperties(@RequestBody String configuration) throws IOException {
		Properties p = new Properties();
	    p.load(new StringReader(configuration));
	    p.forEach((name, value) -> {
	    	configurationService.setValue(name.toString(), value == null ? null : value.toString().split("#")[0].trim());
	    });
	}
	
	@Override
	protected DataFilter toFilter(MultiValueMap<String, Object> parameters) {
		return new DataFilter(getDtoClass(), parameters);
	}
}
