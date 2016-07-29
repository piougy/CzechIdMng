import React, { PropTypes } from 'react';
//
import * as Basic from 'app/components/basic';
import NotificationRecipient from './NotificationRecipient';

/**
 * Notification detail content
 */
export default class NotificationDetail extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
  }

  getContentKey() {
    return 'content.notification';
  }

  componentDidMount() {
    const { notification } = this.props;
    //
    const data = {
      ...notification,
      subject: notification.message.subject,
      textMessage: notification.message.textMessage,
      htmlMessage: notification.message.htmlMessage
    };
    this.refs.form.setData(data);
  }

  render() {
    const { notification } = this.props;
    //
    if (!notification) {
      return null;
    }
    //
    return (
      <div>
        <Basic.AbstractForm ref="form" className="form-horizontal">
          <Basic.DateTimePicker ref="created" label={this.i18n('entity.Notification.created')} readOnly/>
          <Basic.TextField ref="topic" label={this.i18n('entity.Notification.topic')} readOnly/>

          <Basic.LabelWrapper
            label={this.i18n('entity.Notification.from')}>
            <NotificationRecipient recipient={notification.from} style={{ margin: '7px 0' }} identityOnly/>
          </Basic.LabelWrapper>

          <Basic.LabelWrapper
            label={this.i18n('entity.Notification.recipients')}>
            {
              notification.recipients.map(recipient => {
                return (
                  <NotificationRecipient recipient={recipient} style={{ margin: '7px 0' }} identityOnly/>
                );
              })
            }
          </Basic.LabelWrapper>

          <Basic.TextField ref="subject" label={this.i18n('entity.Notification.message.subject')} readOnly/>
          <Basic.TextArea ref="textMessage" label={this.i18n('entity.Notification.message.textMessage')} readOnly/>
          <Basic.TextArea ref="htmlMessage" label={this.i18n('entity.Notification.message.htmlMessage')} readOnly/>

          <Basic.DateTimePicker ref="sent" label={this.i18n('entity.Notification.sent')} readOnly/>
          <Basic.TextArea ref="sentLog" label={this.i18n('entity.Notification.sentLog')} readOnly/>
        </Basic.AbstractForm>
      </div>
    );
  }
}

NotificationDetail.propTypes = {
  notification: PropTypes.object
};
NotificationDetail.defaultProps = {
};
