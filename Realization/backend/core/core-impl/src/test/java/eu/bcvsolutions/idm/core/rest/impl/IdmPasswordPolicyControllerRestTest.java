package eu.bcvsolutions.idm.core.rest.impl;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.domain.IdmPasswordPolicyType;
import eu.bcvsolutions.idm.core.api.dto.IdmPasswordPolicyDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmPasswordPolicyFilter;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoControllerRestTest;
import eu.bcvsolutions.idm.core.api.service.IdmPasswordPolicyService;

/**
 * Controller tests.
 * 
 * TODO: cover other (but in product unused) filter properties.
 * 
 * @author Radek Tomiška
 *
 */
public class IdmPasswordPolicyControllerRestTest extends AbstractReadWriteDtoControllerRestTest<IdmPasswordPolicyDto> {

	@Autowired private IdmPasswordPolicyController controller;
	@Autowired private IdmPasswordPolicyService service;
	
	@Override
	protected AbstractReadWriteDtoController<IdmPasswordPolicyDto, ?> getController() {
		return controller;
	}

	@Override
	protected IdmPasswordPolicyDto prepareDto() {
		IdmPasswordPolicyDto dto = new IdmPasswordPolicyDto();
		dto.setName(getHelper().createName());
		return dto;
	}
	
	@Test
	public void testFindByType() {
		IdmPasswordPolicyDto policy = prepareDto();
		policy.setType(IdmPasswordPolicyType.GENERATE);
		IdmPasswordPolicyDto policyOne = createDto(policy);
		policy = prepareDto();
		policy.setType(IdmPasswordPolicyType.VALIDATE);
		IdmPasswordPolicyDto policyTwo = createDto(policy);
		
		IdmPasswordPolicyFilter filter = new IdmPasswordPolicyFilter();
		filter.setType(IdmPasswordPolicyType.GENERATE);
		
		List<IdmPasswordPolicyDto> results = find(filter);
		//
		Assert.assertFalse(results.isEmpty());
		Assert.assertTrue(results.stream().allMatch(p -> !p.getId().equals(policyTwo.getId())));
		Assert.assertTrue(results.stream().anyMatch(p -> p.getId().equals(policyOne.getId())));
	}
	
	@Test
	public void testFindByDefaultPolicy() {
		IdmPasswordPolicyFilter filter = new IdmPasswordPolicyFilter();
		filter.setType(IdmPasswordPolicyType.GENERATE);
		filter.setDefaultPolicy(Boolean.TRUE);
		
		List<IdmPasswordPolicyDto> results = find(filter);
		//
		// prevent to affect init data
		if (results.isEmpty()) {
			// create and delete default policy
			IdmPasswordPolicyDto policy = prepareDto();
			policy.setType(IdmPasswordPolicyType.GENERATE);
			policy.setDefaultPolicy(true);
			IdmPasswordPolicyDto policyOne = createDto(policy);
			//
			try {
				results = find(filter);
				Assert.assertTrue(results.stream().allMatch(p -> p.isDefaultPolicy()));
				Assert.assertTrue(results.stream().anyMatch(p -> p.getId().equals(policyOne.getId())));
			} finally {
				service.delete(policyOne);
			}
		} else {
			// default policy is initialized by init data
			Assert.assertTrue(results.stream().allMatch(p -> p.isDefaultPolicy()));
		}
	}
}
