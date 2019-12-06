import React from 'react';
//
import { Basic, Domain } from 'czechidm-core';
import VsRequestTable from './VsRequestTable';

/**
 * List of virtual system requests
 *
 * @author Vít Švanda
 */
export default class VsRequests extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      activeKey: 1
    };
  }

  /**
   * "Shorcut" for localization
   */
  getContentKey() {
    return 'vs:content.vs-requests';
  }

  /**
   * Selected navigation item
   */
  getNavigationKey() {
    return 'vs-requests';
  }

  _onChangeSelectTabs(activeKey) {
    this.setState({
      activeKey
    });
  }

  render() {
    const { activeKey } = this.state;

    const searchActive = new Domain.SearchParameters().setFilter('state', 'IN_PROGRESS');
    const searchArchive = new Domain.SearchParameters().setFilter('onlyArchived', true);
    const searchArchiveDefault = new Domain.SearchParameters().setSort('modified', 'desc');

    return (
      <div>
        <Basic.PageHeader>
          <span dangerouslySetInnerHTML={{__html: this.i18n('header')}}/>
        </Basic.PageHeader>
          <Basic.Tabs activeKey={activeKey} onSelect={this._onChangeSelectTabs.bind(this)}>
            <Basic.Tab eventKey={1} title={this.i18n('tabs.active.label')}>
              <VsRequestTable
                uiKey="vs-request-table"
                forceSearchParameters={searchActive}
                filterOpened />
            </Basic.Tab>
            <Basic.Tab eventKey={2} title={this.i18n('tabs.archive.label')}>
              <VsRequestTable
                uiKey="vs-request-table-archive"
                columns={['uid', 'systemId', 'operationType', 'executeImmediately', 'created', 'creator', 'roleRequestId', 'modifier', 'modified', 'state']}
                showRowSelection={false}
                forceSearchParameters={searchArchive}
                defaultSearchParameters={searchArchiveDefault}
                filterOpened />
            </Basic.Tab>
          </Basic.Tabs>
      </div>
    );
  }
}
