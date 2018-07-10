package eu.bcvsolutions.idm.core.rest.impl;

import java.util.List;
import java.util.UUID;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTokenDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmTokenFilter;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoControllerRestTest;
import eu.bcvsolutions.idm.core.security.api.service.TokenManager;

/**
 * Controller tests
 * - crud
 * - filter
 * 
 * @author Radek Tomiška
 *
 */
@Transactional
public class IdmTokenControllerRestTest extends AbstractReadWriteDtoControllerRestTest<IdmTokenDto> {

	@Autowired private IdmTokenController controller;
	@Autowired private TokenManager tokenManager;
	
	@Override
	protected AbstractReadWriteDtoController<IdmTokenDto, ?> getController() {
		return controller;
	}

	@Override
	protected IdmTokenDto prepareDto() {
		IdmIdentityDto owner = new IdmIdentityDto(UUID.randomUUID());
		IdmTokenDto dto = new IdmTokenDto();
		dto.setOwnerId(owner.getId());
		dto.setOwnerType(tokenManager.getOwnerType(owner));
		dto.setTokenType("mock");
		dto.setToken("mock");
		dto.setIssuedAt(new DateTime());
		return dto;
	}
	
	@Test
	public void testFindByOwner() {
		IdmIdentityDto owner = getHelper().createIdentity();
		IdmTokenDto tokenOne =  prepareDto();
		tokenOne.setOwnerId(owner.getId());
		String ownerType = tokenManager.getOwnerType(owner);
		tokenOne.setOwnerType(ownerType);
		tokenOne = createDto(tokenOne);
		createDto(); // other
		createDto(); // other
		
		
		IdmTokenFilter filter = new IdmTokenFilter();
		filter.setOwnerType(ownerType);
		filter.setOwnerId(owner.getId());
		
		List<IdmTokenDto> results = find(filter);
		//
		Assert.assertEquals(1, results.size());
		Assert.assertEquals(tokenOne, results.get(0));
	}
	
	@Test
	public void testFindByDisabled() {
		UUID mockOwnerId = UUID.randomUUID();
		//
		IdmTokenDto tokenOne =  prepareDto();
		tokenOne.setOwnerId(mockOwnerId);
		tokenOne.setDisabled(true);
		tokenOne = createDto(tokenOne);
		IdmTokenDto tokenTwo =  prepareDto();
		tokenTwo.setOwnerId(mockOwnerId);
		tokenTwo.setDisabled(false);
		tokenTwo = createDto(tokenTwo);
		//
		IdmTokenFilter filter = new IdmTokenFilter();
		filter.setOwnerId(mockOwnerId);
		filter.setDisabled(true);
		List<IdmTokenDto> results = find(filter);
		//
		Assert.assertEquals(1, results.size());
		Assert.assertEquals(tokenOne, results.get(0));
		//
		filter.setDisabled(false);
		results = find(filter);
		//
		Assert.assertEquals(1, results.size());
		Assert.assertEquals(tokenTwo, results.get(0));
	}
	
	@Test
	public void testFindByExpirationTill() {
		UUID mockOwnerId = UUID.randomUUID();
		DateTime now = new DateTime();
		//
		IdmTokenDto tokenOne =  prepareDto();
		tokenOne.setOwnerId(mockOwnerId);
		tokenOne.setExpiration(now);
		tokenOne = createDto(tokenOne);
		IdmTokenDto tokenTwo =  prepareDto();
		tokenTwo.setOwnerId(mockOwnerId);
		tokenTwo.setExpiration(now.plusDays(2));
		tokenTwo = createDto(tokenTwo);
		//
		IdmTokenFilter filter = new IdmTokenFilter();
		filter.setOwnerId(mockOwnerId);
		filter.setExpirationTill(now);
		List<IdmTokenDto> results = find(filter);
		//
		Assert.assertEquals(1, results.size());
		Assert.assertEquals(tokenOne, results.get(0));
		//
		filter.setExpirationTill(now.plusDays(1));
		results = find(filter);
		//
		Assert.assertEquals(1, results.size());
		Assert.assertEquals(tokenOne, results.get(0));
		//
		filter.setExpirationTill(now.plusDays(2));
		results = find(filter);
		//
		Assert.assertEquals(2, results.size());
	}
}
