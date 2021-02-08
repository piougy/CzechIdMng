import React from 'react';
//
import { Basic, Domain } from 'czechidm-core';
import SystemTable from '../system/SystemTable';

const uiKey = 'remote-server-systems-table';

/**
 * Systems using remote server.
 *
 * @author Radek Tomiška
 * @since 10.8.0
 */
export default class RemoteServerSystems extends Basic.AbstractContent {

  getUiKey() {
    return uiKey;
  }

  getContentKey() {
    return 'acc:content.remote-servers.systems';
  }

  getNavigationKey() {
    return 'sys-remote-server-systems';
  }

  render() {
    const { entityId } = this.props.match.params;
    const forceSearchParameters = new Domain.SearchParameters().setFilter('remoteServerId', entityId);
    //
    return (
      <Basic.Div className="tab-pane-table-body">
        { this.renderContentHeader({ style: { marginBottom: 0 }}) }

        <Basic.Panel className="no-border last">
          <SystemTable
            showAddButton={ false }
            showRowSelection
            showFilterVirtual={ false }
            filterOpened
            uiKey={ `${ this.getUiKey() }-${ entityId }` }
            forceSearchParameters={ forceSearchParameters }
            match={ this.props.match }
            className="no-margin"/>
        </Basic.Panel>
      </Basic.Div>
    );
  }
}
