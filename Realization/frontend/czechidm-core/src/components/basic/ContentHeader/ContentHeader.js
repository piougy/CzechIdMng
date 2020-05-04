import React from 'react';
import PropTypes from 'prop-types';
import classnames from 'classnames';
//
import AbstractComponent from '../AbstractComponent/AbstractComponent';
import Icon from '../Icon/Icon';
import HelpIcon from '../HelpIcon/HelpIcon';

/**
 * Content header
 *
 * @author Radek Tomiška
 */
class ContentHeader extends AbstractComponent {

  render() {
    const { rendered, showLoading, children, className, help, text, icon, buttons, ...others } = this.props;
    if (!rendered) {
      return null;
    }
    if (!text && !children) {
      return null;
    }

    const classNames = classnames(
      'content-header',
      className
    );
    //
    return (
      <div className={ classNames } { ...others }>
        <h2>
          {
            showLoading
            ?
            <Icon type="fa" icon="refresh" showLoading className="icon-loading"/>
            :
            <div style={{ display: 'flex', alignItems: 'center' }}>
              <Icon value={ icon } style={{ marginRight: 7 }}/>
              <div style={{ flex: 1 }}>
                { text }
                { children }
              </div>
              <div style={{ fontSize: '0.7em' }}>
                { buttons }
              </div>
              <HelpIcon content={ help }/>
            </div>
          }
        </h2>
      </div>
    );
  }
}

ContentHeader.propTypes = {
  ...AbstractComponent.propTypes,
  /**
   * help content
   * @type {[type]}
   */
  help: PropTypes.string
};

ContentHeader.defaultProps = {
  ...AbstractComponent.defaultProps
};

export default ContentHeader;
