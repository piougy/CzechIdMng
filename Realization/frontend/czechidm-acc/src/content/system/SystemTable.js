import React from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import _ from 'lodash';
import uuid from 'uuid';
//
import { Advanced, Basic, Domain, Managers, Utils } from 'czechidm-core';
import { SystemManager } from '../../redux';
import ProvisioningOperationTypeEnum from '../../domain/ProvisioningOperationTypeEnum';
import SystemWizardDetail from '../wizard/SystemWizardDetail';
import RemoteServerSelect from '../../components/RemoteServerSelect/RemoteServerSelect';

const systemManager = new SystemManager();

/**
* Table of target systems.
*
* @author Radek Tomiška
*/
export class SystemTable extends Advanced.AbstractTableContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      filterOpened: this.props.filterOpened,
    };
  }

  getContentKey() {
    return 'acc:content.systems';
  }

  getManager() {
    const { manager } = this.props;
    //
    return manager;
  }

  componentDidMount() {
    super.componentDidMount();
    //
    if (this.refs.text) {
      this.refs.text.focus();
    }
  }

  useFilter(event) {
    if (event) {
      event.preventDefault();
    }
    const filterData = Domain.SearchParameters.getFilterData(this.refs.filterForm);
    //
    // resolve additional filter options
    if (this.refs.remoteServerId) {
      const remoteServerId = this.refs.remoteServerId.getValue();
      if (remoteServerId && remoteServerId.additionalOption) {
        filterData.remoteServerId = null;
        filterData.remote = 'false';
      }
    }
    //
    this.refs.table.useFilterData(filterData);
  }

  cancelFilter(event) {
    if (event) {
      event.preventDefault();
    }
    if (this.refs.remoteServerId) {
      this.refs.remoteServerId.setValue(null);
    }
    this.refs.table.cancelFilter(this.refs.filterForm);
  }

  /**
   * Loads filter from redux state or default.
   */
  loadFilter() {
    if (!this.refs.filterForm) {
      return;
    }
    //  filters from redux
    const _searchParameters = this.getSearchParameters();
    if (_searchParameters) {
      const filterData = {};
      _searchParameters.getFilters().forEach((v, k) => {
        filterData[k] = v;
      });
      // set without catalogue option
      if (filterData.remote) {
        filterData.remote = null;
        filterData.remoteServerId = this._getLocalServerOption();
      }
      //
      this.refs.filterForm.setData(filterData);
    }
  }

  showDetail(entity) {
    if (Utils.Entity.isNew(entity)) {
      const uuidId = uuid.v1();
      this.context.history.push(`/system/${uuidId}/new?new=1`);
    } else {
      this.context.history.push(`/system/${entity.id}/detail`);
    }
  }

  showInWizard(system) {
    this.setState({
      showLoading: true
    }, () => {
      this.getManager().getService().loadConnectorType({reopened: true, metadata: {system: system.id}})
        .then((connectorType) => {
          connectorType.reopened = true;
          this.setState({showLoading: false, showWizard: true, connectorType});
        })
        .catch(ex => {
          this.setState({
            showLoading: false
          });
          this.addError(ex);
        });
    });
  }

  closeWizard(finished, wizardContext) {
    this.setState({
      showWizard: false
    }, () => {
      if (finished && wizardContext && wizardContext.entity) {
        this.context.history.push(`/system/${wizardContext.entity.id}/detail`);
      }
    });
  }

  onDuplicate(bulkActionValue, selectedRows) {
    const { manager, uiKey } = this.props;
    const selectedEntities = manager.getEntitiesByIds(this.context.store.getState(), selectedRows);
    //
    this.refs['confirm-duplicate'].show(
      this.i18n(`action.${bulkActionValue}.message`, {
        count: selectedEntities.length,
        record: manager.getNiceLabel(selectedEntities[0]),
        records: manager.getNiceLabels(selectedEntities).join(', ')
      }),
      this.i18n(`action.${bulkActionValue}.header`, {
        count: selectedEntities.length,
        records: manager.getNiceLabels(selectedEntities).join(', ')
      })
    ).then(() => {
      this.context.store.dispatch(manager.duplicateEntities(selectedEntities, uiKey, (entity, error) => {
        if (entity && error) {
          this.addErrorMessage({ title: this.i18n(`action.delete.error`, { record: manager.getNiceLabel(entity) }) }, error);
        } else {
          this.refs.table.reload();
        }
      }));
    }, () => {
      // nothing
    });
  }

  onChangeRemoteServer(option) {
    if (!option) {
      const { uiKey, _searchParameters } = this.props;
      // cleanup redux state search parameters for additional options
      this.context.store.dispatch(this.getManager().setSearchParameters(_searchParameters.clearFilter('remote'), uiKey));
    }
  }

  _getLocalServerOption() {
    return {
      [Basic.SelectBox.NICE_LABEL]: this.i18n('filter.remoteServerId.option.localServer.label'),
      [Basic.SelectBox.ITEM_FULL_KEY]: this.i18n('filter.remoteServerId.option.localServer.label'),
      [Basic.SelectBox.ITEM_VALUE]: 'acc:local-server'
    };
  }

  /**
   * Return div with all labels for system with information about blocked operations.
   * There can't be used EnumLabel because every enumlable is on new line
   */
  _getBlockedOperations(system) {
    if (system && system.blockedOperation) {
      // every blocked operation has same level in this table
      const level = 'error';
      //
      const createKey = ProvisioningOperationTypeEnum.findKeyBySymbol(ProvisioningOperationTypeEnum.CREATE);
      const updateKey = ProvisioningOperationTypeEnum.findKeyBySymbol(ProvisioningOperationTypeEnum.UPDATE);
      const deleteKey = ProvisioningOperationTypeEnum.findKeyBySymbol(ProvisioningOperationTypeEnum.DELETE);
      return (
        <Basic.Div>
          {
            !system.blockedOperation.createOperation
            ||
            <span>
              {' '}
              <Basic.Label
                level={level}
                value={ProvisioningOperationTypeEnum.getNiceLabel(createKey)}/>
            </span>
          }
          {
            !system.blockedOperation.updateOperation
            ||
            <span>
              {' '}
              <Basic.Label
                level={level}
                value={ProvisioningOperationTypeEnum.getNiceLabel(updateKey)}/>
            </span>
          }
          {
            !system.blockedOperation.deleteOperation
            ||
            <span>
              {' '}
              <Basic.Label
                level={level}
                value={ProvisioningOperationTypeEnum.getNiceLabel(deleteKey)}/>
            </span>
          }
        </Basic.Div>
      );
    }
    return null;
  }

  getTableButtons(showAddButton) {
    return (
      [
        <Basic.SplitButton
          level="success"
          key="add-wizard-button"
          buttonSize="xs"
          onClick={ this.props.showWizardDetail }
          rendered={ Managers.SecurityManager.hasAuthority('SYSTEM_CREATE') && showAddButton }
          title={ this.i18n('button.add') }
          icon="fa:magic"
          pullRight>
          <Basic.MenuItem
            eventKey="1"
            onClick={ this.props.showWizardDetail }
            icon="fa:magic">
            { this.i18n('acc:wizard.addSystemViaWizard') }
          </Basic.MenuItem>
          <Basic.MenuItem
            eventKey="2"
            onClick={ this.showDetail.bind(this, { }) }
            icon="fa:plus">
            { this.i18n('button.addDefault.label') }
          </Basic.MenuItem>
        </Basic.SplitButton>
      ]
    );
  }

  render() {
    const {
      uiKey,
      manager,
      columns,
      defaultSearchParameters,
      forceSearchParameters,
      showAddButton,
      showRowSelection,
      showFilterVirtual
    } = this.props;
    const { filterOpened, showWizard, connectorType } = this.state;
    const _showFilterVirtual = showFilterVirtual && !forceSearchParameters.filters.get('virtual');
    const _showRemoteServer = !forceSearchParameters.getFilters().has('remote') && !forceSearchParameters.getFilters().has('remoteServerId');

    let lgFilterButtons = 9;
    if (_showFilterVirtual) {
      lgFilterButtons -= 3;
    }
    if (_showRemoteServer && Managers.SecurityManager.hasAuthority('REMOTESERVER_AUTOCOMPLETE')) {
      if (_showFilterVirtual) {
        lgFilterButtons -= 3;
      } else {
        lgFilterButtons -= 5;
      }
    }

    return (
      <Basic.Div>
        <Basic.Confirm ref="confirm-duplicate" level="danger"/>
        <Basic.Confirm ref="confirm-delete" level="danger"/>

        <Advanced.Table
          ref="table"
          uiKey={ uiKey }
          manager={ manager }
          filterOpened={ filterOpened }
          defaultSearchParameters={ defaultSearchParameters }
          forceSearchParameters={ forceSearchParameters }
          showRowSelection={ showRowSelection }
          filter={
            <Advanced.Filter onSubmit={ this.useFilter.bind(this) }>
              <Basic.AbstractForm ref="filterForm">
                <Basic.Row className="last">
                  <Basic.Col lg={ 3 }>
                    <Advanced.Filter.TextField
                      ref="text"
                      placeholder={ this.i18n('acc:entity.System.name') }
                      help={ Advanced.Filter.getTextHelp() }/>
                  </Basic.Col>
                  <Basic.Col lg={ 3 } rendered={ _showFilterVirtual }>
                    <Advanced.Filter.BooleanSelectBox
                      ref="virtual"
                      placeholder={ this.i18n('acc:entity.System.systemType.label') }
                      options={ [
                        { value: 'true', niceLabel: this.i18n('acc:entity.System.systemType.virtual') },
                        { value: 'false', niceLabel: this.i18n('acc:entity.System.systemType.notVirtual') }
                      ]}/>
                  </Basic.Col>
                  <Basic.Col
                    lg={ _showFilterVirtual ? 3 : 5 }
                    rendered={ _showRemoteServer && Managers.SecurityManager.hasAuthority('REMOTESERVER_AUTOCOMPLETE') }>
                    <RemoteServerSelect
                      ref="remoteServerId"
                      placeholder={ this.i18n('filter.remoteServerId.placeholder') }
                      additionalOptions={[ this._getLocalServerOption() ]}
                      onChange={ this.onChangeRemoteServer.bind(this) }/>
                  </Basic.Col>
                  <Basic.Col lg={ lgFilterButtons } className="text-right">
                    <Advanced.Filter.FilterButtons cancelFilter={ this.cancelFilter.bind(this) }/>
                  </Basic.Col>
                </Basic.Row>
              </Basic.AbstractForm>
            </Advanced.Filter>
          }
          buttons={ this.getTableButtons(showAddButton) }
          _searchParameters={ this.getSearchParameters() }>

          <Advanced.Column
            header=""
            className="detail-button"
            cell={
              ({ rowIndex, data }) => {
                return ([
                  <Advanced.DetailButton
                    title={ this.i18n('button.detail') }
                    onClick={ this.showDetail.bind(this, data[rowIndex]) }/>,
                  <Basic.Button
                    level="default"
                    key="showInWizard"
                    title={ this.i18n('acc:wizard.openSystemViaWizard') }
                    className="btn-xs"
                    style={{marginLeft: 5}}
                    onClick={ this.showInWizard.bind(this, data[rowIndex])}
                    icon="fa:magic">
                  </Basic.Button>
                ]
                );
              }
            }
            sort={false}/>
          <Advanced.ColumnLink to="/system/:id/detail" property="name" width="15%" sort face="text" rendered={ _.includes(columns, 'name') }/>
          <Advanced.Column property="description" sort face="text" rendered={ _.includes(columns, 'description') }/>
          <Advanced.Column property="queue" sort face="bool" width={ 75 } rendered={ _.includes(columns, 'queue') }/>
          <Advanced.Column
            property="state"
            header={ this.i18n('acc:entity.System.state.label')}
            face="bool"
            width={ 75 }
            rendered={ _.includes(columns, 'state') }
            cell={
              ({ rowIndex, data }) => {
                const entity = data[rowIndex];
                if (entity.disabledProvisioning) {
                  if (entity.disabled) {
                    return this.i18n('acc:entity.System.disabledProvisioning.label');
                  }
                  return this.i18n('acc:entity.System.readonlyDisabledProvisioning.label');
                }
                if (entity.disabled) {
                  return this.i18n('acc:entity.System.disabled.label');
                }
                if (entity.readonly) {
                  return this.i18n('acc:entity.System.readonly.label');
                }
                return null;
              }
            }/>
          <Advanced.Column
            property="blockedOperation"
            width={ 175 }
            cell={({ rowIndex, data }) => {
              return (
                this._getBlockedOperations(data[rowIndex])
              );
            }}
            rendered={ _.includes(columns, 'blockedOperation') }/>
        </Advanced.Table>
        <Basic.Div rendered={showWizard}>
          <SystemWizardDetail
            show={showWizard}
            reopened
            closeWizard={this.closeWizard.bind(this)}
            match={this.props.match}
            connectorType={connectorType}/>
        </Basic.Div>
      </Basic.Div>
    );
  }
}

SystemTable.propTypes = {
  uiKey: PropTypes.string.isRequired,
  manager: PropTypes.object,
  columns: PropTypes.arrayOf(PropTypes.string),
  filterOpened: PropTypes.bool,
  defaultSearchParameters: PropTypes.object,
  forceSearchParameters: PropTypes.object,
  showAddButton: PropTypes.bool,
  showRowSelection: PropTypes.bool,
  showFilterVirtual: PropTypes.bool
};

SystemTable.defaultProps = {
  manager: systemManager,
  columns: ['name', 'description', 'state', 'virtual', 'queue', 'blockedOperation'],
  filterOpened: false,
  _showLoading: false,
  forceSearchParameters: new Domain.SearchParameters(),
  showAddButton: true,
  showRowSelection: true,
  showFilterVirtual: true
};

function select(state, component) {
  return {
    i18nReady: state.config.get('i18nReady'),
    _searchParameters: Utils.Ui.getSearchParameters(state, component.uiKey),
    _showLoading: (component.manager || systemManager).isShowLoading(state, `${ component.uiKey }-detail`)
  };
}

export default connect(select, null, null, { forwardRef: true })(SystemTable);
