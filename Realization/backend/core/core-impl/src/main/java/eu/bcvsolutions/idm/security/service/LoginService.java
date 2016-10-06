package eu.bcvsolutions.idm.security.service;

import eu.bcvsolutions.idm.security.dto.LoginDto;

public interface LoginService {

	public LoginDto login(String username, String password);

}
