package eu.bcvsolutions.idm.acc.rest.impl;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
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
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.acc.domain.AccGroupPermission;
import eu.bcvsolutions.idm.acc.dto.IdentityAccountFilter;
import eu.bcvsolutions.idm.acc.entity.AccIdentityAccount;
import eu.bcvsolutions.idm.acc.service.AccAccountService;
import eu.bcvsolutions.idm.acc.service.AccIdentityAccountService;
import eu.bcvsolutions.idm.core.exception.CoreResultCode;
import eu.bcvsolutions.idm.core.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityLookup;
import eu.bcvsolutions.idm.core.rest.BaseEntityController;
import eu.bcvsolutions.idm.core.rest.impl.DefaultReadWriteEntityController;
import eu.bcvsolutions.idm.security.domain.IfEnabled;;

/**
 * Identity accounts on target system
 * 
 * @author Radek Tomiška
 *
 */
@RestController
@IfEnabled(AccModuleDescriptor.MODULE_ID)
@RequestMapping(value = BaseEntityController.BASE_PATH + "/identityAccounts")
public class AccIdentityAccountController extends DefaultReadWriteEntityController<AccIdentityAccount, IdentityAccountFilter> {
	
	// TODO: lookups overs spring plugin
	private final IdmIdentityLookup identityLookup;
	private final AccAccountService accountService;
	
	@Autowired
	public AccIdentityAccountController(AccIdentityAccountService identityAccountService, IdmIdentityLookup identityLookup, AccAccountService accountService) {
		super(identityAccountService);
		//
		Assert.notNull(identityLookup);
		Assert.notNull(accountService);
		this.identityLookup = identityLookup;
		this.accountService = accountService;
	}
	
	@Override
	@RequestMapping(method = RequestMethod.GET)
	public Resources<?> find(@RequestParam MultiValueMap<String, Object> parameters, 
			@PageableDefault Pageable pageable, 			
			PersistentEntityResourceAssembler assembler) {
		return super.find(parameters, pageable, assembler);
	}
	
	@Override
	@RequestMapping(value= "/search/quick", method = RequestMethod.GET)
	public Resources<?> findQuick(@RequestParam MultiValueMap<String, Object> parameters, 
			@PageableDefault Pageable pageable, 			
			PersistentEntityResourceAssembler assembler) {
		return super.find(parameters, pageable, assembler);
	}
	
	@Override
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	public ResponseEntity<?> get(@PathVariable @NotNull String backendId, PersistentEntityResourceAssembler assembler) {
		return super.get(backendId, assembler);
	}
	
	@Override
	@PreAuthorize("hasAuthority('" + AccGroupPermission.ACCOUNT_WRITE + "')")
	@RequestMapping(method = RequestMethod.POST)
	public ResponseEntity<?> create(HttpServletRequest nativeRequest, PersistentEntityResourceAssembler assembler) throws HttpMessageNotReadableException {
		return super.create(nativeRequest, assembler);
	}
	
	@Override
	@PreAuthorize("hasAuthority('" + AccGroupPermission.ACCOUNT_WRITE + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PUT)
	public ResponseEntity<?> update(
			@PathVariable @NotNull String backendId,
			HttpServletRequest nativeRequest,
			PersistentEntityResourceAssembler assembler) throws HttpMessageNotReadableException {
		return super.update(backendId, nativeRequest, assembler);
	}	
	
	@Override
	@PreAuthorize("hasAuthority('" + AccGroupPermission.ACCOUNT_WRITE + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PATCH)
	public ResponseEntity<?> patch(@PathVariable @NotNull String backendId, HttpServletRequest nativeRequest, PersistentEntityResourceAssembler assembler) 
			throws HttpMessageNotReadableException {
		return super.patch(backendId, nativeRequest, assembler);
	}
	
	@Override
	@PreAuthorize("hasAuthority('" + AccGroupPermission.ACCOUNT_DELETE + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.DELETE)
	public ResponseEntity<?> delete(@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}
	
	@Override
	protected IdentityAccountFilter toFilter(MultiValueMap<String, Object> parameters) {
		IdentityAccountFilter filter = new IdentityAccountFilter();
		filter.setAccountId(convertLongParameter(parameters, "accountId"));
		// TODO: Spring plugin and identity lookup
		String identityIdentifier = convertStringParameter(parameters, "identity");
		if (StringUtils.isNotEmpty(identityIdentifier)) {
			IdmIdentity identity = (IdmIdentity)identityLookup.lookupEntity(identityIdentifier);
			if (identity == null) {
				throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("identity", identityIdentifier));
			}
			filter.setIdentity(identity);
		}
		filter.setRoleId(convertLongParameter(parameters, "roleId"));
		filter.setSystemId(convertLongParameter(parameters, "systemId"));
		return filter;
	}
}
