import React, { PropTypes } from 'react';
import moment from 'moment';
//
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import OperationStateEnum from '../../enums/OperationStateEnum';
import { LocalizationService } from '../../services';

/**
 * Table with long running tasks
 *
 * @author Radek Tomiška
 */
export default class LongRunningTaskTable extends Basic.AbstractContent {

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
              <Basic.AbstractForm ref="filterForm" className="form-horizontal">
                <Basic.Row>
                  <div className="col-lg-4">
                    <Advanced.Filter.DateTimePicker
                      mode="date"
                      ref="from"
                      placeholder={this.i18n('filter.dateFrom.placeholder')}
                      label={this.i18n('filter.dateFrom.label')}/>
                  </div>
                  <div className="col-lg-4">
                    <Advanced.Filter.DateTimePicker
                      mode="date"
                      ref="till"
                      placeholder={this.i18n('filter.dateTill.placeholder')}
                      label={this.i18n('filter.dateTill.label')}/>
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
                      label={this.i18n('filter.operationState.label')}
                      enum={OperationStateEnum}/>
                  </div>
                  <div className="col-lg-4">
                    <Advanced.Filter.TextField
                      ref="text"
                      placeholder={this.i18n('filter.text.placeholder')}
                      label={this.i18n('filter.text.label')}/>
                  </div>
                </Basic.Row>
              </Basic.AbstractForm>
            </Advanced.Filter>
          }
          filterOpened={!filterOpened}>

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
          <Advanced.Column property="instanceId" width={150} header={this.i18n('entity.ScheduleTask.instanceId.label')} sort face="text"/>
          <Advanced.Column property="taskId" sort rendered={false}/>
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
                <Basic.AbstractForm data={detail.entity} className="form-horizontal" readOnly>
                  <Basic.LabelWrapper label={this.i18n('entity.created')}>
                    <div style={{ margin: '7px 0' }}>
                      <Advanced.DateValue value={detail.entity.created} showTime/>
                    </div>
                  </Basic.LabelWrapper>
                  <Basic.LabelWrapper label={this.i18n('entity.LongRunningTask.instanceId.label')}>
                    <div style={{ margin: '7px 0' }}>
                      {detail.entity.instanceId}
                      <span className="help-block">{this.i18n('entity.LongRunningTask.instanceId.help')}</span>
                    </div>
                  </Basic.LabelWrapper>
                  <Basic.LabelWrapper label={this.i18n('entity.LongRunningTask.taskType')}>
                    <div style={{ margin: '7px 0' }}>
                      {detail.entity.taskType}
                    </div>
                  </Basic.LabelWrapper>
                  <Basic.TextArea
                    label={this.i18n('entity.LongRunningTask.taskDescription')}
                    disabled
                    value={detail.entity.taskDescription}/>
                  <Basic.LabelWrapper label={this.i18n('entity.LongRunningTask.counter')}>
                    <div style={{ margin: '7px 0' }}>
                      { manager.getProcessedCount(detail.entity) }
                    </div>
                  </Basic.LabelWrapper>
                  {
                    !detail.entity.modified
                    ||
                    <Basic.LabelWrapper label={this.i18n('entity.LongRunningTask.duration')}>
                      <div style={{ margin: '7px 0' }}>
                        { moment.duration(moment(detail.entity.created).diff(moment(detail.entity.modified))).locale(LocalizationService.getCurrentLanguage()).humanize() }
                      </div>
                    </Basic.LabelWrapper>
                  }
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
