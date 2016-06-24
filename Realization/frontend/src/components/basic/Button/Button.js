'use strict';

import React, { Component, PropTypes } from 'react';
import { Link }  from 'react-router';
import classnames from 'classnames';
//
import AbstractComponent from '../AbstractComponent/AbstractComponent';
import Icon from '../Icon/Icon';
import Tooltip from '../Tooltip/Tooltip';

/**
 * TODO: Improvent:
 * - add icon
 * - add button size (className is abused now)
 */
class Button extends AbstractComponent {

  constructor(props) {
    super(props);
  }

  render() {
    const {
      level,
      text,
      className,
      children,
      showLoading,
      showLoadingIcon,
      showLoadingText,
      disabled,
      hidden,
      type,
      rendered,
      title,
      titlePlacement,
       ...others
    } = this.props;
    //
    if (!rendered) {
      return null;
    }

    const classNames = classnames(
      'btn',
      'btn-' + level,
      { 'hidden': hidden },
      className
    );
    let _showLoadingText = children;
    if (showLoadingText !== null) {
      _showLoadingText = showLoadingText;
    }
    //
    return (
      <Tooltip placement={titlePlacement} value={title}>
        <button
          type={type ? type : 'button'}
          disabled={disabled || showLoading}
          className={classNames} {...others}>
          {
            showLoading
            ?
            <span>
              {
                showLoadingIcon
                ?
                <Icon type="fa" icon="refresh" showLoading={true}/>
                :
                null
              }
              {
                showLoadingIcon && _showLoadingText
                ?
                '\u00a0'
                :
                null
              }
              {_showLoadingText}
            </span>
            :
            children
          }
        </button>
      </Tooltip>
    );
  }
}

Button.propTypes = {
  ...AbstractComponent.propTypes,
  /**
   * Button level / css / class
   */
  level: PropTypes.oneOf(['default', 'success', 'warning', 'info', 'danger', 'link', 'primary']),
  /**
   * When showLoading is true, then showLoadingIcon is shown
   */
  showLoadingIcon: PropTypes.bool,
  /**
   *  When showLoading is true, this text will be shown
   */
  showLoadingText: PropTypes.string,
  /**
   * Help icon title position
   */
  titlePlacement: PropTypes.oneOf(['top', 'bottom', 'right', 'left'])
};
Button.defaultProps = {
  ...AbstractComponent.defaultProps,
  type: 'button',
  level : 'default',
  hidden : false,
  showLoadingIcon: false,
  showLoadingText: null,
  titlePlacement: 'right'
}


export default Button;
