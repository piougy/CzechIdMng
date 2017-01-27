import React, { PropTypes } from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
//
import { Basic, Advanced, Domain, Managers, Utils } from 'czechidm-core';
import { SynchronizationConfigManager, SystemManager } from '../../redux';
import uuid from 'uuid';

const uiKey = 'system-synchronization-configs-table';
const manager = new SynchronizationConfigManager();
const systemManager = new SystemManager();

class SystemSynchronizationConfigs extends Basic.AbstractTableContent {

  constructor(props, context) {
    super(props, context);
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

  componentDidMount() {
    this.selectNavigationItems(['sys-systems', 'system-synchronization-configs']);
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

  _startSynchronization(bulkActionValue, ids) {
    if (event) {
      event.preventDefault();
    }
    this.refs[`confirm-delete`].show(
      this.i18n(`action.startSynchronization.message`),
      this.i18n(`action.startSynchronization.header`)
    ).then(() => {
      for (const id of ids) {
        this.setState({
          showLoading: true
        });
        this.addMessage({ message: this.i18n('action.startSynchronization.started')});
        const promise = this.getManager().getService().startSynchronization(id);
        promise.then((json) => {
          this.setState({
            showLoading: false
          });
          if (this.refs.table) {
            this.refs.table.getWrappedInstance().reload();
          }
          this.addMessage({ message: this.i18n('action.startSynchronization.success', { name: json.name }) });
        }).catch(ex => {
          this.setState({
            showLoading: false
          });
          this.addError(ex);
          if (this.refs.table) {
            this.refs.table.getWrappedInstance().reload();
          }
        });
      }
    }, () => {
      // Rejected
    });
    return;
  }

  _getBulkActions() {
    const actions = [];
    if (Managers.SecurityManager.hasAnyAuthority(['SYSTEM_WRITE'])) {
      actions.push({ value: 'delete', niceLabel: this.i18n('action.delete.action'), action: this.onDelete.bind(this), disabled: false });
    }
    if (Managers.SecurityManager.hasAnyAuthority(['SYNCHRONIZATION_WRITE'])) {
      actions.push({ value: 'start', niceLabel: this.i18n('startSynchronization'), action: this._startSynchronization.bind(this), disabled: false });
    }
    return actions;
  }

  render() {
    const { entityId } = this.props.params;
    const forceSearchParameters = new Domain.SearchParameters().setFilter('systemId', entityId);
    return (
      <div>
        <Helmet title={this.i18n('title')} />
        <Basic.Confirm ref="confirm-delete" level="danger"/>

        <Basic.ContentHeader style={{ marginBottom: 0 }}>
          <span dangerouslySetInnerHTML={{ __html: this.i18n('header') }}/>
        </Basic.ContentHeader>

        <Basic.Panel className="no-border last">
          <Advanced.Table
            ref="table"
            uiKey={uiKey}
            manager={this.getManager()}
            forceSearchParameters={forceSearchParameters}
            showRowSelection={Managers.SecurityManager.hasAnyAuthority(['SYSTEM_WRITE'])}
            actions={this._getBulkActions()}
            buttons={
              [
                <Basic.Button
                  level="success"
                  key="add_button"
                  className="btn-xs"
                  onClick={this.showDetail.bind(this, { }, true)}
                  rendered={Managers.SecurityManager.hasAnyAuthority(['SYSTEM_WRITE'])}>
                  <Basic.Icon type="fa" icon="plus"/>
                  {' '}
                  {this.i18n('button.add')}
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
                      title={this.i18n('button.detail')}
                      onClick={this.showDetail.bind(this, data[rowIndex], false)}/>
                  );
                }
              }/>
            <Advanced.Column
              property="name"
              face="text"
              header={this.i18n('acc:entity.SynchronizationConfig.name')}
              sort/>
            <Advanced.Column
              property="reconciliation"
              face="boolean"
              header={this.i18n('acc:entity.SynchronizationConfig.reconciliation.label')}
              sort/>
            <Advanced.Column
              property="enabled"
              face="boolean"
              header={this.i18n('acc:entity.SynchronizationConfig.enabled')}
              sort/>
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
    system: Utils.Entity.getEntity(state, systemManager.getEntityType(), component.params.entityId),
    _showLoading: Utils.Ui.isShowLoading(state, `${uiKey}-detail`),
  };
}

export default connect(select)(SystemSynchronizationConfigs);
