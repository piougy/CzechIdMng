import React from 'react';
import { connect } from 'react-redux';
//
import { Basic, Managers, Domain } from 'czechidm-core';
import RemoteServerTable from './RemoteServerTable';
import { SystemManager } from '../../redux';
import ConnectorTable from './ConnectorTable';

const systemManager = new SystemManager();

/**
 * Remote server with connectors.
 *
 * @author Radek Tomiška
 * @since 10.8.0
 */
class RemoteServers extends Basic.AbstractContent {

  componentDidMount() {
    super.componentDidMount();
    //
    this.context.store.dispatch(systemManager.fetchAvailableFrameworks());
  }

  getContentKey() {
    return 'acc:content.connector-servers';
  }

  getNavigationKey() {
    return 'sys-connector-servers';
  }

  showDetail(entity) {
    this.context.history.push(`/connectors/${ entity.id }/detail`);
  }

  render() {
    const { availableFrameworks, _showLoading } = this.props;
    const defaultSearchParameters = new Domain.SearchParameters().setFilter('remote', 'false');
    //
    return (
      <Basic.Div>
        { this.renderPageHeader({ header: this.i18n('acc:content.remote-servers.header') }) }
        <Basic.Panel>
          <RemoteServerTable uiKey="remote-server-table"/>
        </Basic.Panel>

        <Basic.ContentHeader
          icon="component:default-connector"
          text={ this.i18n('local.header', { escape: false }) }/>

        <Basic.Panel>
          <ConnectorTable
            uiKey="local-connector-table"
            connectorFrameworks={ availableFrameworks }
            showLoading={ _showLoading }
            defaultSearchParameters={ defaultSearchParameters }/>
        </Basic.Panel>
      </Basic.Div>
    );
  }
}

function select(state) {
  return {
    availableFrameworks: Managers.DataManager.getData(state, SystemManager.AVAILABLE_CONNECTORS),
    _showLoading: Managers.DataManager.isShowLoading(state, SystemManager.AVAILABLE_CONNECTORS)
  };
}

export default connect(select)(RemoteServers);
