package eu.bcvsolutions.idm.acc.event.processor.contract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Comparator;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.acc.TestHelper;
import eu.bcvsolutions.idm.acc.domain.ProvisioningEventType;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningArchiveDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysProvisioningOperationFilter;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningArchiveService;
import eu.bcvsolutions.idm.core.api.dto.IdmContractGuaranteeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmContractSliceDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.service.IdmContractGuaranteeService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Test provisioning after add or remove manually added guarantee to contract
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

public class ContractGuaranteeSaveAndDeleteProcessorTest extends AbstractIntegrationTest {

	@Autowired
	private TestHelper testHelper;
	
	@Autowired
	private IdmContractGuaranteeService contractGuaranteeService;
	
	@Autowired
	private SysProvisioningArchiveService provisioningArchiveService;
	
	@Autowired
	private IdmIdentityService identityService;
	
	@Before
	public void login() {
		this.loginAsAdmin();
	}
	
	@After
	public void logout() {
		super.logout();
	}
	
	@Test
	public void testProvisioningAfterCreateContractGuarantee() {
		SysSystemDto system = testHelper.createTestResourceSystem(true);
		//
		// check before
		SysProvisioningOperationFilter filter = new SysProvisioningOperationFilter();
		filter.setSystemId(system.getId());
		List<SysProvisioningArchiveDto> content = provisioningArchiveService.find(filter, null).getContent();
		assertEquals(0, content.size());
		//
		// create identity
		IdmIdentityDto identity = testHelper.createIdentity();
		testHelper.createIdentityAccount(system, identity);
		//
		// save identity with account, invoke provisioning = create
		identity = identityService.save(identity);
		//
		content = provisioningArchiveService.find(filter, null).getContent();
		assertEquals(1, content.size());
		SysProvisioningArchiveDto sysProvisioningArchiveDto = content.get(0);
		assertEquals(ProvisioningEventType.CREATE, sysProvisioningArchiveDto.getOperationType());
		assertEquals(SystemEntityType.IDENTITY, sysProvisioningArchiveDto.getEntityType());
		assertEquals(identity.getId(), sysProvisioningArchiveDto.getEntityIdentifier());
		//
		IdmIdentityDto guarantee = testHelper.createIdentity();
		IdmIdentityContractDto primeContract = testHelper.getPrimeContract(identity.getId());
		testHelper.createContractGuarantee(primeContract.getId(), guarantee.getId());
		//
		// check after create contract
		content = provisioningArchiveService.find(filter, null).getContent();
		assertEquals(2, content.size());
		sysProvisioningArchiveDto = content.stream().max(Comparator.comparing(SysProvisioningArchiveDto::getCreated)).orElse(null);
		assertEquals(ProvisioningEventType.UPDATE, sysProvisioningArchiveDto.getOperationType());
		assertEquals(SystemEntityType.IDENTITY, sysProvisioningArchiveDto.getEntityType());
		assertEquals(identity.getId(), sysProvisioningArchiveDto.getEntityIdentifier());
	}
	
	@Test
	public void testProvisioningAfterUpdateContractGuarantee() {
		SysSystemDto system = testHelper.createTestResourceSystem(true);
		//
		IdmIdentityDto identity = testHelper.createIdentity();
		testHelper.createIdentityAccount(system, identity);
		//
		// save identity with account, invoke provisioning = create
		identity = identityService.save(identity);
		//
		IdmIdentityDto guarantee = testHelper.createIdentity();
		IdmIdentityContractDto primeContract = testHelper.getPrimeContract(identity.getId());
		IdmContractGuaranteeDto contractGuarantee = testHelper.createContractGuarantee(primeContract.getId(), guarantee.getId());
		//
		IdmIdentityDto newGuarantee = testHelper.createIdentity();
		contractGuarantee.setGuarantee(newGuarantee.getId());
		// save/update
		contractGuarantee = contractGuaranteeService.save(contractGuarantee);
		//
		SysProvisioningOperationFilter filter = new SysProvisioningOperationFilter();
		filter.setSystemId(system.getId());
		List<SysProvisioningArchiveDto> content = provisioningArchiveService.find(filter, null).getContent();
		assertEquals(3, content.size()); // create, add contract guarantee and update = 3 operation
		// sort by created and found last
		SysProvisioningArchiveDto last = content.stream().max(Comparator.comparing(SysProvisioningArchiveDto::getCreated)).orElse(null);;
		assertNotNull(last);
		assertEquals(ProvisioningEventType.UPDATE, last.getOperationType());
		assertEquals(SystemEntityType.IDENTITY, last.getEntityType());
		assertEquals(identity.getId(), last.getEntityIdentifier());
	}
	
