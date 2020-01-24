import React from 'react';
import PropTypes from 'prop-types';
import { Tab, Tabs } from 'react-bootstrap';
import classnames from 'classnames';
import _ from 'lodash';
//
import AbstractComponent from '../AbstractComponent/AbstractComponent';

/**
 * Wrapped bootstrap Tabbs
 * - adds default styles
 * - adds rendered supported
 *
 * @author Radek Tomiška
 */
export default class BasicTabs extends AbstractComponent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      activeKey: 1
    };
  }

  _getRenderedChildren(children) {
    return children.filter(child => {
      return child.props.rendered;
    });
  }

  /**
   * Default method handle a activeKey.
   */
  _onChangeSelectTabs(activeKey) {
    this.setState({
      activeKey
    });
  }

  render() {
    const { id, rendered, position, activeKey, onSelect, className, style, unmountOnExit } = this.props;
    if (!rendered) {
      return null;
    }

    // Since our all componnets are React.PureComponent
    // must be activeKey handle in all causes (if onSelect component is not used).
    const _activeKey = !onSelect ? (activeKey || this.state.activeKey) : undefined;

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
      <Tabs
        id={ _id }
        key={ _activeKey }
        animation={false}
        unmountOnExit={unmountOnExit}
        onSelect={ onSelect || this._onChangeSelectTabs.bind(this)}
        activeKey={ onSelect ? activeKey : _activeKey }
        className={ classNames }
        style={ style }>
        { this._getRenderedChildren(this.props.children) }
      </Tabs>
    );
  }
}

BasicTabs.propTypes = {
  rendered: PropTypes.bool
};

BasicTabs.defaultProps = {
  rendered: true
};

/**
 * Adds rendered to react bootstrap Tab.
 */
export class BasicTab extends AbstractComponent {

  render() {
    const { rendered, ...others } = this.props;
    if (!rendered) {
      return null;
    }

    return (
      <Tab {...others} />
    );
  }
}

BasicTab.propTypes = {
  rendered: PropTypes.bool
};

BasicTab.defaultProps = {
  rendered: true
};

BasicTabs.Tab = BasicTab;
