package eu.bcvsolutions.idm.core.bulk.action.impl.notification;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.bulk.action.AbstractBackupBulkAction;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationTemplateDto;
import eu.bcvsolutions.idm.core.notification.api.dto.filter.IdmNotificationTemplateFilter;
import eu.bcvsolutions.idm.core.notification.api.service.IdmNotificationTemplateService;
import eu.bcvsolutions.idm.core.notification.domain.NotificationGroupPermission;

/**
 * Backup given notification template
 *
 * @author Ondrej Husnik
 * @since 10.6.0
 */
@Component(NotificationTemplateBackupBulkAction.NAME)
@Description("Backup given notification template.")
public class NotificationTemplateBackupBulkAction extends AbstractBackupBulkAction<IdmNotificationTemplateDto, IdmNotificationTemplateFilter> {

	public static final String NAME = "notification-template-backup-bulk-action";

	@Autowired
	private IdmNotificationTemplateService notificationService;
	
	@Override
	public String getName() {
		return NAME;
	}

	@Override
	protected List<String> getAuthoritiesForEntity() {
		return Lists.newArrayList(NotificationGroupPermission.NOTIFICATIONTEMPLATE_READ);
	}

	@Override
	public ReadWriteDtoService<IdmNotificationTemplateDto, IdmNotificationTemplateFilter> getService() {
		return notificationService;
	}
}
