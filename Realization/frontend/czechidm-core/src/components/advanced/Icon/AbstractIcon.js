import React from 'react';
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
export default class AbstractIcon extends Basic.AbstractComponent {

  renderIcon() {
    throw new TypeError('Must override method renderIcon()');
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
      onClick
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
          className={ classnames({ disabled: disabled === true }, className) }
          style={ _style }
          title={ title } />
      );
    }
    //
    return (
      <span
        className={ classnames({ disabled: disabled === true }, className) }
        style={ _style }
        title={ title }
        onClick={ onClick }>
        { this.renderIcon() }
      </span>
    );
  }
}
