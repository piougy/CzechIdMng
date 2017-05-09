package eu.bcvsolutions.idm.core.notification.service.impl;

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
        final E result = super.toEntity(dto, entity);
        result.getRecipients().forEach(r -> r.setNotification(result));
        return result;
    }

    @Override
    protected DTO toDto(E entity, DTO dto) {
        return super.toDto(entity, dto);
    }
}
