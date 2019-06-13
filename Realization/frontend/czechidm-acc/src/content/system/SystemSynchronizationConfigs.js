import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
//
import { Basic, Advanced, Domain, Managers, Utils } from 'czechidm-core';
import { SynchronizationConfigManager, SystemManager } from '../../redux';
import uuid from 'uuid';

const uiKey = 'system-synchronization-configs-table';
const manager = new SynchronizationConfigManager();
const systemManager = new SystemManager();

/**
 * @author Vít Švanda
 */
class SystemSynchronizationConfigs extends Advanced.AbstractTableContent {

  constructor(props, context) {
    super(props, context);
    this.state = {configs: []};
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

  showDetail(entity, add) {
    const systemId = this.props.params.entityId;
    if (add) {
      // When we add new object class, then we need use "new" url
      const uuidId = uuid.v1();
      this.context.router.push(`system/${systemId}/synchronization-configs/${uuidId}/new?new=1&systemId=${systemId}`);
    } else {
      this.context.router.push(`system/${systemId}/synchronization-configs/${entity.id}/detail`);
    }
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
              this.refs.table.getWrappedInstance().reload();
            }
          });
        });
      });
    }, () => {
      // Rejected
    });
    return;
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
              this.refs.table.getWrappedInstance().reload();
            }
            this.addMessage({ message: this.i18n('action.cancelSynchronization.canceled', { name: json.name }) });
          });
        }).catch(ex => {
          this.setState({
            showLoading: false
          }, () => {
            this.addError(ex);
            if (this.refs.table) {
              this.refs.table.getWrappedInstance().reload();
            }
          });
        });
      });
    }, () => {
      // Rejected
    });
    return;
  }

  _getBulkActions() {
    const actions = [];
    if (Managers.SecurityManager.hasAnyAuthority(['SYSTEM_UPDATE'])) {
      actions.push({ value: 'delete', niceLabel: this.i18n('action.delete.action'), action: this.onDelete.bind(this), disabled: false });
    }
    return actions;
  }

  render() {
    const { entityId } = this.props.params;
    const forceSearchParameters = new Domain.SearchParameters().setFilter('systemId', entityId);
    return (
      <div>
        <Basic.Confirm ref="confirm-delete" level="danger"/>

        { this.renderContentHeader() }

        <Basic.Panel className="no-border last">
          <Advanced.Table
            ref="table"
            uiKey={ uiKey }
            manager={ this.getManager() }
            forceSearchParameters={ forceSearchParameters }
            className="no-margin"
            rowClass={ ({rowIndex, data}) => { return !(data[rowIndex].enabled) ? 'disabled' : ''; } }
            showRowSelection={ Managers.SecurityManager.hasAnyAuthority(['SYSTEM_UPDATE']) }
            actions={ this._getBulkActions() }
            buttons={
              [
                <Basic.Button
                  level="success"
                  key="add_button"
                  className="btn-xs"
                  onClick={ this.showDetail.bind(this, { }, true) }
                  rendered={ Managers.SecurityManager.hasAnyAuthority(['SYSTEM_UPDATE']) }
                  icon="fa:plus">
                  { this.i18n('button.add') }
                </Basic.Button>
              ]
            }>
            <Advanced.Column
              property=""
              header=""
              className="detail-button"
              cell={
                ({ rowIndex, data }) => {
                  return (
                    <Advanced.DetailButton
                      title={ this.i18n('button.detail') }
                      style={{ marginRight: 2 }}
                      onClick={ this.showDetail.bind(this, data[rowIndex], false) }/>

                  );
                }
              }/>
            <Advanced.Column
              property=""
              header={ this.i18n('acc:entity.SynchronizationConfig.running') }
              cell={
                ({ rowIndex, data }) => {
                  return (
                    <Basic.Icon
                      value={ data[rowIndex].running ? 'fa:check-square-o' : 'fa:square-o'}
                      disabled/>
                  );
                }
              }/>
            <Advanced.Column
              property="name"
              face="text"
              header={ this.i18n('acc:entity.SynchronizationConfig.name') }
              sort/>
            <Advanced.Column
              property="reconciliation"
              face="boolean"
              header={ this.i18n('acc:entity.SynchronizationConfig.reconciliation.label') }
              sort/>
            <Advanced.Column
              property="enabled"
              face="boolean"
              header={ this.i18n('acc:entity.SynchronizationConfig.enabled') }
              sort/>
            <Advanced.Column
              property=""
              header=""
              width={ 55 }
              className="detail-button"
              cell={
                ({ rowIndex, data }) => {
                  const entity = data[rowIndex];
                  //
                  if (entity.running) {
                    return (
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
                  }
                  return (
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
    system: Utils.Entity.getEntity(state, systemManager.getEntityType(), component.params.entityId),
    _showLoading: Utils.Ui.isShowLoading(state, `${uiKey}-detail`),
  };
}

export default connect(select)(SystemSynchronizationConfigs);
