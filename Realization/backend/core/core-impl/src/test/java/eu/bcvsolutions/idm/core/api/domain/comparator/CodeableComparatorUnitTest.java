package eu.bcvsolutions.idm.core.api.domain.comparator;

import java.util.ArrayList;
import java.util.List;

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
public class CodeableComparatorUnitTest extends AbstractUnitTest {

	@Test
	public void testCompareEquals() {
		List<IdmIdentityDto> identities = new ArrayList<>();
		IdmIdentityDto one = new IdmIdentityDto("one");
		IdmIdentityDto two = new IdmIdentityDto("one");
		identities.add(one);
		identities.add(two);
		
		identities.sort(new CodeableComparator());
		
		Assert.assertEquals(one.getId(), identities.get(0).getId());
		Assert.assertEquals(two.getId(), identities.get(1).getId());
		
		identities.sort(new CodeableComparator(false));
		
		Assert.assertEquals(one.getId(), identities.get(0).getId());
		Assert.assertEquals(two.getId(), identities.get(1).getId());
	}
	
	@Test
	public void testCompareAsc() {
		List<IdmIdentityDto> identities = new ArrayList<>();
		IdmIdentityDto one = new IdmIdentityDto("one");
		IdmIdentityDto two = new IdmIdentityDto("one");
		identities.add(one);
		identities.add(two);
		
		identities.sort(new CodeableComparator());
		
		Assert.assertEquals(one.getId(), identities.get(0).getId());
		Assert.assertEquals(two.getId(), identities.get(1).getId());
		
		identities.sort(new CodeableComparator(false));
		
		Assert.assertEquals(two.getId(), identities.get(0).getId());
		Assert.assertEquals(one.getId(), identities.get(1).getId());
	}
}
