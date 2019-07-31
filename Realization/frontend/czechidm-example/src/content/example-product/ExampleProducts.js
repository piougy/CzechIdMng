import React from 'react';
//
import { Basic } from 'czechidm-core';
import ExampleProductTableComponent from './ExampleProductTable';

/**
 * List of example products
 *
 * @author Radek Tomiška
 */
export default class ExampleProducts extends Basic.AbstractContent {

  /**
   * "Shorcut" for localization
   */
  getContentKey() {
    return 'example:content.example-products';
  }

  /**
   * Selected navigation item
   */
  getNavigationKey() {
    return 'example-products';
  }

  render() {
    return (
      <Basic.Div>
        { this.renderPageHeader() }

        <Basic.Alert text={ this.i18n('info') }/>

        <Basic.Panel>
          <ExampleProductTableComponent uiKey="example-product-table" filterOpened />
        </Basic.Panel>

      </Basic.Div>
    );
  }
}
