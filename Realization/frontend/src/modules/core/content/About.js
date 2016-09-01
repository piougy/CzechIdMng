import React from 'react';
import Helmet from 'react-helmet';
import * as Advanced from 'app/components/advanced';
import * as Basic from 'app/components/basic';

export default class About extends Basic.AbstractContent {

  render() {
    // TODO: dynamic about info
    return (
      <div>
        <Helmet title={this.i18n('content.about.title')} />

        <Basic.Row>
          <div className="col-lg-4 col-lg-offset-4">
            <Basic.Panel>
              <Basic.PanelHeader text={this.i18n('content.about.header')}/>
              <Basic.PanelBody className="text-center">
                <div className="about-logo">
                </div>
                <div className="about-text">
                  <big>{this.i18n('app.version.frontend')}: Ametyst</big>
                  <br />
                  <big>{this.i18n('app.version.releaseDate')}: <Advanced.DateValue value="2016-09-01"/></big>
                  <br />
                  <a href={this.i18n('app.author.homePage')} target="_blank">{this.i18n('app.author.name')}</a>
                  <br />
                  <big>
                    {this.i18n('content.about.sourceCodeOn')}
                    {' '}
                    <a href="https://github.com/bcvsolutions/CzechIdMng" target="_blank">
                      <img src="https://assets-cdn.github.com/images/modules/logos_page/GitHub-Logo.png" style={{ height: 15, marginBottom: 5 }} />
                    </a>
                  </big>
                </div>
              </Basic.PanelBody>
            </Basic.Panel>
          </div>
        </Basic.Row>

      </div>
    );
  }
}
