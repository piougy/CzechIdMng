import React from 'react';
import PropTypes from 'prop-types';
//
import AbstractComponent from '../AbstractComponent/AbstractComponent';
import Loading from '../Loading/Loading';

/**
 * Basic div decorator (supports rendered and showLoading properties).
 *
 * @author Vít Švanda
 * @author Radek Tomiška
 */
export default class Div extends AbstractComponent {

  render() {
    const {
      className,
      rendered,
      showLoading,
      showAnimation,
      style,
      title
    } = this.props;
    //
    if (!rendered) {
      return null;
    }
    //
    return (
      <Loading
        showLoading={ showLoading }
        showAnimation={ showAnimation }
        style={ style }
        containerClassName={ className }
        containerTitle={ title }>
        { this.props.children }
      </Loading>
    );
  }
}

Div.propTypes = {
  ...AbstractComponent.propTypes,
  /**
   * When loading is visible, then show animation too.
   */
  showAnimation: PropTypes.bool,
};
Div.defaultProps = {
  ...AbstractComponent.defaultProps,
  showAnimation: true
};
