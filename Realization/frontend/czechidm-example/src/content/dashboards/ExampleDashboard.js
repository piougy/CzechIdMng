import React from 'react';
import { Basic } from 'czechidm-core';

/**
 * Example dashbord panel
 *
 * @author Vít Švanda
 * @author Radek Tomiška
 */
export default class ExampleDashboard extends Basic.AbstractContent {

  /**
   * "Shorcut" for localization
   */
  getContentKey() {
    return 'example:content.dashboard.exampleDashboard';
  }

  render() {
    return (
      <Basic.Panel>
        <Basic.PanelHeader text={ this.i18n('header') }/>
        <Basic.PanelBody>
          <Basic.Panel className="panel-warning no-margin">
            <Basic.PanelHeader>
              <Basic.Row>
                <Basic.Col lg={ 3 }>
                  <Basic.Icon type="fa" icon="dashboard" className="fa-5x"/>
                </Basic.Col>
                <Basic.Col lg={ 9 }>
                  <div><strong>{ this.i18n('title') }</strong></div>
                  <div>{ this.i18n('text') }</div>
                </Basic.Col>
              </Basic.Row>
            </Basic.PanelHeader>
          </Basic.Panel>
        </Basic.PanelBody>
      </Basic.Panel>
    );
  }
}
