package eu.bcvsolutions.idm.rpt;

import eu.bcvsolutions.idm.core.api.domain.ModuleDescriptor;

/**
 * Report module descriptor interface 
 * 
 * @author Radek Tomiška
 *
 */
public interface RptModuleDescriptor extends ModuleDescriptor {

	static final String MODULE_ID = "rpt";
	//
	static final String TOPIC_REPORT_GENERATE_SUCCESS = String.format("%s:reportGenerateSuccess", MODULE_ID);
	static final String TOPIC_REPORT_GENERATE_FAILED = String.format("%s:reportGenerateFailed", MODULE_ID);
	
	@Override
	default String getId() {
		return MODULE_ID;
	}
}
