import React, { PropTypes } from 'react';
//
import * as Basic from 'app/components/basic';
import NotificationRecipient from './NotificationRecipient';
import NotificationRecipientCell from './NotificationRecipientCell';
import NotificationRecipientsCell from './NotificationRecipientsCell';

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
    const { notification, identityOnly } = this.props;
    //
    if (!notification) {
      return null;
    }
    //
    return (
      <div>
        <Basic.AbstractForm ref="form" className="form-horizontal">
          <Basic.DateTimePicker ref="created" label={this.i18n('entity.Notification.created')} readOnly/>
          <Basic.TextField ref="topic" label={this.i18n('entity.Notification.topic')} readOnly hidden={notification.topic !== ''} />

          <Basic.LabelWrapper
            label={this.i18n('entity.Notification.from')}>
            <NotificationRecipient recipient={notification.from} style={{ margin: '7px 0' }} identityOnly={identityOnly}/>
          </Basic.LabelWrapper>

          <Basic.LabelWrapper
            label={this.i18n('entity.Notification.recipients')}>
            {
              notification.recipients.map(recipient => {
                return (
                  <NotificationRecipient recipient={recipient} style={{ margin: '7px 0' }} identityOnly={identityOnly}/>
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
        {
          notification.relatedNotifications
          ?
          <div>
            <Basic.PanelBody>
              <Basic.ContentHeader text={this.i18n('relatedNotifications')}/>
            </Basic.PanelBody>
            <Basic.Table
              data={notification.relatedNotifications}
              rowClass={({ data, rowIndex }) => { return data[rowIndex].sent ? 'success' : ''; }}>
              <Basic.Column
                property="type"
                header={this.i18n('entity.Notification.type')}/>
              <Basic.Column
                property="created"
                header={this.i18n('entity.Notification.created')}
                cell={<Basic.DateCell format={this.i18n('format.datetime')}/>}
                rendered={false}/>
              <Basic.Column
                property="recipients"
                header={this.i18n('entity.Notification.recipients')}
                cell={<NotificationRecipientsCell />}/>
              <Basic.Column
                property="from"
                header={this.i18n('entity.Notification.from')}
                cell={<NotificationRecipientCell identityOnly={false} />}/>
              <Basic.Column
                property="sent"
                header={this.i18n('entity.Notification.sent')}
                cell={
                  cellProps => {
                    const { rowIndex, data, property } = cellProps;
                    const sent = data[rowIndex][property];
                    if (sent) {
                      return (
                        <Basic.DateCell format={this.i18n('format.datetime')} {...cellProps}/>
                      );
                    }
                    return (
                      <Basic.Label level="danger" text={this.i18n('label.notSent')}/>
                    );
                  }
                }/>
              <Basic.Column
                property="sentLog"
                header={this.i18n('entity.Notification.sentLog')}/>
            </Basic.Table>
          </div>
          :
          null
        }
        </div>
    );
  }
}

NotificationDetail.propTypes = {
  notification: PropTypes.object,
  identityOnly: PropTypes.bool
};
NotificationDetail.defaultProps = {
  identityOnly: false
};
