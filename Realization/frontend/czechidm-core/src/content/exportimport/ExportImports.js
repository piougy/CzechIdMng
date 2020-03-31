import React from 'react';
//
import { Basic } from 'czechidm-core';
import ExportImportTable from './ExportImportTable';

/**
 * List of export-imports
 *
 * @author Vít Švanda
 */
export default class ExportImports extends Basic.AbstractContent {

  /**
   * "Shorcut" for localization
   */
  getContentKey() {
    return 'content.export-imports';
  }

  /**
   * Selected navigation item
   */
  getNavigationKey() {
    return 'export-imports';
  }

  render() {
    return (
      <Basic.Div>
        { this.renderPageHeader() }

        <Basic.Panel>
          <ExportImportTable
            uiKey="export-table"
            filterOpened
            location={ this.props.location }/>
        </Basic.Panel>

      </Basic.Div>
    );
  }
}
