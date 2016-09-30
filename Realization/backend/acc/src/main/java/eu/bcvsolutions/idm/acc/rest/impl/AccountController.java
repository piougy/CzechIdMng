package eu.bcvsolutions.idm.acc.rest.impl;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.core.rest.BaseEntityController;
import eu.bcvsolutions.idm.security.domain.IfEnabled;

/**
 * Identity accounts controller
 * 
 * @author tomiska
 * @deprecated For testing purpose only - will be refactored
 */
@Deprecated
@RestController
@IfEnabled(AccModuleDescriptor.MODULE_ID)
@RequestMapping(value = BaseEntityController.BASE_PATH + "/accounts")
public class AccountController {

	@RequestMapping(method = RequestMethod.GET)
	public String ping() {
		return "pong: accounts";
	}
}
