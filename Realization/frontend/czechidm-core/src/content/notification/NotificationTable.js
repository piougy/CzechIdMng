import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
import uuid from 'uuid';
import _ from 'lodash';
//
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import * as Utils from '../../utils';
import { IdentityManager, SecurityManager, NotificationConfigurationManager, DataManager } from '../../redux';
import NotificationStateEnum from '../../enums/NotificationStateEnum';
import NotificationLevelEnum from '../../enums/NotificationLevelEnum';
import NotificationRecipientsCell from './NotificationRecipientsCell';
import NotificationSentState from './NotificationSentState';
import SearchParameters from '../../domain/SearchParameters';

const notificationConfigurationManager = new NotificationConfigurationManager();

/**
* Audit for sent notifications
*
* @author Radek Tomiška
*/
export class NotificationTable extends Advanced.AbstractTableContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      filterOpened: this.props.filterOpened,
      detail: {
        show: false,
        entity: {}
      }
    };
    this.identityManager = new IdentityManager();
  }

  getContentKey() {
    return 'content.notifications';
  }

  componentDidMount() {
    super.componentDidMount();
    this.context.store.dispatch(notificationConfigurationManager.fetchSupportedNotificationTypes());
  }

  useFilter(event) {
    if (event) {
      event.preventDefault();
    }
    this.refs.table.getWrappedInstance().useFilterForm(this.refs.filterForm);
  }

  cancelFilter(event) {
    if (event) {
      event.preventDefault();
    }
    this.refs.table.getWrappedInstance().cancelFilter(this.refs.filterForm);
  }

  showDetail(entity, event) {
    if (event) {
      event.preventDefault();
    }

    if (entity.id === undefined) {
      const uuidId = uuid.v1();
      this.context.router.push(`/notification/notification/${uuidId}?new=1`);
    } else {
      this.context.router.push('/notification/notification/' + entity.id);// notification/configurations
    }
  }

  _getStatus(data, rowIndex) {
    return (
      <NotificationSentState notification={data[rowIndex]}/>
    );
  }

  render() {
    const { uiKey, notificationManager, supportedNotificationTypes } = this.props;
    const { filterOpened } = this.state;
    const forceSearchParameters = new SearchParameters().setFilter('notificationType', 'notification');
    //
    let _supportedNotificationTypes = null;
    if (supportedNotificationTypes) {
      _supportedNotificationTypes = _.union(supportedNotificationTypes, ['notification']);
    }
    //
    return (
      <div>
        <Basic.Confirm ref="confirm-delete" level="danger"/>

        <Advanced.Table
          ref="table"
          uiKey={uiKey}
          manager={notificationManager}
          rowClass={({rowIndex, data}) => { return Utils.Ui.getRowClass(data[rowIndex]); }}
          filterOpened={filterOpened}
          filter={
            <Advanced.Filter onSubmit={this.useFilter.bind(this)}>
              <Basic.AbstractForm ref="filterForm">
                <Basic.Row>
                  <div className="col-lg-4">
                    <Advanced.Filter.DateTimePicker
                      mode="date"
                      ref="from"
                      placeholder={this.i18n('filter.dateFrom.placeholder')}/>
                  </div>
                  <div className="col-lg-4">
                    <Advanced.Filter.DateTimePicker
                      mode="date"
                      ref="till"
                      placeholder={this.i18n('filter.dateTill.placeholder')}/>
                  </div>
                  <div className="col-lg-4 text-right">
                    <Advanced.Filter.FilterButtons cancelFilter={this.cancelFilter.bind(this)}/>
                  </div>
                </Basic.Row>

                <Basic.Row>
                  <div className="col-lg-4">
                    <Advanced.Filter.TextField
                      ref="text"
                      placeholder={this.i18n('filter.text.placeholder')}/>
                  </div>
                  <div className="col-lg-4">
                    <Advanced.Filter.SelectBox
                      ref="recipient"
                      placeholder={this.i18n('filter.recipient.placeholder')}
                      multiSelect={false}
                      manager={this.identityManager}
                      returnProperty="username"/>
                  </div>
                  <div className="col-lg-4">
                    <Advanced.Filter.SelectBox
                      ref="sender"
                      placeholder={this.i18n('filter.sender.placeholder')}
                      multiSelect={false}
                      manager={this.identityManager}
                      returnProperty="username"/>
                  </div>
                </Basic.Row>

                <Basic.Row className="last">
                  <div className="col-lg-4">
                    <Advanced.Filter.EnumSelectBox
                      ref="state"
                      placeholder={this.i18n('filter.sent.placeholder')}
                      enum={NotificationStateEnum}/>
                  </div>
                  <div className="col-lg-4 hidden">
                    <Advanced.Filter.EnumSelectBox
                      ref="notificationType"
                      placeholder={ this.i18n('entity.NotificationConfiguration.notificationType') }
                      options={ !_supportedNotificationTypes ? null : _supportedNotificationTypes.map(type => { return { value: type, niceLabel: type }; }) }/>
                  </div>
                  <div className="col-lg-4">
                  </div>
                </Basic.Row>
              </Basic.AbstractForm>
            </Advanced.Filter>
          }
          buttons={
            [
              <Basic.Button level="success" key="add_button" className="btn-xs" onClick={this.showDetail.bind(this, {})} rendered={SecurityManager.hasAuthority('NOTIFICATION_CREATE')}>
                <Basic.Icon type="fa" icon="plus"/>
                {' '}
                {this.i18n('button.send')}
              </Basic.Button>
            ]
          }
          forceSearchParameters={ forceSearchParameters }
          _searchParameters={ this.getSearchParameters() }>

          <Advanced.Column
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
          <Advanced.Column property="type" header={this.i18n('entity.Notification.type')} rendered={ false }/>
          <Advanced.Column property="created" sort face="datetime"/>
          <Advanced.Column property="topic" sort face="text"/>
          <Advanced.Column property="message.level" sort face="enum" enumClass={ NotificationLevelEnum }/>
          <Advanced.Column property="message.subject" sort face="text"/>
          <Advanced.Column
            property="recipients"
            cell={
              ({ rowIndex, data }) => {
                return (
                  <NotificationRecipientsCell notification={ data[rowIndex] } identityOnly />
                );
              }
            }/>
          <Advanced.Column
            property="sender"
            cell={
              ({ rowIndex, data }) => {
                return !data[rowIndex]._embedded ? null : this.identityManager.getNiceLabel(data[rowIndex]._embedded.identitySender);
              }
            }/>
          <Advanced.Column
            property="sent"
            cell={
              ({ rowIndex, data }) => {
                return this._getStatus(data, rowIndex);
              }
            }/>
          <Advanced.Column property="sentLog" sort face="text" rendered={false} width="300px"/>
        </Advanced.Table>
      </div>
    );
  }
}

NotificationTable.propTypes = {
  uiKey: PropTypes.string.isRequired,
  notificationManager: PropTypes.object.isRequired,
  filterOpened: PropTypes.bool
};

NotificationTable.defaultProps = {
  filterOpened: false,
  _showLoading: false
};

function select(state, component) {
  return {
    _searchParameters: Utils.Ui.getSearchParameters(state, component.uiKey),
    _showLoading: component.notificationManager.isShowLoading(state, `${component.uiKey}-detail`),
    supportedNotificationTypes: DataManager.getData(state, NotificationConfigurationManager.SUPPORTED_NOTIFICATION_TYPES)
  };
}

export default connect(select, null, null, { withRef: true })(NotificationTable);
