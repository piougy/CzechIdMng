package eu.bcvsolutions.idm.core.model.repository.filter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.domain.ExternalIdentifiable;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleGuaranteeFilter;
import eu.bcvsolutions.idm.core.api.exception.DuplicateExternalIdException;
import eu.bcvsolutions.idm.core.api.exception.EntityTypeNotExternalIdentifiableException;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleGuaranteeService;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * ExternalIdentifiableFilterBuilder test
 * 
 * @author Radek Tomiška
 *
 */
@Transactional
public class ExternalIdentifiableFilterBuilderIntegrationTest extends AbstractIntegrationTest {
	
	@Autowired private IdmIdentityService identityService;
	@Autowired private IdmRoleGuaranteeService roleGuaranteeService;

	@Test
	public void testFindIdentityByExternalId() {
		// prepare data
		IdmIdentityDto identityOne = getHelper().createIdentity((GuardedString) null);
		identityOne.setExternalId("one");
		identityOne = identityService.save(identityOne);
		IdmIdentityDto identityTwo = getHelper().createIdentity((GuardedString) null);
		identityTwo.setExternalId("two");
		identityTwo = identityService.save(identityTwo); 
		//
		IdmIdentityFilter identityFilter = new IdmIdentityFilter();
		identityFilter.setExternalId(identityOne.getExternalId());
		List<IdmIdentityDto> identities = identityService.find(identityFilter, null).getContent();
		//
		assertEquals(1, identities.size());
		assertEquals(identityOne.getId(), identities.get(0).getId());
		//
		identityFilter.setExternalId(identityTwo.getExternalId());
		identities = identityService.find(identityFilter, null).getContent();
		assertEquals(1, identities.size());
		assertEquals(identityTwo.getId(), identities.get(0).getId());

	}
	
	@Test
	public void testCreateDuplicateExternalId() {
		// prepare data
		IdmIdentityDto identityOne = getHelper().createIdentity((GuardedString) null);
		identityOne.setExternalId("one");
		identityOne = identityService.save(identityOne);
		IdmIdentityDto identityThree = getHelper().createIdentity((GuardedString) null);
		identityThree.setExternalId(null);
		identityService.save(identityThree);
		IdmIdentityDto identityTwo = getHelper().createIdentity((GuardedString) null);
		identityTwo.setExternalId("one");
		try {
			identityTwo = identityService.save(identityTwo); 
			fail();
		} catch (DuplicateExternalIdException ex) {
			assertEquals(identityOne.getId(), ex.getDuplicateId());
		}		
	}
	
	@Test(expected = EntityTypeNotExternalIdentifiableException.class)
	public void testTryFindNotExternalIdentifiableEntity() {
		MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
		params.set(ExternalIdentifiable.PROPERTY_EXTERNAL_ID, "wrong");
		IdmRoleGuaranteeFilter filter = new IdmRoleGuaranteeFilter(params);
		roleGuaranteeService.find(filter, null);
	}
	
	@Test
	public void testUpdateExternalId() {
		// prepare data
		IdmIdentityDto identityOne = getHelper().createIdentity((GuardedString) null);
		identityOne.setExternalId("one");
		identityOne = identityService.save(identityOne);
		IdmIdentityDto identityTwo = getHelper().createIdentity((GuardedString) null);
		
		identityTwo.setExternalId("two");
		identityTwo = identityService.save(identityTwo); 
		
		IdmIdentityFilter identityFilter = new IdmIdentityFilter();
		identityFilter.setExternalId(identityTwo.getExternalId());
		List<IdmIdentityDto> identities = identityService.find(identityFilter, null).getContent();
		assertEquals(1, identities.size());
		assertEquals(identityTwo.getId(), identities.get(0).getId());
		
		identityTwo.setExternalId("three");
		identityTwo = identityService.save(identityTwo); 
		identityFilter.setExternalId(identityTwo.getExternalId());
		identities = identityService.find(identityFilter, null).getContent();
		assertEquals(1, identities.size());
		assertEquals(identityTwo.getId(), identities.get(0).getId());
		
		identityTwo.setExternalId("two");
		identityTwo = identityService.save(identityTwo);
		identityFilter.setExternalId(identityTwo.getExternalId());
		identities = identityService.find(identityFilter, null).getContent();
		assertEquals(1, identities.size());
		assertEquals(identityTwo.getId(), identities.get(0).getId());
	}
	
}
