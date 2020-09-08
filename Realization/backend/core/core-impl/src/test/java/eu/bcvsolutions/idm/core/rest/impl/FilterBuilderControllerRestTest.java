package eu.bcvsolutions.idm.core.rest.impl;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.api.dto.FilterBuilderDto;
import eu.bcvsolutions.idm.core.api.dto.filter.FilterBuilderFilter;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.utils.AutowireHelper;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.repository.filter.DefaultManagersFilter;
import eu.bcvsolutions.idm.core.model.repository.filter.GuaranteeManagersFilter;
import eu.bcvsolutions.idm.test.api.AbstractRestTest;
import eu.bcvsolutions.idm.test.api.TestHelper;

/**
 * Filter builder tests.
 * - enable filter
 * - fully find is tested in DefaultFilterManagerUnitTest.
 * 
 * @author Radek Tomiška
 *
 */
@Transactional
public class FilterBuilderControllerRestTest extends AbstractRestTest {

	@Autowired private DefaultManagersFilter defaultManagersFilter;
	@Autowired private GuaranteeManagersFilter guaranteeManagersFilter;
	
	@Test
	public void testEnable() throws Exception {
		try {
			getHelper().enableFilter(defaultManagersFilter.getClass());
			//
			FilterBuilderFilter filter = new FilterBuilderFilter();
			filter.setFilterBuilderClass(AutowireHelper.getTargetType(defaultManagersFilter));
			List<FilterBuilderDto> results = find(filter);
			//
			Assert.assertEquals(1, results.size());
			Assert.assertEquals(defaultManagersFilter.getName(), results.get(0).getName());
			Assert.assertEquals(defaultManagersFilter.getEntityClass(), results.get(0).getEntityClass());
			Assert.assertFalse(results.get(0).isDisabled());
			//
			filter.setFilterBuilderClass(AutowireHelper.getTargetType(guaranteeManagersFilter));
			results = find(filter);
			Assert.assertEquals(1, results.size());
			Assert.assertEquals(guaranteeManagersFilter.getName(), results.get(0).getName());
			Assert.assertEquals(guaranteeManagersFilter.getEntityClass(), results.get(0).getEntityClass());
			Assert.assertTrue(results.get(0).isDisabled());
			//
			getMockMvc().perform(put(BaseController.BASE_PATH + "/filter-builders/" + results.get(0).getId() + "/enable")
					.with(authentication(getAdminAuthentication()))
	                .contentType(TestHelper.HAL_CONTENT_TYPE))
					.andExpect(status().is2xxSuccessful());
			//
			filter.setFilterBuilderClass(AutowireHelper.getTargetType(defaultManagersFilter));
			results = find(filter);
			Assert.assertEquals(1, results.size());
			Assert.assertEquals(defaultManagersFilter.getName(), results.get(0).getName());
			Assert.assertEquals(defaultManagersFilter.getEntityClass(), results.get(0).getEntityClass());
			Assert.assertTrue(results.get(0).isDisabled());
			//
			filter.setFilterBuilderClass(AutowireHelper.getTargetType(guaranteeManagersFilter));
			results = find(filter);
			Assert.assertEquals(1, results.size());
			Assert.assertEquals(guaranteeManagersFilter.getName(), results.get(0).getName());
			Assert.assertEquals(guaranteeManagersFilter.getEntityClass(), results.get(0).getEntityClass());
			Assert.assertFalse(results.get(0).isDisabled());
		} finally {
			getHelper().enableFilter(defaultManagersFilter.getClass());
		}
	}
	
	@Test
	public void testFindByName() {
		FilterBuilderFilter filter = new FilterBuilderFilter();
		filter.setName(defaultManagersFilter.getName());
		filter.setEntityClass(IdmIdentity.class.getCanonicalName());
		List<FilterBuilderDto> results = find(filter);
		//
		Assert.assertFalse(results.isEmpty());
		Assert.assertTrue(results.stream().anyMatch(f -> f.getFilterBuilderClass().equals(AutowireHelper.getTargetClass(defaultManagersFilter))));
		Assert.assertTrue(results.stream().anyMatch(f -> f.getFilterBuilderClass().equals(AutowireHelper.getTargetClass(guaranteeManagersFilter))));
		Assert.assertTrue(results.stream().allMatch(f -> f.getEntityClass().equals(IdmIdentity.class)));
		Assert.assertTrue(results.stream().allMatch(f -> f.getName().equals(defaultManagersFilter.getName())));
		//
		filter.setName(getHelper().createName()); // not-exists
		filter.setEntityClass(IdmIdentity.class.getCanonicalName());
		results = find(filter);
		Assert.assertTrue(results.isEmpty());
	}
	
	protected List<FilterBuilderDto> find(FilterBuilderFilter filter) {
		try {
			String response = getMockMvc().perform(get(BaseController.BASE_PATH + "/filter-builders")
	        		.with(authentication(getAdminAuthentication()))
	        		.params(toQueryParams(filter))
	                .contentType(TestHelper.HAL_CONTENT_TYPE))
					.andExpect(status().isOk())
	                .andExpect(content().contentType(TestHelper.HAL_CONTENT_TYPE))
	                .andReturn()
	                .getResponse()
	                .getContentAsString();
			//
			return toDtos(response, FilterBuilderDto.class);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to find entities", ex);
		}
	}
}
