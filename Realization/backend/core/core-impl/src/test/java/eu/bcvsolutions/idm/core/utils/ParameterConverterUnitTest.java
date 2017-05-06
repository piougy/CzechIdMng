package eu.bcvsolutions.idm.core.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.api.utils.ParameterConverter;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.test.api.AbstractVerifiableUnitTest;

/**
 * Rest parameter converter test. Parameters are in strings.
 * 
 * @author Radek Tomiška
 *
 */
public class ParameterConverterUnitTest extends AbstractVerifiableUnitTest {

	private static final String PARAMETER_NAME = "test-parameter";
	//
	@Mock
	private LookupService entityLookupService;
	private ParameterConverter parameterConverter;
	
	@Before
	public void init() {
		parameterConverter = new ParameterConverter(entityLookupService);
	}
	
	@Test
	public void testEmptyParameter() {
		Map<String, Object> parameters = new HashMap<>();
		//
		assertNull(parameterConverter.toUuid(parameters, PARAMETER_NAME));
	}
	
	@Test
	public void testStringParameter() {
		Map<String, Object> parameters = new HashMap<>();
		String value = "one";
		parameters.put(PARAMETER_NAME, value);
		//
		assertEquals(value, parameterConverter.toString(parameters, PARAMETER_NAME));
	}
	
	@Test
	public void testUuidParameter() {
		Map<String, Object> parameters = new HashMap<>();
		UUID value = UUID.randomUUID();
		parameters.put(PARAMETER_NAME, value.toString());
		//
		assertEquals(value, parameterConverter.toUuid(parameters, PARAMETER_NAME));
	}
	
	@Test
	public void testBooleanParameter() {
		Map<String, Object> parameters = new HashMap<>();
		Boolean value = Boolean.TRUE;
		parameters.put(PARAMETER_NAME, value.toString());
		//
		assertEquals(value, parameterConverter.toBoolean(parameters, PARAMETER_NAME));
		//
		parameters.put(PARAMETER_NAME, "blah");
		//
		assertFalse(parameterConverter.toBoolean(parameters, PARAMETER_NAME));
	}
	
	@Test
	public void testWrongBooleanParameter() {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put(PARAMETER_NAME, "blah");
		//
		assertFalse(parameterConverter.toBoolean(parameters, PARAMETER_NAME));
	}
	
	@Test
	public void testLongParameter() {
		Map<String, Object> parameters = new HashMap<>();
		Long value = Long.MAX_VALUE;
		parameters.put(PARAMETER_NAME, value.toString());
		//
		assertEquals(value, parameterConverter.toLong(parameters, PARAMETER_NAME));
	}
	
	@Test
	public void testDateTimeParameter() {
		Map<String, Object> parameters = new HashMap<>();
		DateTime value = new DateTime();
		parameters.put(PARAMETER_NAME, value.toString());
		//
		assertEquals(value, parameterConverter.toDateTime(parameters, PARAMETER_NAME));
	}
	
	@Test
	public void testEnumParameter() {
		Map<String, Object> parameters = new HashMap<>();
		OperationState value = OperationState.CREATED;
		parameters.put(PARAMETER_NAME, value.toString());
		//
		assertEquals(value, parameterConverter.toEnum(parameters, PARAMETER_NAME, OperationState.class));
	}
	
	@Test
	public void testEntityParameter() {
		Map<String, Object> parameters = new HashMap<>();
		String value = "admin";
		IdmIdentity identity = new IdmIdentity(UUID.randomUUID());		
		when(entityLookupService.lookupEntity(IdmIdentity.class, value)).thenReturn(identity);
		parameters.put(PARAMETER_NAME, value);
		//
		assertEquals(identity, parameterConverter.toEntity(parameters, PARAMETER_NAME, IdmIdentity.class));
		//
		verify(entityLookupService).lookupEntity(IdmIdentity.class, value);
	}
}
