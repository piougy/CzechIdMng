package eu.bcvsolutions.idm.acc.service;

import static org.junit.Assert.assertEquals;

import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.acc.TestHelper;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemEntityDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemEntityFilter;
import eu.bcvsolutions.idm.acc.service.api.SysSystemEntityService;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

public class DefaultSysSystemEntityServiceFilterTest extends AbstractIntegrationTest {
	
	@Autowired private SysSystemEntityService entityService;
	@Autowired private TestHelper helper;
	
	@Before
	public void login(){
		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
	}

	@After
	public void logout(){
		super.logout();
	}
	
	@Test
	public void testSystemId () {		
		SysSystemDto system1 = helper.createTestResourceSystem(true);
		SysSystemDto system2 = helper.createTestResourceSystem(true);
		SysSystemEntityDto entity1 = createEntitySystem("158" + System.currentTimeMillis(), SystemEntityType.CONTRACT, system1.getId(), UUID.randomUUID());
		SysSystemEntityDto entity2 = createEntitySystem("159" + System.currentTimeMillis(), SystemEntityType.CONTRACT, system2.getId(), UUID.randomUUID());
		SysSystemEntityDto entity3 = createEntitySystem("160" + System.currentTimeMillis(), SystemEntityType.CONTRACT, system2.getId(), UUID.randomUUID());
		SysSystemEntityFilter testFilter = new SysSystemEntityFilter();		
		testFilter.setSystemId(system2.getId());
		Page<SysSystemEntityDto> pages = entityService.find(testFilter, null);
		assertEquals(2, pages.getTotalElements());
		assertEquals(entity2.getId(), pages.getContent().get(0).getId());
	}
	
	@Test
	public void testUid() {
		SysSystemDto system = helper.createTestResourceSystem(true);		
		SysSystemEntityDto entity1 = createEntitySystem("158" + System.currentTimeMillis(), SystemEntityType.CONTRACT, system.getId(), UUID.randomUUID());
		createEntitySystem("159" + System.currentTimeMillis(), SystemEntityType.CONTRACT, system.getId(), UUID.randomUUID());
		createEntitySystem("160" + System.currentTimeMillis(), SystemEntityType.CONTRACT, system.getId(), UUID.randomUUID());
		SysSystemEntityFilter testFilter = new SysSystemEntityFilter();		
		testFilter.setUid(entity1.getUid());
		Page<SysSystemEntityDto> pages = entityService.find(testFilter, null);
		assertEquals(1, pages.getTotalElements());
		assertEquals(entity1.getId(), pages.getContent().get(0).getId());
	}
	
	/**
	 * Id in {@link SysSystemEntityFilter} not currently implemented. Use get by id.
	 */
	@Test
	public void testId() {
		SysSystemDto system = helper.createTestResourceSystem(true);		
		createEntitySystem("158" + System.currentTimeMillis(), SystemEntityType.CONTRACT, system.getId(), UUID.randomUUID());
		SysSystemEntityDto entity2 = createEntitySystem("159" + System.currentTimeMillis(), SystemEntityType.CONTRACT, system.getId(), UUID.randomUUID());
		SysSystemEntityDto entity3 = createEntitySystem("160" + System.currentTimeMillis(), SystemEntityType.CONTRACT, system.getId(), UUID.randomUUID());
		SysSystemEntityDto foundedEntity = entityService.get(entity3.getId());		
		assertEquals(entity3.getId(), foundedEntity.getId());
	}
	
	@Test
	public void testEntityType() {
		SysSystemDto system = helper.createTestResourceSystem(true);		
		SysSystemEntityDto entity1 = createEntitySystem("158" + System.currentTimeMillis(), SystemEntityType.ROLE, system.getId(), UUID.randomUUID());
		SysSystemEntityDto entity2 = createEntitySystem("159" + System.currentTimeMillis(), SystemEntityType.TREE, system.getId(), UUID.randomUUID());
		SysSystemEntityDto entity3 = createEntitySystem("160" + System.currentTimeMillis(), SystemEntityType.IDENTITY, system.getId(), UUID.randomUUID());
		SysSystemEntityFilter testFilter = new SysSystemEntityFilter();		
		testFilter.setEntityType(entity2.getEntityType());
		Page<SysSystemEntityDto> pages = entityService.find(testFilter, null);
		assertEquals(1, pages.getTotalElements());
		assertEquals(entity2.getId(), pages.getContent().get(0).getId());
	}
	
	private SysSystemEntityDto createEntitySystem(String uid, SystemEntityType type, UUID systemId, UUID id) {
		SysSystemEntityDto entity = new SysSystemEntityDto();
		entity.setUid(uid);
		entity.setEntityType(type);
		entity.setSystem(systemId);
		entity.setId(id);		
		entityService.save(entity);
		return entity;
	}
}
