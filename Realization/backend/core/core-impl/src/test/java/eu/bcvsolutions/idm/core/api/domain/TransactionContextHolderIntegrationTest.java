package eu.bcvsolutions.idm.core.api.domain;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.test.api.AbstractRestTest;
import eu.bcvsolutions.idm.test.api.TestHelper;

/**
 * Test fill transaction id by rest layer.
 * 
 * @author Radek Tomiška
 *
 */
@Transactional
public class TransactionContextHolderIntegrationTest extends AbstractRestTest {

	@Autowired private IdmIdentityService identityService;
	
	@Test
	public void testUpdateTransactionIdByServiceLayer() {
		
		IdmIdentityDto identity = new IdmIdentityDto(getHelper().createName());
		// save identity without rest layer
		identity = identityService.save(identity);
		// transaction id will be filled by hibernate listener, see {@link AuditableEntityListener#touchForUpdate}
		UUID transactionId = identity.getTransactionId();
		Assert.assertNotNull(transactionId);
		// update the identity without changes in the same user transaction
		identity = identityService.save(identity);
		//
		Assert.assertEquals(transactionId, identity.getTransactionId());
		//
		// generate new transaction id manually
		TransactionContextHolder.setContext(TransactionContextHolder.createEmptyContext());
		identity = identityService.save(identity);
		UUID newTransactionId = TransactionContextHolder.getContext().getTransactionId();
		// no change - preserve transaction id
		Assert.assertEquals(transactionId, identity.getTransactionId());
		//
		identity.setFirstName(getHelper().createName());
		identity = identityService.save(identity);
		Assert.assertEquals(newTransactionId, identity.getTransactionId());
	}
	
	@Test
	@Ignore // FIXME: Why is transaction id changed from FE without change? 
	public void testUpdateTransactionIdByRestLayer() throws Exception {
		IdmIdentityDto identity = new IdmIdentityDto(getHelper().createName());
		// save identity without rest layer
		identity = create(identity);
		// transaction id will be filled by hibernate listener, see {@link AuditableEntityListener#touchForUpdate}
		UUID transactionId = identity.getTransactionId();
		Assert.assertNotNull(transactionId);
		// update the identity without changes but new transaction id is given
		identity = update(identity);
		//
		Assert.assertEquals(transactionId, identity.getTransactionId());
		// no change - preserve transaction id
		Assert.assertEquals(transactionId, identity.getTransactionId());
		//
		identity.setFirstName(getHelper().createName());
		identity = update(identity);
		Assert.assertNotEquals(transactionId, identity.getTransactionId());
	}
	
	
	private IdmIdentityDto create(IdmIdentityDto identity) throws Exception {
		ObjectMapper mapper = getMapper();
		//
		String response = getMockMvc().perform(post(String.format("%s/identities", BaseDtoController.BASE_PATH))
        		.with(authentication(getAdminAuthentication()))
        		.content(mapper.writeValueAsString(identity))
                .contentType(TestHelper.HAL_CONTENT_TYPE))
				.andExpect(status().isCreated())
                .andExpect(content().contentType(TestHelper.HAL_CONTENT_TYPE))
                .andReturn()
                .getResponse()
                .getContentAsString();
		//
		return mapper.readValue(response, IdmIdentityDto.class);
	}
	
	private IdmIdentityDto update(IdmIdentityDto identity) throws Exception {
		ObjectMapper mapper = getMapper();
		//
		String response = getMockMvc().perform(put(String.format("%s/identities/%s", BaseDtoController.BASE_PATH, identity.getId()))
        		.with(authentication(getAdminAuthentication()))
        		.content(mapper.writeValueAsString(identity))
                .contentType(TestHelper.HAL_CONTENT_TYPE))
				.andExpect(status().isOk())
                .andExpect(content().contentType(TestHelper.HAL_CONTENT_TYPE))
                .andReturn()
                .getResponse()
                .getContentAsString();
		//
		return mapper.readValue(response, IdmIdentityDto.class);
	}
}
