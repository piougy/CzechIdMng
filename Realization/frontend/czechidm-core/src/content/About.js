import React, { PropTypes } from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
//
// import * as Advanced from '../components/advanced';
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
    const { version } = this.props;
    // <big>{this.i18n('app.version.releaseDate')}: <Advanced.DateValue value={ buildTimestamp } title={ buildNumber }/></big>
    // <br />
    //
    return (
      <div>
        <Helmet title={this.i18n('content.about.title')} />

        <Basic.Row>
          <Basic.Col lg={ 4 } className="col-lg-offset-4">
            <Basic.Panel>
              <Basic.PanelHeader text={this.i18n('content.about.header')}/>
              <Basic.PanelBody className="text-center">
                <div className="about-logo">
                </div>
                <div className="about-text">
                  <big>{this.i18n('app.version.frontend')}: { version }</big>
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
    version: ConfigurationManager.getPublicValue(state, 'idm.pub.core.build.version'),
    buildNumber: ConfigurationManager.getPublicValue(state, 'idm.pub.core.build.buildNumber'),
    buildTimestamp: ConfigurationManager.getPublicValue(state, 'idm.pub.core.build.buildTimestamp')
    // TODO: timestamp - eclipse don't replace pom properties ...
  };
}

export default connect(select)(About);
