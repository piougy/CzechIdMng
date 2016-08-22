import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
import moment from 'moment';
//
import * as Basic from '../components/basic';
import packageInfo from '../../package.json';

/**
 * Idm footer
 */
class Footer extends Basic.AbstractContextComponent {

  constructor(props, context) {
    super(props, context);
  }

  /**
   * Jump to page top
   */
  jumpTop() {
    $('html, body').animate({
      scrollTop: 0
    }, 'fast');
  }

  render() {
    const { backendVersion } = this.props;

    return (
      <footer>
        <div className="pull-left">
          {/* TODO: about page - #74 */}
          <span title={this.i18n('app.version.backend') + ': ' + backendVersion} className="hidden">
            {this.i18n('app.version.frontend')} {packageInfo.version}
          </span>
          <span style={{margin: '0 10px'}} className="hidden">|</span>
          &copy; { moment(new Date()).format('YYYY') } &nbsp;&nbsp;
          <a href="http://www.bcvsolutions.eu" target="_blank">{this.i18n('app.author')}</a>
          <span style={{margin: '0 10px'}}>|</span>
          <a href="http://redmine.czechidm.com/projects/czechidmng" target="_blank">HelpDesk</a>
        </div>
        <div className="pull-right">
          <Basic.Button type="button" className="btn-xs" aria-label="Left Align"
                  onClick={this.jumpTop.bind(this)}>
            <Basic.Icon icon="chevron-up"/>
          </Basic.Button>
        </div>
        <div className="clearfix"></div>
      </footer>
    );
  }
}

Footer.propTypes = {
  backendVersion: PropTypes.string
};

Footer.defaultProps = {
  backendVersion: null
};

// Which props do we want to inject, given the global state?
// Note: use https://github.com/faassen/reselect for better performance.
function select() {
  return {
    backendVersion: 'x.x.x'// settingManager.getValue(state, 'environment.version')
  };
}

// Wrap the component to inject dispatch and state into it
export default connect(select)(Footer);
