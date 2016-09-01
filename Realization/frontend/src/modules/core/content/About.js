import React from 'react';
import Helmet from 'react-helmet';
import * as Basic from 'app/components/basic';

export default class About extends Basic.AbstractContent {

  render() {
    // TODO: dynamic about info
    return (
      <div>
        <Helmet title={this.i18n('app.about')} />

        <Basic.PageHeader>
          {this.i18n('app.about')}
        </Basic.PageHeader>

        <Basic.Row>
            <Basic.Panel className="text-center col-lg-4 col-lg-offset-4" >
              <div className="about-logo">
              </div>
                <img style={{ marginTop: 15 }} />
                <Basic.Row className="about-text" style={{ marginTop: 15 }}>
                  <big>Verze: 1.0.0</big>
                  <br />
                  <big>BCV solutions s.r.o. </big>
                  <br />
                  <big>
                    <span className="glyphicon glyphicon-menu-left"></span>
                    <span className="glyphicon glyphicon-menu-right"></span>
                    on
                    {' '}
                    <a href="https://github.com/bcvsolutions/CzechIdMng">
                      <img src="https://assets-cdn.github.com/images/modules/logos_page/GitHub-Logo.png" style={{ height: 15, marginBottom: 5 }} />
                    </a>
                  </big>
                </Basic.Row>
            </Basic.Panel>

        </Basic.Row>

      </div>
    );
  }
}
