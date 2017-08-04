import React from 'react';
//
import { Basic } from 'czechidm-core';
import ExampleProductTable from './ExampleProductTable';

/**
 * List of example products
 *
 * @author Radek Tomiška
 */
export default class ExampleProducts extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
  }

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
      <div>
        { this.renderPageHeader() }

        <Basic.Alert text={ this.i18n('info') }/>

        <Basic.Panel>
          <ExampleProductTable uiKey="example-product-table" filterOpened />
        </Basic.Panel>

      </div>
    );
  }
}
