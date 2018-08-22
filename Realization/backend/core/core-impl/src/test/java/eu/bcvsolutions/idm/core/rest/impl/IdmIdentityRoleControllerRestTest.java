package eu.bcvsolutions.idm.core.rest.impl;

import java.util.List;

import org.joda.time.LocalDate;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoControllerRestTest;

/**
 * Controller tests
 * - CRUD
 * - filter tests
 * 
 * @author Radek Tomiška
 *
 */
public class IdmIdentityRoleControllerRestTest extends AbstractReadWriteDtoControllerRestTest<IdmIdentityRoleDto> {

	@Autowired private IdmIdentityRoleController controller;
	
	@Override
	protected AbstractReadWriteDtoController<IdmIdentityRoleDto, ?> getController() {
		return controller;
	}
	
	@Override
	protected boolean isReadOnly() {
		return true;
	}

	@Override
	protected IdmIdentityRoleDto prepareDto() {
		IdmIdentityRoleDto dto = new IdmIdentityRoleDto();
		dto.setIdentityContractDto(getHelper().getPrimeContract(getHelper().createIdentity().getId()));
		dto.setRole(getHelper().createRole().getId());
		dto.setValidFrom(new LocalDate());
		dto.setValidTill(new LocalDate().plusDays(1));
		return dto;
	}
	
	@Test
	public void findByText() {
		// username
		IdmIdentityDto identity = getHelper().createIdentity();
		IdmIdentityRoleDto createIdentityRole = getHelper().createIdentityRole(identity, getHelper().createRole());
		IdmIdentityDto other = getHelper().createIdentity();
		getHelper().createIdentityRole(other, getHelper().createRole());
		//
		IdmIdentityRoleFilter filter = new IdmIdentityRoleFilter();
		filter.setText(identity.getUsername());
		List<IdmIdentityRoleDto> results = find(filter);
		//
		Assert.assertEquals(1, results.size());
		Assert.assertTrue(results.stream().anyMatch(ir -> ir.getId().equals(createIdentityRole.getId())));
	}
	
	@Test
	public void findInvalidRoles() {
		IdmIdentityDto identity = getHelper().createIdentity();
		getHelper().createIdentityRole(identity, getHelper().createRole()); // valid
		IdmIdentityRoleDto inValidByDate = getHelper().createIdentityRole(identity, getHelper().createRole(), null, LocalDate.now().minusDays(2));
		IdmIdentityContractDto invalidContract = getHelper().createIdentityContact(identity, null, null, LocalDate.now().minusDays(2));
		IdmIdentityRoleDto inValidByContract = getHelper().createIdentityRole(invalidContract, getHelper().createRole());
		//
		IdmIdentityRoleFilter filter = new IdmIdentityRoleFilter();
		filter.setIdentityId(identity.getId());
		filter.setValid(Boolean.FALSE);
		List<IdmIdentityRoleDto> results = find(filter);
		//
		Assert.assertEquals(2, results.size());
		Assert.assertTrue(results.stream().anyMatch(ir -> ir.getId().equals(inValidByDate.getId())));
		Assert.assertTrue(results.stream().anyMatch(ir -> ir.getId().equals(inValidByContract.getId())));
	}
	
	@Test
	public void findAutomaticRoles() {
		IdmIdentityContractDto contract = getHelper().getPrimeContract(getHelper().createIdentity().getId());
		IdmIdentityRoleDto normal = getHelper().createIdentityRole(contract, getHelper().createRole()); // normal
		// automatic
		IdmIdentityRoleDto automaticIdentityRole = new IdmIdentityRoleDto();
		automaticIdentityRole.setIdentityContract(contract.getId());
		automaticIdentityRole.setRole(getHelper().createRole().getId());
		automaticIdentityRole.setAutomaticRole(getHelper().createAutomaticRole(getHelper().createRole().getId()).getId());
		IdmIdentityRoleDto automatic = createDto(automaticIdentityRole);
		//
		IdmIdentityRoleFilter filter = new IdmIdentityRoleFilter();
		filter.setIdentityContractId(contract.getId());
		filter.setAutomaticRole(Boolean.TRUE);
		List<IdmIdentityRoleDto> results = find(filter);
		//
		Assert.assertEquals(1, results.size());
		Assert.assertTrue(results.stream().anyMatch(ir -> ir.getId().equals(automatic.getId())));
		//
		filter.setAutomaticRole(Boolean.FALSE);
		results = find(filter);
		//
		Assert.assertEquals(1, results.size());
		Assert.assertTrue(results.stream().anyMatch(ir -> ir.getId().equals(normal.getId())));
	}
	
	@Test
	public void findDirectRoles() {
		IdmIdentityContractDto contract = getHelper().getPrimeContract(getHelper().createIdentity().getId());
		IdmIdentityRoleDto normal = getHelper().createIdentityRole(contract, getHelper().createRole()); // normal
		// not direct
		IdmIdentityRoleDto notDirectIdentityRole = new IdmIdentityRoleDto();
		notDirectIdentityRole.setIdentityContract(contract.getId());
		notDirectIdentityRole.setRole(getHelper().createRole().getId());
		notDirectIdentityRole.setDirectRole(normal.getId());
		IdmIdentityRoleDto notDirect = createDto(notDirectIdentityRole);
		//
		IdmIdentityRoleFilter filter = new IdmIdentityRoleFilter();
		filter.setIdentityContractId(contract.getId());
		filter.setDirectRole(Boolean.TRUE);
		List<IdmIdentityRoleDto> results = find(filter);
		//
		Assert.assertEquals(1, results.size());
		Assert.assertTrue(results.stream().anyMatch(ir -> ir.getId().equals(normal.getId())));
		//
		filter.setDirectRole(Boolean.FALSE);
		results = find(filter);
		//
		Assert.assertEquals(1, results.size());
		Assert.assertTrue(results.stream().anyMatch(ir -> ir.getId().equals(notDirect.getId())));
	}
}
