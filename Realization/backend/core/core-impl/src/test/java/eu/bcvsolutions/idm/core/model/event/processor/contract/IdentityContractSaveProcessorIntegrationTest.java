package eu.bcvsolutions.idm.core.model.event.processor.contract;

import static org.junit.Assert.fail;

import java.time.LocalDate;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.IdmContractSliceDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.IdmContractSliceService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.model.entity.IdmContractSlice_;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Save and update operation of identity contract.
 * Test contract modification when contract is controlled by time slice.
 * 
 * @author Ondrej Husnik
 */
@Transactional
public class IdentityContractSaveProcessorIntegrationTest extends AbstractIntegrationTest {
	
	@Autowired
	private IdmIdentityContractService contractService;
	@Autowired
	private IdmContractSliceService sliceService;
	
	@Before
	public void init() {
		getHelper().loginAdmin();
	}
	
	@After
	public void logout() {
		getHelper().logout();
	}
	
	@Test
	public void contractUpdateUnderSliceControl() {
		IdmIdentityDto identity = getHelper().createIdentity();
		List<IdmIdentityContractDto> contracts = contractService.findAllByIdentity(identity.getId());
		Assert.assertEquals(1,contracts.size());
		
		// contract without slices successfully saved
		IdmIdentityContractDto contractWithoutSlice = contracts.get(0);
		contractWithoutSlice.setValidFrom(LocalDate.now().minusDays(1));
		contractWithoutSlice = contractService.save(contractWithoutSlice);
		Assert.assertEquals(LocalDate.now().minusDays(1), contractWithoutSlice.getValidFrom());
		
		IdmContractSliceDto slice = getHelper().createContractSlice(identity, null, LocalDate.now().minusDays(1), null, null);
		IdmIdentityContractDto contractWithSlice = getLookupService().lookupEmbeddedDto(slice, IdmContractSlice_.parentContract);
		// modification of a contract controlled by slice has to throw
		contractWithSlice.setValidFrom(LocalDate.now().minusDays(1));
		try {
			contractService.save(contractWithSlice);
			fail();
		} catch (ResultCodeException ex) {
			Assert.assertTrue(CoreResultCode.CONTRACT_IS_CONTROLLED_CANNOT_BE_MODIFIED.toString()
					.equals(ex.getError().getErrors().get(0).getStatusEnum()));
		}
		
		// updated slice
		slice.setContractValidFrom(LocalDate.now().minusDays(2));
		slice = sliceService.save(slice);
		Assert.assertEquals(LocalDate.now().minusDays(2), slice.getContractValidFrom());
		
		// slice controlled contract is supposed to be also updated 
		contractWithSlice = contractService.get(contractWithSlice);
		Assert.assertEquals(LocalDate.now().minusDays(2), contractWithSlice.getValidFrom());
	}
}
