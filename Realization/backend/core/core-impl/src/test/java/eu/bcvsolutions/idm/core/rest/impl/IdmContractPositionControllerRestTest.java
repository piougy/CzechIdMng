package eu.bcvsolutions.idm.core.rest.impl;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.dto.IdmContractPositionDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeNodeDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmContractPositionFilter;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoControllerRestTest;

/**
 * Controller tests
 * - CRUD
 * - test filters
 * 
 * @author Radek Tomiška
 *
 */
public class IdmContractPositionControllerRestTest extends AbstractReadWriteDtoControllerRestTest<IdmContractPositionDto> {

	@Autowired private IdmContractPositionController controller;
	
	@Override
	protected AbstractReadWriteDtoController<IdmContractPositionDto, ?> getController() {
		return controller;
	}

	@Override
	protected IdmContractPositionDto prepareDto() {
		IdmContractPositionDto dto = new IdmContractPositionDto();
		dto.setIdentityContract(getHelper().getPrimeContract(getHelper().createIdentity().getId()).getId());
		dto.setWorkPosition(getHelper().createTreeNode().getId());
		dto.setPosition(getHelper().createName());
		return dto;
	}
	
	@Test
	public void testFilterByContract() {
		IdmIdentityDto identity = getHelper().createIdentity();
		IdmIdentityContractDto primeContract = getHelper().getPrimeContract(identity);
		IdmIdentityContractDto otherContract = getHelper().createIdentityContact(identity);
		//
		IdmContractPositionDto dtoOne = createDto(new IdmContractPositionDto(primeContract.getId(), null));
		createDto(new IdmContractPositionDto(otherContract.getId(), null)); // other
		//
		IdmContractPositionFilter filter = new IdmContractPositionFilter();
		filter.setIdentityContractId(primeContract.getId());
		//
		List<IdmContractPositionDto> results = find(filter);
		//
		Assert.assertEquals(1, results.size());
		Assert.assertEquals(dtoOne.getId(), results.get(0).getId());
	}
	
	@Test
	public void testFilterByWorkPosition() {
		IdmIdentityDto identity = getHelper().createIdentity();
		IdmIdentityContractDto primeContract = getHelper().getPrimeContract(identity);
		//
		IdmTreeNodeDto treeNodeOne = getHelper().createTreeNode();
		IdmContractPositionDto dtoOne = createDto(new IdmContractPositionDto(primeContract.getId(), treeNodeOne.getId()));
		createDto(new IdmContractPositionDto(primeContract.getId(), getHelper().createTreeNode().getId())); // other
		//
		IdmContractPositionFilter filter = new IdmContractPositionFilter();
		filter.setWorkPosition(treeNodeOne.getId());
		//
		List<IdmContractPositionDto> results = find(filter);
		//
		Assert.assertEquals(1, results.size());
		Assert.assertEquals(dtoOne.getId(), results.get(0).getId());
	}
}
