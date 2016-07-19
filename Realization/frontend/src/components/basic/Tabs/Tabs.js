

import React, { Component, PropTypes } from 'react';
import { Tab, Tabs } from 'react-bootstrap';
import classnames from 'classnames';
//
import AbstractComponent from '../AbstractComponent/AbstractComponent'

/**
 * Wrapped bootstrap Tabbs
 * - adds default styles
 *
 */
export default class BasicTabs extends AbstractComponent {

  render() {
    const { rendered, position, className, ...others } = this.props;
    if (!rendered) {
      return null;
    }

    const classNames = classnames(
      {'tab-horizontal' : !position || position === 'top'},
      {'tab-vertical' : position && position === 'left'},
      className
    );

    return (
      <Tabs position={position} className={classNames} {...others}>
        {this.props.children}
      </Tabs>
    );
  }
}

BasicTabs.Tab = Tab;
