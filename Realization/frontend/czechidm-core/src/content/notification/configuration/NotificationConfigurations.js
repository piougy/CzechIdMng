import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
import Helmet from 'react-helmet';
//
import * as Basic from '../../../components/basic';
import * as Advanced from '../../../components/advanced';
import * as Utils from '../../../utils';
import { NotificationConfigurationManager, SecurityManager, DataManager, NotificationTemplateManager } from '../../../redux';
import NotificationLevelEnum from '../../../enums/NotificationLevelEnum';

const uiKey = 'notification-configurations-table';
const manager = new NotificationConfigurationManager();
const notificationTemplateManager = new NotificationTemplateManager();

/**
 * List of notifications
 */
export default class NotificationConfigurations extends Basic.AbstractTableContent {

  constructor(props, context) {
    super(props, context);
  }

  getManager() {
    return manager;
  }

  getUiKey() {
    return uiKey;
  }

  getContentKey() {
    return 'content.notificationConfigurations';
  }

  componentDidMount() {
    this.selectNavigationItems(['notification', 'notification-configurations']);
    this.context.store.dispatch(manager.fetchSupportedNotificationTypes());
  }

  showDetail(entity) {
    super.showDetail(entity, () => {
      this.refs.topic.focus();
    });
  }

  save(entity, event) {
    const formEntity = this.refs.form.getData();
    //
    super.save(formEntity, event);
  }

  afterSave(entity, error) {
    if (!error) {
      this.addMessage({ message: this.i18n('save.success', { name: this.getManager().getNiceLabel(entity) }) });
    }
    //
    super.afterSave(entity, error);
  }

  render() {
    const { _showLoading, _supportedNotificationTypesLoading, _supportedNotificationTypes } = this.props;
    const { detail } = this.state;

    return (
      <div>
        <Helmet title={this.i18n('title')} />
        <Basic.Confirm ref="confirm-delete" level="danger"/>

        <Basic.ContentHeader>
          <span dangerouslySetInnerHTML={{ __html: this.i18n('header') }}/>
        </Basic.ContentHeader>

        <Basic.Panel className="last">
          <Advanced.Table
            ref="table"
            uiKey={uiKey}
            manager={this.getManager()}
            showRowSelection={SecurityManager.hasAnyAuthority(['NOTIFICATIONCONFIGURATION_WRITE'])}
            actions={
              SecurityManager.hasAnyAuthority(['NOTIFICATIONCONFIGURATION_WRITE'])
              ?
              [{ value: 'delete', niceLabel: this.i18n('action.delete.action'), action: this.onDelete.bind(this), disabled: false }]
              :
              null
            }
            buttons={
              [
                <Basic.Button
                  level="success"
                  key="add_button"
                  className="btn-xs"
                  onClick={this.showDetail.bind(this, { level: NotificationLevelEnum.findKeyBySymbol(NotificationLevelEnum.SUCCESS) })}
                  rendered={SecurityManager.hasAnyAuthority(['NOTIFICATIONCONFIGURATION_WRITE'])}>
                  <Basic.Icon type="fa" icon="plus"/>
                  {' '}
                  {this.i18n('button.add')}
                </Basic.Button>
              ]
            }>
            <Advanced.Column
              property=""
              header=""
              className="detail-button"
              cell={
                ({ rowIndex, data }) => {
                  return (
                    <Advanced.DetailButton
                      title={this.i18n('button.detail')}
                      onClick={this.showDetail.bind(this, data[rowIndex])}/>
                  );
                }
              }/>
            <Advanced.Column property="topic" width="30%" header={this.i18n('entity.NotificationConfiguration.topic')} sort face="text" />
            <Advanced.Column property="template" header={this.i18n('entity.NotificationConfiguration.template')} sort />
            <Advanced.Column property="level" width="75px" header={this.i18n('entity.NotificationConfiguration.level')} sort face="enum" enumClass={NotificationLevelEnum} />
            <Advanced.Column property="notificationType" header={this.i18n('entity.NotificationConfiguration.notificationType')} sort face="text" />
          </Advanced.Table>
        </Basic.Panel>

        <Basic.Modal
          bsSize="large"
          show={detail.show}
          onHide={this.closeDetail.bind(this)}
          backdrop="static"
          keyboard={!_showLoading}
          showLoading={_supportedNotificationTypesLoading}>

          <form onSubmit={this.save.bind(this, {})}>
            <Basic.Modal.Header closeButton={!_showLoading} text={this.i18n('create.header')} rendered={Utils.Entity.isNew(detail.entity)}/>
            <Basic.Modal.Header closeButton={!_showLoading} text={this.i18n('edit.header', { name: detail.entity.topic })} rendered={!Utils.Entity.isNew(detail.entity)}/>
            <Basic.Modal.Body>
              <Basic.AbstractForm ref="form" showLoading={_showLoading}>
                <Basic.TextField
                  ref="topic"
                  label={this.i18n('entity.NotificationConfiguration.topic')}
                  required/>
                <Basic.TextField
                  ref="parameter"
                  label={this.i18n('entity.NotificationConfiguration.parameter.name')}
                  helpBlock={this.i18n('entity.NotificationConfiguration.parameter.help')}/>
                <Basic.SelectBox
                  ref="template"
                  label={this.i18n('entity.NotificationConfiguration.template')}
                  manager={notificationTemplateManager}/>
                <Basic.EnumSelectBox
                  ref="level"
                  enum={NotificationLevelEnum}
                  useSymbol={false}
                  label={this.i18n('entity.NotificationConfiguration.level')}/>
                <Basic.EnumSelectBox
                  ref="notificationType"
                  label={this.i18n('entity.NotificationConfiguration.notificationType')}
                  options={!_supportedNotificationTypes ? null : _supportedNotificationTypes.map(type => { return { value: type, niceLabel: type }; })}
                  required/>
                <Basic.TextArea
                  ref="description"
                  label={this.i18n('entity.NotificationConfiguration.description')}
                  helpBlock={this.i18n('entity.NotificationConfiguration.description')}
                  required/>
              </Basic.AbstractForm>
            </Basic.Modal.Body>

            <Basic.Modal.Footer>
              <Basic.Button
                level="link"
                onClick={this.closeDetail.bind(this)}
                showLoading={_showLoading}>
                {this.i18n('button.close')}
              </Basic.Button>
              <Basic.Button
                type="submit"
                level="success"
                showLoading={_showLoading}
                showLoadingIcon
                showLoadingText={this.i18n('button.saving')}>
                {this.i18n('button.save')}
              </Basic.Button>
            </Basic.Modal.Footer>
          </form>
        </Basic.Modal>
      </div>
    );
  }
}

NotificationConfigurations.propTypes = {
  _showLoading: PropTypes.bool,
  _supportedNotificationTypesLoading: PropTypes.bool
};
NotificationConfigurations.defaultProps = {
  _showLoading: false,
  _supportedNotificationTypesLoading: true
};

function select(state) {
  return {
    _showLoading: Utils.Ui.isShowLoading(state, `${uiKey}-detail`),
    _supportedNotificationTypesLoading: Utils.Ui.isShowLoading(state, NotificationConfigurationManager.SUPPORTED_NOTIFICATION_TYPES),
    _supportedNotificationTypes: DataManager.getData(state, NotificationConfigurationManager.SUPPORTED_NOTIFICATION_TYPES)
  };
}

export default connect(select)(NotificationConfigurations);
