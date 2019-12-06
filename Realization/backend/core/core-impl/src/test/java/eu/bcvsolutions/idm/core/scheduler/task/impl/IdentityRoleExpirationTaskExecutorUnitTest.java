package eu.bcvsolutions.idm.core.scheduler.task.impl;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import java.time.LocalDate;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.test.api.AbstractVerifiableUnitTest;

/**
 * 
 * @author Jan Helbich
 *
 */
public class IdentityRoleExpirationTaskExecutorUnitTest extends AbstractVerifiableUnitTest {
	
	@Spy
	@InjectMocks
	private IdentityRoleExpirationTaskExecutor executor;

	@Mock
	private IdmIdentityRoleService service;
	
	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void testTwoPageIterations() {
		List<IdmIdentityRoleDto> roles = new ArrayList<>();
		roles.add(getTestRole());
		roles.add(getTestRole());
		//
		when(service.findExpiredRoles((LocalDate) any(), (PageRequest) any()))
			.thenReturn(new PageImpl<IdmIdentityRoleDto>(roles)) // first call
			.thenReturn(new PageImpl<IdmIdentityRoleDto>(new ArrayList<>())); // second call - empty page
		//
		doNothing().when(service).delete((IdmIdentityRoleDto) any());
		//
		when(executor.updateState()).thenReturn(true);
		//
		Boolean result = executor.process();
		Assert.assertTrue(result);
		verify(service, times(2)).findExpiredRoles((LocalDate) any(), (PageRequest) any());
		verify(service, times(2)).delete((IdmIdentityRoleDto) any());
		verify(executor, times(2)).updateState();
	}
	
	@Test
	public void testBreakOnUpdateStateFail() {
		List<IdmIdentityRoleDto> roles = new ArrayList<>();
		roles.add(getTestRole());
		roles.add(getTestRole());
		//
		when(service.findExpiredRoles((LocalDate) any(), (PageRequest) any()))
			.thenReturn(new PageImpl<IdmIdentityRoleDto>(roles)); // first call
		//
		doNothing().when(service).delete((IdmIdentityRoleDto) any());
		//
		when(executor.updateState())
			.thenReturn(true) // first call - OK
			.thenReturn(false); // second call - fail update state and break
		//
		Boolean result = executor.process();
		Assert.assertTrue(result);
		verify(service, times(1)).findExpiredRoles((LocalDate) any(), (PageRequest) any());
		verify(service, times(2)).delete((IdmIdentityRoleDto) any());
		verify(executor, times(2)).updateState();
	}


	private IdmIdentityRoleDto getTestRole() {
		return new IdmIdentityRoleDto();
	}
}
