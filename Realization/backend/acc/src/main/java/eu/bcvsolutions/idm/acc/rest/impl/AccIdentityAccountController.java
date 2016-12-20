package eu.bcvsolutions.idm.acc.rest.impl;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.acc.domain.AccGroupPermission;
import eu.bcvsolutions.idm.acc.dto.IdentityAccountFilter;
import eu.bcvsolutions.idm.acc.entity.AccAccount;
import eu.bcvsolutions.idm.acc.entity.AccIdentityAccount;
import eu.bcvsolutions.idm.core.api.rest.BaseEntityController;
import eu.bcvsolutions.idm.core.api.service.EntityLookupService;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.rest.impl.DefaultReadWriteEntityController;
import eu.bcvsolutions.idm.security.api.domain.Enabled;;

/**
 * Identity accounts on target system
 * 
 * @author Radek Tomiška
 *
 */
@RepositoryRestController
@Enabled(AccModuleDescriptor.MODULE_ID)
@RequestMapping(value = BaseEntityController.BASE_PATH + "/identity-accounts")
public class AccIdentityAccountController extends DefaultReadWriteEntityController<AccIdentityAccount, IdentityAccountFilter> {
	
	@Autowired
	public AccIdentityAccountController(EntityLookupService entityLookupService) {
		super(entityLookupService);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	public Resources<?> find(@RequestParam MultiValueMap<String, Object> parameters, 
			@PageableDefault Pageable pageable, 			
			PersistentEntityResourceAssembler assembler) {
		return super.find(parameters, pageable, assembler);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value= "/search/quick", method = RequestMethod.GET)
	public Resources<?> findQuick(@RequestParam MultiValueMap<String, Object> parameters, 
			@PageableDefault Pageable pageable, 			
			PersistentEntityResourceAssembler assembler) {
		return super.find(parameters, pageable, assembler);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	public ResponseEntity<?> get(@PathVariable @NotNull String backendId, PersistentEntityResourceAssembler assembler) {
		return super.get(backendId, assembler);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.ACCOUNT_WRITE + "')")
	@RequestMapping(method = RequestMethod.POST)
	public ResponseEntity<?> create(HttpServletRequest nativeRequest, PersistentEntityResourceAssembler assembler) throws HttpMessageNotReadableException {
		return super.create(nativeRequest, assembler);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.ACCOUNT_WRITE + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PUT)
	public ResponseEntity<?> update(
			@PathVariable @NotNull String backendId,
			HttpServletRequest nativeRequest,
			PersistentEntityResourceAssembler assembler) throws HttpMessageNotReadableException {
		return super.update(backendId, nativeRequest, assembler);
	}	
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.ACCOUNT_WRITE + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PATCH)
	public ResponseEntity<?> patch(@PathVariable @NotNull String backendId, HttpServletRequest nativeRequest, PersistentEntityResourceAssembler assembler) 
			throws HttpMessageNotReadableException {
		return super.patch(backendId, nativeRequest, assembler);
	}
	
	@Override
	public AccIdentityAccount patchEntity(AccIdentityAccount entity) {
		Assert.notNull(entity);
		Assert.notNull(entity.getAccount());
		Assert.notNull(entity.getAccount().getId());
		// we don't have excerpt projection - we need to fill account manually (id only from  FE) 
		entity.setAccount(entityLookupService.lookup(AccAccount.class, entity.getAccount().getId()));
		return super.patchEntity(entity);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.ACCOUNT_DELETE + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.DELETE)
	public ResponseEntity<?> delete(@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}
	
	@Override
	protected IdentityAccountFilter toFilter(MultiValueMap<String, Object> parameters) {
		IdentityAccountFilter filter = new IdentityAccountFilter();
		filter.setAccountId(convertUuidParameter(parameters, "accountId"));
		IdmIdentity identity = convertEntityParameter(parameters, "identity", IdmIdentity.class);
		if (identity != null) {
			filter.setIdentityId(identity.getId());
		}
		filter.setRoleId(convertUuidParameter(parameters, "roleId"));
		filter.setSystemId(convertUuidParameter(parameters, "systemId"));
		filter.setOwnership(convertBooleanParameter(parameters, "ownership"));
		return filter;
	}
}
