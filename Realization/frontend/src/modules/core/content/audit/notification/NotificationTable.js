import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
//
import * as Basic from 'app/components/basic';
import * as Advanced from 'app/components/advanced';
import * as Utils from 'core/utils';
import NotificationRecipient from './NotificationRecipient';

/**
* Table of roles
*/
export class NotificationTable extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      filterOpened: this.props.filterOpened,
      detail: {
        show: false,
        entity: {}
      }
    };
  }

  getContentKey() {
    return 'content.notifications';
  }

  componentDidMount() {
  }

  componentDidUpdate() {
  }

  useFilter(event) {
    if (event) {
      event.preventDefault();
    }
    this.refs.table.getWrappedInstance().useFilter(this.refs.filterForm);
  }

  cancelFilter(event) {
    if (event) {
      event.preventDefault();
    }
    this.refs.table.getWrappedInstance().cancelFilter(this.refs.filterForm);
  }

  render() {
    const { uiKey, notificationManager } = this.props;
    const { filterOpened } = this.state;

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
              <Basic.AbstractForm ref="filterForm" className="form-horizontal">
                <Basic.Row className="last">
                  <div className="col-lg-4">
                    <Advanced.Filter.TextField
                      ref="text"
                      placeholder={this.i18n('filter.placeholder')}
                      label=""
                      labelSpan=""
                      componentSpan="col-lg-12"/>
                  </div>
                  <div className="col-lg-4">
                  </div>
                  <div className="col-lg-4 text-right">
                    <Advanced.Filter.FilterButtons cancelFilter={this.cancelFilter.bind(this)}/>
                  </div>
                </Basic.Row>
              </Basic.AbstractForm>
            </Advanced.Filter>
          }
          >

          <Advanced.Column
            header=""
            className="detail-button"
            cell={
              ({ rowIndex, data }) => {
                return (
                  <Advanced.DetailButton
                    title={this.i18n('button.detail')}
                    onClick={() => alert('not implemented')}/>
                );
              }
            }
            sort={false}/>
          <Advanced.Column property="created" sort face="datetime"/>
          <Advanced.Column property="topic" sort face="text"/>
          <Advanced.Column property="message.subject" sort face="text"/>
          <Advanced.Column
            property="recipients"
            cell={
              ({ rowIndex, data, property }) => {
                return data[rowIndex][property].map(recipient => {
                  return (
                    <NotificationRecipient recipient={recipient} />
                  );
                });
              }
            }/>
          <Advanced.Column
            property="from"
            cell={
              ({ rowIndex, data, property }) => {
                return (
                  <NotificationRecipient recipient={data[rowIndex][property]} />
                );
              }
            }/>
          <Advanced.Column property="sent" sort face="datetime"/>
          <Advanced.Column property="sentLog" sort face="text"/>
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
    _searchParameters: state.data.ui[component.uiKey] ? state.data.ui[component.uiKey].searchParameters : {},
    _showLoading: component.notificationManager.isShowLoading(state, `${component.uiKey}-detail`)
  };
}

export default connect(select, null, null, { withRef: true })(NotificationTable);
