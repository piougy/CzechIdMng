import React, { PropTypes } from 'react';
import classnames from 'classnames';
//
import AbstractComponent from '../AbstractComponent/AbstractComponent';
import Icon from '../Icon/Icon';

/**
 * Label box
 */
class Label extends AbstractComponent {

  constructor(props) {
    super(props);
  }

  _onClose(event) {
    const { onClose } = this.props;
    if (!onClose) {
      return;
    }
    onClose(event);
  }

  render() {
    const { level, title, text, className, icon, rendered, showLoading, ...others } = this.props;
    if (!rendered || !text) {
      return null;
    }
    const classNames = classnames(
      'label',
      'label-' + (level === 'error' ? 'danger' : level),
      className
    );
    return (
      <span className={classNames} {...others}>
        {
          showLoading
          ?
          <Icon type="fa" icon="refresh" showLoading/>
          :
          text
        }
      </span>
    );
  }
}

Label.propTypes = {
  ...AbstractComponent.propTypes,
  /**
   * Label level / css / class
   */
  level: PropTypes.oneOf(['default', 'success', 'warning', 'info', 'danger', 'error', 'primary']),
  /**
   * Label text content
   */
  text: PropTypes.string
};

Label.defaultProps = {
  ...AbstractComponent.defaultProps,
  level: 'default'
};

export default Label;
