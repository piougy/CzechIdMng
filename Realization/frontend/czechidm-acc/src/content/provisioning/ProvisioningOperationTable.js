import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
import _ from 'lodash';
import classnames from 'classnames';
//
import { Basic, Advanced, Enums, Utils } from 'czechidm-core';
import SystemEntityTypeEnum from '../../domain/SystemEntityTypeEnum';
import ProvisioningOperationTypeEnum from '../../domain/ProvisioningOperationTypeEnum';
import { SystemManager } from '../../redux';

const systemManager = new SystemManager();

export class ProvisioningOperationTable extends Advanced.AbstractTableContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      filterOpened: this.props.filterOpened,
    };
  }

  getContentKey() {
    return 'acc:content.provisioningOperations';
  }

  clearSelectedRows() {
    this.refs.table.getWrappedInstance().clearSelectedRows();
  }

  reload() {
    this.refs.table.getWrappedInstance().reload();
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
    const { uiKey, manager, showRowSelection, actions, showDetail, forceSearchParameters, columns } = this.props;
    const { filterOpened } = this.state;
    //
    return (
      <Advanced.Table
        ref="table"
        uiKey={uiKey}
        manager={manager}
        showRowSelection={showRowSelection}
        actions={actions}
        forceSearchParameters={forceSearchParameters}
        filterOpened={filterOpened}
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

              <Basic.Row className={ classnames({ last: !_.includes(columns, 'entityIdentifier') })}>
                <div className="col-lg-4">
                  <Advanced.Filter.EnumSelectBox
                    ref="resultState"
                    placeholder={this.i18n('acc:entity.ProvisioningOperation.resultState')}
                    enum={Enums.OperationStateEnum}/>
                </div>
                <div className="col-lg-4">
                  <Advanced.Filter.EnumSelectBox
                    ref="operationType"
                    placeholder={this.i18n('acc:entity.ProvisioningOperation.operationType')}
                    enum={ProvisioningOperationTypeEnum}/>
                </div>
                <div className="col-lg-4">
                  {
                    !_.includes(columns, 'system')
                    ||
                    <Advanced.Filter.SelectBox
                      ref="systemId"
                      placeholder={this.i18n('acc:entity.System._type')}
                      multiSelect={false}
                      manager={systemManager}/>
                  }
                </div>
              </Basic.Row>

              <Basic.Row className="last" rendered={ _.includes(columns, 'entityIdentifier') }>
                <div className="col-lg-4">
                  <Advanced.Filter.EnumSelectBox
                    ref="entityType"
                    placeholder={this.i18n('acc:entity.SystemEntity.entityType')}
                    enum={SystemEntityTypeEnum}/>
                </div>
                <div className="col-lg-4">
                  <Advanced.Filter.TextField
                    ref="entityIdentifier"
                    placeholder={this.i18n('acc:entity.ProvisioningOperation.entityIdentifier')}/>
                </div>
                <div className="col-lg-4">
                  <Advanced.Filter.TextField
                    ref="systemEntityUid"
                    placeholder={this.i18n('acc:entity.SystemEntity.uid')}/>
                </div>
              </Basic.Row>
            </Basic.AbstractForm>
          </Advanced.Filter>
        }
        _searchParameters={ this.getSearchParameters() }>
        {
          !showDetail
          ||
          <Advanced.Column
            property=""
            header=""
            className="detail-button"
            cell={
              ({ rowIndex, data }) => {
                return (
                  <Advanced.DetailButton
                    title={this.i18n('button.detail')}
                    onClick={() => showDetail(data[rowIndex])}/>
                );
              }
            }/>
        }
        <Advanced.Column
          property="resultState"
          width={75}
          header={this.i18n('acc:entity.ProvisioningOperation.resultState')}
          face="text"
          cell={
            ({ rowIndex, data }) => {
              const entity = data[rowIndex];
              const content = (<Basic.EnumValue value={entity.resultState} enum={Enums.OperationStateEnum}/>);
              if (!entity.result || !entity.result.code) {
                return content;
              }
              return (
                <Basic.Tooltip placement="bottom" value={`${this.i18n('detail.resultCode')}: ${entity.result.code}`}>
                  { <span>{content}</span> }
                </Basic.Tooltip>
              );
            }
          }
          rendered={_.includes(columns, 'resultState')}/>
        <Advanced.Column property="created" width="125px" header={this.i18n('entity.created')} sort face="datetime" rendered={_.includes(columns, 'created')}/>
        <Advanced.Column
          property="operationType"
          width={75}
          header={this.i18n('acc:entity.ProvisioningOperation.operationType')}
          sort
          face="enum"
          enumClass={ProvisioningOperationTypeEnum}
          rendered={_.includes(columns, 'operationType')}/>
        <Advanced.Column
          property="entityType"
          width={75}
          header={this.i18n('acc:entity.SystemEntity.entityType')}
          sort
          sortProperty="systemEntity.entityType"
          face="enum"
          enumClass={SystemEntityTypeEnum}
          rendered={_.includes(columns, 'entityType')} />
        <Advanced.Column
          property="entityIdentifier"
          header={this.i18n('acc:entity.ProvisioningOperation.entity')}
          face="text"
          cell={
            /* eslint-disable react/no-multi-comp */
            ({ rowIndex, data }) => {
              const entity = data[rowIndex];
              return (
                <Advanced.EntityInfo entityType={entity.entityType} entityIdentifier={entity.entityIdentifier} face="popover"/>
              );
            }
          }
          rendered={_.includes(columns, 'entityIdentifier')}/>
        <Advanced.ColumnLink
          to="/system/:_target/detail"
          target="system.id"
          access={{ 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['SYSTEM_READ']}}
          property="system.name"
          header={this.i18n('acc:entity.System.name')}
          rendered={_.includes(columns, 'system')} />
        <Advanced.Column
          property="systemEntityUid"
          header={this.i18n('acc:entity.SystemEntity.uid')}
          sort
          face="text"
          rendered={_.includes(columns, 'systemEntityUid')}/>
      </Advanced.Table>
    );
  }
}

ProvisioningOperationTable.propTypes = {
  uiKey: PropTypes.string.isRequired,
  manager: PropTypes.object.isRequired,
  /**
   * Enable row selection - checkbox in first cell
   */
  showRowSelection: PropTypes.oneOfType([PropTypes.bool, PropTypes.func]),
  /**
   * Bulk actions e.g. { value: 'activate', niceLabel: this.i18n('content.identities.action.activate.action'), action: this.onActivate.bind(this) }
   */
  actions: PropTypes.arrayOf(PropTypes.object),
  /**
   * Detail  callback
   */
  showDetail: PropTypes.func,
  /**
   * "Hard filters"
   */
  forceSearchParameters: PropTypes.object,
  /**
   * Rendered columns
   */
  columns: PropTypes.arrayOf(PropTypes.string),
};
ProvisioningOperationTable.defaultProps = {
  showRowSelection: false,
  actions: [],
  forceSearchParameters: null,
  columns: ['resultState', 'created', 'operationType', 'entityType', 'entityIdentifier', 'system', 'systemEntityUid']
};

function select(state, component) {
  return {
    _searchParameters: Utils.Ui.getSearchParameters(state, component.uiKey)
  };
}

export default connect(select, null, null, { withRef: true })(ProvisioningOperationTable);
