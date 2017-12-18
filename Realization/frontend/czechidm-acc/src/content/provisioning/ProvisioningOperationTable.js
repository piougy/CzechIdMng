import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
import _ from 'lodash';
import classnames from 'classnames';
//
import { Basic, Advanced, Enums, Utils, Managers } from 'czechidm-core';
import SystemEntityTypeEnum from '../../domain/SystemEntityTypeEnum';
import ProvisioningOperationTypeEnum from '../../domain/ProvisioningOperationTypeEnum';
import { SystemManager, ProvisioningOperationManager } from '../../redux';

const systemManager = new SystemManager();
const manager = new ProvisioningOperationManager();

/**
 * Provisioning operation and archive table
 *
 * @author Radk Tomiška
 */
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

  _useFilterForm(filterForm) {
    const filters = {};
    const filterValues = filterForm.getData();
    for (const property in filterValues) {
      if (!filterValues.hasOwnProperty(property)) {
        continue;
      }
      const filterComponent = filterForm.getComponent(property);
      const field = filterComponent.props.field || property;
      // if filterComponent uses multiSelect
      if (filterComponent.props.multiSelect === true) {
        // if filterEnumSelectBox uses Symbol
        if (filterComponent.props.useSymbol && filterValues[property] !== null) {
          const filledValues = [];
          filterValues.states.forEach(item => {
            filledValues.push(filterComponent.props.enum.findKeyBySymbol(item));
          } );
          filters[field] = filledValues;
        } else {
          // if filterComponent does not useSymbol
          let filledValues;
          filledValues = filterValues[property];
          filters[field] = filledValues;
        }
      } else {
        // filterComponent does not use multiSelect
        // requestData.accounts.push(resourceValue);
        let filledValue = filterValues[property];
        if (filterComponent.props.enum) { // enumeration
          filledValue = filterComponent.props.enum.findKeyBySymbol(filledValue);
        }
        filters[field] = filledValue;
      }
    }
    this._useFilterData(filters);
  }

  _useFilterData(formData) {
    let userSearchParameters = this.props._searchParameters ? this.props._searchParameters : manager.getDefaultSearchParameters();
   //
    for (const property in formData) {
      if (!formData.hasOwnProperty(property)) {
        continue;
      }
      userSearchParameters = userSearchParameters.setFilter(property, formData[property]);
    }
    manager.cleanAll(userSearchParameters)
    .then(response => {
      if (response.status === 202) {
        this.addMessage({ message: this.i18n('acc:entity.ProvisioningOperation.lrt.start')});
        return {};
      }
      return response.json();
    }).then(json => {
      if (Utils.Response.hasError(json)) {
        throw Utils.Response.getFirstError(json);
      }
    });
  }

  _cleanAll() {
    this.refs['confirm-cleanAll'].show(
      this.i18n(`action.cleanAll.message`),
      this.i18n(`action.cleanAll.header`)
    ).then(() => {
      this._useFilterForm(this.refs.filterForm);
    }, () => {
      // nothing
    });
  }

  render() {
    const { uiKey, showRowSelection, actions, showDetail, forceSearchParameters, columns, isArchive } = this.props;
    const { filterOpened } = this.state;
    //
    return (
      <div>
      <Basic.Confirm ref="confirm-cleanAll" level="danger"/>
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
        buttons={
          [
            <Basic.Button level="danger" key="clean_button" className="btn-xs"
                    onClick={this._cleanAll.bind(this)}
                    rendered={Managers.SecurityManager.hasAnyAuthority('SYSTEM_ADMIN')}>
              <Basic.Icon type="fa" icon="minus"/>
              {' '}
              {this.i18n('button.clean')}
            </Basic.Button>
          ]
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
                    onClick={() => showDetail(data[rowIndex], isArchive)}/>
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
          width={ 75 }
          header={ this.i18n('acc:entity.SystemEntity.entityType') }
          sort
          face="enum"
          enumClass={ SystemEntityTypeEnum }
          rendered={ _.includes(columns, 'entityType') } />
        <Advanced.Column
          property="entityIdentifier"
          header={ this.i18n('acc:entity.ProvisioningOperation.entity') }
          face="text"
          cell={
            /* eslint-disable react/no-multi-comp */
            ({ rowIndex, data }) => {
              const entity = data[rowIndex];
              return (
                <Advanced.EntityInfo
                  entityType={ entity.entityType }
                  entityIdentifier={ entity.entityIdentifier }
                  entity={ entity._embedded.entity }
                  face="popover"/>
              );
            }
          }
          rendered={ _.includes(columns, 'entityIdentifier') }/>
        <Advanced.Column
          property="system.name"
          header={ this.i18n('acc:entity.System.name') }
          sort
          cell={
            ({ rowIndex, data }) => {
              const entity = data[rowIndex];
              return (
                <Advanced.EntityInfo
                  entityType="system"
                  entityIdentifier={ entity.system }
                  entity={ entity._embedded.system }
                  face="popover"/>
              );
            }
          }
          rendered={_.includes(columns, 'system')} />
        <Advanced.Column
          property="systemEntityUid"
          header={this.i18n('acc:entity.SystemEntity.uid')}
          sort
          sortProperty="systemEntityUid"
          face="text"
          rendered={isArchive && _.includes(columns, 'systemEntityUid')}/>
        <Advanced.Column
          property="_embedded.systemEntity.uid"
          header={this.i18n('acc:entity.SystemEntity.uid')}
          sort
          sortProperty="systemEntity"
          face="text"
          rendered={!isArchive && _.includes(columns, 'systemEntityUid')}/>
      </Advanced.Table>
    </div>
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
