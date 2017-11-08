package eu.bcvsolutions.idm.core.model.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import eu.bcvsolutions.idm.core.api.service.EntityEventManager;

/**
 * "Naive" service for @TransactionalEventListener test.
 * 
 * @author Radek Tomiška
 *
 */
@Service
public class TransactionalEventListenerTestService {
	
	private final EntityEventManager entityEventManager;
	private static StringBuilder result = new StringBuilder();
	
	@Autowired
	public TransactionalEventListenerTestService(EntityEventManager entityEventManager) {
		this.entityEventManager = entityEventManager;
	}

	@Transactional
	public void process(String content) {
		entityEventManager.publishEvent(new TestContext(content));
	}
	
	@TransactionalEventListener
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	private void processInternal(TestContext context) {
		result.append(context.content);
	}
	
	public String getResult() {
		return result.toString();
	}
	
	public void clearResult() {
		result = new StringBuilder();
	}
	
	private static class TestContext {
		private String content;
		
		public TestContext(String content) {
			this.content = content;
		}
		
	}
}
