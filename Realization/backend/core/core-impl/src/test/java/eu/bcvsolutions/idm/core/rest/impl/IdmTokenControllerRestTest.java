package eu.bcvsolutions.idm.core.rest.impl;

import java.util.List;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTokenDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmTokenFilter;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoControllerRestTest;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.domain.IdmJwtAuthentication;
import eu.bcvsolutions.idm.core.security.api.filter.IdmAuthenticationFilter;
import eu.bcvsolutions.idm.core.security.api.service.TokenManager;
import eu.bcvsolutions.idm.core.security.service.impl.JwtAuthenticationMapper;
import eu.bcvsolutions.idm.test.api.TestHelper;

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
	@Autowired private JwtAuthenticationMapper jwtTokenMapper;
	
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
		dto.setIssuedAt(ZonedDateTime.now());
		return dto;
	}
	
	@Override
	protected boolean supportsAutocomplete() {
		return false;
	}
	
	@Override
	protected boolean supportsPost() {
		return false;
	}
	
	@Override
	protected boolean supportsPut() {
		return false;
	}
	
	@Override
	protected boolean supportsPatch() {
		return false;
	}
	
	@Override
	public void testFindByTransactionId() {
		// token is used even for login => two records are inserted all time in one request
	}
	
	@Test
	public void testGenerate() throws Exception {
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmTokenDto token = new IdmTokenDto();
		token.setOwnerId(identity.getId());
		token.setOwnerType(tokenManager.getOwnerType(identity));
		token.setTokenType("custom");
		token.setExpiration(ZonedDateTime.now().plusDays(1));
		ObjectMapper mapper = getMapper();
		//
		String response = getMockMvc().perform(post(getBaseUrl())
        		.with(authentication(getAdminAuthentication()))
        		.content(mapper.writeValueAsString(token))
                .contentType(TestHelper.HAL_CONTENT_TYPE))
				.andExpect(status().isCreated())
                .andExpect(content().contentType(TestHelper.HAL_CONTENT_TYPE))
                .andReturn()
                .getResponse()
                .getContentAsString();
		IdmTokenDto createdToken = (IdmTokenDto) mapper.readValue(response, token.getClass());
		Assert.assertNotNull(createdToken);
		Assert.assertNotNull(createdToken.getId());
		Assert.assertTrue(token.getExpiration().isEqual(createdToken.getExpiration()));
		Assert.assertEquals(token.getTokenType(), createdToken.getTokenType());
		//
		// token is filled
		String jwtToken = createdToken.getProperties().getString(IdmAuthenticationFilter.AUTHENTICATION_TOKEN_NAME);
		Assert.assertNotNull(jwtToken);
		IdmJwtAuthentication readToken = jwtTokenMapper.readToken(jwtToken);
		Assert.assertEquals(createdToken.getId(), readToken.getId());
		Assert.assertTrue(token.getExpiration().isEqual(readToken.getExpiration()));
		//
		IdmTokenDto getToken = getDto(createdToken.getId());
		// token is not filled after get
		Assert.assertNull(getToken.getProperties().get(IdmAuthenticationFilter.AUTHENTICATION_TOKEN_NAME));
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
		ZonedDateTime now = ZonedDateTime.now().truncatedTo(ChronoUnit.MILLIS);
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
