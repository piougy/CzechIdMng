import React from 'react';
import PropTypes from 'prop-types';
import classnames from 'classnames';
//
import AbstractComponent from '../AbstractComponent/AbstractComponent';
import Icon from '../Icon/Icon';

/**
 * Badge box.
 *
 * @author Radek Tomiška
 */
class Badge extends AbstractComponent {

  _onClose(event) {
    const { onClose } = this.props;
    if (!onClose) {
      return;
    }
    onClose(event);
  }

  render() {
    const { level, title, text, value, className, rendered, showLoading, style, ...others } = this.props;
    const _text = text || value;
    if (!rendered || !_text) {
      return null;
    }
    const classNames = classnames(
      'badge',
      `badge-${ (level === 'error' ? 'danger' : level) }`,
      className
    );
    //
    let _style = style;
    if (others.onClick) {
      _style = {
        cursor: 'pointer',
        ...style
      };
    }
    return (
      <span
        className={ classNames }
        title={ title }
        style={ _style }
        { ...others }>
        {
          showLoading
          ?
          <Icon type="fa" icon="refresh" showLoading/>
          :
          _text
        }
      </span>
    );
  }
}

Badge.propTypes = {
  ...AbstractComponent.propTypes,
  /**
   * Badge level / css / class
   */
  level: PropTypes.oneOf(['default', 'success', 'warning', 'info', 'danger', 'error']),
  /**
   * Badge text content
   */
  text: PropTypes.oneOfType([
    PropTypes.string,
    PropTypes.node,
    PropTypes.number
  ]),
  /**
   * Badge text content (text alias - text has higher priority)
   */
  value: PropTypes.oneOfType([
    PropTypes.string,
    PropTypes.node,
    PropTypes.number
  ])
};

Badge.defaultProps = {
  ...AbstractComponent.defaultProps,
  level: 'default'
};

export default Badge;
