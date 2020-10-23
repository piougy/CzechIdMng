package eu.bcvsolutions.idm.core.model.repository.filter;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;

import eu.bcvsolutions.idm.core.api.dto.FilterBuilderDto;
import eu.bcvsolutions.idm.core.api.dto.filter.FilterBuilderFilter;
import eu.bcvsolutions.idm.core.api.exception.FilterSizeExceededException;
import eu.bcvsolutions.idm.core.api.repository.filter.FilterBuilder;
import eu.bcvsolutions.idm.core.api.repository.filter.FilterKey;
import eu.bcvsolutions.idm.core.api.repository.filter.FilterManager;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.test.api.AbstractUnitTest;

/**
 * Event manager unit tests
 * - event priority
 * - find events to execute
 * - resurrect event 
 * 
 * @author Radek Tomiška
 *
 */
public class DefaultFilterManagerUnitTest extends AbstractUnitTest {

	@Mock private ApplicationContext context;
	@Mock private List<? extends FilterBuilder<?, ?>> builders;
	@Mock private ConfigurationService configurationService;
	//
	@InjectMocks private DefaultFilterManager manager;
	
	@Test
	public void testEmptyFilter() {
		Assert.assertTrue(manager.passFilter(new FilterBuilderDto(), null));
		FilterBuilderFilter filter = new FilterBuilderFilter();
		Assert.assertTrue(manager.passFilter(new FilterBuilderDto(), filter));
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void testFindById() {
		FilterBuilderFilter filter = new FilterBuilderFilter();
		filter.setId(UUID.randomUUID());
		manager.passFilter(new FilterBuilderDto(), filter);
	}
	
	@SuppressWarnings("serial")
	@Test
	public void testFindByText() {
		FilterBuilderDto dto = new FilterBuilderDto();
		dto.setName("test-x");
		dto.setDescription("x-Test-x");
		dto.setEntityClass(IdmIdentity.class);
		dto.setFilterBuilderClass(IdmIdentityService.class);
		//
		FilterBuilderFilter filter = new FilterBuilderFilter();
		filter.setText("tesT-");
		Assert.assertTrue(manager.passFilter(dto, filter));
		filter.setText("x-tesT");
		Assert.assertTrue(manager.passFilter(dto, filter));
		filter.setText("xtesT");
		Assert.assertFalse(manager.passFilter(dto, filter));
		dto.setDescription(null);
		filter.setText("x-t");
		Assert.assertFalse(manager.passFilter(dto, filter));
		filter.setText("ident");
		Assert.assertTrue(manager.passFilter(dto, filter));
		filter.setText("service");
		Assert.assertTrue(manager.passFilter(dto, filter));
		//
		dto.setFilterBuilderClass((new IdmIdentity() {}).getClass()); // inline classes with null cannonical name
		Assert.assertFalse(manager.passFilter(dto, filter));
		filter.setText("ident");
		Assert.assertTrue(manager.passFilter(dto, filter));
	}
	
	@Test
	public void testFindByName() {
		FilterBuilderDto dto = new FilterBuilderDto();
		dto.setName("test-x");
		dto.setEntityClass(IdmIdentity.class);
		//
		FilterBuilderFilter filter = new FilterBuilderFilter();
		filter.setName("test-x");
		Assert.assertTrue(manager.passFilter(dto, filter));
		filter.setName("test-");
		Assert.assertFalse(manager.passFilter(dto, filter));
		filter.setName("test-X");
		Assert.assertFalse(manager.passFilter(dto, filter));
	}
	
	@Test
	public void testFindByDescription() {
		FilterBuilderDto dto = new FilterBuilderDto();
		dto.setDescription("tesT-x");
		dto.setEntityClass(IdmIdentity.class);
		//
		FilterBuilderFilter filter = new FilterBuilderFilter();
		filter.setDescription("Test-x");
		Assert.assertTrue(manager.passFilter(dto, filter));
		filter.setDescription("test-");
		Assert.assertTrue(manager.passFilter(dto, filter));
		filter.setDescription("tee");
		Assert.assertFalse(manager.passFilter(dto, filter));
		dto.setDescription(null);
		Assert.assertFalse(manager.passFilter(dto, filter));
	}
	
	@Test
	public void testFindByModule() {
		FilterBuilderDto dto = new FilterBuilderDto();
		dto.setModule("test");
		dto.setEntityClass(IdmIdentity.class);
		//
		FilterBuilderFilter filter = new FilterBuilderFilter();
		filter.setModule("test");
		Assert.assertTrue(manager.passFilter(dto, filter));
		filter.setModule("tes");
		Assert.assertFalse(manager.passFilter(dto, filter));
	}
	
	@Test
	public void testFindByEntityClass() {
		FilterBuilderDto dto = new FilterBuilderDto();
		dto.setModule("test");
		dto.setEntityClass(IdmIdentity.class);
		//
		FilterBuilderFilter filter = new FilterBuilderFilter();
		filter.setEntityClass(IdmIdentity.class.getCanonicalName());
		Assert.assertTrue(manager.passFilter(dto, filter));
		filter.setEntityClass(IdmIdentity.class.getSimpleName());
		Assert.assertFalse(manager.passFilter(dto, filter));
	}
	
	@Test
	@SuppressWarnings("serial")
	public void testFindByFilterBuilderClass() {
		FilterBuilderDto dto = new FilterBuilderDto();
		dto.setModule("test");
		dto.setEntityClass(IdmIdentity.class);
		dto.setFilterBuilderClass(IdmIdentityService.class);
		//
		FilterBuilderFilter filter = new FilterBuilderFilter();
		filter.setFilterBuilderClass(IdmIdentityService.class.getCanonicalName());
		Assert.assertTrue(manager.passFilter(dto, filter));
		filter.setFilterBuilderClass(IdmIdentityService.class.getSimpleName());
		Assert.assertFalse(manager.passFilter(dto, filter));
		dto.setFilterBuilderClass((new IdmIdentity() {}).getClass()); // inline classes with null cannonical name
		filter.setFilterBuilderClass(IdmIdentity.class.getCanonicalName());
		Assert.assertFalse(manager.passFilter(dto, filter));
	}
	
	@Test
	public void testCheckFilterSizeExceededDisabled() {
		Mockito.when(configurationService.getIntegerValue(
				FilterManager.PROPERTY_CHECK_FILTER_SIZE_MAXIMUM, 
				FilterManager.DEFAULT_CHECK_FILTER_SIZE_MAXIMUM)).thenReturn(-1);
		//
		List<UUID> values = Stream.generate(UUID::randomUUID).limit(11).collect(Collectors.toList());
		//
		Assert.assertEquals(values.size(), manager.checkFilterSizeExceeded(new FilterKey(IdmIdentity.class, "mockProperty"), values).size());
	}
	
	@Test(expected = FilterSizeExceededException.class)
	public void testCheckFilterSizeExceeded() {
		Mockito.when(configurationService.getIntegerValue(
				FilterManager.PROPERTY_CHECK_FILTER_SIZE_MAXIMUM, 
				FilterManager.DEFAULT_CHECK_FILTER_SIZE_MAXIMUM)).thenReturn(10);
		//
		List<UUID> values = Stream.generate(UUID::randomUUID).limit(11).collect(Collectors.toList());
		//
		Assert.assertEquals(values.size(), manager.checkFilterSizeExceeded(new FilterKey(IdmIdentity.class, "mockProperty"), values).size());
	}
	
	@Test
	public void testCheckFilterSizeNull() {
		Mockito.when(configurationService.getIntegerValue(
				FilterManager.PROPERTY_CHECK_FILTER_SIZE_MAXIMUM, 
				FilterManager.DEFAULT_CHECK_FILTER_SIZE_MAXIMUM)).thenReturn(10);
		//
		Assert.assertNull(manager.checkFilterSizeExceeded(new FilterKey(IdmIdentity.class, "mockProperty"), null));
	}
	
	@Test
	public void testCheckFilterSize() {
		Mockito.when(configurationService.getIntegerValue(
				FilterManager.PROPERTY_CHECK_FILTER_SIZE_MAXIMUM, 
				FilterManager.DEFAULT_CHECK_FILTER_SIZE_MAXIMUM)).thenReturn(10);
		//
		List<UUID> values = Stream.generate(UUID::randomUUID).limit(10).collect(Collectors.toList());
		//
		Assert.assertEquals(values.size(), manager.checkFilterSizeExceeded(new FilterKey(IdmIdentity.class, "mockProperty"), values).size());
		//
		values = Stream.generate(UUID::randomUUID).limit(9).collect(Collectors.toList());
		//
		Assert.assertEquals(values.size(), manager.checkFilterSizeExceeded(new FilterKey(IdmIdentity.class, "mockProperty"), values).size());
	}
}
