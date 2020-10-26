package eu.bcvsolutions.idm.core.bulk.action.impl.notification;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.bulk.action.AbstractRemoveBulkAction;
import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.DefaultResultModel;
import eu.bcvsolutions.idm.core.api.dto.ResultModels;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationTemplateDto;
import eu.bcvsolutions.idm.core.notification.api.dto.filter.IdmNotificationTemplateFilter;
import eu.bcvsolutions.idm.core.notification.api.service.IdmNotificationTemplateService;
import eu.bcvsolutions.idm.core.notification.domain.NotificationGroupPermission;

/**
 * Delete given notification template
 *
 * @author Ondrej Husnik
 * @since 10.6.0
 */
@Component(NotificationTemplateDeleteBulkAction.NAME)
@Description("Delete given notification template.")
public class NotificationTemplateDeleteBulkAction extends AbstractRemoveBulkAction<IdmNotificationTemplateDto, IdmNotificationTemplateFilter> {

	public static final String NAME = "notification-template-delete-bulk-action";

	@Autowired
	private IdmNotificationTemplateService notificationService;
	
	@Override
	public String getName() {
		return NAME;
	}

	@Override
	protected List<String> getAuthoritiesForEntity() {
		return Lists.newArrayList(NotificationGroupPermission.NOTIFICATIONTEMPLATE_DELETE);
	}

	@Override
	public ReadWriteDtoService<IdmNotificationTemplateDto, IdmNotificationTemplateFilter> getService() {
		return notificationService;
	}
	
	@Override
	public ResultModels prevalidate() {
		IdmBulkActionDto action = getAction();
		List<UUID> entities = getEntities(action, new StringBuilder());
		ResultModels results = new ResultModels();

		IdmNotificationTemplateFilter templateFilter = new IdmNotificationTemplateFilter();
		templateFilter.setUnmodifiable(Boolean.TRUE);
		List<IdmNotificationTemplateDto> unmodifiable = notificationService.find(templateFilter, null).getContent();
		List<IdmNotificationTemplateDto> templatesToWarn = unmodifiable.stream().filter(dto -> {
			return entities.contains(dto.getId());
		}).collect(Collectors.toList());

		for (IdmNotificationTemplateDto item : templatesToWarn) {
			results.addInfo(new DefaultResultModel(CoreResultCode.NOTIFICATION_SYSTEM_TEMPLATE_DELETE_FAILED,
					ImmutableMap.of("template", item.getCode())));
		}
		return results;
	}
}
