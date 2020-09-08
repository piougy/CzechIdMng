import React from 'react';
import PropTypes from 'prop-types';
import classnames from 'classnames';
import _ from 'lodash';
//
import AbstractComponent from '../AbstractComponent/AbstractComponent';
import ComponentService from '../../../services/ComponentService';

const componentService = new ComponentService();
//
export const TYPE_GLYPHICON = 'glyph';
export const TYPE_FONT_AWESOME = 'fa'; // https://fortawesome.github.io/Font-Awesome/examples/
export const TYPE_COMPONENT = 'component';

/**
 * Icon
 * - it's a little advanced icon now (component usage)
 *
 * TODO: use FontAwesomeIcon for the fa type
 * TODO: fas, fab, far
 *
 * @author Radek Tomiška
 */
export default class Icon extends AbstractComponent {

  /**
   * Returns resolved type and icon from given parameters
   *
   * @param  {string} type  icon type
   * @param  {string} icon  requested icon - could contain type definition e.g. `fa:group`
   * @param  {string} value parameter icon alias
   * @return {{_type, _icon}}  object represents resolved type and icon
   */
  resolveParams(type, icon, value) {
    // value could contain type definition
    //
    const _iconValue = icon || value;
    if (!_iconValue) {
      return {};
    }
    const _iconValues = _iconValue.split(':');
    let _type = type;
    let _icon = _iconValue;
    if (_iconValues.length === 2) {
      _type = _iconValues[0];
      _icon = _iconValues[1];
    }
    return {
      _type,
      _icon
    };
  }

  render() {
    const {
      rendered,
      type,
      icon,
      value,
      ...other
    } = this.props;
    //
    if (!rendered) {
      return null;
    }
    //
    // value could contain type definition
    const { _type, _icon } = this.resolveParams(type, icon, value);
    if (_type === TYPE_COMPONENT) {
      const component = componentService.getIconComponent(_icon);
      if (component) {
        const IconComponent = component.component;
        if (_.isString(IconComponent)) {
          // recursive - basic icon
          return (
            <Icon value={ IconComponent } { ...other } />
          );
        }
        return (
          <IconComponent { ...other }/>
        );
      }
      return (
        <span title="Icon not found in component library">
          {' '}
          { _icon }
          {' '}
        </span>
      );
    }
    // Basic icon will be rendered
    const {
      className,
      showLoading,
      color,
      style,
      disabled,
      title,
      onClick,
      level
    } = this.props;
    //
    // without icon defined returns null
    if (!_icon) {
      return null;
    }
    //
    let classNames = classnames(
      `icon-${ level }`,
    );
    if (showLoading) {
      classNames = classnames(
        classNames,
        'fa',
        'fa-refresh',
        'fa-spin',
        className
      );
    } else {
      classNames = classnames(
        classNames,
        { glyphicon: _type === TYPE_GLYPHICON },
        { [`glyphicon-${ _icon}`]: _type === TYPE_GLYPHICON },
        { fa: _type === TYPE_FONT_AWESOME },
        { [`fa-${ _icon}`]: _type === TYPE_FONT_AWESOME },
        { disabled: disabled === true },
        className,
      );
    }
    const _style = _.merge({}, style);
    if (color) {
      _style.color = color;
    }
    return (
      <span
        title={ title }
        className={ classNames }
        aria-hidden="true"
        style={ _style }
        onClick={ onClick } />
    );
  }
}

Icon.propTypes = {
  ...AbstractComponent.propTypes,
  /**
   * glyphicon or font-awesome, default glyph
   */
  type: PropTypes.oneOf([TYPE_GLYPHICON, TYPE_FONT_AWESOME]),
  /**
   * glyphicon or font-awesome (by type) suffix name
   */
  icon: PropTypes.oneOfType([
    PropTypes.string,
    PropTypes.bool // false => no icon
  ]),
  /**
   * glyphicon or font-awesome (by type) suffix name - alias to icon property, has lower priority
   */
  value: PropTypes.string,
  /**
   * css only
   */
  disabled: PropTypes.bool,
  /**
   * Standard onClick callback
   */
  onClick: PropTypes.func,
  /**
   * Icon level (~color) / css / class
   */
  level: PropTypes.oneOf(['default', 'success', 'warning', 'info', 'danger', 'error', 'primary'])
};

Icon.defaultProps = {
  ...AbstractComponent.defaultProps,
  type: TYPE_GLYPHICON,
  dibaled: false,
  level: 'default'
};
