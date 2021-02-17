import React from 'react';
import PropTypes from 'prop-types';
import { SplitButton } from 'react-bootstrap';
import AbstractComponent from '../AbstractComponent/AbstractComponent';
import Icon from '../Icon/Icon';

/**
 * Quick continue button.
 *
 * @author Radek Tomiška
 */
class BasicSplitButton extends AbstractComponent {

  render() {
    const {
      level,
      buttonSize,
      icon,
      rendered,
      showLoading,
      showLoadingIcon,
      showLoadingText,
      disabled,
      title,
      ...others } = this.props;
    if (!rendered) {
      return null;
    }
    let _showLoadingText = title;
    if (showLoadingText) {
      _showLoadingText = showLoadingText;
    }
    const _title = (
      <span>
        {
          showLoading
          ?
          <span>
            {
              showLoadingIcon
              ?
              <Icon type="fa" icon="refresh" showLoading/>
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
            { _showLoadingText }
          </span>
          :
          <span>
            <Icon
              value={ icon }
              className="icon-left"
              style={ title ? { marginRight: 5 } : {} }/>
            { title }
          </span>
        }
      </span>
    );
    return (
      <SplitButton
        bsStyle={ level }
        bsSize={ buttonSize }
        disabled={ disabled || showLoading }
        title={ _title }
        { ...others }>
        { this.props.children }
      </SplitButton>
    );
  }
}

BasicSplitButton.propTypes = {
  ...AbstractComponent.propTypes,
  title: PropTypes.oneOfType([PropTypes.string, PropTypes.element]).isRequired,
  level: PropTypes.oneOf(['default', 'success', 'warning', 'info', 'danger', 'link']),
};
BasicSplitButton.defaultProps = {
  ...AbstractComponent.defaultProps,
  level: 'default'
};


export default BasicSplitButton;
