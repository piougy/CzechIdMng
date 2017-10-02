import React from 'react';
import { Basic, Domain, Managers } from 'czechidm-core';
import VsRequestTable from '../vs-request/VsRequestTable';

/**
 * Virtual system dashbord panel
 *
 * @author Vít Švanda
 */
export default class VsDashboard extends Basic.AbstractContent {

  /**
   * "Shorcut" for localization
   */
  getContentKey() {
    return 'vs:content.dashboard.vsDashboard';
  }

  render() {
    if (!Managers.SecurityManager.hasAccess({ 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['VSREQUEST_READ']})) {
      return null;
    }
    const searchActive = new Domain.SearchParameters().setFilter('state', 'IN_PROGRESS');
    return (
      <Basic.Panel>
        <Basic.PanelHeader text={this.i18n('header')}/>
        <Basic.PanelBody >
          <VsRequestTable
            uiKey="vs-request-table-dashboard"
            columns= {['uid', 'systemId', 'operationType', 'created', 'creator', 'operations']}
            showFilter={false}
            forceSearchParameters={searchActive}
            showToolbar={false}
            showPageSize={false}
            showRowSelection={false}
            showId={false}
            filterOpened={false} />
        </Basic.PanelBody>
      </Basic.Panel>
    );
  }
}
