package eu.bcvsolutions.idm.core.api.event.processor;

import com.google.common.annotations.Beta;
import eu.bcvsolutions.idm.core.api.domain.MonitoringLevel;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmMonitoringResultDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.EventType;
import eu.bcvsolutions.idm.core.api.service.BaseDtoService;
import eu.bcvsolutions.idm.core.api.service.MonitoringManager;
import eu.bcvsolutions.idm.core.api.service.ReadDtoService;
import java.text.MessageFormat;
import javax.persistence.Table;

/**
 * Abstract processor for databe monitoring
 *
 * @author Vít Švanda
 * @param <DTO>
 */
@Beta
public abstract class AbstractMonitoringDatabaseProcessor<DTO extends AbstractDto> extends CoreEventProcessor<DTO> {

	public AbstractMonitoringDatabaseProcessor(EventType... type) {
		super(type);
	}

	protected String getTableName(BaseDtoService service) {
		Class entityClass = service.getEntityClass();
		if (entityClass == null) {
			return null;
		}
		Table table = (Table) entityClass.getAnnotation(Table.class);
		if (table != null) {
			return table.name();
		}
		return service.getEntityClass().getSimpleName();
	}
	
	protected String getDtoName(BaseDtoService service) {
		Class dtoClass = service.getDtoClass();
		if (dtoClass == null) {
			return null;
		}
		
		return dtoClass.getSimpleName();
	}

	protected IdmMonitoringResultDto countToResult(ReadDtoService service) {
		long count = service.count(null);
		long threshold = 500;
		MonitoringLevel level = MonitoringLevel.OK;
		if (count > threshold) {
			level = MonitoringLevel.WARNING;
		}

		IdmMonitoringResultDto result = new IdmMonitoringResultDto();
		result.setModule(getModule());
		result.setType(MonitoringManager.MONITORING_TYPE_DATABASE);
		result.setName(MessageFormat.format("{0} ({1})", getDtoName(service), getTableName(service)));
		result.setValue(String.valueOf(count));
		result.setThreshold(String.valueOf(threshold));
		result.setLevel(level);

		return result;
	}

}
