import React from 'react';
import { connect } from 'react-redux';
import { Basic, Domain, Managers } from 'czechidm-core';
import VsRequestTable from '../vs-request/VsRequestTable';

const uiKey = 'vs-request-table-dashboard';

/**
 * Virtual system dashbord panel
 *
 * @author Vít Švanda
 */
class VsDashboard extends Basic.AbstractContent {

  /**
   * "Shorcut" for localization
   */
  getContentKey() {
    return 'vs:content.dashboard.vsDashboard';
  }

  render() {
    const { _total } = this.props;
    //
    if (!Managers.SecurityManager.hasAccess({ 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['VSREQUEST_READ']})) {
      return null;
    }
    const searchActive = new Domain.SearchParameters().setFilter('state', 'IN_PROGRESS');
    return (
      <div className={ _total ? '' : 'hidden' }>
        <Basic.ContentHeader
          icon="link"
          text={ this.i18n('header') }/>
        <Basic.Panel>
          <VsRequestTable
            uiKey={ uiKey }
            columns={['uid', 'systemId', 'operationType', 'created', 'creator', 'operations']}
            showFilter={false}
            forceSearchParameters={searchActive}
            showToolbar={false}
            showPageSize={false}
            showRowSelection={false}
            showId={false}
            filterOpened={false}/>
        </Basic.Panel>
      </div>
    );
  }
}

function select(state) {
  const ui = state.data.ui[uiKey];
  if (!ui) {
    return {};
  }
  return {
    _total: ui.total
  };
}

export default connect(select)(VsDashboard);
