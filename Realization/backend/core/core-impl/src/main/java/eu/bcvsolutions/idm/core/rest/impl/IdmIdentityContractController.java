package eu.bcvsolutions.idm.core.rest.impl;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.rest.webmvc.RepositoryRestController;
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
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.BaseEntityController;
import eu.bcvsolutions.idm.core.api.service.EntityLookupService;
import eu.bcvsolutions.idm.core.eav.rest.impl.IdmFormDefinitionController;
import eu.bcvsolutions.idm.core.model.dto.filter.IdentityContractFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.entity.eav.IdmIdentityContractFormValue;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.security.api.domain.IdmGroupPermission;

/**
 * Identity contract endpoint
 * 
 * TODO: change permissions - app admin can edit for now
 * 
 * @author Radek Tomiška
 *
 */
@RepositoryRestController
@RequestMapping(value = BaseEntityController.BASE_PATH + "/identity-contracts")
public class IdmIdentityContractController extends DefaultReadWriteEntityController<IdmIdentityContract, IdentityContractFilter> {
	
	private final IdmFormDefinitionController formDefinitionController;
	
	@Autowired
	public IdmIdentityContractController(
			EntityLookupService entityLookupService, 
			IdmIdentityContractService identityWorkPositionService,
			IdmFormDefinitionController formDefinitionController) {
		super(entityLookupService, identityWorkPositionService);
		//
		Assert.notNull(formDefinitionController);
		//
		this.formDefinitionController = formDefinitionController;
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.APP_ADMIN + "')")
	public ResponseEntity<?> post(HttpServletRequest nativeRequest, PersistentEntityResourceAssembler assembler)
			throws HttpMessageNotReadableException {
		return super.post(nativeRequest, assembler);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.APP_ADMIN + "')")
	public ResponseEntity<?> patch(@PathVariable @NotNull String backendId, HttpServletRequest nativeRequest,
			PersistentEntityResourceAssembler assembler) throws HttpMessageNotReadableException {
		// TODO Auto-generated method stub
		return super.patch(backendId, nativeRequest, assembler);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.APP_ADMIN + "')")
	public ResponseEntity<?> put(@PathVariable @NotNull String backendId, HttpServletRequest nativeRequest,
			PersistentEntityResourceAssembler assembler) throws HttpMessageNotReadableException {
		return super.put(backendId, nativeRequest, assembler);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.APP_ADMIN + "')")
	public ResponseEntity<?> delete(@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}
	
	/**
	 * Returns form definition to given entity.
	 * 
	 * @param backendId
	 * @param assembler
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/{backendId}/form-definition", method = RequestMethod.GET)
	public ResponseEntity<?> getFormDefinition(@PathVariable @NotNull String backendId, PersistentEntityResourceAssembler assembler) {
		return formDefinitionController.getDefinition(IdmIdentityContract.class, assembler);
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
	public Resources<?> getFormValues(@PathVariable @NotNull String backendId, PersistentEntityResourceAssembler assembler) {
		IdmIdentityContract entity = getEntity(backendId);
		if (entity == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		return formDefinitionController.getFormValues(entity, null, assembler);
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
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.APP_ADMIN + "')")
	@RequestMapping(value = "/{backendId}/form-values", method = RequestMethod.POST)
	public Resources<?> saveFormValues(
			@PathVariable @NotNull String backendId,
			@RequestBody @Valid List<IdmIdentityContractFormValue> formValues,
			PersistentEntityResourceAssembler assembler) {		
		IdmIdentityContract entity = getEntity(backendId);
		if (entity == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		return formDefinitionController.saveFormValues(entity, null, formValues, assembler);
	}
	
	@Override
	protected IdentityContractFilter toFilter(MultiValueMap<String, Object> parameters) {
		IdentityContractFilter filter = new IdentityContractFilter();
		filter.setText(getParameterConverter().toString(parameters, "text"));
		filter.setIdentity(getParameterConverter().toEntity(parameters, "identity", IdmIdentity.class));
		return filter;
	}
}
