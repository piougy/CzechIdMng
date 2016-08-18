import React, { PropTypes } from 'react';
import { IdentityManager, NotificationManager } from 'core/redux';
import * as Basic from 'app/components/basic';
import NotificationRecipient from './NotificationRecipient';
import NotificationRecipientCell from './NotificationRecipientCell';
import NotificationRecipientsCell from './NotificationRecipientsCell';
import NotificationSentState from '../notification/NotificationSentState';

/**
 * Notification detail content
 */
export default class NotificationDetail extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.identityManager = new IdentityManager();
    this.notificationManager = new NotificationManager();
  }

  getContentKey() {
    return 'content.notification';
  }

  save(event) {
    const { uiKey } = this.props;

    if (event) {
      event.preventDefault();
    }
    if (!this.refs.form.isFormValid()) {
      return;
    }
    const entity = this.refs.form.getData();
    // append recipients to recipientsData
    const recipientsData = [];
    if (entity.recipients) {
      entity.recipients.forEach(entityId => {
        recipientsData.push({identityRecipient: this.identityManager.getSelfLink(entityId)});
      });
    }
    const sender = {identityRecipient: this.identityManager.getSelfLink(entity.sender)};
    const saveEntity = {
      ...entity,
      sender,
      recipients: recipientsData,
      message: {
        subject: entity.subject,
        textMessage: entity.textMessage,
        htmlMessage: entity.htmlMessage
      }
    };
    this.context.store.dispatch(this.notificationManager.createEntity(saveEntity, `${uiKey}-detail`, (createdEntity, error) => {
      this._afterSave(createdEntity, error);
      if (!error) {
        this.refs.table.getWrappedInstance().reload();
      }
    }));
  }

  _afterSave(entity, error) {
    if (error) {
      this.refs.form.processEnded();
      this.addError(error);
      return;
    }
    this.addMessage({ message: this.i18n('save.success', { name: entity.name }) });
    this.closeDetail();
    this.context.router.replace('audit/notifications/');
  }

  closeDetail() {
    this.setState({
      detail: {
        show: false,
        entity: {}
      }
    });
  }

  componentDidMount() {
    const { notification, isNew } = this.props;
    let data;
    if (isNew) {
      data = { ...notification };
    } else {
      data = {
        ...notification,
        subject: notification.message.subject,
        textMessage: notification.message.textMessage,
        htmlMessage: notification.message.htmlMessage
      };
    }
    this.refs.form.setData(data);
  }

  render() {
    const { notification, identityOnly, isNew } = this.props;
    //
    if (!notification) {
      return null;
    }
    //
    return (
      <div>
        <Basic.AbstractForm ref="form" className="form-horizontal">
          <Basic.DateTimePicker ref="created" label={this.i18n('entity.Notification.created')} readOnly hidden={isNew}/>
          <Basic.TextField ref="topic" label={this.i18n('entity.Notification.topic')} readOnly={!isNew} />

            <Basic.SelectBox hidden={!isNew} required
              ref="sender"
              label={this.i18n('entity.Notification.sender')}
              manager={this.identityManager}/>

          <Basic.LabelWrapper hidden={isNew}
            label={this.i18n('entity.Notification.sender')}>
            <NotificationRecipient recipient={notification.sender} style={{ margin: '7px 0' }} identityOnly={identityOnly}/>
          </Basic.LabelWrapper>

          <Basic.LabelWrapper hidden={isNew}
            label={this.i18n('entity.Notification.recipients')}>
            {
              notification.recipients
              ?
              notification.recipients.map(recipient => {
                return (
                  <NotificationRecipient recipient={recipient} style={{ margin: '7px 0' }} identityOnly={identityOnly}/>
                );
              })
              :
              null
            }
          </Basic.LabelWrapper>

          <Basic.SelectBox hidden={!isNew}
            ref="recipients" required
            label={this.i18n('entity.Notification.recipients')}
            manager={this.identityManager}
            multiSelect />

          <Basic.TextField ref="subject" required label={this.i18n('entity.Notification.message.subject')} readOnly={!isNew} />
          <Basic.TextArea ref="textMessage" label={this.i18n('entity.Notification.message.textMessage')} readOnly={!isNew} />
          <Basic.TextArea ref="htmlMessage" label={this.i18n('entity.Notification.message.htmlMessage')} readOnly={!isNew} />

          <Basic.LabelWrapper hidden={isNew}
            label={this.i18n('entity.Notification.sent')}>
            <NotificationSentState notification={notification}/>
          </Basic.LabelWrapper>

          <Basic.TextArea ref="sentLog" label={this.i18n('entity.Notification.sentLog')} readOnly hidden={isNew}/>
        </Basic.AbstractForm>
        {
          notification.relatedNotifications
          ?
          <div>
            <Basic.PanelBody>
              <Basic.ContentHeader text={this.i18n('relatedNotifications')}/>
            </Basic.PanelBody>
            <Basic.Table hidden={isNew}
              data={notification.relatedNotifications}>
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
                property="sender"
                header={this.i18n('entity.Notification.from')}
                cell={<NotificationRecipientCell identityOnly={false} />}/>
              <Basic.Column
                property="sent"
                header={this.i18n('entity.Notification.sent')}
                cell={
                  ({ rowIndex, data}) => {
                    return (
                      <NotificationSentState notification={data[rowIndex]}/>
                    );
                  }
                }/>
              <Basic.Column
                property="sentLog"
                header={this.i18n('entity.Notification.sentLog')}
                width="20%"/>
            </Basic.Table>
          </div>
          :
          null
        }
        <Basic.PanelFooter>
          <Basic.Button type="button" level="link" onClick={this.context.router.goBack}>{this.i18n('button.back')}</Basic.Button>
          <Basic.Button hidden={!isNew}
              onClick={this.save.bind(this)}
              level="success"
              showLoadingIcon
              showLoadingText={this.i18n('button.saving')}>
              {this.i18n('button.save')}
          </Basic.Button>
        </Basic.PanelFooter>
        </div>
    );
  }
}

NotificationDetail.propTypes = {
  notification: PropTypes.object,
  identityOnly: PropTypes.bool,
  isNew: PropTypes.bool
};
NotificationDetail.defaultProps = {
  identityOnly: false
};
