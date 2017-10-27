import React, { PropTypes } from 'react';
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import * as Utils from '../../utils';
import { LongRunningTaskItemManager } from '../../redux';
import SearchParameters from '../../domain/SearchParameters';
import OperationStateEnum from '../../enums/OperationStateEnum';
import { SecurityManager } from '../../redux';

const manager = new LongRunningTaskItemManager();

/**
 * Shows all items which were already added into the queue. Supports delete also.
 *
 * @author Marek Klement
 */
export default class QueueItemTable extends Advanced.AbstractTableContent {

  constructor(props) {
    super(props);
    // default filter status
    // true - open
    // false - close
    this.state = {
      filterOpened: false
    };
  }

  getManager() {
    return manager;
  }

  getType(name) {
    if (name) {
      const type = name.split('.');
      return type[type.length - 1];
    }
    return null;
  }

  cutDto(type) {
    if (type) {
      if (type.substring(type.length - 3, type.length) === 'Dto') {
        type = type.substring(0, type.length - 3);
        return type;
      }
      return type;
    }
    return null;
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

  getAdvancedFilter() {
    return (
      <Advanced.Filter onSubmit={this.useFilter.bind(this)}>
        <Basic.AbstractForm ref="filterForm">
        <Basic.Row>
          <div className="col-lg-4">
            <Advanced.Filter.DateTimePicker
              mode="date"
              ref="from"
              placeholder={this.i18n('entity.LongRunningTaskItem.filter.from.placeholder')}/>
          </div>
          <div className="col-lg-4">
            <Advanced.Filter.DateTimePicker
              mode="date"
              ref="till"
              placeholder={this.i18n('entity.LongRunningTaskItem.filter.till.placeholder')}/>
          </div>
          <div className="col-lg-4 text-right">
            <Advanced.Filter.FilterButtons cancelFilter={this.cancelFilter.bind(this)}/>
          </div>
        </Basic.Row>
          <Basic.Row className="last">
            <div className="col-lg-4">
              <Advanced.Filter.EnumSelectBox
                ref="operationState"
                placeholder={this.i18n('entity.LongRunningTaskItem.filter.state.placeholder')}
                enum={OperationStateEnum}/>
            </div>
            <div className="col-lg-4">
              <Advanced.Filter.TextField
                ref="text"
                placeholder={this.i18n('entity.LongRunningTaskItem.filter.text.placeholder')}/>
            </div>
          </Basic.Row>
        </Basic.AbstractForm>
      </Advanced.Filter>
    );
  }

  render() {
    const { uiKey, entity, showRowSelection } = this.props;
    const { filterOpened } = this.state;
    const forceSearchParameters = new SearchParameters().setFilter('scheduledTaskId', entity.scheduledTask);
    return (
      <div>
      <Basic.Confirm ref="confirm-delete" level="danger"/>
      <Basic.PageHeader>
        {this.i18n('entity.QueueItem.header')} <small> {Utils.Ui.getSimpleJavaType(entity.taskType)}</small>
      </Basic.PageHeader>
      <Advanced.Table
        ref="table"
        showRowSelection={showRowSelection}
        uiKey={uiKey}
        manager={manager}
        forceSearchParameters={forceSearchParameters}
        rowClass={({rowIndex, data}) => { return Utils.Ui.getRowClass(data[rowIndex]); }}
        filter={this.getAdvancedFilter()}
        filterOpened={filterOpened}
        _searchParameters={null}
        actions={
          SecurityManager.hasAnyAuthority(['SYSTEM_UPDATE'])
          ?
          [{ value: 'delete', niceLabel: this.i18n('action.delete.action'), action: this.onDelete.bind(this), disabled: false }]
          :
          null
        }
        buttons={null}>
        <Advanced.Column
          property="operationResult.state"
          header={this.i18n('entity.LongRunningTaskItem.state')}
          width={75}
          sort
          face="enum"
          enumClass={OperationStateEnum}
        />
        <Advanced.Column
          property="referencedEntityId"
          header={this.i18n('entity.LongRunningTaskItem.entityName')}
          cell={
            ({ rowIndex, data, property }) => {
              return (
                <Advanced.EntityInfo
                  entityType={ this.cutDto(this.getType(data[rowIndex].referencedDtoType)) }
                  entityIdentifier={ data[rowIndex][property] }
                  face="popover"
                  showEntityType={ false }/>
              );
            }
          }
          width={250}
          sort
          face="text"
        />
        <Advanced.Column
          property="referencedDtoType"
          header={this.i18n('entity.LongRunningTaskItem.entityType')}
          width={75}
          sort
          face="text"
        />
        <Advanced.Column
          property="created"
          header={this.i18n('entity.LongRunningTaskItem.created')}
          width={75}
          sort
          face="datetime"
        />
      </Advanced.Table>
      </div>
    );
  }
}

QueueItemTable.propTypes = {
  uiKey: PropTypes.string.isRequired,
  manager: PropTypes.object.isRequired,
  showRowSelection: PropTypes.boolean
};

QueueItemTable.defaultProps = {
  showRowSelection: true
};
