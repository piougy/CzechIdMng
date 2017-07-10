import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
import moment from 'moment';
import _ from 'lodash';
//
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import * as Utils from '../../utils';
import OperationStateEnum from '../../enums/OperationStateEnum';
import { LocalizationService } from '../../services';
import { SecurityManager } from '../../redux';

/**
 * Table with long running tasks
 *
 * @author Radek Tomiška
 */
class LongRunningTaskTable extends Advanced.AbstractTableContent {

  constructor(props, context) {
    super(props, context);
    // default filter status
    // true - open
    // false - close
    this.state = {
      detail: {
        show: false,
        entity: null
      },
      filterOpened: false
    };
  }

  getContentKey() {
    return 'content.scheduler.all-tasks';
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

  /**
   * Shows modal detail with given entity
   */
  showDetail(entity) {
    this.setState({
      detail: {
        show: true,
        entity
      }
    });
  }

  /**
   * Close modal detail
   */
  closeDetail() {
    this.setState({
      detail: {
        show: false,
        entity: null
      }
    });
  }

  processCreated() {
    const { manager } = this.props;
    //
    this.context.store.dispatch(manager.processCreated('task-queue-process-created', () => {
      this.addMessage({ level: 'success', message: this.i18n('action.processCreated.success') });
    }));
  }

  render() {
    const { uiKey, manager } = this.props;
    const { filterOpened, detail } = this.state;

    return (
      <div>
        <Advanced.Table
          ref="table"
          uiKey={uiKey}
          manager={manager}
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
                <Basic.Row className="last">
                  <div className="col-lg-4">
                    <Advanced.Filter.EnumSelectBox
                      ref="operationState"
                      placeholder={this.i18n('filter.operationState.placeholder')}
                      enum={OperationStateEnum}/>
                  </div>
                  <div className="col-lg-4">
                    <Advanced.Filter.TextField
                      ref="text"
                      placeholder={this.i18n('filter.text.placeholder')}/>
                  </div>
                </Basic.Row>
              </Basic.AbstractForm>
            </Advanced.Filter>
          }
          filterOpened={filterOpened}
          buttons={
            [
              <Basic.Button
                level="success"
                key="start-button"
                type="submit"
                className="btn-xs"
                rendered={SecurityManager.hasAuthority('SCHEDULER_EXECUTE')}
                onClick={ this.processCreated.bind(this) }>
                <Basic.Icon icon="play"/>
                {' '}
                { this.i18n('action.processCreated.button') }
              </Basic.Button>
            ]
          }
          _searchParameters={ this.getSearchParameters() }>

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
          <Advanced.Column property="result.state" width={75} header={this.i18n('entity.LongRunningTask.result.state')} sort face="enum" enumClass={OperationStateEnum}/>
          <Advanced.Column property="created" width={150} header={this.i18n('entity.created')} sort face="datetime"/>
          <Advanced.Column
            property="taskType"
            width={150}
            sort
            cell={
              /* eslint-disable react/no-multi-comp */
              ({ rowIndex, data, property }) => {
                const entity = data[rowIndex];
                const propertyValue = entity[property];
                return (
                  <span title={propertyValue}>{ propertyValue.split('.').pop(-1) }</span>
                );
              }
            }/>
          <Advanced.Column
            property="taskProperties"
            header={ this.i18n('entity.LongRunningTask.taskProperties.label') }
            cell={
              /* eslint-disable react/no-multi-comp */
              ({ rowIndex, data, property }) => {
                const entity = data[rowIndex];
                const propertyValue = entity[property];

                return _.keys(propertyValue).map(propertyName => {
                  return (
                    <div>{ propertyName }: { propertyValue[propertyName] }</div>
                  );
                });
              }
            }/>
          <Advanced.Column property="taskDescription" sort />
          <Advanced.Column
            property="counter"
            width={75}
            sort
            cell={
              /* eslint-disable react/no-multi-comp */
              ({ rowIndex, data }) => {
                const entity = data[rowIndex];
                return (
                  <span>{manager.getProcessedCount(entity)}</span>
                );
              }
            }/>
        </Advanced.Table>

        <Basic.Modal
          bsSize="large"
          show={detail.show}
          onHide={this.closeDetail.bind(this)}
          backdrop="static">
          <Basic.Modal.Header closeButton text={this.i18n('detail.header')}/>
          <Basic.Modal.Body>
            {
              !detail.entity
              ||
              <div>
                <Basic.AbstractForm data={detail.entity} readOnly>
                  <Basic.Row>
                    <Basic.Col lg={ 6 }>
                      <Basic.LabelWrapper label={this.i18n('entity.created')}>
                        <div style={{ margin: '7px 0' }}>
                          <Advanced.DateValue value={detail.entity.created} showTime/>
                        </div>
                      </Basic.LabelWrapper>
                    </Basic.Col>
                    <Basic.Col lg={ 6 }>
                      <Basic.LabelWrapper label={this.i18n('entity.LongRunningTask.instanceId.label')}>
                        <div style={{ margin: '7px 0' }}>
                          {detail.entity.instanceId}
                          <span className="help-block">{this.i18n('entity.LongRunningTask.instanceId.help')}</span>
                        </div>
                      </Basic.LabelWrapper>
                    </Basic.Col>
                  </Basic.Row>

                  <Basic.Row>
                    <Basic.Col lg={ 6 }>
                      <Basic.LabelWrapper label={this.i18n('entity.LongRunningTask.taskType')}>
                        <div style={{ margin: '7px 0' }}>
                          { Utils.Ui.getSimpleJavaType(detail.entity.taskType) }
                        </div>
                      </Basic.LabelWrapper>
                    </Basic.Col>
                    <Basic.Col lg={ 6 }>
                      <Basic.LabelWrapper label={this.i18n('entity.LongRunningTask.taskProperties.label')}>
                        <div style={{ margin: '7px 0' }}>
                          {
                            _.keys(detail.entity.taskProperties).map(propertyName => {
                              return (
                                <div>{ propertyName }: { detail.entity.taskProperties[propertyName] }</div>
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
                    value={detail.entity.taskDescription}/>

                  <Basic.Row>
                    <Basic.Col lg={ 6 }>
                      <Basic.LabelWrapper label={this.i18n('entity.LongRunningTask.counter')}>
                        <div style={{ margin: '7px 0' }}>
                          { manager.getProcessedCount(detail.entity) }
                        </div>
                      </Basic.LabelWrapper>
                    </Basic.Col>
                    <Basic.Col lg={ 6 }>
                      {
                        !detail.entity.modified
                        ||
                        <Basic.LabelWrapper label={this.i18n('entity.LongRunningTask.duration')}>
                          <div style={{ margin: '7px 0' }}>
                            { moment.duration(moment(detail.entity.created).diff(moment(detail.entity.modified))).locale(LocalizationService.getCurrentLanguage()).humanize() }
                          </div>
                        </Basic.LabelWrapper>
                      }
                    </Basic.Col>
                  </Basic.Row>

                </Basic.AbstractForm>
                <br />

                <h3 style={{ margin: '0 0 10px 0', padding: 0, borderBottom: '1px solid #ddd' }}>{ this.i18n('detail.result') }</h3>
                <div style={{ marginBottom: 15 }}>
                  <Basic.EnumValue value={detail.entity.resultState} enum={OperationStateEnum}/>
                  {
                    (!detail.entity.result || !detail.entity.result.code)
                    ||
                    <span style={{ marginLeft: 15 }}>
                      {this.i18n('detail.resultCode')}: {detail.entity.result.code}
                    </span>
                  }
                  <Basic.FlashMessage message={this.getFlashManager().convertFromResultModel(detail.entity.result.model)} style={{ marginTop: 15 }}/>
                </div>
                {
                  (!detail.entity.result || !detail.entity.result.stackTrace)
                  ||
                  <div>
                    <textArea
                      rows="10"
                      value={detail.entity.result.stackTrace}
                      readOnly
                      style={{ width: '100%', marginBottom: 15 }}/>
                  </div>
                }
              </div>
            }
          </Basic.Modal.Body>

          <Basic.Modal.Footer>
            <Basic.Button
              level="link"
              onClick={this.closeDetail.bind(this)}>
              {this.i18n('button.close')}
            </Basic.Button>
          </Basic.Modal.Footer>
        </Basic.Modal>
      </div>
    );
  }
}

LongRunningTaskTable.propTypes = {
  uiKey: PropTypes.string.isRequired,
  manager: PropTypes.object.isRequired
};

LongRunningTaskTable.defaultProps = {
};

function select(state, component) {
  return {
    _searchParameters: Utils.Ui.getSearchParameters(state, component.uiKey)
  };
}

export default connect(select)(LongRunningTaskTable);
