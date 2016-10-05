package eu.bcvsolutions.idm.core.model.validator;

import java.text.MessageFormat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.repository.IdmRoleRepository;

public class IdmRoleValidator implements Validator {

	@Autowired
	private IdmRoleRepository roleRepository;

	@Override
	public boolean supports(Class<?> clazz) {
		return IdmRole.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		IdmRole role = (IdmRole) target;		
		IdmRole exists = roleRepository.findOneByName(role.getName());
		if (exists != null && !exists.equals(role)) {
			errors.rejectValue("name", "duplicate", MessageFormat.format("role with name {0} already exists.", role.getName()));
		}
	}

}
