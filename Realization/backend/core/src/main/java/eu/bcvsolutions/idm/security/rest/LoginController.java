package eu.bcvsolutions.idm.security.rest;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.exception.CoreResultCode;
import eu.bcvsolutions.idm.core.exception.RestApplicationException;
import eu.bcvsolutions.idm.core.model.domain.ResourceWrapper;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRepository;
import eu.bcvsolutions.idm.notification.entity.IdmMessage;
import eu.bcvsolutions.idm.notification.service.NotificationService;
import eu.bcvsolutions.idm.security.dto.LoginDto;
import eu.bcvsolutions.idm.security.service.LoginService;

@RestController
@RequestMapping(value = "/api/authentication")
public class LoginController {

	@Autowired
	private LoginService loginService;
	
	@Autowired
	private NotificationService notificationService;
	
	@Autowired
	private IdmIdentityRepository identityRepository;

	@RequestMapping(method = RequestMethod.POST)
	public ResourceWrapper<LoginDto> login(@Valid @RequestBody(required = true) LoginDto loginDto) {
		if(loginDto == null || loginDto.getUsername() == null || loginDto.getPassword() == null){
			throw new RestApplicationException(CoreResultCode.AUTH_FAILED, "Username and password must be filled");
		}
		return new ResourceWrapper<LoginDto>(loginService.login(loginDto.getUsername(), loginDto.getPassword()));
	}
	
	@RequestMapping(method = RequestMethod.GET)
	public void test() {
		IdmIdentity identityOne = identityRepository.findOneByUsername("tomiska");
		IdmIdentity identityTwo = identityRepository.findOneByUsername("svanda");
		notificationService.send("topicOne", new IdmMessage("Předmět", "text", "html"), identityOne);
		// notificationService.send("topicTwo", new IdmMessage("Předmět2", "text2", "html2"), identityTwo);
		// notificationService.send("topicOne", new IdmMessage("Předmět3", "text3", "html3"), Lists.newArrayList(identityOne, identityTwo));
	}
}
