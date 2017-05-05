import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
import * as Basic from '../../../components/basic';
import * as Advanced from '../../../components/advanced';
import * as Utils from '../../../utils';
import { IdentityManager } from '../../../redux';
import NotificationRecipientsCell from '../NotificationRecipientsCell';
import NotificationSentState from '../NotificationSentState';
import NotificationLevelEnum from '../../../enums/NotificationLevelEnum';

/**
* Table of audit log for emails
*/
export class EmailTable extends Basic.AbstractContent {

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
    return 'content.emails';
  }

  componentDidMount() {
  }

  componentDidUpdate() {
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
    this.context.router.push('/notification/emails/' + entity.id);
  }

  render() {
    const { uiKey, emailManager } = this.props;
    const { filterOpened } = this.state;

    return (
      <div>
        <Basic.Confirm ref="confirm-delete" level="danger"/>

        <Advanced.Table
          ref="table"
          uiKey={uiKey}
          manager={emailManager}
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
                      placeholder={this.i18n('content.notifications.filter.dateFrom.placeholder')}/>
                  </div>
                  <div className="col-lg-4">
                    <Advanced.Filter.DateTimePicker
                      mode="date"
                      ref="till"
                      placeholder={this.i18n('content.notifications.filter.dateTill.placeholder')}/>
                  </div>
                  <div className="col-lg-4 text-right">
                    <Advanced.Filter.FilterButtons cancelFilter={this.cancelFilter.bind(this)}/>
                  </div>
                </Basic.Row>

                <Basic.Row>
                  <div className="col-lg-4">
                    <Advanced.Filter.TextField
                      ref="text"
                      placeholder={this.i18n('content.notifications.filter.text.placeholder')}/>
                  </div>
                  <div className="col-lg-4">
                    <Advanced.Filter.SelectBox
                      ref="recipient"
                      placeholder={this.i18n('content.notifications.filter.recipient.placeholder')}
                      multiSelect={false}
                      manager={this.identityManager}
                      returnProperty="username"/>
                  </div>
                  <div className="col-lg-4">
                    <Advanced.Filter.SelectBox
                      ref="sender"
                      placeholder={this.i18n('content.notifications.filter.sender.placeholder')}
                      multiSelect={false}
                      manager={this.identityManager}
                      returnProperty="username"/>
                  </div>
                </Basic.Row>

                <Basic.Row className="last">
                  <div className="col-lg-4">
                    <Advanced.Filter.BooleanSelectBox
                      ref="sent"
                      placeholder={this.i18n('content.notifications.filter.sent.placeholder')}/>
                  </div>
                  <div className="col-lg-4">
                  </div>
                  <div className="col-lg-4">
                  </div>
                </Basic.Row>
              </Basic.AbstractForm>
            </Advanced.Filter>
          }>

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
          <Advanced.Column property="created" sort face="datetime"/>
          <Advanced.Column property="message.level" sort face="enum" enumClass={NotificationLevelEnum}/>
          <Advanced.Column property="message.subject" sort face="text"/>
          <Advanced.Column
            property="recipients"
              cell={
                ({ rowIndex, data }) => {
                  return (
                    <NotificationRecipientsCell notifId={data[rowIndex].id} identityOnly />
                  );
                }
              }/>
          <Advanced.Column
            property="sender"
            cell={
              ({ rowIndex, data, property }) => {
                return !data[rowIndex]._embedded ? null : this.identityManager.getNiceLabel(data[rowIndex]._embedded[property]);
              }
            }/>
          <Advanced.Column
            property="sent"
            cell={
              ({ rowIndex, data}) => {
                return (
                  <NotificationSentState notification={data[rowIndex]}/>
                );
              }
            }/>
          <Advanced.Column property="sentLog" sort face="text" width="20%"/>
        </Advanced.Table>
      </div>
    );
  }
}

EmailTable.propTypes = {
  uiKey: PropTypes.string.isRequired,
  emailManager: PropTypes.object.isRequired,
  filterOpened: PropTypes.bool
};

EmailTable.defaultProps = {
  filterOpened: false,
  _showLoading: false
};

function select(state, component) {
  return {
    _searchParameters: state.data.ui[component.uiKey] ? state.data.ui[component.uiKey].searchParameters : {},
    _showLoading: component.emailManager.isShowLoading(state, `${component.uiKey}-detail`)
  };
}

export default connect(select, null, null, { withRef: true })(EmailTable);
