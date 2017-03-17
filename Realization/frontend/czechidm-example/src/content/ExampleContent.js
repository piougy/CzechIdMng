import React from 'react';
import Helmet from 'react-helmet';
//
import { Basic } from 'czechidm-core';

/**
 * Example content (page)
 *
 * @author Radek Tomiška
 */
export default class ExampleContent extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
  }

  getContentKey() {
    return 'example:content.example';
  }

  render() {
    return (
      <div>
        <Helmet title={this.i18n('title')} />

        <Basic.PageHeader>
          <Basic.Icon value="link"/>
          {' '}
          {this.i18n('header')}
        </Basic.PageHeader>

        <Basic.Panel>
          <Basic.PanelBody>
            { this.i18n('text') }
          </Basic.PanelBody>
        </Basic.Panel>
      </div>
    );
  }
}
