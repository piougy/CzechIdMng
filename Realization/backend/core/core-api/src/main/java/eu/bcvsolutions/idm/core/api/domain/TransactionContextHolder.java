package eu.bcvsolutions.idm.core.api.domain;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.Assert;

/**
 * Associates a user transaction with the current execution thread.
 * Context is propagated into newly created threads automatically
 * 
 * @author Radek Tomiška
 * @since 9.7.0
 * @see SecurityContextHolder
 */
public final class TransactionContextHolder {

	// Inheritable thread strategy cannot be use here, because it doesn't work well with thread pools.
	// Delegation of the Transaction context is realized by eu.bcvsolutions.idm.core.config.DelegatingTransactionContextRunnable.
	private static final ThreadLocal<TransactionContext> contextHolder = new ThreadLocal<>();
	
	private TransactionContextHolder() {}
	
	public static void clearContext() {
		contextHolder.remove();
	}

	public static TransactionContext getContext() {
		TransactionContext ctx = contextHolder.get();
		//
		if (ctx == null) {
			ctx = createEmptyContext();
			contextHolder.set(ctx);
		}

		return ctx;
	}

	public static void setContext(TransactionContext context) {
		Assert.notNull(context, "Only non-null TransactionContext instances are permitted");
		//
		contextHolder.set(context);
	}

	public static TransactionContext createEmptyContext() {
		return new TransactionContext();
	}
	
}
