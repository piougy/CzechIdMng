package eu.bcvsolutions.idm.core.model.event.processor.contract;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.api.dto.IdmContractGuaranteeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmContractGuaranteeFilter;
import eu.bcvsolutions.idm.core.api.service.IdmContractGuaranteeService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormProjectionDto;
import eu.bcvsolutions.idm.core.eav.api.service.IdmFormProjectionService;
import eu.bcvsolutions.idm.core.eav.service.impl.IdentityFormProjectionRoute;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Add currently logged user as identity contract guarantee, when new contract is created with projection with enabled direct guarantees.
 * 
 * @author Radek Tomiška
 */
@Transactional
public class IdentityContractAddGuaranteeByProjectionProcessorIntegrationTest extends AbstractIntegrationTest {
	
	@Autowired private IdmFormProjectionService formProjectionService;
	@Autowired private LookupService lookupService;
	@Autowired private IdmIdentityService identityService;
	@Autowired private IdmContractGuaranteeService contractGuaranteeService;
	
	@Test
	public void testGuaranteeIsCreated() {
		IdmIdentityDto manager = getHelper().createIdentity();
		// create identity with projection
		IdmIdentityDto identity = new IdmIdentityDto(getHelper().createName());
		identity.setFormProjection(createProjection(true).getId());
		identity = identityService.save(identity);
		//
		try {
			getHelper().login(manager);
			//
			// create new contract for identity with projection
			IdmIdentityContractDto contract = getHelper().createContract(identity);
			//
			IdmContractGuaranteeFilter filter = new IdmContractGuaranteeFilter();
			filter.setIdentityContractId(contract.getId());
			filter.setGuaranteeId(manager.getId());
			//
			List<IdmContractGuaranteeDto> guarantees = contractGuaranteeService.find(filter, null).getContent();
			Assert.assertEquals(1, guarantees.size());
			Assert.assertTrue(guarantees.stream().allMatch(g -> g.getGuarantee().equals(manager.getId())));
		} finally {
			logout();
		}
	}
	
	@Test
	public void testGuaranteeIsNotCreatedWithoutLogin() {
		// create identity with projection
		IdmIdentityDto identity = new IdmIdentityDto(getHelper().createName());
		identity.setFormProjection(createProjection(true).getId());
		identity = identityService.save(identity);
		//
		// create new contract for identity with projection
		IdmIdentityContractDto contract = getHelper().createContract(identity);
		//
		IdmContractGuaranteeFilter filter = new IdmContractGuaranteeFilter();
		filter.setIdentityContractId(contract.getId());
		//
		List<IdmContractGuaranteeDto> guarantees = contractGuaranteeService.find(filter, null).getContent();
		Assert.assertTrue(guarantees.isEmpty());
	}
	
	@Test
	public void testGuaranteeIsNotCreatedWithoutProjection() {
		IdmIdentityDto manager = getHelper().createIdentity();
		// create identity without projection
		IdmIdentityDto identity = getHelper().createIdentity();
		try {
			getHelper().login(manager);
			//
			// create new contract for identity without projection
			IdmIdentityContractDto contract = getHelper().createContract(identity);
			//
			IdmContractGuaranteeFilter filter = new IdmContractGuaranteeFilter();
			filter.setIdentityContractId(contract.getId());
			//
			List<IdmContractGuaranteeDto> guarantees = contractGuaranteeService.find(filter, null).getContent();
			Assert.assertTrue(guarantees.isEmpty());
		} finally {
			logout();
		}
	}
	
	@Test
	public void testGuaranteeIsNotCreatedWithProjectionNotConfigured() {
		IdmIdentityDto manager = getHelper().createIdentity();
		// create identity without projection
		IdmIdentityDto identity = new IdmIdentityDto(getHelper().createName());
		identity.setFormProjection(createProjection(false).getId());
		identity = identityService.save(identity);
		try {
			getHelper().login(manager);
			//
			// create new contract for identity with projection with not enabled guarantees
			IdmIdentityContractDto contract = getHelper().createContract(identity);
			//
			IdmContractGuaranteeFilter filter = new IdmContractGuaranteeFilter();
			filter.setIdentityContractId(contract.getId());
			//
			List<IdmContractGuaranteeDto> guarantees = contractGuaranteeService.find(filter, null).getContent();
			Assert.assertTrue(guarantees.isEmpty());
		} finally {
			logout();
		}
	}
	
	private IdmFormProjectionDto createProjection(boolean setContractGuarantee) {
		IdmFormProjectionDto dto = new IdmFormProjectionDto();
		dto.setCode(getHelper().createName());
		dto.setOwnerType(lookupService.getOwnerType(IdmIdentityDto.class));
		dto.setRoute(IdentityFormProjectionRoute.PROJECTION_NAME);
		dto.getProperties().put(IdentityFormProjectionRoute.PARAMETER_SET_CONTRACT_GUARANTEE, setContractGuarantee);
		//
		return formProjectionService.save(dto);
	}

}
