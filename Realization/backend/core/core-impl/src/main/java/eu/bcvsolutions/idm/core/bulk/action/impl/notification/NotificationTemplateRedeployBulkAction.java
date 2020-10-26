package eu.bcvsolutions.idm.core.bulk.action.impl.notification;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.bulk.action.AbstractRedeployBulkAction;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.ResultModel;
import eu.bcvsolutions.idm.core.api.dto.ResultModels;
import eu.bcvsolutions.idm.core.api.exception.DefaultErrorModel;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationTemplateDto;
import eu.bcvsolutions.idm.core.notification.api.dto.filter.IdmNotificationTemplateFilter;
import eu.bcvsolutions.idm.core.notification.api.service.IdmNotificationTemplateService;
import eu.bcvsolutions.idm.core.notification.domain.NotificationGroupPermission;

/**
 * Redeploy given notification template.
 *
 * @author Ondrej Husnik
 * @author Radek Tomiška
 * @since 10.6.0
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
	public ResultModels prevalidate() {
		ResultModels results = super.prevalidate();
		//
		// add info message about classpath
		String redeployFolder = getConfigurationService().getValue(IdmNotificationTemplateService.TEMPLATE_FOLDER);
		if (StringUtils.isNotEmpty(redeployFolder)) {
			ResultModel result = new DefaultErrorModel(
					CoreResultCode.DEPLOY_TEMPLATE_FOLDER_FOUND, 
					ImmutableMap.of("redeployFolder", redeployFolder)
			);
			results.addInfo(result);
		}
		//
		return results;
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
