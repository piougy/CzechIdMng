import React from 'react';
//
import AbstractComponent from '../AbstractComponent/AbstractComponent';
import Loading from '../Loading/Loading';

/**
 * Basic div decorator (supports rendered and showLoading properties)
 *
 * @author Vít Švanda
 */
export default class Div extends AbstractComponent {

  constructor(props) {
    super(props);
  }

  render() {
    const { className, rendered, showLoading, style } = this.props;
    if (!rendered) {
      return null;
    }
    return (
      <div className={className} style={style}>
        <Loading showLoading={showLoading}>
          {this.props.children}
        </Loading>
      </div>
    );
  }
}

Div.propTypes = {
  ...AbstractComponent.propTypes
};
