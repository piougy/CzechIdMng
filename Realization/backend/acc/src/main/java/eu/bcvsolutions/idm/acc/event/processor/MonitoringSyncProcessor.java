package eu.bcvsolutions.idm.acc.event.processor;

import eu.bcvsolutions.idm.acc.dto.AbstractSysSyncConfigDto;
import eu.bcvsolutions.idm.acc.dto.SysSchemaObjectClassDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysSyncConfigFilter;
import eu.bcvsolutions.idm.acc.entity.SysSyncConfig_;
import eu.bcvsolutions.idm.acc.entity.SysSystemMapping_;
import eu.bcvsolutions.idm.acc.service.api.SysSyncConfigService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.domain.MonitoringLevel;
import eu.bcvsolutions.idm.core.api.dto.IdmMonitoringResultDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import eu.bcvsolutions.idm.core.api.dto.IdmMonitoringTypeDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.model.event.MonitoringEvent;
import java.text.MessageFormat;

/**
 * Monitoring of ACC sync - processor.
 *
 * @author Vít Švanda
 *
 */
@Component(MonitoringSyncProcessor.PROCESSOR_NAME)
@Description("Monitoring of ACC sync - processor.")
public class MonitoringSyncProcessor
		extends CoreEventProcessor<IdmMonitoringTypeDto> {

	public static final String PROCESSOR_NAME = "acc-monitoring-sync-processor";
	public static final String MONITORING_TYPE_SYNC = "monitoring-sync";

	@Autowired
	private SysSyncConfigService syncConfigService;
	@Autowired
	private SysSystemService systemService;

	public MonitoringSyncProcessor() {
		super(MonitoringEvent.MonitoringEventType.CHECK);
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmMonitoringTypeDto> process(EntityEvent<IdmMonitoringTypeDto> event) {
		IdmMonitoringTypeDto monitoringType = event.getContent();
		monitoringType.setModule(getModule());

		SysSyncConfigFilter syncFilter = new SysSyncConfigFilter();
		syncFilter.setIncludeLastLog(Boolean.TRUE);
		syncConfigService.find(syncFilter, null).getContent().stream()
				.filter(sync -> sync.isEnabled())
				.forEach(sync -> monitoringType.getResults().add(this.syncToMonitoringResult(sync)));

		return new DefaultEventResult<>(event, this);
	}

	@Override
	public boolean conditional(EntityEvent<IdmMonitoringTypeDto> event) {
		boolean result = super.conditional(event);

		if (!result) {
			return false;
		}
		return MONITORING_TYPE_SYNC.equals(event.getContent().getType());
	}

	private IdmMonitoringResultDto syncToMonitoringResult(AbstractSysSyncConfigDto sync) {
		SysSystemMappingDto mapping = DtoUtils.getEmbedded(sync,
				SysSyncConfig_.systemMapping.getName(),
				SysSystemMappingDto.class);

		SysSchemaObjectClassDto schema = DtoUtils.getEmbedded(mapping,
				SysSystemMapping_.objectClass.getName(),
				SysSchemaObjectClassDto.class);

		SysSystemDto system = systemService.get(schema.getSystem());
		IdmMonitoringResultDto result = new IdmMonitoringResultDto();
		result.setDtoId(sync.getId());
		result.setDtoType(sync.getClass().getCanonicalName());
		result.setDto(sync);
		result.setName(MessageFormat.format("{0} ({1})", system.getName(), sync.getName()));
		result.setModule(getModule());
		MonitoringLevel level = MonitoringLevel.OK;
		if (sync.getLastSyncLog() != null && sync.getLastSyncLog().isContainsError()) {
			level = MonitoringLevel.ERROR;
		}
		result.setLevel(level);

		return result;
	}

}
