package eu.bcvsolutions.idm.core.bulk.action.impl.notification;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.bulk.action.AbstractRedeployBulkAction;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationTemplateDto;
import eu.bcvsolutions.idm.core.notification.api.dto.filter.IdmNotificationTemplateFilter;
import eu.bcvsolutions.idm.core.notification.api.service.IdmNotificationTemplateService;
import eu.bcvsolutions.idm.core.notification.domain.NotificationGroupPermission;

/**
 * Redeploy given notification template
 *
 * @author Ondrej Husnik
 *
 */
@Component(NotificationTemplateRedeployBulkAction.NAME)
@Description("Redeploy given notification template.")
public class NotificationTemplateRedeployBulkAction extends AbstractRedeployBulkAction<IdmNotificationTemplateDto, IdmNotificationTemplateFilter> {

	public static final String NAME = "notification-template-redeploy-bulk-action";

	@Autowired
	private IdmNotificationTemplateService notificationService;
	
	@Override
	public String getName() {
		return NAME;
	}

	@Override
	protected List<String> getAuthoritiesForEntity() {
		return Lists.newArrayList(NotificationGroupPermission.NOTIFICATIONTEMPLATE_UPDATE);
	}

	@Override
	public ReadWriteDtoService<IdmNotificationTemplateDto, IdmNotificationTemplateFilter> getService() {
		return notificationService;
	}
}
