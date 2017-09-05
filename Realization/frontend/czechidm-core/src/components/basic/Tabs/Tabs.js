import React from 'react';
import { Tab, Tabs } from 'react-bootstrap';
import classnames from 'classnames';
import _ from 'lodash';
//
import AbstractComponent from '../AbstractComponent/AbstractComponent';

/**
 * Wrapped bootstrap Tabbs
 * - adds default styles
 *
 */
export default class BasicTabs extends AbstractComponent {

  constructor(props) {
    super(props);
  }

  render() {
    const { id, rendered, position, activeKey, onSelect, className, ...others } = this.props;
    if (!rendered) {
      return null;
    }

    const classNames = classnames(
      {'tab-horizontal': !position || position === 'top'},
      {'tab-vertical': position && position === 'left'}, // TODO: not implemened
      className
    );
    //
    let _id = id;
    if (!_id) {
      _id = _.uniqueId('tooltip_');
    }
    //
    return (
      <Tabs id={_id} onSelect={onSelect} activeKey={activeKey} className={classNames}>
        {this.props.children}
      </Tabs>
    );
  }
}

BasicTabs.Tab = Tab;
