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

	// inheritable thread strategy is supported only.
	private static final ThreadLocal<TransactionContext> contextHolder = new InheritableThreadLocal<TransactionContext>();
	
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
