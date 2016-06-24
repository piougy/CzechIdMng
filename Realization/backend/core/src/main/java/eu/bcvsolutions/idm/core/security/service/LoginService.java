package eu.bcvsolutions.idm.core.security.service;

import eu.bcvsolutions.idm.core.security.dto.LoginDto;

public interface LoginService {

	public LoginDto login(String username, String password);

}
