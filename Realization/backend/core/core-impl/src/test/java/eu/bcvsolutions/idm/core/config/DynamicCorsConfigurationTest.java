package eu.bcvsolutions.idm.core.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.web.cors.CorsConfiguration;

import eu.bcvsolutions.idm.core.config.domain.DynamicCorsConfiguration;
import eu.bcvsolutions.idm.core.model.service.IdmConfigurationService;
import eu.bcvsolutions.idm.core.test.AbstractUnitTest;

/**
 * Test configuration change for DynamicCorsConfiguration
 * 
 * @author Radek Tomiška 
 *
 */
public class DynamicCorsConfigurationTest extends AbstractUnitTest {
	
	private static final String ORIGIN_VALUE = "http://localhost";
	
	@Mock
	private IdmConfigurationService configurationService;
	
	@InjectMocks
	private DynamicCorsConfiguration dynamicCorsConfiguration = new DynamicCorsConfiguration();
	
	@Test
	public void testCheckOriginWithoutConfig() {
		when(configurationService.getValue(DynamicCorsConfiguration.PROPERTY_ALLOWED_ORIGIN)).thenReturn(null);
		
		assertNull(dynamicCorsConfiguration.checkOrigin(ORIGIN_VALUE));
		
		verify(configurationService).getValue(DynamicCorsConfiguration.PROPERTY_ALLOWED_ORIGIN);
	}
	
	@Test
	public void testCheckOriginSuccess() {
		when(configurationService.getValue(DynamicCorsConfiguration.PROPERTY_ALLOWED_ORIGIN)).thenReturn(ORIGIN_VALUE);
		
		assertEquals(ORIGIN_VALUE, dynamicCorsConfiguration.checkOrigin(ORIGIN_VALUE));
		
		verify(configurationService).getValue(DynamicCorsConfiguration.PROPERTY_ALLOWED_ORIGIN);
	}
	
	@Test
	public void testCheckOriginFailed() {
		when(configurationService.getValue(DynamicCorsConfiguration.PROPERTY_ALLOWED_ORIGIN)).thenReturn(ORIGIN_VALUE);
		
		assertNull(dynamicCorsConfiguration.checkOrigin("http://localhost:3000"));
		
		verify(configurationService).getValue(DynamicCorsConfiguration.PROPERTY_ALLOWED_ORIGIN);
	}
	
	@Test
	public void testCheckOriginAllWithoutCredentials() {
		when(configurationService.getValue(DynamicCorsConfiguration.PROPERTY_ALLOWED_ORIGIN)).thenReturn(CorsConfiguration.ALL);
		
		dynamicCorsConfiguration.setAllowCredentials(false);
		assertEquals(CorsConfiguration.ALL, dynamicCorsConfiguration.checkOrigin(ORIGIN_VALUE));
		
		verify(configurationService).getValue(DynamicCorsConfiguration.PROPERTY_ALLOWED_ORIGIN);
	}
	
	@Test
	public void testCheckOriginAllWithCredentials() {
		when(configurationService.getValue(DynamicCorsConfiguration.PROPERTY_ALLOWED_ORIGIN)).thenReturn(CorsConfiguration.ALL);
		dynamicCorsConfiguration.setAllowCredentials(true);
		assertEquals(ORIGIN_VALUE, dynamicCorsConfiguration.checkOrigin(ORIGIN_VALUE));
		
		verify(configurationService).getValue(DynamicCorsConfiguration.PROPERTY_ALLOWED_ORIGIN);
	}

}
