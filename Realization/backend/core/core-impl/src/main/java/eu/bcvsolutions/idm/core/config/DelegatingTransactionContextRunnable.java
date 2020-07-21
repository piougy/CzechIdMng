package eu.bcvsolutions.idm.core.config;

import eu.bcvsolutions.idm.core.api.domain.TransactionContext;
import eu.bcvsolutions.idm.core.api.domain.TransactionContextHolder;
import org.springframework.util.Assert;

/**
 * Wraps a delegate {@link Runnable} with logic for setting up a {@link TransactionContext}
 * before invoking the delegate {@link Runnable} and then removing the
 * {@link TransactionContext} after the delegate has completed.
 *
 * Based on the {@link org.springframework.security.concurrent.DelegatingSecurityContextRunnable}.
 *
 * @author Vít Švanda
 */
public class DelegatingTransactionContextRunnable implements Runnable {

    private final Runnable delegate;
    private final TransactionContext delegateTransactionContext;
    private TransactionContext originalTransactionContext;


    public DelegatingTransactionContextRunnable(Runnable delegate,
                                                TransactionContext transactionContext) {
        Assert.notNull(delegate, "delegate cannot be null");
        Assert.notNull(transactionContext, "transactionContext cannot be null");
        this.delegate = delegate;
        this.delegateTransactionContext = transactionContext;
    }

    public DelegatingTransactionContextRunnable(Runnable delegate) {
        this(delegate, TransactionContextHolder.getContext());
    }

    @Override
    public void run() {
        this.originalTransactionContext = TransactionContextHolder.getContext();

        try {
            TransactionContextHolder.setContext(delegateTransactionContext);
            delegate.run();
        } finally {
            // Here is uses same logic as in original DelegatingSecurityContextRunnable
            // class (restoring of original context), but this maybe cause problem with using same instance of context.
            TransactionContext emptyContext = TransactionContextHolder.createEmptyContext();
            if (emptyContext.equals(originalTransactionContext)) {
                TransactionContextHolder.clearContext();
            } else {
                TransactionContextHolder.setContext(originalTransactionContext);
            }
            this.originalTransactionContext = null;
        }
    }

    @Override
    public String toString() {
        return delegate.toString();
    }
}
