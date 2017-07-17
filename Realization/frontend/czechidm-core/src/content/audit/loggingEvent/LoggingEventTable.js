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
    this.context.router.push(`/audit/events/${entityId}`);
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
                    onClick={this.showDetail.bind(this, data[rowIndex].eventId)}/>
                );
              }
            }
            sort={false}/>
          <Advanced.ColumnLink to="audit/events/:id" property="id" sort face="text" />
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
