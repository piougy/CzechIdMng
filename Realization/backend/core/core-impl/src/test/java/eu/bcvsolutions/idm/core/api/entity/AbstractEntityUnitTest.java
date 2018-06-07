package eu.bcvsolutions.idm.core.api.entity;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.test.api.AbstractUnitTest;

/**
 * Base entity method unit tests
 * 
 * @author Radek Tomiška
 *
 */
public class AbstractEntityUnitTest extends AbstractUnitTest {

	@Test
	public void testEqualsAndHashCode() {
		AbstractEntity entityOne = new IdmIdentity();
		AbstractEntity entityTwo = new IdmIdentity();
		//
		Assert.assertFalse(entityOne.equals(entityTwo));
		Assert.assertFalse(entityOne.equals(null));
		Assert.assertEquals(entityOne.hashCode(), entityTwo.hashCode());
		//
		Object mockResource = Mockito.mock(IdmIdentity.class);
		//
		Assert.assertTrue(mockResource.equals(mockResource));
		Assert.assertFalse(mockResource.equals(null));
		//
		entityOne = new IdmIdentity(UUID.randomUUID());
		entityTwo = new IdmIdentity(UUID.randomUUID());
		//
		Assert.assertFalse(entityOne.equals(entityTwo));
		Assert.assertNotEquals(entityOne.hashCode(), entityTwo.hashCode());
		Assert.assertNotEquals(entityOne.toString(), entityTwo.toString());
		//
		entityOne = new IdmIdentity(UUID.randomUUID());
		entityTwo = new IdmIdentity(entityOne.getId());
		//
		Assert.assertTrue(entityOne.equals(entityTwo));
		Assert.assertEquals(entityOne.hashCode(), entityTwo.hashCode());
		Assert.assertEquals(entityOne.toString(), entityTwo.toString());
		//
		entityOne = new IdmIdentity(UUID.randomUUID());
		entityTwo = new IdmIdentity();
		//
		Assert.assertFalse(entityOne.equals(entityTwo));
		Assert.assertNotEquals(entityOne.hashCode(), entityTwo.hashCode());
		Assert.assertNotEquals(entityOne.toString(), entityTwo.toString());
		//
		entityOne = new IdmIdentity();
		entityTwo = new IdmIdentity(UUID.randomUUID());
		//
		Assert.assertFalse(entityOne.equals(entityTwo));
		Assert.assertNotEquals(entityOne.hashCode(), entityTwo.hashCode());
		Assert.assertNotEquals(entityOne.toString(), entityTwo.toString());
	}
}
