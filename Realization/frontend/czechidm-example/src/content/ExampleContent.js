import React from 'react';
import Helmet from 'react-helmet';
//
import { Basic } from 'czechidm-core';
import { ExampleManager } from '../redux';

const manager = new ExampleManager();

/**
 * Example content (page)
 *
 * @author Radek Tomiška
 */
export default class ExampleContent extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
  }

  /**
   * "Shorcut" for localization
   */
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

        <Basic.Panel>
          <Basic.PanelHeader text={ this.i18n('error.header') }/>
          <Basic.PanelBody>
            <Basic.AbstractForm style={{ padding: 0 }}>
              <Basic.TextField ref="parameter" label={ this.i18n('error.parameter.label') } className="last"/>
            </Basic.AbstractForm>
          </Basic.PanelBody>
          <Basic.PanelFooter>
            <Basic.Button
              level="warning"
              onClick={ () => { this.context.store.dispatch(manager.clientError(this.refs.parameter.getValue())); } }>
              { this.i18n('error.button.client') }
            </Basic.Button>
            {' '}
            <Basic.Button
              level="danger"
              onClick={ () => { this.context.store.dispatch(manager.serverError(this.refs.parameter.getValue())); } }>
              { this.i18n('error.button.server') }
            </Basic.Button>
          </Basic.PanelFooter>
        </Basic.Panel>

      </div>
    );
  }
}
