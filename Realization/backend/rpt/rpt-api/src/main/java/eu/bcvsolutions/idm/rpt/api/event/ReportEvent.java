package eu.bcvsolutions.idm.rpt.api.event;

import java.io.Serializable;
import java.util.Map;

import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.EventType;
import eu.bcvsolutions.idm.rpt.api.dto.RptReportDto;

/**
 * Events for reports - creating new report
 * 
 * @author Radek Tomiška
 *
 */
public class ReportEvent extends CoreEvent<RptReportDto> {

	private static final long serialVersionUID = 1L;

	/**
	 * Supported events
	 *
	 */
	public enum ReportEventType implements EventType {
		GENERATE
	}
	
	public ReportEvent(ReportEventType operation, RptReportDto content) {
		super(operation, content);
	}
	
	public ReportEvent(ReportEventType operation, RptReportDto content, Map<String, Serializable> properties) {
		super(operation, content, properties);
	}

}