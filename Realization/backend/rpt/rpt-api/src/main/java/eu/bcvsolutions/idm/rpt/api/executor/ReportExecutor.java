package eu.bcvsolutions.idm.rpt.api.executor;

import java.util.List;

import org.springframework.plugin.core.Plugin;

import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.service.Configurable;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskExecutor;
import eu.bcvsolutions.idm.rpt.api.dto.RptReportDto;

/**
 * Generate report. Report executor implements LRT.
 * 
 * @author Radek Tomiška
 *
 */
public interface ReportExecutor extends Configurable, Plugin<String>, LongRunningTaskExecutor<RptReportDto> {
	
	static final String CONFIGURABLE_TYPE = "report-executor";

	@Override
	default String getConfigurableType() {
		return CONFIGURABLE_TYPE;
	}
	
	/**
	 * Generate report data
	 * 
	 * @param filter
	 * @return
	 */
	RptReportDto generate(RptReportDto report);

	@Override
	default String getModule() {
		return Configurable.super.getModule();
	}

	@Override
	default String getName() {
		return Configurable.super.getName();
	}

	@Override
	default List<String> getPropertyNames() {
		// TODO: form definition will be used
		return Configurable.super.getPropertyNames();
	}
	
	/**
	 * Returns form definition for report filter
	 * 
	 * TODO: move to schedulable task executor
	 * 
	 * @return
	 */
	IdmFormDefinitionDto getFormDefinition();
	
	/**
	 * Generate report for given event content
	 * 
	 * @param event
	 */
	void setEvent(EntityEvent<RptReportDto> event);

	@Override
	default String getDescription() {
		return Configurable.super.getDescription();
	}
}
