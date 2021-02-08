import React from 'react';
import { connect } from 'react-redux';
//
import { Basic, Domain, Managers } from 'czechidm-core';
import { RemoteServerManager } from '../../redux';
import ConnectorTable from './ConnectorTable';

const manager = new RemoteServerManager();

/**
 * Connectors available on remote server.
 *
 * @author Radek Tomiška
 * @since 10.8.0
 */
class RemoteServerConnectors extends Basic.AbstractContent {

  componentDidMount() {
    super.componentDidMount();
    //
    const { entityId } = this.props.match.params;
    //
    this.context.store.dispatch(manager.fetchAvailableFrameworks(entityId, `remote-server-${ entityId }-connector-table`));
  }

  getContentKey() {
    return 'acc:content.remote-servers.connectors';
  }

  getNavigationKey() {
    return 'sys-remote-server-connectors';
  }

  render() {
    const { showLoading, availableFrameworks } = this.props;
    const { entityId } = this.props.match.params;
    const defaultSearchParameters = new Domain.SearchParameters().setFilter('remoteServerId', entityId);
    //
    return (
      <Basic.Div className="tab-pane-table-body" style={{ minHeight: 300 }}>
        { this.renderContentHeader({ style: { marginBottom: 0 }}) }

        <ConnectorTable
          uiKey={ `remote-server-${ entityId }-connector-table` }
          connectorFrameworks={ availableFrameworks }
          showLoading={ showLoading }
          className="no-margin"
          defaultSearchParameters={ defaultSearchParameters }/>
      </Basic.Div>
    );
  }
}

function select(state, component) {
  const { entityId } = component.match.params;
  const entity = manager.getEntity(state, entityId);
  //
  return {
    entity,
    showLoading: manager.isShowLoading(state, null, entityId)
      || Managers.DataManager.isShowLoading(state, `remote-server-${ entityId }-connector-table`),
    availableFrameworks: Managers.DataManager.getData(state, `remote-server-${ entityId }-connector-table`)
  };
}

export default connect(select)(RemoteServerConnectors);
