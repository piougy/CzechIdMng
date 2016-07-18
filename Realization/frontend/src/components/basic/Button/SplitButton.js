

import React, { Component, PropTypes } from 'react';
import { SplitButton } from 'react-bootstrap';
import AbstractComponent from '../AbstractComponent/AbstractComponent'
import classnames from 'classnames';


class BasicSplitButton extends AbstractComponent {

  constructor(props) {
    super(props);
  }

  render() {
    const { level, rendered, showLoading, disabled, title, ...others } = this.props;
    if (!rendered) {
      return null;
    }
    const _title = title || ''; // title is required for SplitButton ... why?
    return (
      <SplitButton bsStyle={level} disabled={disabled || showLoading} title={_title} {...others}>
        {this.props.children}
      </SplitButton>
    );
  }
}

BasicSplitButton.propTypes = {
  ...AbstractComponent.propTypes,
  level: PropTypes.oneOf(['default', 'success', 'warning', 'info', 'danger', 'link']),
};
BasicSplitButton.defaultProps = {
  ...AbstractComponent.defaultProps,
  level : 'default'
}


export default BasicSplitButton;
