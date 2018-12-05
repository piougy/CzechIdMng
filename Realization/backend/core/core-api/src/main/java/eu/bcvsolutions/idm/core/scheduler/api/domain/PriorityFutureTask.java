package eu.bcvsolutions.idm.core.scheduler.api.domain;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

/**
 * Prioritized future task. Used in thread pools to execute more prioritized threads early.
 * 
 * @author Radek Tomiška
 *
 * @param <T>
 */
public class PriorityFutureTask<T> extends FutureTask<T> {

    private int priority;

    public PriorityFutureTask(Callable<T> callable, int priority) {
    	super(callable);
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }
}