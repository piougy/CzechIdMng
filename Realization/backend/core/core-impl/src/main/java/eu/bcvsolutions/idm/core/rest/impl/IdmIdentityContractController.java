package eu.bcvsolutions.idm.core.rest.impl;

import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.Resources;
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
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdentityContractFilter;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormDefinition;
import eu.bcvsolutions.idm.core.eav.rest.impl.IdmFormDefinitionController;
import eu.bcvsolutions.idm.core.eav.service.api.FormService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.entity.eav.IdmIdentityContractFormValue;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityContractRepository;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;

/**
 * Identity contract endpoint
 * 
 * TODO: eav to dtos
 * 
 * @author Radek Tomiška
 *
 */
@RepositoryRestController // TODO: @RestController after eav to dto
@RequestMapping(value = BaseDtoController.BASE_PATH + "/identity-contracts")
public class IdmIdentityContractController extends AbstractReadWriteDtoController<IdmIdentityContractDto, IdentityContractFilter> {
	
	private final IdmFormDefinitionController formDefinitionController;
	private final IdmIdentityContractRepository identityContractRepository;
	private final FormService formService;
	
	@Autowired
	public IdmIdentityContractController(
			LookupService entityLookupService, 
			IdmIdentityContractService identityContractService,
			IdmFormDefinitionController formDefinitionController,
			IdmIdentityContractRepository identityContractRepository,
			FormService formService) {
		super(identityContractService);
		//
		Assert.notNull(formDefinitionController);
		Assert.notNull(identityContractRepository);
		Assert.notNull(formService);
		//
		this.formDefinitionController = formDefinitionController;
		this.identityContractRepository = identityContractRepository;
		this.formService = formService;
	}
	
	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.IDENTITYCONTRACT_READ + "')")
	public Resources<?> find(@RequestParam MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}

	@ResponseBody
	@RequestMapping(value = "/search/quick", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.IDENTITYCONTRACT_READ + "')")
	public Resources<?> findQuick(@RequestParam MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}
	
	@ResponseBody
	@RequestMapping(value = "/search/autocomplete", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.IDENTITYCONTRACT_AUTOCOMPLETE + "')")
	public Resources<?> autocomplete(
			@RequestParam MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.autocomplete(parameters, pageable);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.IDENTITYCONTRACT_READ + "')")
	public ResponseEntity<?> get(@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}

	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.IDENTITYCONTRACT_CREATE + "') or hasAuthority('" + CoreGroupPermission.IDENTITYCONTRACT_UPDATE + "')")
	public ResponseEntity<?> post(@Valid @RequestBody IdmIdentityContractDto dto) {
		return super.post(dto);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PUT)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.IDENTITYCONTRACT_UPDATE + "')")
	public ResponseEntity<?> put(@PathVariable @NotNull String backendId, @Valid @RequestBody IdmIdentityContractDto dto) {
		return super.put(backendId, dto);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PATCH)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.IDENTITYCONTRACT_UPDATE + "')")
	public ResponseEntity<?> patch(@PathVariable @NotNull String backendId, HttpServletRequest nativeRequest)
			throws HttpMessageNotReadableException {
		return super.patch(backendId, nativeRequest);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.DELETE)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.IDENTITYCONTRACT_DELETE + "')")
	public ResponseEntity<?> delete(@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}/permissions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.IDENTITYCONTRACT_READ + "')")
	public Set<String> getPermissions(@PathVariable @NotNull String backendId) {
		return super.getPermissions(backendId);
	}
	
	/**
	 * Returns form definition to given entity.
	 * 
	 * @param backendId
	 * @param assembler
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/{backendId}/form-definitions", method = RequestMethod.GET)
	public ResponseEntity<?> getFormDefinitions(@PathVariable @NotNull String backendId, PersistentEntityResourceAssembler assembler) {
		return formDefinitionController.getDefinitions(IdmIdentityContract.class, assembler);
	}
	
	/**
	 * Returns entity's filled form values
	 * 
	 * @param backendId
	 * @param assembler
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/{backendId}/form-values", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.IDENTITYCONTRACT_READ + "')")
	public Resources<?> getFormValues(
			@PathVariable @NotNull String backendId, 
			@RequestParam(name = "definitionCode") String definitionCode,
			PersistentEntityResourceAssembler assembler) {
		IdmIdentityContractDto dto = getDto(backendId);
		if (dto == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		//
		checkAccess(dto, IdmBasePermission.READ);
		//
		IdmFormDefinition formDefinition = null; // default
		if (StringUtils.isNotEmpty(definitionCode)) {
			formDefinition = formService.getDefinition(IdmIdentityContract.class, definitionCode);
		}
		//
		return formDefinitionController.getFormValues(identityContractRepository.findOne(dto.getId()), formDefinition, assembler);
	}
	
	/**
	 * Saves entity's form values
	 * 
	 * @param backendId
	 * @param formValues
	 * @param assembler
	 * @return
	 */
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.IDENTITYCONTRACT_UPDATE + "')")
	@RequestMapping(value = "/{backendId}/form-values", method = RequestMethod.POST)
	public Resources<?> saveFormValues(
			@PathVariable @NotNull String backendId,
			@RequestParam(name = "definitionCode") String definitionCode,
			@RequestBody @Valid List<IdmIdentityContractFormValue> formValues,
			PersistentEntityResourceAssembler assembler) {		
		IdmIdentityContractDto dto = getDto(backendId);
		if (dto == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		// 
		checkAccess(dto, IdmBasePermission.UPDATE);
		//
		IdmFormDefinition formDefinition = null; // default
		if (StringUtils.isNotEmpty(definitionCode)) {
			formDefinition = formService.getDefinition(IdmIdentityContract.class, definitionCode);
		}
		//
		return formDefinitionController.saveFormValues(identityContractRepository.findOne(dto.getId()), formDefinition, formValues, assembler);
	}
	
	@Override
	protected IdentityContractFilter toFilter(MultiValueMap<String, Object> parameters) {
		IdentityContractFilter filter = new IdentityContractFilter(parameters);
		filter.setText(getParameterConverter().toString(parameters, "text"));
		filter.setIdentity(getParameterConverter().toEntityUuid(parameters, "identity", IdmIdentity.class));
		filter.setValid(getParameterConverter().toBoolean(parameters, "valid"));
		filter.setExterne(getParameterConverter().toBoolean(parameters, "externe"));
		filter.setDisabled(getParameterConverter().toBoolean(parameters, "disabled"));
		filter.setMain(getParameterConverter().toBoolean(parameters, "main"));
		filter.setValidNowOrInFuture(getParameterConverter().toBoolean(parameters, "validNowOrInFuture"));
		// TODO: localdate converters
		return filter;
	}
}