	@Test
	public void testProvisioningAfterDeleteContractGuarantee() {
		SysSystemDto system = testHelper.createTestResourceSystem(true);
		//
		IdmIdentityDto identity = testHelper.createIdentity();
		testHelper.createIdentityAccount(system, identity);
		//
		// save identity with account, invoke provisioning = create
		identity = identityService.save(identity);
		//
		IdmIdentityDto guarantee = testHelper.createIdentity();
		IdmIdentityContractDto primeContract = testHelper.getPrimeContract(identity.getId());
		IdmContractGuaranteeDto contractGuarantee = testHelper.createContractGuarantee(primeContract.getId(), guarantee.getId());
		// delete
		contractGuaranteeService.delete(contractGuarantee);
		//
		SysProvisioningOperationFilter filter = new SysProvisioningOperationFilter();
		filter.setSystemId(system.getId());
		List<SysProvisioningArchiveDto> content = provisioningArchiveService.find(filter, null).getContent();
		assertEquals(3, content.size()); // create, add contract guarantee and delete = 3 operation
		SysProvisioningArchiveDto last = content.stream().max(Comparator.comparing(SysProvisioningArchiveDto::getCreated)).get();
		assertEquals(ProvisioningEventType.UPDATE, last.getOperationType());
		assertEquals(SystemEntityType.IDENTITY, last.getEntityType());
		assertEquals(identity.getId(), last.getEntityIdentifier());
	}
	
	@Test
	public void testCreateContractGuaranteeWithoutProvisioning() {
		IdmIdentityDto identity = testHelper.createIdentity();
		//
		IdmIdentityDto guarantee = testHelper.createIdentity();
		IdmIdentityContractDto primeContract = testHelper.getPrimeContract(identity.getId());
		testHelper.createContractGuarantee(primeContract.getId(), guarantee.getId());
		//
		SysProvisioningOperationFilter filter = new SysProvisioningOperationFilter();
		filter.setEntityIdentifier(identity.getId());
		List<SysProvisioningArchiveDto> content = provisioningArchiveService.find(filter, null).getContent();
		assertEquals(0, content.size());
	}
	
	@Test
	public void testDeleteContractGuaranteeWithoutProvisioning() {
		IdmIdentityDto identity = testHelper.createIdentity();
		//
		IdmIdentityDto guarantee = testHelper.createIdentity();
		IdmIdentityContractDto primeContract = testHelper.getPrimeContract(identity.getId());
		IdmContractGuaranteeDto contractGuarantee = testHelper.createContractGuarantee(primeContract.getId(), guarantee.getId());
		// delete
		contractGuaranteeService.delete(contractGuarantee);
		//
		SysProvisioningOperationFilter filter = new SysProvisioningOperationFilter();
		filter.setEntityIdentifier(identity.getId());
		List<SysProvisioningArchiveDto> content = provisioningArchiveService.find(filter, null).getContent();
		assertEquals(0, content.size()); 
	}
	
	@Test
	public void testUpdateContractGuaranteeWithoutProvisioning() {
		IdmIdentityDto identity = testHelper.createIdentity();
		//
		IdmIdentityDto guarantee = testHelper.createIdentity();
		IdmIdentityContractDto primeContract = testHelper.getPrimeContract(identity.getId());
		IdmContractGuaranteeDto contractGuarantee = testHelper.createContractGuarantee(primeContract.getId(), guarantee.getId());
		//
		IdmIdentityDto newGuarantee = testHelper.createIdentity();
		contractGuarantee.setGuarantee(newGuarantee.getId());
		// save/update
		contractGuarantee = contractGuaranteeService.save(contractGuarantee);
		//
		SysProvisioningOperationFilter filter = new SysProvisioningOperationFilter();
		filter.setEntityIdentifier(identity.getId());
		List<SysProvisioningArchiveDto> content = provisioningArchiveService.find(filter, null).getContent();
		assertEquals(0, content.size()); 
	}
}
