import React from 'react';
import PropTypes from 'prop-types';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
//
import * as Advanced from '../components/advanced';
import * as Basic from '../components/basic';
import { ConfigurationManager } from '../redux';

/**
 * Simple about content
 *
 * @author Ondřej Kopr
 * @author Radek Tomiška
 */
class About extends Basic.AbstractContent {

  render() {
    const { version, buildNumber, buildTimestamp } = this.props;
    //
    return (
      <div>
        <Helmet title={ this.i18n('content.about.title') } />

        <Basic.Row>
          <Basic.Col lg={ 4 } className="col-lg-offset-4">
            <Basic.Panel>
              <Basic.PanelHeader text={ this.i18n('content.about.header') } />
              <Basic.PanelBody className="text-center">
                <div className="about-logo" />
                <div className="about-text">
                  <big>
                    { this.i18n('app.version.frontend') }
                    :
                    { version }
                  </big>
                  <br />
                  {
                    !buildTimestamp
                    ||
                    (
                      <Basic.Div>
                        <big>
                          { this.i18n('app.version.releaseDate') }
                          :
                          <Advanced.DateValue
                            value={ buildTimestamp }
                            title={ `${ this.i18n('entity.Module.buildNumber') }: ${ buildNumber }` } />
                        </big>
                      </Basic.Div>
                    )
                  }
                  <a
                    href={ this.i18n('app.author.homePage') }
                    target="_blank"
                    rel="noreferrer noopener">
                    {this.i18n('app.author.name')}
                  </a>
                  <br />
                  <big>
                    { this.i18n('content.about.sourceCodeOn') }
                    {' '}
                    <Basic.Link href="https://github.com/bcvsolutions/CzechIdMng" isExternal text="GitHub" />
                  </big>
                </div>
              </Basic.PanelBody>
            </Basic.Panel>
          </Basic.Col>
        </Basic.Row>
      </div>
    );
  }
}

About.propTypes = {
  version: PropTypes.string
};

function select(state) {
  return {
    i18nReady: state.config.get('i18nReady'),
    version: ConfigurationManager.getPublicValue(state, 'idm.pub.core.build.version'),
    buildNumber: ConfigurationManager.getPublicValue(state, 'idm.pub.core.build.buildNumber'),
    buildTimestamp: parseInt(ConfigurationManager.getPublicValue(state, 'idm.pub.core.build.buildTimestamp'), 10)
  };
}

export default connect(select)(About);
