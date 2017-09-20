package eu.bcvsolutions.idm.core.api.service;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRepository;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.test.api.AbstractUnitTest;

/**
 * Basic helper methods test
 * 
 * @author Radek Tomiška
 *
 */
public class AbstractReadDtoServiceUnitTest extends AbstractUnitTest {

	@Mock private IdmIdentityRepository repository;
	//
	private AbstractReadDtoService<?, ?, ?> service;
	
	@Before
	public void init() {
		service = new TestAbstractReadDtoService(repository);
	}
	
	@Test
	public void testTrimNullWithNotNullElements() {
		BasePermission[] input = new IdmBasePermission[]{ IdmBasePermission.READ, IdmBasePermission.UPDATE };
		BasePermission[] result = service.trimNull(input);
		//
		Assert.assertArrayEquals(input, result);
	}
	
	@Test
	public void testTrimNullWithNullElements() {
		BasePermission[] input = new IdmBasePermission[]{ IdmBasePermission.READ, null, IdmBasePermission.UPDATE, null };
		BasePermission[] result = service.trimNull(input);
		//
		Assert.assertArrayEquals(new IdmBasePermission[]{ IdmBasePermission.READ, IdmBasePermission.UPDATE }, result);
	}
	
	@Test
	public void testTrimNullWithNull() {
		BasePermission[] input = null;
		BasePermission[] result = service.trimNull(input);
		//
		Assert.assertArrayEquals(input, result);
	}
	
	private static class TestAbstractReadDtoService extends AbstractReadDtoService<IdmIdentityDto, IdmIdentity, IdmIdentityFilter> {

		public TestAbstractReadDtoService(IdmIdentityRepository repository) {
			super(repository);
		}
		
	}
	
}
