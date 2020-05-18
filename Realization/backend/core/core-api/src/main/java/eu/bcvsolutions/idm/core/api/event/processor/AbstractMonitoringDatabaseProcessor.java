package eu.bcvsolutions.idm.core.api.event.processor;

import com.google.common.annotations.Beta;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmMonitoringResultDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.EventType;
import eu.bcvsolutions.idm.core.api.service.BaseDtoService;
import eu.bcvsolutions.idm.core.api.service.ReadDtoService;
import eu.bcvsolutions.idm.core.notification.api.domain.NotificationLevel;
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
	
	public static String MONITORING_TYPE_DATABASE = "monitoring-database";
	
	public AbstractMonitoringDatabaseProcessor(EventType... type) {
		super(type);
	}

	protected String getTableName(BaseDtoService<?> service) {
		Class<?> entityClass = service.getEntityClass();
		if (entityClass == null) {
			return null;
		}
		Table table = entityClass.getAnnotation(Table.class);
		if (table != null) {
			return table.name();
		}
		return service.getEntityClass().getSimpleName();
	}
	
	protected String getDtoName(BaseDtoService<?> service) {
		Class<?> dtoClass = service.getDtoClass();
		if (dtoClass == null) {
			return null;
		}
		
		return dtoClass.getSimpleName();
	}

	/**
	 * Create monitoring result.
	 * 
	 * @param service
	 * @return
	 */
	protected IdmMonitoringResultDto countToResult(ReadDtoService<?,?> service) {
		long count = service.count(null);
		long threshold = 500000;
		NotificationLevel level = NotificationLevel.SUCCESS;
		if (count > threshold) {
			level = NotificationLevel.WARNING;
		}

		IdmMonitoringResultDto result = new IdmMonitoringResultDto();
		result.setModule(getModule());
		result.setType(MONITORING_TYPE_DATABASE);
		result.setName(MessageFormat.format("{0} ({1})", getDtoName(service), getTableName(service)));
		result.setValue(String.valueOf(count));
		result.setThreshold(String.valueOf(threshold));
		result.setLevel(level);

		return result;
	}

}
