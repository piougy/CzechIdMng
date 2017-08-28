import React from 'react';
//
import { Basic } from 'czechidm-core';
import VsRequestTable from './VsRequestTable';

/**
 * List of virtual system requests
 *
 * @author Vít Švanda
 */
export default class VsRequests extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
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

  render() {
    return (
      <div>
        { this.renderPageHeader() }

        <Basic.Panel>
          <VsRequestTable uiKey="vs-request-table" filterOpened />
        </Basic.Panel>

      </div>
    );
  }
}
