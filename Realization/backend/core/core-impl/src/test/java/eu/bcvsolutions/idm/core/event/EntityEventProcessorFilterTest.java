package eu.bcvsolutions.idm.core.event;

import static org.junit.Assert.assertEquals;

import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.core.api.dto.EntityEventProcessorDto;
import eu.bcvsolutions.idm.core.api.dto.filter.EntityEventProcessorFilter;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author
 * Petr Hanák
 */
public class EntityEventProcessorFilterTest extends AbstractIntegrationTest {
	@Autowired
	private EntityEventManager eventManager;

	@Before
	public void login() {
		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
	}

	@After
	public void logout() {
		super.logout();
	}

	@Test
	public void testNameFilter() {
		String testProcessorName = "contract-guarantee-delete";
		EntityEventProcessorFilter filter = new EntityEventProcessorFilter();
		filter.setName("contract-guarantee");

		List<EntityEventProcessorDto> result = eventManager.find(filter);
		assertEquals(2, result.size());
		assertEquals(testProcessorName, result.get(0).getName());
	}

	@Test
	public void testDescriptionFilter() {
		String testProcessorName = "contract-guarantee-save";
		EntityEventProcessorFilter filter = new EntityEventProcessorFilter();
		filter.setDescription("Save");

		List<EntityEventProcessorDto> result = eventManager.find(filter);
		assertEquals(1, result.size());
		assertEquals(testProcessorName, result.get(0).getName());
	}

	@Test
	public void testEventTypeFilter() {
		String testProcessorName = "identity-password-expired-processor";
		EntityEventProcessorFilter filter = new EntityEventProcessorFilter();
		filter.setEventType("PASSWORD");

		List<EntityEventProcessorDto> result = eventManager.find(filter);
		assertEquals(3, result.size());
		assertEquals(testProcessorName, result.get(0).getName());
	}

	@Test
	public void testEntityTypeFilter() {
		String testProcessorName = "role-catalogue-delete-processor";
		EntityEventProcessorFilter filter = new EntityEventProcessorFilter();
		filter.setEntityType("IdmRoleCatalogueDto");

		List<EntityEventProcessorDto> result = eventManager.find(filter);
		assertEquals(2, result.size());
		assertEquals(testProcessorName, result.get(0).getName());
	}

	@Test
	public void testModuleFilter() {
		String testProcessorName = "contract-guarantee-delete";
		EntityEventProcessorFilter filter = new EntityEventProcessorFilter();
		filter.setModule("core");

		List<EntityEventProcessorDto> result = eventManager.find(filter);
		assertEquals(42, result.size());
		assertEquals(testProcessorName, result.get(0).getName());
	}

	@Test
	public void testEntityEventProcessorFilter() {
		String testProcessorName = "contract-guarantee-delete";
		EntityEventProcessorFilter filter = new EntityEventProcessorFilter();
		filter.setName("contract");
		filter.setDescription("manually");
		filter.setModule("core");
		filter.setEntityType("IdmContractGuaranteeDto");
		filter.setEventType("DELETE");

		List<EntityEventProcessorDto> result = eventManager.find(filter);
		assertEquals(1, result.size());
		assertEquals(testProcessorName, result.get(0).getName());

	}
}
