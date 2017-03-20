import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
//
import { IdentityManager, NotificationManager, NotificationTemplateManager } from '../../redux';
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import NotificationRecipient from './NotificationRecipient';
import NotificationRecipientsCell from './NotificationRecipientsCell';
import NotificationSentState from '../notification/NotificationSentState';
import NotificationLevelEnum from '../../enums/NotificationLevelEnum';

/**
 * Notification detail content
 */
class NotificationDetail extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      showLoading: false,
      templateActive: false // if is choose template deactivate some fields
    };
    this.identityManager = new IdentityManager();
    this.notificationManager = new NotificationManager();
    this.notificationTemplateManager = new NotificationTemplateManager();
  }

  getContentKey() {
    return 'content.notification';
  }

  componentDidMount() {
    const { notification, isNew, userContext } = this.props;
    let data;
    if (isNew) {
      notification.sender = userContext.username;
      data = { ...notification };
      this.refs.subject.focus();
    } else {
      let template = null;
      if (notification.message.template) {
        template = notification.message.template;
      } else if (notification.message._embedded && notification.message._embedded.template) {
        template = notification.message._embedded.template;
      }
      data = {
        ...notification,
        subject: notification.message.subject,
        textMessage: notification.message.textMessage,
        htmlMessage: notification.message.htmlMessage,
        level: notification.message.level,
        template
      };
    }
    this.refs.form.setData(data);
  }

  save(event) {
    const { uiKey } = this.props;

    if (event) {
      event.preventDefault();
    }
    if (!this.refs.form.isFormValid()) {
      return;
    }

    this.setState({
      showLoading: true
    }, this.refs.form.processStarted());

    const entity = this.refs.form.getData();
    // append recipients to recipientsData
    const recipientsData = [];
    if (entity.recipients) {
      entity.recipients.forEach(entityId => {
        recipientsData.push({identityRecipient: this.identityManager.getSelfLink(entityId)});
      });
    }
    const sender = this.identityManager.getSelfLink(entity.sender);
    const template = this.notificationTemplateManager.getSelfLink(entity.template);
    const saveEntity = {
      ...entity,
      sender,
      recipients: recipientsData,
      message: {
        subject: entity.subject,
        textMessage: entity.textMessage,
        htmlMessage: entity.htmlMessage,
        level: entity.level,
        template
      }
    };

    this.context.store.dispatch(this.notificationManager.createEntity(saveEntity, `${uiKey}-detail`, (createdEntity, error) => {
      this._afterSave(createdEntity, error);
    }));
  }

  _afterSave(entity, error) {
    if (error) {
      this.addError(error);
      this.setState({
        showLoading: false
      }, this.refs.form.processEnded());
      return;
    }
    this.addMessage({ message: this.i18n('sent.success', { name: entity.name }) });
    this.context.router.replace('notification/notifications/');
  }

  _pickTemplate(template, event) {
    if (event) {
      event.preventDefault();
    }
    if (template) {
      this.setState({
        templateActive: true
      });
      const data = {
        ...this.refs.form.getData(),
        subject: template.subject,
        textMessage: template.bodyText,
        htmlMessage: template.bodyHtml
      };
      this.refs.form.setData(data);
    } else {
      this.setState({
        templateActive: false
      });
    }
  }

  render() {
    const { notification, identityOnly, isNew } = this.props;
    const { showLoading } = this.state;
    //
    if (!notification) {
      return null;
    }
    //
    return (
      <div>
        <Basic.AbstractForm ref="form" style={{ padding: 15 }}>
          <Basic.DateTimePicker ref="created" label={this.i18n('entity.Notification.created')} readOnly hidden={isNew}/>

          <Basic.LabelWrapper hidden={isNew}
            label={this.i18n('entity.Notification.sent')}>
            <NotificationSentState notification={notification} style={{ margin: '7px 0' }}/>
          </Basic.LabelWrapper>

          <Basic.TextField ref="topic" label={this.i18n('entity.Notification.topic')} readOnly={!isNew} />

          <Basic.TextField ref="subject" required label={this.i18n('entity.Notification.message.subject')} readOnly={!isNew} />

          <Basic.EnumSelectBox
            hidden={!isNew}
            ref="level"
            enum={NotificationLevelEnum}
            useSymbol={false}
            label={this.i18n('entity.Notification.message.level')}
            required/>

          <Basic.LabelWrapper hidden={isNew || !notification.message.level}
              label={this.i18n('entity.Notification.message.level')}>
              <div style={{ margin: '7px 0' }}>
                {
                  !notification.message
                  ||
                  <Basic.EnumValue value={notification.message.level} enum={NotificationLevelEnum} />
                }
              </div>
          </Basic.LabelWrapper>

          <Basic.SelectBox
            hidden={!isNew}
            ref="sender"
            label={this.i18n('entity.Notification.sender')}
            manager={this.identityManager}/>

          <Basic.SelectBox
            readOnly={!isNew}
            onChange={this._pickTemplate.bind(this)}
            ref="template"
            label={this.i18n('entity.Notification.template')}
            manager={this.notificationTemplateManager}/>

          <Basic.LabelWrapper hidden={isNew || !notification._embedded || !notification._embedded.sender}
            label={this.i18n('entity.Notification.sender')}>
            <div style={{ margin: '7px 0' }}>
              {
                !notification._embedded
                ||
                this.identityManager.getNiceLabel(notification._embedded.sender)
              }
            </div>
          </Basic.LabelWrapper>

          <Basic.LabelWrapper hidden={isNew}
            label={this.i18n('entity.Notification.recipients')}>
            {
              notification.recipients
              ?
              <div style={{ margin: '7px 0' }}>
                {
                  notification.recipients.map(recipient => {
                    return (
                      <NotificationRecipient recipient={recipient} identityOnly={identityOnly}/>
                    );
                  })
                }
              </div>
              :
              null
            }
          </Basic.LabelWrapper>

          <Basic.SelectBox hidden={!isNew}
            ref="recipients" required
            label={this.i18n('entity.Notification.recipients')}
            manager={this.identityManager}
            multiSelect />

          <Basic.TextArea ref="textMessage" label={this.i18n('entity.Notification.message.textMessage')} readOnly={!isNew} hidden={!isNew && !notification.message.textMessage} />
          <Advanced.RichTextArea ref="htmlMessage" label={this.i18n('entity.Notification.message.htmlMessage')} readOnly={!isNew} hidden={!isNew && !notification.message.htmlMessage} />

          <Basic.LabelWrapper hidden={isNew || !notification.message.model}
            label={this.i18n('entity.Notification.message.model')}>
            {
              !notification.message
              ||
              <Basic.FlashMessage level={notification.message.level} message={this.getFlashManager().convertFromResultModel(notification.message.model)} style={{ margin: '7px 0' }}/>
            }
          </Basic.LabelWrapper>

          <Basic.TextArea ref="sentLog" label={this.i18n('entity.Notification.sentLog')} readOnly hidden={isNew || !notification.sentLog} max={2000} />
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
                header={this.i18n('entity.Notification.sender')}
                cell={
                  ({ rowIndex, data, property }) => {
                    return !data[rowIndex]._embedded ? null : this.identityManager.getNiceLabel(data[rowIndex]._embedded[property]);
                  }
                }/>
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
          <Basic.Button type="button" level="link" onClick={this.context.router.goBack} showLoading={showLoading}>{this.i18n('button.back')}</Basic.Button>
          <Basic.Button hidden={!isNew}
              onClick={this.save.bind(this)}
              level="success"
              showLoadingIcon
              showLoadingText={this.i18n('button.sending')}>
              {this.i18n('button.send')}
          </Basic.Button>
        </Basic.PanelFooter>
        </div>
    );
  }
}

NotificationDetail.propTypes = {
  notification: PropTypes.object,
  identityOnly: PropTypes.bool,
  isNew: PropTypes.bool,
  userContext: PropTypes.object
};
NotificationDetail.defaultProps = {
  identityOnly: false,
  showLoading: false,
  userContext: null
};

function select(state) {
  return {
    userContext: state.security.userContext
  };
}

export default connect(select)(NotificationDetail);
