import React from 'react';
//
import AbstractComponent from '../AbstractComponent/AbstractComponent';
import Loading from '../Loading/Loading';

/**
 * Basic div decorator (supports rendered and showLoading properties)
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
      <div className={ className } style={ style } title={ title }>
        <Loading showLoading={ showLoading } showAnimation={ showAnimation }>
          { this.props.children }
        </Loading>
      </div>
    );
  }
}

Div.propTypes = {
  ...AbstractComponent.propTypes
};
