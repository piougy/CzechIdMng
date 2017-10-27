import React, { PropTypes } from 'react';
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import * as Utils from '../../utils';
import _ from 'lodash';
import moment from 'moment';
import { LocalizationService } from '../../services';
import { LongRunningTaskItemManager } from '../../redux';
import SearchParameters from '../../domain/SearchParameters';
import OperationStateEnum from '../../enums/OperationStateEnum';
import uuid from 'uuid';
import { SecurityManager } from '../../redux';

const manager = new LongRunningTaskItemManager();

/**
 * Table with long running task items and detail of LRT
 *
 * @author Marek Klement
 */
export default class LongRunningTaskItemTable extends Advanced.AbstractTableContent {

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

  showListOfProcessedItems(entity) {
    if (entity.id === undefined) {
      const uuidId = uuid.v1();
      this.context.router.push(`long-running-task/queue/new?id=${uuidId}`);
    } else {
      this.context.router.push(`long-running-task/queue/${encodeURIComponent(entity.id)}`);
    }
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

  getLongRunningTaskInfo(entity, longRunningTaskManager) {
    return (
      <div>
      <Basic.PageHeader>
        {this.i18n('entity.LongRunningTaskItem.header')} <small> {Utils.Ui.getSimpleJavaType(entity.taskType)}</small>
      </Basic.PageHeader>
        <Basic.AbstractForm data={entity} readOnly>
          <Basic.Row>
            <Basic.Col lg={ 6 }>
              <Basic.LabelWrapper label={this.i18n('entity.created')}>
                <div style={{ margin: '7px 0' }}>
                  <Advanced.DateValue value={entity.created} showTime/>
                </div>
              </Basic.LabelWrapper>
            </Basic.Col>
            <Basic.Col lg={ 6 }>
              <Basic.LabelWrapper label={this.i18n('entity.LongRunningTask.instanceId.label')}>
                <div style={{ margin: '7px 0' }}>
                  {entity.instanceId}
                  <span className="help-block">{this.i18n('entity.LongRunningTask.instanceId.help')}</span>
                </div>
              </Basic.LabelWrapper>
            </Basic.Col>
          </Basic.Row>

          <Basic.Row>
            <Basic.Col lg={ 6 }>
              <Basic.LabelWrapper label={this.i18n('entity.LongRunningTask.taskType')}>
                <div style={{ margin: '7px 0' }}>
                  { Utils.Ui.getSimpleJavaType(entity.taskType) }
                </div>
              </Basic.LabelWrapper>
            </Basic.Col>
            <Basic.Col lg={ 6 }>
              <Basic.LabelWrapper label={this.i18n('entity.LongRunningTask.taskProperties.label')}>
                <div style={{ margin: '7px 0' }}>
                  {
                    _.keys(entity.taskProperties).map(propertyName => {
                      return (
                        <div>{ propertyName }: { '' + entity.taskProperties[propertyName] }</div>
                      );
                    })
                  }
                </div>
              </Basic.LabelWrapper>
            </Basic.Col>
          </Basic.Row>

          <Basic.TextArea
            label={this.i18n('entity.LongRunningTask.taskDescription')}
            disabled
            value={entity.taskDescription}/>

          <Basic.Row>
            <Basic.Col lg={ 6 }>
              {
                !entity.taskStarted
                &&
                <Basic.LabelWrapper label={this.i18n('entity.LongRunningTask.notstarted')}/>
                ||
                <Basic.LabelWrapper label={this.i18n('entity.LongRunningTask.counter')}>
                  <div style={{ margin: '7px 0' }}>
                    { longRunningTaskManager.getProcessedCount(entity) }
                  </div>
                </Basic.LabelWrapper>
              }
            </Basic.Col>
            <Basic.Col lg={ 6 }>
              {
                !entity.modified
                ||
                <Basic.LabelWrapper label={this.i18n('entity.LongRunningTask.duration')}>
                    <div style={{ margin: '7px 0' }}>
                      <Basic.Tooltip ref="popover" placement="bottom" value={ moment.utc(moment.duration(moment(entity.modified).diff(moment(entity.taskStarted))).asMilliseconds()).format('HH:mm:ss,SSS')}>
                        <span>
                          { moment.duration(moment(entity.taskStarted).diff(moment(entity.modified))).locale(LocalizationService.getCurrentLanguage()).humanize() }
                        </span>
                      </Basic.Tooltip>
                    </div>
                </Basic.LabelWrapper>
              }
            </Basic.Col>
          </Basic.Row>

          <Basic.Row>
            <Basic.Col lg={ 6 }>
              {
                !entity.taskStarted
                ||
                <Basic.LabelWrapper label={this.i18n('entity.LongRunningTask.started')}>
                  <div style={{ margin: '7px 0' }}>
                    <Advanced.DateValue value={entity.taskStarted} showTime/>
                  </div>
                </Basic.LabelWrapper>
              }
            </Basic.Col>
          </Basic.Row>

        </Basic.AbstractForm>
        </div>
    );
  }

  render() {
    const { uiKey, longRunningTaskManager, entity } = this.props;
    const { filterOpened } = this.state;
    const forceSearchParameters = new SearchParameters().setFilter('longRunningTaskId', entity.id);
    return (
      <div>
      <Basic.Confirm ref="confirm-delete" level="danger"/>
      {this.getLongRunningTaskInfo(entity, longRunningTaskManager)}
      <Advanced.Table
        ref="table"
        uiKey={uiKey}
        manager={manager}
        forceSearchParameters={forceSearchParameters}
        rowClass={({rowIndex, data}) => { return Utils.Ui.getRowClass(data[rowIndex]); }}
        filter={this.getAdvancedFilter()}
        filterOpened={filterOpened}
        _searchParameters={null}
        buttons={
          [
            <Basic.Button
              level="info"
              key="history-button"
              type="submit"
              className="btn-xs"
              rendered={SecurityManager.hasAuthority('SCHEDULER_EXECUTE')}
              onClick={ this.showListOfProcessedItems.bind(this, entity) }>
              {this.i18n('entity.LongRunningTaskItem.history')}
            </Basic.Button>
          ]
        }>
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

LongRunningTaskItemTable.propTypes = {
  uiKey: PropTypes.string.isRequired,
  manager: PropTypes.object.isRequired
};
