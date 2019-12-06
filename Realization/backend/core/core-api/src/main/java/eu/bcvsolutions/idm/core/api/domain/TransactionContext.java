package eu.bcvsolutions.idm.core.api.domain;

import java.io.Serializable;
import java.util.UUID;

/**
 * Contains transaction information associated with the current thread of execution.
 * 
 * @author Radek Tomiška
 * @since 9.7.0
 */
public class TransactionContext implements Serializable {

	private static final long serialVersionUID = 1L;
	//
	private final UUID transactionId;
	
	public TransactionContext() {
		this(UUID.randomUUID());
	}
	
	public TransactionContext(UUID transactionId) {
		this.transactionId = transactionId;
	}
	
	public UUID getTransactionId() {
		return transactionId;
	}
}
