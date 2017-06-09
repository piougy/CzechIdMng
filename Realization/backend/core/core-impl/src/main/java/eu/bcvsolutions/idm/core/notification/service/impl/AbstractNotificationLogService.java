package eu.bcvsolutions.idm.core.notification.service.impl;

import java.util.List;

import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationDto;
import eu.bcvsolutions.idm.core.notification.dto.filter.NotificationFilter;
import eu.bcvsolutions.idm.core.notification.entity.IdmNotification;

public class AbstractNotificationLogService<DTO extends IdmNotificationDto, E extends IdmNotification, F extends NotificationFilter>  
		extends AbstractReadWriteDtoService<DTO, E, F> {

    public AbstractNotificationLogService(AbstractEntityRepository<E, F> repository) {
        super(repository);
    }

    @Override
    protected E toEntity(DTO dto, E entity) {
    	Class<E> e = getEntityClass();
    	if (e.getSimpleName().equals(IdmNotification.class.getSimpleName())) {
			return null;
		}
    	
        final E result = super.toEntity(dto, entity);
        result.getRecipients().forEach(r -> r.setNotification(result));
        return result;
    }

    /**
	 * We want to have recipients in returned lists
	 */
	@Override
	protected List<DTO> toDtos(List<E> entities, boolean trimmed) {
		return super.toDtos(entities, false);
	}
}
