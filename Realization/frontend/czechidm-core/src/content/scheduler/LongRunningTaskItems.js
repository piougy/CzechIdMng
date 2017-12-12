import React from 'react';
import { connect } from 'react-redux';
//
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import * as Utils from '../../utils';
import { LongRunningTaskItemManager } from '../../redux';
import SearchParameters from '../../domain/SearchParameters';
import OperationStateEnum from '../../enums/OperationStateEnum';
//
const UIKEY = 'long-running-task-item-table';
const manager = new LongRunningTaskItemManager();

/**
 * Queue(list) of processed items
 *
 * @author Marek Klement
 * @author Radek Tomiška
 */
export default class LongRunningTaskItems extends Advanced.AbstractTableContent {

  constructor(props, context) {
    super(props, context);
    //
    this.state = {
      filterOpened: false
    };
  }

  getManager() {
    return manager;
  }

  getNavigationKey() {
    return 'long-running-task-items';
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

  render() {
    const { entityId } = this.props.params;
    const { filterOpened } = this.state;
    const forceSearchParameters = new SearchParameters().setFilter('longRunningTaskId', entityId);
    //
    return (
      <Basic.Panel className="no-border last">
        <Advanced.Table
          ref="table"
          uiKey={ UIKEY }
          manager={ manager }
          forceSearchParameters={ forceSearchParameters }
          rowClass={({rowIndex, data}) => { return Utils.Ui.getRowClass(data[rowIndex]); }}
          filter={
            <Advanced.Filter onSubmit={this.useFilter.bind(this)}>
              <Basic.AbstractForm ref="filterForm">
                <Basic.Row className="last">
                  <Basic.Col lg={ 4 }>
                    <Advanced.Filter.EnumSelectBox
                      ref="operationState"
                      placeholder={this.i18n('entity.LongRunningTaskItem.result.state')}
                      enum={OperationStateEnum}/>
                  </Basic.Col>
                  <Basic.Col lg={ 4 }>
                    <Advanced.Filter.TextField
                      ref="referencedEntityId"
                      placeholder={this.i18n('entity.LongRunningTaskItem.referencedEntityId.placeholder')}/>
                  </Basic.Col>
                  <Basic.Col lg={ 4 } className="text-right">
                    <Advanced.Filter.FilterButtons cancelFilter={this.cancelFilter.bind(this)}/>
                  </Basic.Col>
                </Basic.Row>
              </Basic.AbstractForm>
            </Advanced.Filter>
          }
          filterOpened={filterOpened}
          _searchParameters={ this.getSearchParameters() }>
          <Advanced.Column
            property="operationResult.state"
            header={this.i18n('entity.LongRunningTaskItem.result.state')}
            width={75}
            sort
            face="enum"
            enumClass={OperationStateEnum}
          />
          <Advanced.Column
            property="referencedEntityId"
            header={this.i18n('entity.LongRunningTaskItem.referencedEntityId')}
            cell={
              ({ rowIndex, data, property }) => {
                return (
                  <Advanced.EntityInfo
                    entityType={ Utils.Ui.getSimpleJavaType(data[rowIndex].referencedDtoType) }
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
            header={this.i18n('entity.LongRunningTaskItem.referencedDtoType')}
            width={75}
            sort
            face="text"
          />
          <Advanced.Column
            property="created"
            header={this.i18n('entity.created')}
            width={75}
            sort
            face="datetime"
          />
        </Advanced.Table>
      </Basic.Panel>
    );
  }
}

function select(state) {
  return {
    _searchParameters: Utils.Ui.getSearchParameters(state, UIKEY)
  };
}

export default connect(select)(LongRunningTaskItems);
