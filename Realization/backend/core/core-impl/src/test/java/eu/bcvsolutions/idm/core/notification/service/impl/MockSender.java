package eu.bcvsolutions.idm.core.notification.service.impl;

import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationDto;
import eu.bcvsolutions.idm.core.notification.entity.IdmNotification;

/**
 * Test moc sender
 * 
 * @author Radek Tomiška
 *
 */
public class MockSender extends AbstractNotificationSender<IdmNotificationDto> {

	public static final String NOTIFICATION_TYPE = "mock-custom-type";
	private final String testName;
	private final int order;

	public MockSender(String testName, int order) {
		this.testName = testName;
		this.order = order;
	}

	@Override
	public String getType() {
		return NOTIFICATION_TYPE;
	}

	public String getTestName() {
		return testName;
	}

	@Override
	public int getOrder() {
		return order;
	}

	@Override
	public Class<? extends BaseEntity> getNotificationType() {
		return IdmNotification.class;
	}

	@Override
	public IdmNotificationDto send(IdmNotificationDto notification) {
		return null; // not needed
	}
}