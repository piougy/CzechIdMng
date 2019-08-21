package eu.bcvsolutions.idm.acc.rest.impl;

import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.acc.DefaultAccTestHelper;
import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.domain.ProvisioningContext;
import eu.bcvsolutions.idm.acc.domain.ProvisioningEventType;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningOperationDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysProvisioningOperationFilter;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningAttribute;
import eu.bcvsolutions.idm.acc.repository.SysProvisioningAttributeRepository;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoControllerRestTest;

/**
 * Controller tests
 * - CRUD
 * - TODO: all filters
 * 
 * @author Radek Tomiška
 *
 */
public class SysProvisioningOperationControllerRestTest extends AbstractReadWriteDtoControllerRestTest<SysProvisioningOperationDto> {

	@Autowired private SysProvisioningOperationController controller;
	@Autowired private SysProvisioningAttributeRepository provisioningAttributeRepository;
	
	@Override
	protected AbstractReadWriteDtoController<SysProvisioningOperationDto, ?> getController() {
		return controller;
	}

	@Override
	protected SysProvisioningOperationDto prepareDto() {
		SysSystemDto system = getHelper().createTestResourceSystem(false);
		//
		return prepareDto(system);
	}
	
	private SysProvisioningOperationDto prepareDto(SysSystemDto system) {
		SysProvisioningOperationDto dto = new SysProvisioningOperationDto();
		dto.setSystem(system.getId());
		dto.setEntityIdentifier(UUID.randomUUID());
		dto.setOperationType(ProvisioningEventType.CANCEL);
		dto.setEntityType(SystemEntityType.CONTRACT);
		dto.setProvisioningContext(new ProvisioningContext());
		dto.setResult(new OperationResult(OperationState.CANCELED));
		dto.setSystemEntity(getHelper().createSystemEntity(system).getId());
		//
		return dto;
	}
	
	@Override
	protected boolean isReadOnly() {
		return true;
	}
	
	@Override
	protected boolean supportsAutocomplete() {
		return false;
	}
	
	@Test
	public void testFindByAttributeUpdated() {
		SysSystemDto system = getHelper().createTestResourceSystem(false);
		String attributeOne = getHelper().createName();
		String attributeTwo = getHelper().createName();
		String attributeThree = getHelper().createName();
		// prepare provisioning operation with attributes
		SysProvisioningOperationDto operationOne = createDto(prepareDto(system));
		createAttribute(operationOne.getId(), attributeOne, false);
		createAttribute(operationOne.getId(), attributeTwo, true);
		SysProvisioningOperationDto operationTwo = createDto(prepareDto(system));
		createAttribute(operationTwo.getId(), attributeOne, true);
		createAttribute(operationTwo.getId(), attributeTwo, false);
		SysProvisioningOperationDto operationThree = createDto(prepareDto(system));
		createAttribute(operationThree.getId(), attributeTwo, false);
		createAttribute(operationThree.getId(), attributeThree, false);
		SysProvisioningOperationDto operationFour = createDto(prepareDto(system));
		createAttribute(operationFour.getId(), attributeThree, false);
		//
		SysProvisioningOperationFilter filter = new SysProvisioningOperationFilter();
		filter.setSystemId(system.getId());
		filter.setAttributeUpdated(Lists.newArrayList(attributeOne));
		List<SysProvisioningOperationDto> results = find(filter);
		//
		Assert.assertEquals(1, results.size());
		Assert.assertEquals(operationOne.getId(), results.get(0).getId());
		//
		filter.setAttributeUpdated(Lists.newArrayList(attributeOne, attributeTwo));
		results = find(filter);
		//
		Assert.assertEquals(3, results.size());
		Assert.assertTrue(results.stream().anyMatch(r -> r.getId().equals(operationOne.getId())));
		Assert.assertTrue(results.stream().anyMatch(r -> r.getId().equals(operationTwo.getId())));
		Assert.assertTrue(results.stream().anyMatch(r -> r.getId().equals(operationThree.getId())));
		//
		filter.setAttributeUpdated(Lists.newArrayList(attributeThree));
		results = find(filter);
		Assert.assertEquals(2, results.size());
		Assert.assertTrue(results.stream().anyMatch(r -> r.getId().equals(operationThree.getId())));
		Assert.assertTrue(results.stream().anyMatch(r -> r.getId().equals(operationFour.getId())));
	}
	
