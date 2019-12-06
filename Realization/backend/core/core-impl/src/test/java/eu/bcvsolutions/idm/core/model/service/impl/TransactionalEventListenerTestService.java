package eu.bcvsolutions.idm.core.model.service.impl;

/**
 * "Naive" service for @TransactionalEventListener test.
 * 
 * @author Radek Tomiška
 *
 */
public interface TransactionalEventListenerTestService {

	/**
	 * Test process - set result
	 * 
	 * @param content
	 */
	void process(String content);
	
	/**
	 * Clear result.
	 */
	void clearResult();
	
	/**
	 * Set result.
	 * @return processed result.
	 */
	String getResult();
	
}
