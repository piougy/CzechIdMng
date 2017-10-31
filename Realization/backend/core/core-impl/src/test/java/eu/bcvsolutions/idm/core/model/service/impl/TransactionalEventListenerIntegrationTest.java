package eu.bcvsolutions.idm.core.model.service.impl;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;

import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * TransactionalEventListener integration test:
 * - event sequence
 * 
 * @author Radek Tomiška
 *
 */
public class TransactionalEventListenerIntegrationTest extends AbstractIntegrationTest {

	@Autowired private TransactionalEventListenerTestService service;
	
	@Before
	public void init() {
		service.clearResult();
	}
	
	@Test
	public void testEventSequenceWithoutTransaction() {
		for(int i = 0; i < 10; i++) {
			service.process(i+"");
		}
		//
		Assert.assertEquals("0123456789", service.getResult());
	}
	
	@Test
	public void testEventSequenceWithTransaction() {
		getTransactionTemplate().execute(new TransactionCallback<Object>() {
			public Object doInTransaction(TransactionStatus transactionStatus) {
				for(int i = 0; i < 10; i++) {
					service.process(i+"");
				}
				Assert.assertEquals("", service.getResult());
				return null;
			}
		});
		//
		Assert.assertEquals("0123456789", service.getResult());
	}
}