	@Test
	public void testFindByRemovedUpdated() {
		SysSystemDto system = getHelper().createTestResourceSystem(false);
		String attributeOne = getHelper().createName();
		String attributeTwo = getHelper().createName();
		String attributeThree = getHelper().createName();
		// prepare provisioning operation with attributes
		SysProvisioningOperationDto operationOne = createDto(prepareDto(system));
		createAttribute(operationOne.getId(), attributeOne, false);
		createAttribute(operationOne.getId(), attributeTwo, true);
		SysProvisioningOperationDto operationTwo = createDto(prepareDto(system));
		createAttribute(operationTwo.getId(), attributeOne, true);
		createAttribute(operationTwo.getId(), attributeTwo, false);
		SysProvisioningOperationDto operationThree = createDto(prepareDto(system));
		createAttribute(operationThree.getId(), attributeTwo, false);
		createAttribute(operationThree.getId(), attributeThree, false);
		SysProvisioningOperationDto operationFour = createDto(prepareDto(system));
		createAttribute(operationFour.getId(), attributeThree, false);
		//
		SysProvisioningOperationFilter filter = new SysProvisioningOperationFilter();
		filter.setSystemId(system.getId());
		filter.setAttributeRemoved(Lists.newArrayList(attributeOne));
		List<SysProvisioningOperationDto> results = find(filter);
		//
		Assert.assertEquals(1, results.size());
		Assert.assertEquals(operationTwo.getId(), results.get(0).getId());
		//
		filter.setAttributeRemoved(Lists.newArrayList(attributeOne, attributeTwo));
		results = find(filter);
		//
		Assert.assertEquals(2, results.size());
		Assert.assertTrue(results.stream().anyMatch(r -> r.getId().equals(operationOne.getId())));
		Assert.assertTrue(results.stream().anyMatch(r -> r.getId().equals(operationTwo.getId())));
		//
		filter.setAttributeRemoved(Lists.newArrayList(attributeThree));
		results = find(filter);
		Assert.assertTrue(results.isEmpty());
	}
	
	@Test
	public void testFindByEmptyProvisioning() {
		SysSystemDto system = getHelper().createTestResourceSystem(false);
		String attributeOne = getHelper().createName();
		String attributeTwo = getHelper().createName();
		// prepare provisioning operation with attributes
		// not empty
		SysProvisioningOperationDto operationOne = createDto(prepareDto(system));
		createAttribute(operationOne.getId(), attributeOne, false);
		SysProvisioningOperationDto operationTwo = createDto(prepareDto(system));
		createAttribute(operationTwo.getId(), attributeOne, true);
		createAttribute(operationTwo.getId(), attributeTwo, false);
		SysProvisioningOperationDto operation = prepareDto(system);
		operation.setOperationType(ProvisioningEventType.DELETE);
		SysProvisioningOperationDto operationThree = createDto(operation);
		// empty
		SysProvisioningOperationDto operationEmpty = createDto(prepareDto(system));
		operation = prepareDto(system);
		operation.setResult(new OperationResult(OperationState.NOT_EXECUTED));
		operation.getResult().setCode(AccResultCode.PROVISIONING_SYSTEM_READONLY.name());
		SysProvisioningOperationDto operationReadOnly = createDto(operation);
		// other are excluded from search - attributes are not evaluated
		SysProvisioningOperationDto operationFour = prepareDto(system);
		operationFour.setResult(new OperationResult(OperationState.NOT_EXECUTED));
		operationFour = createDto(operationFour);
		SysProvisioningOperationDto operationFive = prepareDto(system);
		operationFive.setResult(new OperationResult(OperationState.RUNNING));
		operationFive = createDto(operationFive);
		SysProvisioningOperationDto operationSix = prepareDto(system);
		operationSix.setResult(new OperationResult(OperationState.CREATED));
		operationSix = createDto(operationSix);
		//
		SysProvisioningOperationFilter filter = new SysProvisioningOperationFilter();
		filter.setSystemId(system.getId());
		filter.setEmptyProvisioning(Boolean.TRUE);
		List<SysProvisioningOperationDto> results = find(filter);
		//
		Assert.assertEquals(2, results.size());
		Assert.assertTrue(results.stream().anyMatch(r -> r.getId().equals(operationEmpty.getId())));
		Assert.assertTrue(results.stream().anyMatch(r -> r.getId().equals(operationReadOnly.getId())));
		//
		filter.setEmptyProvisioning(Boolean.FALSE);
		results = find(filter);
		//
		Assert.assertEquals(3, results.size());
		Assert.assertTrue(results.stream().anyMatch(r -> r.getId().equals(operationOne.getId())));
		Assert.assertTrue(results.stream().anyMatch(r -> r.getId().equals(operationTwo.getId())));
		Assert.assertTrue(results.stream().anyMatch(r -> r.getId().equals(operationThree.getId())));
	}
	
	@Override
	protected DefaultAccTestHelper getHelper() {
		return (DefaultAccTestHelper) super.getHelper();
	}
	
	private SysProvisioningAttribute createAttribute(UUID provisioningId, String name, boolean removed) {
		SysProvisioningAttribute attribute = new SysProvisioningAttribute(provisioningId, name);
		attribute.setRemoved(removed);
		//
		return provisioningAttributeRepository.save(attribute);
	}
}
