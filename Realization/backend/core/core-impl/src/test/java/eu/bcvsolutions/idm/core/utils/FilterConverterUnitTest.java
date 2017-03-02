package eu.bcvsolutions.idm.core.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.bcvsolutions.idm.core.api.dto.filter.QuickFilter;
import eu.bcvsolutions.idm.core.api.service.EntityLookupService;
import eu.bcvsolutions.idm.core.api.utils.FilterConverter;
import eu.bcvsolutions.idm.test.api.AbstractVerifiableUnitTest;

/**
 * Rest filter converter test. Parameters are in strings.
 * 
 * @author Radek Tomiška
 *
 */
public class FilterConverterUnitTest extends AbstractVerifiableUnitTest {

	@Mock
	private EntityLookupService entityLookupService;
	private FilterConverter filterConverter;
	
	@Before
	public void init() {
		filterConverter = new FilterConverter(entityLookupService, new ObjectMapper());
	}
	
	@Test
	public void testEmptyFilter() {
		Map<String, Object> parameters = new HashMap<>();
		//
		assertNull(filterConverter.toFilter(parameters, TestFilter.class));
	}
	
	@Test
	public void testFilledFilter() {
		Map<String, Object> parameters = new HashMap<>();
		UUID treeTypeId = UUID.randomUUID();
		parameters.put("treeTypeId", treeTypeId.toString());
		//
		TestFilter filter = filterConverter.toFilter(parameters, TestFilter.class);
		assertEquals(treeTypeId, filter.getTreeTypeId());
		assertNull(filter.getTreeNodeId());
		assertNull(filter.getDefaultTreeType());
		//
		parameters.put("defaultTreeType", "true");
		filter = filterConverter.toFilter(parameters, TestFilter.class);
		assertEquals(treeTypeId, filter.getTreeTypeId());
		assertNull(filter.getTreeNodeId());
		assertTrue(filter.getDefaultTreeType());
	}
	
	public static class TestFilter extends QuickFilter {
		
		private UUID treeTypeId;	
		private UUID treeNodeId;	
		private Boolean defaultTreeType;

		public UUID getTreeTypeId() {
			return treeTypeId;
		}

		public void setTreeTypeId(UUID treeTypeId) {
			this.treeTypeId = treeTypeId;
		}
		
		public UUID getTreeNodeId() {
			return treeNodeId;
		}
		
		public void setTreeNodeId(UUID treeNodeId) {
			this.treeNodeId = treeNodeId;
		}
		
		public Boolean getDefaultTreeType() {
			return defaultTreeType;
		}
		
		public void setDefaultTreeType(Boolean defaultTreeType) {
			this.defaultTreeType = defaultTreeType;
		}
	}
}
