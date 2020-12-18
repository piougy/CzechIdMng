import React from 'react';
import PropTypes from 'prop-types';
import _ from 'lodash';
import classnames from 'classnames';
//
import * as Basic from '../../basic';

/**
 * Abstract icon - boiler plate for custom icon components - provide default implementation for the common props.
 *
 * @author Radek Tomiška
 * @since 9.4.0
 */
class AbstractIcon extends Basic.AbstractComponent {

  renderIcon() {
    throw new TypeError('Must override method renderIcon()');
  }

  /**
   * Icon class names, e.g. based by icon size.
   *
   * @return {object} classnames
   * @since 10.8.0
   */
  getClassName(additionalClassName = null) {
    const { className, iconSize, disabled } = this.props;
    //
    return classnames(
      { disabled: disabled === true },
      { 'fa-2x': iconSize === 'sm' },
      { 'fa-6x': iconSize === 'lg' },
      className,
      additionalClassName
    );
  }

  render() {
    const {
      rendered,
      showLoading,
      className,
      color,
      style,
      disabled,
      title,
      onClick,
      iconSize
    } = this.props;
    //
    if (!rendered) {
      return null;
    }
    const _style = _.merge({}, style);
    if (color) {
      _style.color = color;
    }
    if (showLoading) {
      return (
        <Basic.Icon
          value="fa:refresh"
          showLoading
          disabled={ disabled }
          className={ className }
          style={ _style }
          title={ title }
          iconSize={ iconSize }/>
      );
    }
    //
    const others = {
      onClick
    };
    //
    return (
      <span
        className={ classnames({ disabled: disabled === true }, className) }
        style={ _style }
        title={ title }
        { ...others }>
        { this.renderIcon() }
      </span>
    );
  }
}

AbstractIcon.propTypes = {
  ...Basic.AbstractComponent.propTypes,
  /**
   * On click icon callback
   */
  onClick: PropTypes.func,
  /**
   * Icon size.
   *
   * @since 10.8.0
   */
  iconSize: PropTypes.oneOf(['default', 'sm', 'lg'])
};
AbstractIcon.defaultProps = {
  ...Basic.AbstractComponent.defaultProps,
  iconSize: 'default'
};

export default AbstractIcon;
