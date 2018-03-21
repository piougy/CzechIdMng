package eu.bcvsolutions.idm.core.api.domain.comparator;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.test.api.AbstractUnitTest;

/**
 * Comparator test
 * 
 * @author Radek Tomiška
 *
 */
public class CreatedComparatorUnitTest extends AbstractUnitTest {

	@Test
	public void testCompareEquals() {
		List<IdmIdentityDto> identities = new ArrayList<>();
		DateTime created = new DateTime();
		IdmIdentityDto one = new IdmIdentityDto(UUID.randomUUID());
		one.setCreated(created);
		IdmIdentityDto two = new IdmIdentityDto(UUID.randomUUID());
		two.setCreated(created);
		identities.add(one);
		identities.add(two);
		
		identities.sort(new CreatedComparator());
		
		Assert.assertEquals(one.getId(), identities.get(0).getId());
		Assert.assertEquals(two.getId(), identities.get(1).getId());
		
		identities.sort(new CreatedComparator(false));
		
		Assert.assertEquals(one.getId(), identities.get(0).getId());
		Assert.assertEquals(two.getId(), identities.get(1).getId());
	}
	
	@Test
	public void testCompareAsc() {
		List<IdmIdentityDto> identities = new ArrayList<>();
		DateTime created = new DateTime();
		IdmIdentityDto one = new IdmIdentityDto(UUID.randomUUID());
		one.setCreated(created.minusSeconds(1));
		IdmIdentityDto two = new IdmIdentityDto(UUID.randomUUID());
		two.setCreated(created);
		identities.add(one);
		identities.add(two);
		
		identities.sort(new CreatedComparator());
		
		Assert.assertEquals(one.getId(), identities.get(0).getId());
		Assert.assertEquals(two.getId(), identities.get(1).getId());
		
		identities.sort(new CreatedComparator(false));
		
		Assert.assertEquals(two.getId(), identities.get(0).getId());
		Assert.assertEquals(one.getId(), identities.get(1).getId());
	}
}
