import PropTypes from 'prop-types';
import React from 'react';
import { connect } from 'react-redux';
//
import { Basic, Domain, Managers, Utils, Advanced} from 'czechidm-core';
import uuid from 'uuid';
import { SynchronizationConfigManager, SystemManager } from '../../redux';
import SyncStatistic from '../sync/SyncStatistic';
import SyncResult from '../sync/SyncResult';

const uiKey = 'system-synchronization-configs-table';
const manager = new SynchronizationConfigManager();
const systemManager = new SystemManager();

/**
 * @author Vít Švanda
 */
class SystemSynchronizationConfigs extends Advanced.AbstractTableContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      configs: [],
      longPollingInprogress: false,
      automaticRefreshOn: true
    };
    this.canSendLongPollingRequest = false;
  }

  getManager() {
    return manager;
  }

  getUiKey() {
    return uiKey;
  }

  getContentKey() {
    return 'acc:content.system.systemSynchronizationConfigs';
  }

  getNavigationKey() {
    return 'system-synchronization-configs';
  }

  componentWillReceiveProps(props) {
    this._initComponent(props);
  }

  componentWillUnmount() {
    super.componentWillUnmount();
    // Stop rquest of check rquests (next long-polling request will be not created)
    this.canSendLongPollingRequest = false;
  }

  componentDidMount() {
    super.componentDidMount();
    // Allow chcek of unresolved requests
    this.canSendLongPollingRequest = true;
    this._initComponent(this.props);
  }

  _initComponent(props) {
    const { entityId } = props.match.params;
    if (!this.state.longPollingInprogress && this._isLongPollingEnabled()) {
      // Long-polling request can be send.
      this.setState({longPollingInprogress: true}, () => {
        this._sendLongPollingRequest(entityId);
      });
    }
  }

  _sendLongPollingRequest(entityId) {
    Managers.LongPollingManager.sendLongPollingRequest.bind(this, entityId, systemManager.getService())();
  }

  _toggleAutomaticRefresh() {
    const { entityId } = this.props.match.params;
    const canSendLongPollingRequest = this.canSendLongPollingRequest;

    this.canSendLongPollingRequest = !canSendLongPollingRequest;
    this.setState({
      automaticRefreshOn: !canSendLongPollingRequest
    }, () => {
      if (this.canSendLongPollingRequest) {
        this._refreshAll();
        if (!this.state.longPollingInprogress) {
          this._sendLongPollingRequest(entityId);
        }
      }
    });
  }

  _isLongPollingEnabled() {
    const {_longPollingEnabled } = this.props;
    return _longPollingEnabled;
  }

  _refreshAll(props = null) {
    this.refs.table.getWrappedInstance().reload(props);
  }

  showDetail(entity, add) {
    const systemId = this.props.match.params.entityId;
    if (add) {
      // When we add new object class, then we need use "new" url
      const uuidId = uuid.v1();
      this.context.history.push(`/system/${systemId}/synchronization-configs/${uuidId}/new?new=1&systemId=${systemId}`);
    } else {
      this.context.history.push(`/system/${systemId}/synchronization-configs/${entity.id}/detail`);
    }
  }

  _redirectToLastLog(entity) {
    const systemId = this.props.match.params.entityId;
    this.context.history.push(`/system/${systemId}/synchronization-logs/${entity.lastSyncLog.id}/detail`);
  }

  _startSynchronization(bulkActionValue, sync, event) {
    if (event) {
      event.preventDefault();
    }
    this.refs[`confirm-delete`].show(
      this.i18n(`action.startSynchronization.message`, { name: sync.name }),
      this.i18n(`action.startSynchronization.header`)
    ).then(() => {
      this.setState({
        showLoading: true
      }, () => {
        const promise = this.getManager().getService().startSynchronization(sync.id);
        promise.then(() => {
          // set running state in redux only => reload is not needed
          sync.running = true;
          this.getManager().receiveEntity(sync.id, sync, null, () => {
            this.setState({
              showLoading: false
            }, () => {
              this.addMessage({ level: 'info', message: this.i18n('action.startSynchronization.started', { name: sync.name })});
            });
          });
        }).catch(ex => {
          this.setState({
            showLoading: false
          }, () => {
            this.addError(ex);
            if (this.refs.table) {
              this.refs.table.reload();
            }
          });
        });
      });
    }, () => {
      // Rejected
    });
  }

  _cancelSynchronization(sync, event) {
    if (event) {
      event.preventDefault();
    }
    this.refs[`confirm-delete`].show(
      this.i18n(`action.cancelSynchronization.message`, {name: sync.name}),
      this.i18n(`action.cancelSynchronization.header`)
    ).then(() => {
      this.setState({
        showLoading: true
      }, () => {
        const promise = this.getManager().getService().cancelSynchronization(sync.id);
        promise.then((json) => {
          this.setState({
            showLoading: false
          }, () => {
            if (this.refs.table) {
              this.refs.table.reload();
            }
            this.addMessage({ message: this.i18n('action.cancelSynchronization.canceled', { name: json.name }) });
          });
        }).catch(ex => {
          this.setState({
            showLoading: false
          }, () => {
            this.addError(ex);
            if (this.refs.table) {
              this.refs.table.reload();
            }
          });
        });
      });
    }, () => {
      // Rejected
    });
  }

  _getBulkActions() {
    const actions = [];
    if (Managers.SecurityManager.hasAnyAuthority(['SYSTEM_UPDATE'])) {
      actions.push({ value: 'delete', niceLabel: this.i18n('action.delete.action'), action: this.onDelete.bind(this), disabled: false });
    }
    return actions;
  }

  _renderActionButtons(entity) {
    const results = [];
    results.push(
      <Basic.Button
        ref="logButton"
        type="button"
        rendered={ Managers.SecurityManager.hasAnyAuthority(['SYNCHRONIZATION_READ']) }
        style={{ marginRight: 2 }}
        title={ this.i18n('acc:entity.SynchronizationConfig.button.lastSyncLog') }
        titlePlacement="bottom"
        onClick={ this._redirectToLastLog.bind(this, entity) }
        disabled={ !entity.lastSyncLog }
        className="btn-xs"
        icon="fa:list-alt"/>
    );
    if (entity.running) {
      results.push(
        <Basic.Button
          ref="cancelButton"
          type="button"
          level="danger"
          rendered={ Managers.SecurityManager.hasAnyAuthority(['SYNCHRONIZATION_UPDATE']) }
          style={{ marginRight: 2 }}
          title={ this.i18n('button.cancel') }
          titlePlacement="bottom"
          onClick={ this._cancelSynchronization.bind(this, entity) }
          className="btn-xs"
          icon="fa:remove"/>
      );
    } else {
      results.push(
        <Basic.Button
          ref="startButton"
          type="button"
          level="success"
          rendered={ Managers.SecurityManager.hasAnyAuthority(['SYNCHRONIZATION_CREATE']) }
          style={{ marginRight: 2 }}
          title={ this.i18n('button.start') }
          titlePlacement="bottom"
          onClick={ this._startSynchronization.bind(this, null, entity) }
          disabled={ !entity.enabled }
          className="btn-xs"
          icon="fa:play"/>
      );
    }

    return results;
  }

  _renderResultCell(rowIndex, data) {
    const actions = [];
    const sync = data[rowIndex];
    if (!sync || !sync.lastSyncLog || !sync.lastSyncLog.syncActionLogs) {
      return actions;
    }
    const log = sync.lastSyncLog;
    return (
      <SyncResult log={log}/>
    );
  }

  _renderStatisticCell(rowIndex, data) {
    const actions = [];
    const sync = data[rowIndex];
    if (!sync || !sync.lastSyncLog || !sync.lastSyncLog.syncActionLogs) {
      return actions;
    }
    const log = sync.lastSyncLog;
    return (
      <SyncStatistic log={log}/>
    );
  }

  _getToolbar(key) {
    const {automaticRefreshOn} = this.state;
    const longPollingEnabled = this._isLongPollingEnabled();
    const data = {};
    data[`automaticRefreshSwitch-${key}`] = automaticRefreshOn && longPollingEnabled;
    return (
      <Basic.Toolbar>
        <div className="pull-left">
          <Basic.AbstractForm
            ref={`automaticRefreshForm-${key}`}
            readOnly={!longPollingEnabled}
            style={{padding: '0px'}}
            data={data}>
            <Basic.ToggleSwitch
              ref={`automaticRefreshSwitch-${key}`}
              label={this.i18n('content.identity.roles.automaticRefreshSwitch')}
              onChange={this._toggleAutomaticRefresh.bind(this, key)}
            />
          </Basic.AbstractForm>
        </div>
        <div className="pull-right">
          <Basic.Button
            level="success"
            key="add_button"
            className="btn-xs"
            onClick={ this.showDetail.bind(this, { }, true) }
            rendered={ Managers.SecurityManager.hasAnyAuthority(['SYSTEM_UPDATE']) }
            icon="fa:plus">
            { this.i18n('button.add') }
          </Basic.Button>
          <Advanced.RefreshButton
            rendered={!automaticRefreshOn || !longPollingEnabled}
            onClick={ this._refreshAll.bind(this) }/>
        </div>
      </Basic.Toolbar>
    );
  }

  render() {
    const { entityId } = this.props.match.params;
    const forceSearchParameters = new Domain.SearchParameters()
      .setFilter('systemId', entityId)
      .setFilter('includeLastLog', true);
    return (
      <div>
        <Basic.Confirm ref="confirm-delete" level="danger"/>

        { this.renderContentHeader() }

        <Basic.Panel className="no-border last">
          {this._getToolbar('system-sync')}
          <Advanced.Table
            ref="table"
            uiKey={ uiKey }
            showRefreshButton={false}
            manager={ this.getManager() }
            forceSearchParameters={ forceSearchParameters }
            rowClass={({rowIndex, data}) => {
              if (!(data[rowIndex].enabled)) {
                return 'disabled';
              }
              if (data[rowIndex].lastSyncLog && data[rowIndex].lastSyncLog.containsError) {
                return 'danger';
              }
              return '';
            }
            }
            showRowSelection={ Managers.SecurityManager.hasAnyAuthority(['SYSTEM_UPDATE']) }
            actions={ this._getBulkActions() }>
            <Advanced.Column
              property=""
              header=""
              className="detail-button"
              cell={
                ({ rowIndex, data }) => (
                  <Advanced.DetailButton
                    title={ this.i18n('button.detail') }
                    style={{ marginRight: 2 }}
                    onClick={ this.showDetail.bind(this, data[rowIndex], false) }/>
                )
              }/>
            <Advanced.Column
              property=""
              header={ this.i18n('acc:entity.SynchronizationConfig.running') }
              cell={
                ({ rowIndex, data }) => (
                  <Basic.Icon
                    value={ data[rowIndex].running ? 'fa:check-square-o' : 'fa:square-o'}
                    disabled/>
                )
              }/>
            <Advanced.Column
              property="name"
              face="text"
              header={ this.i18n('acc:entity.SynchronizationConfig.name') }
              sort/>
            <Advanced.Column
              rendered={false}
              property="reconciliation"
              face="boolean"
              header={ this.i18n('acc:entity.SynchronizationConfig.reconciliation.label') }
              sort/>
            <Advanced.Column
              rendered={false}
              property="enabled"
              face="boolean"
              header={ this.i18n('acc:entity.SynchronizationConfig.enabled') }
              sort/>
            <Advanced.Column
              property="syncActionLogs"
              header={this.i18n('acc:entity.SynchronizationLog.results')}
              cell={
                ({ rowIndex, data }) => this._renderResultCell(rowIndex, data)
              }
            />
            <Advanced.Column
              property="statistic"
              header={this.i18n('acc:entity.SynchronizationLog.statistic.label')}
              cell={
                ({ rowIndex, data }) => this._renderStatisticCell(rowIndex, data)
              }
            />
            <Advanced.Column
              property=""
              header=""
              width={ 55 }
              className="detail-button"
              cell={
                ({ rowIndex, data }) => this._renderActionButtons(data[rowIndex])
              }/>
          </Advanced.Table>
        </Basic.Panel>
      </div>
    );
  }
}

SystemSynchronizationConfigs.propTypes = {
  system: PropTypes.object,
  _showLoading: PropTypes.bool,
};
SystemSynchronizationConfigs.defaultProps = {
  system: null,
  _showLoading: false,
};

function select(state, component) {
  return {
    i18nReady: state.config.get('i18nReady'),
    system: Utils.Entity.getEntity(state, systemManager.getEntityType(), component.match.params.entityId),
    _showLoading: Utils.Ui.isShowLoading(state, `${uiKey}-detail`),
    _longPollingEnabled: Managers.ConfigurationManager.getPublicValueAsBoolean(state, 'idm.pub.app.long-polling.enabled', true)
  };
}

export default connect(select)(SystemSynchronizationConfigs);
