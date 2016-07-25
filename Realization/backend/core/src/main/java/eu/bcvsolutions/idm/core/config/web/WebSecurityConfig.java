package eu.bcvsolutions.idm.core.config.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import eu.bcvsolutions.idm.core.security.filter.OAuthAuthenticationFilter;
import eu.bcvsolutions.idm.core.security.service.impl.OAuthAuthenticationManager;

/**
 * Web security configuration
 * 
 * @author Radek Tomiška <radek.tomiska@bcvsolutions.eu>
 *
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	@Override
    protected void configure(HttpSecurity http) throws Exception {
    	 http.csrf().disable(); 
    	 http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    	 http.addFilterAfter(oAuthAuthenticationFilter(), BasicAuthenticationFilter.class)
			.authorizeRequests()
			.antMatchers(HttpMethod.OPTIONS)
			.permitAll()
			.and()
			.authorizeRequests()
			.antMatchers("/api/authentication")
			.permitAll()
			.and()
			.authorizeRequests()
			.antMatchers("/api/**")
			.fullyAuthenticated()
			.and()
			.anonymous()
			.disable();    	 
    }
	
	@Override
	public void configure(WebSecurity web) throws Exception {
		// public controllers
		web.ignoring().antMatchers( //
				"/api", // endpoint with supported services list
				"/api/authentication" // login / out
			);
	}
   
	@Bean
	public OAuthAuthenticationManager oAuthAuthenticationManager() {
		return new OAuthAuthenticationManager();
	}

	@Bean
	public OAuthAuthenticationFilter oAuthAuthenticationFilter() {
		return new OAuthAuthenticationFilter();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
