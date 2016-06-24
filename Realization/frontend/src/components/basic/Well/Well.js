'use strict';

import React, { PropTypes } from 'react';
import classnames from 'classnames';
//
import AbstractComponent from '../AbstractComponent/AbstractComponent';
import Icon from '../Icon/Icon';

/**
 * Bootstrap row
 */
class Well extends AbstractComponent {

  constructor(props) {
    super(props);
  }

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
          <Icon type="fa" icon="refresh" showLoading={true}/>
          :
          children
        }
      </div>
    );
  }
}

Well.propTypes = {
  ...AbstractComponent.propTypes
}

Well.defaultProps = {
  ...AbstractComponent.defaultProps
}

export default Well;
