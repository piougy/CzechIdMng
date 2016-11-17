package eu.bcvsolutions.idm.acc.rest.impl;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.acc.domain.AccGroupPermission;
import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.entity.SysSystemFormValue;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.QuickFilter;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteEntityController;
import eu.bcvsolutions.idm.core.api.rest.BaseEntityController;
import eu.bcvsolutions.idm.core.api.service.EntityLookupService;
import eu.bcvsolutions.idm.core.model.domain.IdmGroupPermission;
import eu.bcvsolutions.idm.eav.entity.IdmFormDefinition;
import eu.bcvsolutions.idm.eav.rest.impl.IdmFormDefinitionController;
import eu.bcvsolutions.idm.eav.service.api.FormService;
import eu.bcvsolutions.idm.security.api.domain.IfEnabled;;

/**
 * Target system setting controller
 * 
 * @author Radek Tomiška
 *
 */
@RestController
@IfEnabled(AccModuleDescriptor.MODULE_ID)
@RequestMapping(value = BaseEntityController.BASE_PATH + "/systems")
public class SysSystemController extends AbstractReadWriteEntityController<SysSystem, QuickFilter> {

	private final SysSystemService systemService;
	private final FormService formService;
	
	@Autowired 
	private IdmFormDefinitionController formDefinitionController;
	
	@Autowired
	public SysSystemController(
			EntityLookupService entityLookupService, 
			SysSystemService systemService, 
			FormService formService) {
		super(entityLookupService, systemService);
		//
		Assert.notNull(systemService);
		Assert.notNull(formService);
		//
		this.systemService = systemService;
		this.formService = formService;
	}

	@Override
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_READ + "') or hasAuthority('"
			+ IdmGroupPermission.ROLE_READ + "')")
	public Resources<?> find(@RequestParam MultiValueMap<String, Object> parameters, @PageableDefault Pageable pageable,
			PersistentEntityResourceAssembler assembler) {
		return super.find(parameters, pageable, assembler);
	}

	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_READ + "') or hasAuthority('"
			+ IdmGroupPermission.ROLE_READ + "')")
	@RequestMapping(value = "/search/quick", method = RequestMethod.GET)
	public Resources<?> findQuick(@RequestParam MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable, PersistentEntityResourceAssembler assembler) {
		return super.find(parameters, pageable, assembler);
	}

	@Override
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_READ + "') or hasAuthority('"
			+ IdmGroupPermission.ROLE_READ + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	public ResponseEntity<?> get(@PathVariable @NotNull String backendId, PersistentEntityResourceAssembler assembler) {
		return super.get(backendId, assembler);
	}

	@Override
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_WRITE + "')")
	@RequestMapping(method = RequestMethod.POST)
	public ResponseEntity<?> create(HttpServletRequest nativeRequest, PersistentEntityResourceAssembler assembler)
			throws HttpMessageNotReadableException {
		return super.create(nativeRequest, assembler);
	}

	@Override
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_WRITE + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PUT)
	public ResponseEntity<?> update(@PathVariable @NotNull String backendId, HttpServletRequest nativeRequest,
			PersistentEntityResourceAssembler assembler) throws HttpMessageNotReadableException {
		return super.update(backendId, nativeRequest, assembler);
	}

	@Override
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_WRITE + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PATCH)
	public ResponseEntity<?> patch(@PathVariable @NotNull String backendId, HttpServletRequest nativeRequest,
			PersistentEntityResourceAssembler assembler) throws HttpMessageNotReadableException {
		return super.patch(backendId, nativeRequest, assembler);
	}

	@Override
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_DELETE + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.DELETE)
	public ResponseEntity<?> delete(@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}
	
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_WRITE + "')")
	@RequestMapping(value = "/{backendId}/generate-schema", method = RequestMethod.POST)
	public ResponseEntity<?> generateSchema(@PathVariable @NotNull String backendId, PersistentEntityResourceAssembler assembler) {
		SysSystem system = getEntity(backendId);
		if (system == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		systemService.generateSchema(system);
		return new ResponseEntity<>(toResource(system, assembler), HttpStatus.OK);
	}

	@Override
	protected QuickFilter toFilter(MultiValueMap<String, Object> parameters) {
		QuickFilter filter = new QuickFilter();
		filter.setText((String) parameters.toSingleValueMap().get("text"));
		return filter;
	}
	
	/**
	 * Test usage only
	 * 
	 * @return
	 */
	@Deprecated
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_WRITE + "')")
	@RequestMapping(value = "/test/create-test-system", method = RequestMethod.POST)
	public ResponseEntity<?> createTestSystem() {
		systemService.createTestSystem();
		return new ResponseEntity<Object>(HttpStatus.NO_CONTENT);
	}	
	
	/**
	 * Returns connector form definition to given system
	 * or throws exception with code {@code CONNECTOR_CONFIGURATION_FOR_SYSTEM_NOT_FOUND}, when system is wrong configured
	 * 
	 * @param backendId
	 * @param assembler
	 * @return
	 */
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_READ + "')")
	@RequestMapping(value = "/{backendId}/connector-form-definition", method = RequestMethod.GET)
	public ResponseEntity<?> getConnectorFormDefinition(@PathVariable @NotNull String backendId, PersistentEntityResourceAssembler assembler) {
		SysSystem system = getEntity(backendId);
		if (system == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		IdmFormDefinition formDefinition = getConnectorFormDefinition(system);
		return formDefinitionController.get(formDefinition.getId().toString(), assembler);	
	}
	
	/**
	 * Returns filled connector configuration
	 * or throws exception with code {@code CONNECTOR_CONFIGURATION_FOR_SYSTEM_NOT_FOUND}, when system is wrong configured
	 * 
	 * @param backendId
	 * @param assembler
	 * @return
	 */
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_READ + "')")
	@RequestMapping(value = "/{backendId}/connector-form-values", method = RequestMethod.GET)
	public Resources<?> getConnectorFormValues(@PathVariable @NotNull String backendId, PersistentEntityResourceAssembler assembler) {
		SysSystem system = getEntity(backendId);
		if (system == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		IdmFormDefinition formDefinition = getConnectorFormDefinition(system);
		return toResources(formService.getValues(system, formDefinition), assembler, getEntityClass(), null);
	}
	
	/**
	 * Saves connector configuration form values
	 * 
	 * @param backendId
	 * @param formValues
	 * @param assembler
	 * @return
	 */
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_WRITE + "')")
	@RequestMapping(value = "/{backendId}/connector-form-values", method = RequestMethod.POST)
	public Resources<?> saveConnectorFormValues(
			@PathVariable @NotNull String backendId,
			@RequestBody @Valid List<SysSystemFormValue> formValues,
			PersistentEntityResourceAssembler assembler) {		
		SysSystem system = getEntity(backendId);
		if (system == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		formService.saveValues(system, formValues);
		return getConnectorFormValues(backendId, assembler);
	}
	
	/**
	 * Returns definition for given system 
	 * or throws exception with code {@code CONNECTOR_CONFIGURATION_FOR_SYSTEM_NOT_FOUND}, when system is wrong configured
	 * 
	 * @param system
	 * @return
	 */
	private IdmFormDefinition getConnectorFormDefinition(SysSystem system) {
		Assert.notNull(system);
		//
		try {
			return systemService.getConnectorFormDefinition(system.getConnectorKey());
		} catch(Exception ex) {
			throw new ResultCodeException(AccResultCode.CONNECTOR_CONFIGURATION_FOR_SYSTEM_NOT_FOUND, ImmutableMap.of("system", system.getName()), ex);
		}
	}
}
