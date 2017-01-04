package eu.bcvsolutions.idm.ic.api;

import org.identityconnectors.framework.api.operations.SyncApiOp;

/**
 * Callback interface that an application implements in order to handle results
 * from {@link SyncApiOp} in a stream-processing fashion.
 */
public interface IcSyncResultsHandler {

    /**
     * Called to handle a delta in the stream. The Connector framework will call
     * this method multiple times, once for each result. Although this method is
     * callback, the framework will invoke it synchronously. Thus, the framework
     * guarantees that once an application's call to {@link SyncApiOp#sync
     * SyncApiOp#sync()} returns, the framework will no longer call this method
     * to handle results from that <code>sync()</code> operation.
     *
     * @param delta
     *            The change
     * @return True if the application wants to continue processing more
     *         results.
     * @throws RuntimeException
     *             If the application encounters an exception. This will stop
     *             iteration and the exception will propagate to the
     *             application.
     */
    public boolean handle(IcSyncDelta delta);
}
