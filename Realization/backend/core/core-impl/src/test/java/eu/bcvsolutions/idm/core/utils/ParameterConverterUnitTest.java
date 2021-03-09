package eu.bcvsolutions.idm.core.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
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
	public void testIntegerParameter() {
		MultiValueMap<String, Object> parameters = new LinkedMultiValueMap<>();
		Integer value = Integer.MAX_VALUE;
		parameters.set(PARAMETER_NAME, value.toString());
		//
		Assert.assertEquals(value, parameterConverter.toInteger(parameters, PARAMETER_NAME));
		Assert.assertEquals(value.intValue(), parameterConverter.toInteger(parameters.toSingleValueMap(), PARAMETER_NAME, 1));
		Assert.assertEquals(1, parameterConverter.toInteger(parameters.toSingleValueMap(), "mock", 1));
	}
	
	@Test(expected = ResultCodeException.class)
	public void testWrongIntegerParameter() {
		MultiValueMap<String, Object> parameters = new LinkedMultiValueMap<>();
		parameters.set(PARAMETER_NAME, "mock");
		//
		parameterConverter.toInteger(parameters, PARAMETER_NAME);
	}
	
	@Test
	public void testLongParameter() {
		MultiValueMap<String, Object> parameters = new LinkedMultiValueMap<>();
		Long value = Long.MAX_VALUE;
		parameters.set(PARAMETER_NAME, value.toString());
		//
		assertEquals(value, parameterConverter.toLong(parameters, PARAMETER_NAME));
		Assert.assertEquals(value.longValue(), parameterConverter.toLong(parameters.toSingleValueMap(), PARAMETER_NAME, 1));
		Assert.assertEquals(1, parameterConverter.toLong(parameters.toSingleValueMap(), "mock", 1));
	}
	
	@Test(expected = ResultCodeException.class)
	public void testWrongLongParameter() {
		MultiValueMap<String, Object> parameters = new LinkedMultiValueMap<>();
		parameters.set(PARAMETER_NAME, "mock");
		//
		parameterConverter.toLong(parameters, PARAMETER_NAME);
	}
	
	@Test
	public void testDateTimeParameter() {
		Map<String, Object> parameters = new HashMap<>();
		ZonedDateTime value = ZonedDateTime.now();
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
	
	@Test
	public void testBigDecimalParameter() {
		MultiValueMap<String, Object> parameters = new LinkedMultiValueMap<String, Object>();
		Assert.assertNull(parameterConverter.toBigDecimal(parameters, PARAMETER_NAME));
		//
		parameters.set(PARAMETER_NAME, "");
		Assert.assertNull(parameterConverter.toBigDecimal(parameters, PARAMETER_NAME));
		//
		BigDecimal value = new BigDecimal("10.1");
		parameters.set(PARAMETER_NAME, value.toString());
		//
		Assert.assertEquals(value, parameterConverter.toBigDecimal(parameters, PARAMETER_NAME));
		//
		parameters.set(PARAMETER_NAME, value);
		Assert.assertEquals(value, parameterConverter.toBigDecimal(parameters, PARAMETER_NAME));
	}
	
	@Test(expected = ResultCodeException.class)
	public void testBigDecimalWrongParameter() {
		MultiValueMap<String, Object> parameters = new LinkedMultiValueMap<String, Object>();
		//
		parameters.set(PARAMETER_NAME, "wrong");
		parameterConverter.toBigDecimal(parameters, PARAMETER_NAME);
	}
}
