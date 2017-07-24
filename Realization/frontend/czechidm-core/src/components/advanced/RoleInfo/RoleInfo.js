import React, { PropTypes } from 'react';
import classNames from 'classnames';
import { connect } from 'react-redux';
import { Link } from 'react-router';
//
import * as Basic from '../../basic';
import { RoleManager, SecurityManager } from '../../../redux/';
import UuidInfo from '../UuidInfo/UuidInfo';
import AbstractEntityInfo from '../EntityInfo/AbstractEntityInfo';

const manager = new RoleManager();

/**
 * Role basic information (info card)
 *
 * @author Radek Tomiška
 */
export class RoleInfo extends AbstractEntityInfo {

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
    if (!SecurityManager.hasAccess({ 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['ROLE_READ']})) { // TODO: asynchronous permissions by fetch autorities on selected entity
      return false;
    }
    return true;
  }

  _renderFull() {
    const { className, entity, style } = this.props;
    //
    let _entity = this.props._entity;
    if (entity) { // entity prop has higher priority
      _entity = entity;
    }
    //
    const panelClassNames = classNames(
      'abstract-entity-info',
      { 'panel-success': _entity && !_entity.disabled },
      { 'panel-warning': _entity && _entity.disabled },
      className
    );

    //
    return (
      <Basic.Panel className={panelClassNames} style={style}>
        <Basic.PanelHeader>
          <Basic.Row>
            <div className="col-lg-12">
              <div><strong>{manager.getNiceLabel(_entity)}</strong></div>
              <div>{_entity.roleType.toLowerCase()}</div>
              <div>{_entity.subRoles}</div>
              <div>{_entity.supRoles}</div>
              <div><i>{_entity.disabled ? this.i18n('component.advanced.RoleInfo.disabledInfo') : null}</i></div>
              {
              !this.showLink()
                ||
                <div>
                  <Link to={`/role/${encodeURIComponent(this.getEntityId())}/detail`}>
                    <Basic.Icon value="fa:angle-double-right"/>
                    {' '}
                    {this.i18n('component.advanced.RoleInfo.profileLink')}
                  </Link>
                </div>
              }
            </div>
          </Basic.Row>
        </Basic.PanelHeader>
      </Basic.Panel>
    );
  }


  /**
   * TODO: implement different face
   */
  render() {
    const { rendered, showLoading, className, entity, face, entityIdentifier, _showLoading, style } = this.props;
    //
    if (!rendered) {
      return null;
    }
    let _entity = this.props._entity;
    if (entity) { // entity prop has higher priority
      _entity = entity;
    }
    //
    if (showLoading || (_showLoading && this.getEntityId() && !_entity)) {
      switch (face) {
        case 'text':
        case 'link':
        case 'popover': {
          return (
            <Basic.Icon value="refresh" showLoading className={className} style={style}/>
          );
        }
        default: {
          return (
            <Basic.Well showLoading className={`abstract-entity-info ${className}`} style={style}/>
          );
        }
      }
    }
    if (!_entity) {
      if (!this.getEntityId()) {
        return null;
      }
      return (<UuidInfo className={className} value={ this.getEntityId() } style={style} />);
    }
    //
    if (showLoading || (_showLoading && entityIdentifier && !_entity)) {
      return (
        <Basic.Icon className={ classNames } value="refresh" showLoading style={style}/>
      );
    }

    switch (face) {
      case 'text':
      case 'link': {
        if (!this.showLink() || face === 'text') {
          return (
            <span className={className} style={style}>{ manager.getNiceLabel(_entity) }</span>
          );
        }
        return (
          <Link to={`/role/${encodeURIComponent(this.getEntityId())}/detail`}>{manager.getNiceLabel(_entity)}</Link>
        );
      }
      case 'popover': {
        return (
          <Basic.Popover
            trigger="click"
            value={this._renderFull()}
            className="abstract-entity-info-popover">
            {
              <span
                className={ classNames }
                style={ style }>
                <a href="#" onClick={ (e) => e.preventDefault() }>{ manager.getNiceLabel(_entity) }</a>
              </span>
            }
          </Basic.Popover>
        );
      }
      default: {
        return this._renderFull();
      }
    }

    if (!_entity) {
      if (!entityIdentifier) {
        return null;
      }
      return (<UuidInfo className={ classNames } value={ entityIdentifier } style={style}/>);
    }
    //
    if (!this.showLink()) {
      return (
        <span className={ classNames }>{ manager.getNiceLabel(_entity) }</span>
      );
    }
    return (
      <Link className={ classNames } to={`/role/${entityIdentifier}/detail`}>{manager.getNiceLabel(_entity)}</Link>
    );
  }
}

RoleInfo.propTypes = {
  ...AbstractEntityInfo.propTypes,
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
RoleInfo.defaultProps = {
  ...AbstractEntityInfo.defaultProps,
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
export default connect(select)(RoleInfo);
