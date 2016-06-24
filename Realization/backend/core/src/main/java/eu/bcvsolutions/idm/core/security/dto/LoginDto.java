package eu.bcvsolutions.idm.core.security.dto;

public class LoginDto {

	private String username;
	private String password;
	private String token;
	private IdmJwtAuthenticationDto authentication;
	
	public String getUsername() {
		return username;
	}
	
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setUsername(String username) {
		this.username = username;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public void setAuthentication(IdmJwtAuthenticationDto authenticationDto) {
		this.authentication = authenticationDto;	
	}
	public IdmJwtAuthenticationDto getAuthentication(){
		return this.authentication;
	}
	
	
}
