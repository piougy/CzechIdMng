import React from 'react';
import PropTypes from 'prop-types';
import classnames from 'classnames';
//
import AbstractComponent from '../AbstractComponent/AbstractComponent';
import Icon from '../Icon/Icon';

/**
 * Page header
 *
 * @author Radek Tomiška
 */
class PageHeader extends AbstractComponent {

  render() {
    const {
      rendered,
      showLoading,
      icon,
      children,
      className,
      text,
      ...others
    } = this.props;
    //
    if (!rendered) {
      return null;
    }
    //
    const classNames = classnames(
      'page-header',
      className
    );
    return (
      <div className={ classNames } { ...others }>
        <h1>
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
            </div>
          }
        </h1>
      </div>
    );
  }
}

PageHeader.propTypes = {
  ...AbstractComponent.propTypes,
  /**
   * Table Header
   */
  text: PropTypes.oneOfType([PropTypes.string, PropTypes.element]),
  /**
   * Header left icon
   */
  icon: PropTypes.string
};

PageHeader.defaultProps = {
  ...AbstractComponent.defaultProps
};

export default PageHeader;
