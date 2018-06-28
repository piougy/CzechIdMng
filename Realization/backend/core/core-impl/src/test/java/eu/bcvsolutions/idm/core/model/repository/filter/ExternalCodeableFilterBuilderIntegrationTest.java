package eu.bcvsolutions.idm.core.model.repository.filter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.domain.ExternalCodeable;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleGuaranteeFilter;
import eu.bcvsolutions.idm.core.api.exception.DuplicateExternalCodeException;
import eu.bcvsolutions.idm.core.api.exception.EntityTypeNotExternalCodeableException;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleGuaranteeService;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * ExternalCodeableFilterBuilder test
 * 
 * @author Radek Tomiška
 *
 */
@Transactional
public class ExternalCodeableFilterBuilderIntegrationTest extends AbstractIntegrationTest {
	
	@Autowired private IdmIdentityService identityService;
	@Autowired private IdmRoleGuaranteeService roleGuaranteeService;

	@Test
	public void testFindIdentityByExternalCode() {
		// prepare data
		IdmIdentityDto identityOne = getHelper().createIdentity((GuardedString) null);
		identityOne.setExternalCode("one");
		identityOne = identityService.save(identityOne);
		IdmIdentityDto identityTwo = getHelper().createIdentity((GuardedString) null);
		identityTwo.setExternalCode("two");
		identityTwo = identityService.save(identityTwo); 
		//
		IdmIdentityFilter identityFilter = new IdmIdentityFilter();
		identityFilter.setExternalCode(identityOne.getExternalCode());
		List<IdmIdentityDto> identities = identityService.find(identityFilter, null).getContent();
		//
		assertEquals(1, identities.size());
		assertEquals(identityOne.getId(), identities.get(0).getId());
		//
		identityFilter.setExternalCode(identityTwo.getExternalCode());
		identities = identityService.find(identityFilter, null).getContent();
		assertEquals(1, identities.size());
		assertEquals(identityTwo.getId(), identities.get(0).getId());

	}
	
	@Test(expected = EntityTypeNotExternalCodeableException.class)
	public void testTryFindNotExternalCodeentifiableEntity() {
		MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
		params.set(ExternalCodeable.PROPERTY_EXTERNAL_CODE, "wrong");
		IdmRoleGuaranteeFilter filter = new IdmRoleGuaranteeFilter(params);
		roleGuaranteeService.find(filter, null);
	}
	
	@Test
	public void testUpdateExternalCode() {
		// prepare data
		IdmIdentityDto identityOne = getHelper().createIdentity((GuardedString) null);
		identityOne.setExternalCode("one");
		identityOne = identityService.save(identityOne);
		IdmIdentityDto identityTwo = getHelper().createIdentity((GuardedString) null);
		
		identityTwo.setExternalCode("two");
		identityTwo = identityService.save(identityTwo); 
		
		IdmIdentityFilter identityFilter = new IdmIdentityFilter();
		identityFilter.setExternalCode(identityTwo.getExternalCode());
		List<IdmIdentityDto> identities = identityService.find(identityFilter, null).getContent();
		assertEquals(1, identities.size());
		assertEquals(identityTwo.getId(), identities.get(0).getId());
		
		identityTwo.setExternalCode("three");
		identityTwo = identityService.save(identityTwo); 
		identityFilter.setExternalCode(identityTwo.getExternalCode());
		identities = identityService.find(identityFilter, null).getContent();
		assertEquals(1, identities.size());
		assertEquals(identityTwo.getId(), identities.get(0).getId());
		
		identityTwo.setExternalCode("two");
		identityTwo = identityService.save(identityTwo);
		identityFilter.setExternalCode(identityTwo.getExternalCode());
		identities = identityService.find(identityFilter, null).getContent();
		assertEquals(1, identities.size());
		assertEquals(identityTwo.getId(), identities.get(0).getId());
	}
	
	@Test
	public void testCreateDuplicateExternalId() {
		// prepare data
		IdmIdentityDto identityOne = getHelper().createIdentity((GuardedString) null);
		identityOne.setExternalCode("one");
		identityOne = identityService.save(identityOne);
		IdmIdentityDto identityThree = getHelper().createIdentity((GuardedString) null);
		identityThree.setExternalCode(null);
		identityService.save(identityThree);
		IdmIdentityDto identityTwo = getHelper().createIdentity((GuardedString) null);
		identityTwo.setExternalCode("one");
		try {
			identityTwo = identityService.save(identityTwo); 
			fail();
		} catch (DuplicateExternalCodeException ex) {
			assertEquals(identityOne.getId(), ex.getDuplicateId());
		}		
	}
	
}
