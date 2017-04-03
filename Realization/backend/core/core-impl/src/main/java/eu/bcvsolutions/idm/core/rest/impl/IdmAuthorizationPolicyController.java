package eu.bcvsolutions.idm.core.rest.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resources;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.model.dto.IdmAuthorizationPolicyDto;
import eu.bcvsolutions.idm.core.model.dto.filter.AuthorizationPolicyFilter;
import eu.bcvsolutions.idm.core.model.service.api.IdmAuthorizationPolicyService;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizationManager;

/**
 * Controller for assigning authorization evaluators to roles.
 * 
 * @author Radek Tomiška
 *
 */
@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/authorization-policies")
public class IdmAuthorizationPolicyController extends DefaultReadWriteDtoController<IdmAuthorizationPolicyDto, AuthorizationPolicyFilter> {
	
	private final AuthorizationManager authorizationManager;
	
	@Autowired
	public IdmAuthorizationPolicyController(
			IdmAuthorizationPolicyService service,
			AuthorizationManager authorizationManager) {
		super(service);
		//
		Assert.notNull(authorizationManager);
		//
		this.authorizationManager = authorizationManager;
	}
	
	/**
	 * Returns all registered tasks
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET, value = "/search/supported")
	public Resources<?> getSupportedEvaluators() {
		return new Resources<>(authorizationManager.getSupportedEvaluators());
	}
	
	/**
	 * Returns all registered tasks
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET, value = "/search/authorizable-types")
	public Resources<?> getAuthorizableTypes() {
		return new Resources<>(authorizationManager.getAuthorizableTypes());
	}
}
