package eu.bcvsolutions.idm.core.scheduler.exception;

import com.google.common.collect.ImmutableMap;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;

/**
 * Thrown, when added scheduler cron trigger expression is wrong  
 * 
 * @author Radek Tomiška
 *
 */
public class InvalidCronExpressionException extends SchedulerException {
	
	private static final long serialVersionUID = 1L;
	private final String cron;	
	
	public InvalidCronExpressionException(String cron, Throwable throwable) {
		super(CoreResultCode.SCHEDULER_INVALID_CRON_EXPRESSION, ImmutableMap.of("cron", cron), throwable);
		this.cron = cron;
	}
	
	public String getCron() {
		return cron;
	}
}
