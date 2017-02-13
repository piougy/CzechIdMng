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

import eu.bcvsolutions.idm.core.api.service.EntityLookupService;
import eu.bcvsolutions.idm.core.api.utils.FilterConverter;
import eu.bcvsolutions.idm.core.model.dto.filter.TreeNodeFilter;
import eu.bcvsolutions.idm.test.api.AbstractUnitTest;

/**
 * Rest filter converter test. Parameters are in strings.
 * 
 * @author Radek Tomiška
 *
 */
public class FilterConverterUnitTest extends AbstractUnitTest {

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
		assertNull(filterConverter.toFilter(parameters, TreeNodeFilter.class));
	}
	
	@Test
	public void testFilledFilter() {
		Map<String, Object> parameters = new HashMap<>();
		UUID treeTypeId = UUID.randomUUID();
		parameters.put("treeTypeId", treeTypeId.toString());
		//
		TreeNodeFilter filter = filterConverter.toFilter(parameters, TreeNodeFilter.class);
		assertEquals(treeTypeId, filter.getTreeTypeId());
		assertNull(filter.getTreeNodeId());
		assertNull(filter.getDefaultTreeType());
		//
		parameters.put("defaultTreeType", "true");
		filter = filterConverter.toFilter(parameters, TreeNodeFilter.class);
		assertEquals(treeTypeId, filter.getTreeTypeId());
		assertNull(filter.getTreeNodeId());
		assertTrue(filter.getDefaultTreeType());
	}
}
