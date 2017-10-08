package eu.bcvsolutions.idm.core.model.service.impl;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;
import eu.bcvsolutions.idm.test.api.TestHelper;

/**
 * Default lookup service test:
 * - get dto by uuid / code
 * - get entity by uuid / code
 * 
 * @author Radek Tomiška
 *
 */
public class DefaultLookupServiceIntegrationTest extends AbstractIntegrationTest {
	
	@Autowired private TestHelper helper;
	@Autowired private ApplicationContext context;
	//
	private DefaultLookupService lookupService;
	
	@Before
	public void init() {
		lookupService = context.getAutowireCapableBeanFactory().createBean(DefaultLookupService.class);
	}
	
	@Test
	public void testIdentityLookupByUuid() {
		IdmIdentityDto dto = helper.createIdentity();
		//
		// by dto class
		Assert.assertEquals(dto.getId(), lookupService.lookupDto(IdmIdentityDto.class, dto.getId()).getId());
		Assert.assertEquals(dto.getId(), lookupService.lookupEntity(IdmIdentityDto.class, dto.getId()).getId());
		// by entity class
		Assert.assertEquals(dto.getId(), lookupService.lookupDto(IdmIdentity.class, dto.getId()).getId());
		Assert.assertEquals(dto.getId(), lookupService.lookupEntity(IdmIdentity.class, dto.getId()).getId());
	}
	
	@Test
	public void testIdentityLookupByCode() {
		IdmIdentityDto dto = helper.createIdentity();
		//
		// by dto class
		Assert.assertEquals(dto.getId(), lookupService.lookupDto(IdmIdentityDto.class, dto.getCode()).getId());
		Assert.assertEquals(dto.getId(), lookupService.lookupEntity(IdmIdentityDto.class, dto.getCode()).getId());
		// by entity class
		Assert.assertEquals(dto.getId(), lookupService.lookupDto(IdmIdentity.class, dto.getCode()).getId());
		Assert.assertEquals(dto.getId(), lookupService.lookupEntity(IdmIdentity.class, dto.getCode()).getId());
	}
}
