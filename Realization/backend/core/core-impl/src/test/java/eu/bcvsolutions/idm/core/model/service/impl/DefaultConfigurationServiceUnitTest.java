package eu.bcvsolutions.idm.core.model.service.impl;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.modelmapper.ModelMapper;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.service.ConfidentialStorage;
import eu.bcvsolutions.idm.core.model.entity.IdmConfiguration;
import eu.bcvsolutions.idm.core.model.repository.IdmConfigurationRepository;
import eu.bcvsolutions.idm.test.api.AbstractUnitTest;

/**
 * Configuration service unit tests:
 * - multiple properties
 * 
 * @author Radek Tomiška
 *
 */
public class DefaultConfigurationServiceUnitTest extends AbstractUnitTest {

	private static final String VALUE_ONE = "valueOne";
	private static final String VALUE_TWO = "value Two";
	private static final String VALUE_THREE = "valueThree";
	//
	@Spy private ModelMapper modelMapper = new ModelMapper();
	@Mock private IdmConfigurationRepository repository;
	@Mock private ConfidentialStorage confidentialStorage;
	//
	@InjectMocks private DefaultConfigurationService service;
	
	@Test
	public void testGetValuesWithNull() {
		when(repository.findOneByName(any(String.class))).thenReturn(null);
		//
		List<String> results = service.getValues("key");
		//
		Assert.assertNotNull(results);
		Assert.assertEquals(0, results.size());
	}
	
	@Test
	public void testGetValuesWithEmpty() {
		IdmConfiguration configurationItem = new IdmConfiguration(null, "  , ,  ,,, ,");
		when(repository.findOneByName(any(String.class))).thenReturn(configurationItem);
		//
		List<String> results = service.getValues("key");
		//
		Assert.assertNotNull(results);
		Assert.assertEquals(0, results.size());
	}
	
	@Test
	public void testGetValues() {
		IdmConfiguration configurationItem = new IdmConfiguration(null, String.format("%s,%s,%s", VALUE_ONE, VALUE_TWO, VALUE_THREE));
		when(repository.findOneByName(any(String.class))).thenReturn(configurationItem);
		//
		List<String> results = service.getValues("key");
		//
		Assert.assertNotNull(results);
		Assert.assertEquals(3, results.size());
		Assert.assertTrue(results.stream().anyMatch(e -> e.equals(VALUE_ONE)));
		Assert.assertTrue(results.stream().anyMatch(e -> e.equals(VALUE_TWO)));
		Assert.assertTrue(results.stream().anyMatch(e -> e.equals(VALUE_THREE)));
	}
	
	@Test
	public void testGetValuesWithBlankSpaces() {
		IdmConfiguration configurationItem = new IdmConfiguration(null, String.format(" %s   ,%s,  %s,,,     ,", VALUE_ONE, VALUE_TWO, VALUE_THREE));
		when(repository.findOneByName(any(String.class))).thenReturn(configurationItem);
		//
		List<String> results = service.getValues("key");
		//
		Assert.assertNotNull(results);
		Assert.assertEquals(3, results.size());
		Assert.assertTrue(results.stream().anyMatch(e -> e.equals(VALUE_ONE)));
		Assert.assertTrue(results.stream().anyMatch(e -> e.equals(VALUE_TWO)));
		Assert.assertTrue(results.stream().anyMatch(e -> e.equals(VALUE_THREE)));
	}
	
	@Test
	public void testSaveValues() {
		ConfigurationAnswer answer = new ConfigurationAnswer();
		when(repository.saveAndFlush(any(IdmConfiguration.class))).thenAnswer(answer);
		//
		service.setValues("key", Lists.newArrayList(VALUE_ONE, VALUE_TWO, VALUE_THREE));
		//
		Assert.assertEquals(String.format("%s,%s,%s", VALUE_ONE, VALUE_TWO, VALUE_THREE), answer.getValue());
	}
	
	@Test
	public void testSaveValuesWithNull() {
		ConfigurationAnswer answer = new ConfigurationAnswer();
		when(repository.saveAndFlush(any(IdmConfiguration.class))).thenAnswer(answer);
		//
		service.setValues("key", null);
		//
		Assert.assertEquals(null, answer.getValue());
	}
	
	@Test
	public void testSaveValuesWithEmptyList() {
		ConfigurationAnswer answer = new ConfigurationAnswer();
		when(repository.saveAndFlush(any(IdmConfiguration.class))).thenAnswer(answer);
		//
		service.setValues("key", Lists.newArrayList());
		//
		Assert.assertEquals(null, answer.getValue());
	}
	
	@Test
	public void testSaveValuesWithNullValue() {
		ConfigurationAnswer answer = new ConfigurationAnswer();
		when(repository.saveAndFlush(any(IdmConfiguration.class))).thenAnswer(answer);
		//
		String nullValue = null;
		service.setValues("key", Lists.newArrayList(nullValue));
		//
		Assert.assertEquals(null, answer.getValue());
	}
	
	@Test
	public void testSaveValuesWithNullValues() {
		ConfigurationAnswer answer = new ConfigurationAnswer();
		when(repository.saveAndFlush(any(IdmConfiguration.class))).thenAnswer(answer);
		//
		service.setValues("key", Lists.newArrayList(null, VALUE_ONE, null, null, VALUE_TWO, VALUE_THREE));
		//
		Assert.assertEquals(String.format("%s,%s,%s", VALUE_ONE, VALUE_TWO, VALUE_THREE), answer.getValue());
	}
	
	/**
	 * Simple repository save method listener
	 * 
	 * @author Radek Tomiška
	 *
	 */
	private class ConfigurationAnswer implements Answer<IdmConfiguration> {

		private IdmConfiguration configurationItem;
		
		@Override
		public IdmConfiguration answer(InvocationOnMock invocation) throws Throwable {
			configurationItem = (IdmConfiguration) invocation.getArguments()[0];
			return configurationItem;
		}
		
		public String getValue() {
			return configurationItem == null ? null : configurationItem.getValue();
		}
	}
}
