import React, { PropTypes } from 'react';
import classnames from 'classnames';
import { connect } from 'react-redux';
import { Link } from 'react-router';
//
import { Basic, Advanced, Managers } from 'czechidm-core';
import { SystemManager } from '../../redux';

const manager = new SystemManager();

/**
 * Target system basic information (info card)
 *
 * @author Radek Tomiška
 */
export class SystemInfo extends Advanced.AbstractEntityInfo {

  constructor(props, context) {
    super(props, context);
  }

  getManager() {
    return manager;
  }

  showLink() {
    if (!super.showLink()) {
      return false;
    }
    if (!Managers.SecurityManager.hasAccess({ 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['SYSTEM_READ']})) {
      return false;
    }
    return true;
  }

  /**
   * TODO: implement different face
   */
  render() {
    const { rendered, showLoading, className, entity, entityIdentifier, _showLoading, style } = this.props;
    //
    if (!rendered) {
      return null;
    }
    let _entity = this.props._entity;
    if (entity) { // entity prop has higher priority
      _entity = entity;
    }
    //
    const classNames = classnames(
      'system-info',
      className
    );
    if (showLoading || (_showLoading && entityIdentifier && !_entity)) {
      return (
        <Basic.Icon className={ classNames } value="refresh" showLoading style={style}/>
      );
    }
    if (!_entity) {
      if (!entityIdentifier) {
        return null;
      }
      return (<Advanced.UuidInfo className={ classNames } value={ entityIdentifier } style={style}/>);
    }
    //
    if (!this.showLink()) {
      return (
        <span className={ classNames }>{ manager.getNiceLabel(_entity) }</span>
      );
    }
    return (
      <Link className={ classNames } to={`/system/${entityIdentifier}/detail`}>{manager.getNiceLabel(_entity)}</Link>
    );
  }
}

SystemInfo.propTypes = {
  ...Advanced.AbstractEntityInfo.propTypes,
  /**
   * Selected entity - has higher priority
   */
  entity: PropTypes.object,
  /**
   * Selected entity's id - entity will be loaded automatically
   */
  entityIdentifier: PropTypes.string,
  /**
   * Internal entity loaded by given identifier
   */
  _entity: PropTypes.object,
  _showLoading: PropTypes.bool
};
SystemInfo.defaultProps = {
  ...Advanced.AbstractEntityInfo.defaultProps,
  entity: null,
  face: 'link',
  _showLoading: true,
};

function select(state, component) {
  return {
    _entity: manager.getEntity(state, component.entityIdentifier),
    _showLoading: manager.isShowLoading(state, null, component.entityIdentifier)
  };
}
export default connect(select)(SystemInfo);
