import React, { PropTypes } from 'react';
//
import * as Basic from '../../../components/basic';
import * as Advanced from '../../../components/advanced';
import NotificationStateEnum from '../../../enums/NotificationStateEnum';

/**
 * Notification sent state
 */
export default class NotificationSentState extends Basic.AbstractComponent {

  constructor(props) {
    super(props);
  }

  render() {
    const { rendered, notification, ...others } = this.props;
    //
    if (!rendered || !notification) {
      return null;
    }
    const sentCount = !notification.relatedNotifications ? 0 : notification.relatedNotifications.reduce((result, _notification) => { return result + (_notification.sent ? 1 : 0); }, 0);
    return (
      <div {...others}>
        {
          !notification.relatedNotifications || notification.relatedNotifications.length === 0
          ?
          <span>
            {
              notification.sent !== null
              ?
              <Basic.Label level="success" text={<Advanced.DateValue value={notification.sent} showTime/>}/>
              :
              <Basic.Label level="danger" text={NotificationStateEnum.getNiceLabelBySymbol(NotificationStateEnum.NOT)}/>
            }
          </span>
          :
          <span>
            {
              sentCount === notification.relatedNotifications.length
              ?
              <Basic.Label level="success" text={<Advanced.DateValue value={notification.sent} showTime/>}/>
              :
              <span>
                {
                  sentCount === 0
                  ?
                  <Basic.Label level="danger" text={NotificationStateEnum.getNiceLabelBySymbol(NotificationStateEnum.NOT)}/>
                  :
                  <Basic.Label level="warning" text={NotificationStateEnum.getNiceLabelBySymbol(NotificationStateEnum.PARTLY)}/>
                }
              </span>
            }
          </span>
        }
      </div>
    );
  }
}

NotificationSentState.propTypes = {
  ...Basic.AbstractComponent.propTypes,
  notification: PropTypes.object
};
NotificationSentState.defaultProps = {
  ...Basic.AbstractComponent.defaultProps
};
