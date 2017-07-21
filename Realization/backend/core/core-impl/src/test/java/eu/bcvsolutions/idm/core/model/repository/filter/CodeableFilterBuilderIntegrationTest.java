package eu.bcvsolutions.idm.core.model.repository.filter;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.TestHelper;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * UuidFilterBuilder test
 * 
 * @author Radek Tomiška
 *
 */
public class CodeableFilterBuilderIntegrationTest extends AbstractIntegrationTest {
	
	@Autowired private TestHelper helper;
	@Autowired private CodeableFilter<IdmIdentity> identityFilter;
	
	@Test
	public void testFindIdentityByUuid() {
		// prepare data
		IdmIdentityDto identityOne = helper.createIdentity();
		IdmIdentityDto identityTwo = helper.createIdentity();
		IdmRole roleOne = helper.createRole();
		//
		DataFilter dataFilter = new DataFilter(IdmIdentityDto.class);
		dataFilter.setCodeableIdentifier(identityOne.getId().toString());
		List<IdmIdentity> identities = identityFilter.find(dataFilter, null).getContent();
		//
		assertEquals(1, identities.size());
		assertEquals(identityOne.getId(), identities.get(0).getId());
		//
		dataFilter.setCodeableIdentifier(identityTwo.getId().toString());
		identities = identityFilter.find(dataFilter, null).getContent();
		assertEquals(1, identities.size());
		assertEquals(identityTwo.getId(), identities.get(0).getId());
		//
		dataFilter.setCodeableIdentifier(roleOne.getId().toString());
		assertEquals(0, identityFilter.find(dataFilter, null).getTotalElements());
	}
	
	@Test
	public void testFindIdentityByUsername() {
		// prepare data
		IdmIdentityDto identityOne = helper.createIdentity();
		IdmIdentityDto identityTwo = helper.createIdentity();
		IdmRole roleOne = helper.createRole();
		//
		DataFilter dataFilter = new DataFilter(IdmIdentityDto.class);
		dataFilter.setCodeableIdentifier(identityOne.getUsername());
		List<IdmIdentity> identities = identityFilter.find(dataFilter, null).getContent();
		//
		assertEquals(1, identities.size());
		assertEquals(identityOne.getId(), identities.get(0).getId());
		//
		dataFilter.setCodeableIdentifier(identityTwo.getUsername());
		identities = identityFilter.find(dataFilter, null).getContent();
		assertEquals(1, identities.size());
		assertEquals(identityTwo.getId(), identities.get(0).getId());
		//
		dataFilter.setCodeableIdentifier(roleOne.getId().toString());
		assertEquals(0, identityFilter.find(dataFilter, null).getTotalElements());
	}
}
