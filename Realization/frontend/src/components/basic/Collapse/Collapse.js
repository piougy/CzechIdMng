'use strict';

import React, { PropTypes } from 'react';
import { Collapse } from 'react-bootstrap';
//
import AbstractComponent from '../AbstractComponent/AbstractComponent';
import Loading from '../Loading/Loading';

export default class BasicCollapse extends AbstractComponent {

  render() {
    const { rendered, showLoading, children, ...others } = this.props;
    if (!rendered) {
      return null;
    }

    return (
      <Collapse {...others}>
        {
          showLoading
          ?
          <Loading isStatic showLoading={true}/>
          :
          children
        }
      </Collapse>
    );
  }
}

BasicCollapse.propTypes = {
  ...AbstractComponent.propTypes,
  /**
   * If collapse children is shown
   */
  in: PropTypes.bool,

  /**
   * ... and other react bootstap collapse props
   */
}

BasicCollapse.defaultProps = {
  ...AbstractComponent.defaultProps,
  in: false
}
