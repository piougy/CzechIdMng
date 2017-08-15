package eu.bcvsolutions.idm.core.rest;

import static org.junit.Assert.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.LinkedHashMap;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.core.TestHelper;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleCatalogueDto;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleCatalogue_;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityService;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleCatalogueService;
import eu.bcvsolutions.idm.core.security.api.domain.IdmJwtAuthentication;
import eu.bcvsolutions.idm.core.security.api.utils.IdmAuthorityUtils;
import eu.bcvsolutions.idm.test.api.AbstractRestTest;

/**
 * Rest test for role catalogue
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

public class IdmRoleCatalogueControllerTest extends AbstractRestTest {

	@Autowired
	private IdmIdentityService identityService;
	
	@Autowired
	private IdmRoleCatalogueService roleCatalogueService;
	
	@Autowired
	private TestHelper testHelper;
	
	private Authentication getAuthentication() {
		return new IdmJwtAuthentication(identityService.getByUsername(InitTestData.TEST_ADMIN_USERNAME), null, Lists.newArrayList(IdmAuthorityUtils.getAdminAuthority()), "test");
	}
	
	@Before
	public void init() {
		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
		// remove garbage from another tests
		for (IdmRoleCatalogueDto root : roleCatalogueService.findRoots(null).getContent()) {
			removeRecursivelyAllRoleCatalogue(root);
		}
	}
	
	private void removeRecursivelyAllRoleCatalogue(IdmRoleCatalogueDto dto) {
		for (IdmRoleCatalogueDto child : roleCatalogueService.findChildrenByParent(dto.getId(), null).getContent()) {
			removeRecursivelyAllRoleCatalogue(child);
		}
		roleCatalogueService.delete(dto);
	}
	
	@After
	public void logout() {
		SecurityContextHolder.clearContext();
		super.logout();
	}
	
	@Test
	public void testRoleCatalogueChildren() throws Exception {
		IdmRoleCatalogueDto root = testHelper.createRoleCatalogue("root");
		testHelper.createRoleCatalogue("ccc", root.getId());
		testHelper.createRoleCatalogue("cc", root.getId());
		testHelper.createRoleCatalogue("c", root.getId());
		testHelper.createRoleCatalogue("aaa", root.getId());
		testHelper.createRoleCatalogue("aa", root.getId());
		testHelper.createRoleCatalogue("a", root.getId());
		testHelper.createRoleCatalogue("bbb", root.getId());
		testHelper.createRoleCatalogue("bb", root.getId());
		testHelper.createRoleCatalogue("b", root.getId());
		testHelper.createRoleCatalogue("abc", root.getId());
		
		getMockMvc().perform(get(BaseController.BASE_PATH + "/role-catalogues/search/children?parent=" + root.getId() + "&size=10&page=0&sort=name,asc")
				.with(authentication(getAuthentication()))
				.contentType(InitTestData.HAL_CONTENT_TYPE))
				.andExpect(status().isOk())
				.andExpect(content().contentType(InitTestData.HAL_CONTENT_TYPE));
		
		String jsonString = this.getJsonAsString("/role-catalogues/search/children", "parent=" + root.getId() ,20l, 0l, "name", "asc", getAuthentication());
		List<LinkedHashMap<String, Object>> catalogues = this.getEmbeddedList("roleCatalogues", jsonString);
		
		assertEquals(10, catalogues.size());
		assertEquals("a", catalogues.get(0).get(IdmRoleCatalogue_.name.getName()));
		assertEquals("ccc", catalogues.get(9).get(IdmRoleCatalogue_.name.getName()));
		
		this.getJsonAsString("/role-catalogues/search/children", "parent=" + root.getId() ,20l, 0l, "name", "desc", getAuthentication());
		catalogues = this.getEmbeddedList("roleCatalogues", jsonString);
		
		assertEquals(10, catalogues.size());
		assertEquals("ccc", catalogues.get(9).get(IdmRoleCatalogue_.name.getName()));
		assertEquals("a", catalogues.get(0).get(IdmRoleCatalogue_.name.getName()));
		
		// clear data
		for (IdmRoleCatalogueDto dto : roleCatalogueService.findChildrenByParent(root.getId(), null).getContent()) {
			roleCatalogueService.delete(dto);
		}
		roleCatalogueService.delete(root);
	}
	
	@Test
	public void testRoleCatalogueRootsSort() throws Exception {
		testHelper.createRoleCatalogue("ccc");
		testHelper.createRoleCatalogue("cc");
		testHelper.createRoleCatalogue("c");
		testHelper.createRoleCatalogue("aaa");
		testHelper.createRoleCatalogue("aa");
		testHelper.createRoleCatalogue("a");
		testHelper.createRoleCatalogue("bbb");
		testHelper.createRoleCatalogue("bb");
		testHelper.createRoleCatalogue("b");
		testHelper.createRoleCatalogue("abc");

		getMockMvc().perform(get(BaseController.BASE_PATH + "/role-catalogues/search/roots?size=10&page=0&sort=name,asc")
				.with(authentication(getAuthentication()))
				.contentType(InitTestData.HAL_CONTENT_TYPE))
				.andExpect(status().isOk())
				.andExpect(content().contentType(InitTestData.HAL_CONTENT_TYPE));

		String jsonString = this.getJsonAsString("/role-catalogues/search/roots", null, 20l, 0l, "name", "asc", getAuthentication());
		List<LinkedHashMap<String, Object>> catalogues = this.getEmbeddedList("roleCatalogues", jsonString);
		
		assertEquals(10, catalogues.size());
		assertEquals("a", catalogues.get(0).get(IdmRoleCatalogue_.name.getName()));
		assertEquals("ccc", catalogues.get(9).get(IdmRoleCatalogue_.name.getName()));

		jsonString = this.getJsonAsString("/role-catalogues/search/roots", null, 20l, 0l, "name", "desc", getAuthentication());
		catalogues = this.getEmbeddedList("roleCatalogues", jsonString);
		
		assertEquals(10, catalogues.size());
		assertEquals("a", catalogues.get(9).get(IdmRoleCatalogue_.name.getName()));
		assertEquals("ccc", catalogues.get(0).get(IdmRoleCatalogue_.name.getName()));
		
		// clear data
		for (IdmRoleCatalogueDto dto : roleCatalogueService.find(null).getContent()) {
			roleCatalogueService.delete(dto);
		}
	}
}
