import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
import _ from 'lodash';
//
import * as Utils from '../../../utils';
import * as Basic from '../../../components/basic';
import * as Advanced from '../../../components/advanced';
import { LoggingEventManager } from '../../../redux';
import LogTypeEnum from '../../../enums/LogTypeEnum';

const manager = new LoggingEventManager();

/**
* Table of logging events
*
* @author Ondřej Kopr
*/
export class EventTable extends Advanced.AbstractTableContent {

  constructor(props, context) {
    super(props, context);
  }

  componentDidMount() {
    super.componentDidMount();
  }

  getContentKey() {
    return 'content.audit.event';
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
    if (this.refs.table !== undefined) {
      this.refs.table.getWrappedInstance().cancelFilter(this.refs.filterForm);
    }
  }

  /**
  * Method get last string of split string by dot.
  * Used for get niceLabel for type entity.
  */
  _getType(name) {
    const type = name.split('.');
    return type[type.length - 1];
  }

  /**
  * Method get last string of arrays split string by dot.
  * Used method _getType
  */
  _getTypeArray(arrayOfName) {
    for (const index in arrayOfName) {
      if (arrayOfName.hasOwnProperty(index)) {
        arrayOfName[index] = this._getType(arrayOfName[index]);
      }
    }
    return _.join(arrayOfName, ', ');
  }

  _getAdvancedFilter() {
    return (
      <Advanced.Filter onSubmit={this.useFilter.bind(this)}>
        <Basic.AbstractForm ref="filterForm">
          <Basic.Row>
            <Basic.Col lg={ 4 }>
              <Advanced.Filter.DateTimePicker
                mode="datetime"
                ref="from"
                placeholder={this.i18n('filter.dateFrom.placeholder')}/>
            </Basic.Col>
            <Basic.Col lg={ 4 }>
              <Advanced.Filter.DateTimePicker
                mode="datetime"
                ref="till"
                placeholder={this.i18n('filter.dateTill.placeholder')}/>
            </Basic.Col>
            <div className="col-lg-4 text-right">
              <Advanced.Filter.FilterButtons cancelFilter={this.cancelFilter.bind(this)}/>
            </div>
          </Basic.Row>
          <Basic.Row>
            <Basic.Col lg={ 4 }>
              <Advanced.Filter.EnumSelectBox
                ref="levelString"
                searchable
                placeholder={this.i18n('entity.LoggingEvent.levelString')}
                enum={LogTypeEnum}/>
            </Basic.Col>
            <Basic.Col lg={ 4 }>
              <Advanced.Filter.TextField
                ref="text"
                placeholder={this.i18n('entity.LoggingEvent.text')}/>
            </Basic.Col>
            <Basic.Col lg={ 4 }>
              <Advanced.Filter.TextField
                className="pull-right"
                ref="callerFilename"
                placeholder={this.i18n('entity.LoggingEvent.callerFilename')}/>
            </Basic.Col>
          </Basic.Row>
          <Basic.Row className="last">
            <Basic.Col lg={ 4 }>
              <Advanced.Filter.TextField
                ref="callerLine"
                placeholder={this.i18n('entity.LoggingEvent.callerLine')}/>
            </Basic.Col>
            <Basic.Col lg={ 4 }>
              <Advanced.Filter.TextField
                ref="callerMethod"
                placeholder={this.i18n('entity.LoggingEvent.callerMethod')}/>
            </Basic.Col>
            <Basic.Col lg={ 4 }>
              <Advanced.Filter.TextField
                ref="loggerName"
                placeholder={this.i18n('entity.LoggingEvent.loggerName')}/>
            </Basic.Col>
          </Basic.Row>
        </Basic.AbstractForm>
      </Advanced.Filter>
    );
  }

  /**
   * Method for show detail of revision, redirect to detail
   *
   * @param entityId id of revision
   */
  showDetail(entityId) {
    this.context.router.push(`/audit/logging-event/${entityId}`);
  }

  render() {
    const { uiKey } = this.props;
    return (
      <div>
        <Advanced.Table
          ref="table"
          uiKey={uiKey}
          filterOpened
          manager={manager} showId={false}
          rowClass={({rowIndex, data}) => { return Utils.Ui.getRowClass(data[rowIndex]); }}
          filter={ this._getAdvancedFilter() }
          _searchParameters={ this.getSearchParameters() }>
          <Advanced.Column
            header=""
            className="detail-button"
            cell={
              ({ rowIndex, data }) => {
                return (
                  <Advanced.DetailButton
                    title={this.i18n('button.detail')}
                    onClick={this.showDetail.bind(this, data[rowIndex].id)}/>
                );
              }
            }
            sort={false}/>
          <Advanced.ColumnLink to="/audit/logging-event/:id" property="id" sort face="text" />
          <Advanced.Column sort
            property="callerClass"
            cell={
              ({ rowIndex, data, property }) => {
                return (
                  <span title={data[rowIndex][property]}>
                    { this._getType(data[rowIndex][property]) }
                  </span>
                );
              }}
            />
          <Advanced.Column sort
            property="callerMethod"/>
          <Advanced.Column property="levelString"
            sort face="enum" enumClass={LogTypeEnum}/>
          <Advanced.Column property="timestmp"
            sort face="datetime"/>
        </Advanced.Table>
      </div>
    );
  }
}

EventTable.propTypes = {
  // table uiKey
  uiKey: PropTypes.string.isRequired,
};

EventTable.defaultProps = {
  isDetail: false
};

function select(state, component) {
  return {
    _searchParameters: Utils.Ui.getSearchParameters(state, component.uiKey)
  };
}

export default connect(select)(EventTable);
