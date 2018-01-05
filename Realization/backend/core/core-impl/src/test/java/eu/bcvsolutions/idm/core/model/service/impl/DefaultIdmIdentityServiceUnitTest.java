package eu.bcvsolutions.idm.core.model.service.impl;

import static org.mockito.Mockito.when;

import java.util.UUID;

import org.joda.time.LocalDate;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.modelmapper.ModelMapper;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.config.domain.RoleConfiguration;
import eu.bcvsolutions.idm.core.api.domain.ContractState;
import eu.bcvsolutions.idm.core.api.domain.IdentityState;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.repository.IdmAuthorityChangeRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRepository;
import eu.bcvsolutions.idm.test.api.AbstractUnitTest;

/**
 * Identity service unit tests
 * - nice label
 * 
 * @author Radek Tomiška
 *
 */
public class DefaultIdmIdentityServiceUnitTest extends AbstractUnitTest {

	@Mock private IdmIdentityRepository repository;
	@Mock private IdmRoleService roleService;
	@Mock private IdmAuthorityChangeRepository authChangeRepository;
	@Mock private EntityEventManager entityEventManager;
	@Mock private RoleConfiguration roleConfiguration;
	@Mock private FormService formService;
	@Mock private IdmIdentityContractService identityContractService;
	@Spy 
	private ModelMapper modelMapper = new ModelMapper();
	//
	@InjectMocks 
	private DefaultIdmIdentityService service;
	
	@Test
	public void testNiceLabelWithNull() {
		Assert.assertNull(service.getNiceLabel(null));
	}
	
	@Test
	public void testNiceLabelWithUsernameOnly() {
		IdmIdentityDto identity = new IdmIdentityDto();
		String username = "validation_test_" + System.currentTimeMillis();
		identity.setUsername(username);
		//
		Assert.assertEquals(username, service.getNiceLabel(identity));
	}
	
	@Test
	public void testNiceLabelWithTitlesAndFirstnameOnly() {
		IdmIdentityDto identity = new IdmIdentityDto();
		String username = "validation_test_" + System.currentTimeMillis();
		identity.setUsername(username);
		identity.setFirstName("firstname");
		identity.setTitleAfter("csc.");
		identity.setTitleBefore("Bc.");
		//
		Assert.assertEquals(username, service.getNiceLabel(identity));
	}
	
	@Test
	public void testNiceLabelWithLastnameOnly() {
		IdmIdentityDto identity = new IdmIdentityDto();
		identity.setLastName("lastName");
		//
		Assert.assertEquals(identity.getLastName(), service.getNiceLabel(identity));
	}
	
	@Test
	public void testNiceLabelWithFirstnameLastName() {
		IdmIdentityDto identity = new IdmIdentityDto();
		identity.setFirstName("firstname");
		identity.setLastName("lastName");
		//
		Assert.assertEquals(String.format("%s %s", 
				identity.getFirstName(),
				identity.getLastName()), 
				service.getNiceLabel(identity));
	}
	
	@Test
	public void testNiceLabelWithFullName() {
		IdmIdentityDto identity = new IdmIdentityDto();
		identity.setFirstName("firstname");
		identity.setLastName("lastName");
		identity.setTitleAfter("csc.");
		identity.setTitleBefore("Bc.");
		//
		Assert.assertEquals(String.format("%s %s %s, %s", 
				identity.getTitleBefore(),
				identity.getFirstName(),
				identity.getLastName(),
				identity.getTitleAfter()), 
				service.getNiceLabel(identity));
	}
	
	
	@Test
	public void testValidState() {
		IdmIdentity identity = new IdmIdentity(UUID.randomUUID());
		IdmIdentityContractDto contractOne = new IdmIdentityContractDto();
		contractOne.setState(ContractState.DISABLED);
		IdmIdentityContractDto contractTwo = new IdmIdentityContractDto();
		when(repository.findOne(identity.getId())).thenReturn(identity);	
		when(identityContractService.findAllByIdentity(identity.getId())).thenReturn(Lists.newArrayList(contractOne, contractTwo));	
		//
		Assert.assertEquals(IdentityState.VALID, service.evaluateState(identity.getId()));
	}
	
	@Test
	public void testDisabledState() {
		IdmIdentity identity = new IdmIdentity(UUID.randomUUID());
		IdmIdentityContractDto contractOne = new IdmIdentityContractDto();
		contractOne.setState(ContractState.DISABLED);
		IdmIdentityContractDto contractTwo = new IdmIdentityContractDto();
		contractTwo.setState(ContractState.DISABLED);
		when(repository.findOne(identity.getId())).thenReturn(identity);	
		when(identityContractService.findAllByIdentity(identity.getId())).thenReturn(Lists.newArrayList(contractOne, contractTwo));	
		//
		Assert.assertEquals(IdentityState.LEFT, service.evaluateState(identity.getId()));
	}
	
	@Test
	public void testNoContractState() {
		IdmIdentity identity = new IdmIdentity(UUID.randomUUID());
		when(repository.findOne(identity.getId())).thenReturn(identity);	
		when(identityContractService.findAllByIdentity(identity.getId())).thenReturn(Lists.newArrayList());	
		//
		Assert.assertEquals(IdentityState.NO_CONTRACT, service.evaluateState(identity.getId()));
	}	
	
	@Test
	public void testDisabledManuallyState() {
		IdmIdentity identity = new IdmIdentity(UUID.randomUUID());
		identity.setState(IdentityState.DISABLED_MANUALLY);
		IdmIdentityContractDto contractOne = new IdmIdentityContractDto();
		IdmIdentityContractDto contractTwo = new IdmIdentityContractDto();
		when(repository.findOne(identity.getId())).thenReturn(identity);	
		when(identityContractService.findAllByIdentity(identity.getId())).thenReturn(Lists.newArrayList(contractOne, contractTwo));	
		//
		Assert.assertEquals(IdentityState.DISABLED_MANUALLY, service.evaluateState(identity.getId()));
	}
	
	@Test
	public void testFutureContractState() {
		IdmIdentity identity = new IdmIdentity(UUID.randomUUID());
		IdmIdentityContractDto contractOne = new IdmIdentityContractDto();
		contractOne.setState(ContractState.DISABLED);
		IdmIdentityContractDto contractTwo = new IdmIdentityContractDto();
		contractTwo.setValidFrom(new LocalDate().plusDays(1));
		when(repository.findOne(identity.getId())).thenReturn(identity);	
		when(identityContractService.findAllByIdentity(identity.getId())).thenReturn(Lists.newArrayList(contractOne, contractTwo));	
		//
		Assert.assertEquals(IdentityState.FUTURE_CONTRACT, service.evaluateState(identity.getId()));
	}
}
