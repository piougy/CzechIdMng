import React from 'react';
import classnames from 'classnames';
//
import AbstractComponent from '../AbstractComponent/AbstractComponent';
import Icon from '../Icon/Icon';

/**
 * Bootstrap decorator.
 *
 * @author Radek Tomiška
 */
class Well extends AbstractComponent {

  render() {
    const { rendered, showLoading, children, className, ...others } = this.props;
    if (!rendered) {
      return null;
    }

    const classNames = classnames(
      'well',
      { 'text-center': showLoading },
      className
    );
    return (
      <div className={classNames} {...others}>
        {
          showLoading
          ?
          <Icon type="fa" icon="refresh" showLoading/>
          :
          children
        }
      </div>
    );
  }
}

Well.propTypes = {
  ...AbstractComponent.propTypes
};

Well.defaultProps = {
  ...AbstractComponent.defaultProps
};

export default Well;
