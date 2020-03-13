import React from 'react';
import { connect } from 'react-redux';
import moment from 'moment';
//
import { Basic, Advanced, Utils, Managers, Services, Domain } from 'czechidm-core';
import { ExportImportManager, ImportLogManager } from '../../redux';
import ExportImportTypeEnum from '../../enums/ExportImportTypeEnum';
import ConceptRoleRequestOperationEnum from '../../enums/ConceptRoleRequestOperationEnum';
import OperationStateEnum from '../../enums/OperationStateEnum';

const manager = new ExportImportManager();
const longRunningTaskManager = new Managers.LongRunningTaskManager();
const importLogManager = new ImportLogManager();

/**
* Detail of export-imports
*
* @author Vít Švanda
*
*/
export class ExportImportDetail extends Advanced.AbstractTableContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      ...this.state,
      report: null,
      activeKey: 1,
      longRunningTask: null
    };
  }

  /**
   * "Shorcut" for localization
   */
  getContentKey() {
    return 'content.export-imports';
  }

  /**
   * Base manager for this agenda (used in `AbstractTableContent`)
   */
  getManager() {
    return manager;
  }

  componentDidMount() {
    super.componentDidMount();
  }

  /**
   * Submit filter action
   */
  useLogFilter(event) {
    if (event) {
      event.preventDefault();
    }
    this.refs.table_import_log.useFilterForm(this.refs.logFilterForm);
  }

  /**
   * Cancel filter action
   */
  cancelLogFilter(event) {
    if (event) {
      event.preventDefault();
    }
    this.refs.table_import_log.cancelFilter(this.refs.logFilterForm);
  }

  _onChangeSelectTabs(activeKey) {
    this.setState({activeKey});
  }

  /**
  * Method get last string of split string by dot.
  * Used for get niceLabel for type entity.
  */
  _getType(name) {
    return Utils.Ui.getSimpleJavaType(name);
  }

  renderButtons(entity, className = '') {
    if (!entity.data) {
      return null;
    }
    return (
      <div>
        <a
          key="export-download-zip"
          href={ this.getManager().getService().getDownloadUrl(entity.id) }
          title={ this.i18n('action.download.title')}
          className={ `btn btn-primary ${className}` }
          style={{ color: 'white', marginLeft: 3 }}>
          <Basic.Icon value="fa:download" />
          {' '}
        </a>
        <Basic.Button
          ref="import-dry-run"
          type="button"
          level="info"
          rendered={ Managers.SecurityManager.hasAnyAuthority(['EXPORTIMPORT_UPDATE']) }
          style={{ marginLeft: 3 }}
          title={ this.i18n('action.dry-run.title') }
          titlePlacement="bottom"
          onClick={ this._execute.bind(this, entity, true) }
          disabled={ entity.type !== 'IMPORT' }
          className="btn-xs"
          icon="fa:play"/>
        <Basic.Button
          ref="import-execute"
          type="button"
          level="success"
          rendered={ Managers.SecurityManager.hasAnyAuthority(['EXPORTIMPORT_UPDATE']) }
          style={{ marginLeft: 3 }}
          title={ this.i18n('action.execute.title') }
          titlePlacement="bottom"
          onClick={ this._execute.bind(this, entity, false) }
          disabled={ entity.type !== 'IMPORT' }
          className="btn-xs"
          icon="fa:play"/>
      </div>
    );
  }

  render() {
    const {
      showLoading,
      detail,
      longRunningTask,
    } = this.props;

    let isImport = false;
    if (detail.entity && detail.entity.type) {
      isImport = detail.entity.type === 'IMPORT';
    }

    const tableImportLogforceSearchParameters = new Domain.SearchParameters()
      .setFilter('batchId', detail.entity ? detail.entity.id : Domain.SearchParameters.BLANK_UUID);

    return (
      <Basic.Div showLoading={showLoading}>
        <Basic.Tabs onSelect={this._onChangeSelectTabs.bind(this)}>
          <Basic.Tab
            eventKey={1}
            title={this.i18n('content.export-imports.detail.tab')}
            className="bordered">
            <Basic.Panel className="no-border">
              <Basic.AbstractForm
                ref="form"
                readOnly={ !Utils.Entity.isNew(detail.entity) }
                className="panel-body">
                <Basic.EnumLabel
                  ref="type"
                  enum={ ExportImportTypeEnum }
                  style={{marginRight: '5px'}}/>
                <Basic.TextField
                  ref="name"
                  readOnly
                  label={ this.i18n('entity.ExportImport.name') }/>
                <Basic.Div rendered={longRunningTask}>
                  <Basic.Row>
                    <Basic.Col lg={ 6 }>
                      <Basic.LabelWrapper label={this.i18n('entity.LongRunningTask.counter')}>
                        { longRunningTaskManager.getProcessedCount(longRunningTask) }
                      </Basic.LabelWrapper>
                    </Basic.Col>
                    <Basic.Col lg={ 6 }>
                      <Basic.Div rendered={longRunningTask && longRunningTask.taskStarted}>
                        <Basic.LabelWrapper label={this.i18n('entity.LongRunningTask.duration')}>
                          <Basic.Tooltip
                            ref="popover"
                            placement="bottom"
                            value={ longRunningTask ? moment.utc(moment.duration(moment(longRunningTask.modified)
                              .diff(moment(longRunningTask.taskStarted)))
                              .asMilliseconds())
                              .format(this.i18n('format.times')) : null}>
                            <span>
                              {longRunningTask ? moment.duration(moment(longRunningTask.taskStarted)
                                .diff(moment(longRunningTask.modified)))
                                .locale(Services.LocalizationService.getCurrentLanguage())
                                .humanize() : null}
                            </span>
                          </Basic.Tooltip>
                        </Basic.LabelWrapper>
                      </Basic.Div>
                    </Basic.Col>
                  </Basic.Row>
                  <Advanced.OperationResult value={ longRunningTask ? longRunningTask.result : null} face="full"/>
                </Basic.Div>
              </Basic.AbstractForm>
            </Basic.Panel>
          </Basic.Tab>
          <Basic.Tab
            eventKey={2}
            rendered={isImport}
            title={this.i18n('content.export-imports.treeLogs.name')}
            className="bordered">
            <Advanced.Tree
              manager={ importLogManager }
              forceSearchParameters={ tableImportLogforceSearchParameters }
              className="panel-body"
              header={ null }
              onChange={ () => false }
              nodeStyle={{ paddingLeft: 0 }}
              nodeIconClassName={ null }
              nodeContent={ ({ node }) => {
                const operation = node.operation;
                return (
                  <span>
                    <Basic.EnumValue
                      value={operation}
                      enum={ ConceptRoleRequestOperationEnum }
                      style={{marginRight: '5px'}}/>
                    <Advanced.EntityInfo
                      entityType={ this._getType(node.type) }
                      entityIdentifier={ node.dto ? node.dto.id : null}
                      showTree={false}
                      showLink={operation === 'UPDATE'}
                      face="popover"
                      entity={ node.dto }
                      showEntityType
                      showIcon/>
                  </span>
                );
              }}
            />
          </Basic.Tab>
          <Basic.Tab
            eventKey={3}
            rendered={isImport}
            title={this.i18n('content.export-imports.tableLogs.name')}
            className="bordered">
            <Advanced.Table
              ref="table_import_log"
              uiKey="table-import-log"
              filterOpened
              manager={ importLogManager }
              rowClass={ ({rowIndex, data}) => { return Utils.Ui.getRowClass(data[rowIndex]); } }
              forceSearchParameters={ tableImportLogforceSearchParameters }
              filter={
                <Advanced.Filter onSubmit={this.useLogFilter.bind(this)}>
                  <Basic.AbstractForm ref="logFilterForm">
                    <Basic.Row>
                      <Basic.Col lg={ 3 }>
                        <Advanced.Filter.EnumSelectBox
                          ref="operation"
                          enum={ ConceptRoleRequestOperationEnum }/>
                      </Basic.Col>
                      <Basic.Col lg={ 3 }>
                        <Advanced.Filter.EnumSelectBox
                          ref="operationState"
                          placeholder={this.i18n('filter.operationState.placeholder')}
                          enum={OperationStateEnum}/>
                      </Basic.Col>
                      <Basic.Col lg={ 3 }>
                        <Advanced.Filter.TextField
                          ref="text"
                          placeholder={this.i18n('filter.textLog.placeholder')}/>
                      </Basic.Col>
                      <Basic.Col lg={ 3 } className="text-right">
                        <Advanced.Filter.FilterButtons cancelFilter={this.cancelLogFilter.bind(this)}/>
                      </Basic.Col>
                    </Basic.Row>
                  </Basic.AbstractForm>
                </Advanced.Filter>
              }
            >
              <Advanced.Column
                property="id"
                header={ this.i18n('content.export-imports.ImportDescriptor.dto') }
                cell={
                  /* eslint-disable react/no-multi-comp */
                  ({ rowIndex, data }) => {
                    const value = data[rowIndex];
                    if (!value) {
                      return null;
                    }
                    const operation = value.operation;
                    return (
                      <Advanced.EntityInfo
                        entityType={ this._getType(value.type) }
                        entityIdentifier={ value.dto ? value.dto.id : null}
                        face="popover"
                        entity={ value.dto }
                        showEntityType
                        showLink={operation === 'UPDATE'}
                        showIcon/>
                    );
                  }
                }/>
              <Advanced.Column
                property="operation"
                face="enum"
                enumClass={ConceptRoleRequestOperationEnum}
                sort
                header={ this.i18n('content.export-imports.ImportDescriptor.operation') }
                width={ 100 }/>
              <Advanced.Column
                property="type"
                header={ this.i18n('content.export-imports.ImportDescriptor.type') }
                sort
                cell={
                  ({ data, rowIndex }) => {
                    return this._getType(data[rowIndex].type);
                  }
                }/>
              <Advanced.Column
                property="result"
                header={ this.i18n('content.export-imports.ImportDescriptor.result') }
                sort
                sortProperty="result.state"
                cell={
                  ({ data, rowIndex }) => {
                    return <Advanced.OperationResult value={ data[rowIndex].result }/>;
                  }
                }/>
            </Advanced.Table>
          </Basic.Tab>
        </Basic.Tabs>
      </Basic.Div>
    );
  }
}

function select(state, component) {
  return {
    _searchParameters: Utils.Ui.getSearchParameters(state, component.uiKey), // persisted filter state in redux,
    showLoading: Utils.Ui.isShowLoading(state, `${component.uiKey}-detail`)
  };
}

export default connect(select, null, null, { forwardRef: true })(ExportImportDetail);
