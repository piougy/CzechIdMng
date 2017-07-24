import React, { PropTypes } from 'react';
import classnames from 'classnames';
import { connect } from 'react-redux';
import { Link } from 'react-router';
//
import * as Basic from '../../basic';
import { IdentityContractManager, SecurityManager, IdentityManager, TreeTypeManager} from '../../../redux/';
import UuidInfo from '../UuidInfo/UuidInfo';
import AbstractEntityInfo from '../EntityInfo/AbstractEntityInfo';

const manager = new IdentityContractManager();
const identityManager = new IdentityManager();
const treeTypeManager = new TreeTypeManager();


/**
 * Component for rendering nice identifier for identity contracts, similar function as roleInfo
 *
 * @author Ondrej Kopr
 * @author Radek Tomiška
 */
export class IdentityContractInfo extends AbstractEntityInfo {

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
    if (!SecurityManager.hasAccess({ 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['IDENTITYCONTRACT_READ']})) {
      return false;
    }
    return true;
  }

  getEntityId() {
    const { entityIdentifier, entity } = this.props;
    // id has higher priority
    if (entityIdentifier) {
      return entityIdentifier;
    }
    if (entity) {
      return entity.id;
    }
    return null;
  }

  getEntity() {
    const { entity, _entity } = this.props;
    //
    if (entity) { // entity is given by props
      return entity;
    }
    return _entity; // loaded by redux
  }

  _renderFull() {
    const { className, style, entityIdentifier } = this.props;
    const _entity = this.getEntity();
    //
    const panelClassNames = classnames(
      'identity-info',
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
            <div>{treeTypeManager.getNiceLabel(_entity._embedded.workPosition._embedded.treeType)}</div>
            <div>{identityManager.getNiceLabel(_entity._embedded.identity)}</div>
              <div><i>{_entity.disabled ? this.i18n('component.advanced.IdentityContractInfo.disabledInfo') : null}</i></div>
              {
                !this.showLink()
                ||
                <div>
                  <Link to={`/identity/${_entity._embedded.identity.username}/identity-contract/${entityIdentifier}/detail`}>
                    <Basic.Icon value="fa:angle-double-right"/>
                    {' '}
                    {this.i18n('component.advanced.IdentityContractInfo.profileLink')}
                  </Link>
                </div>
              }
            </div>
          </Basic.Row>
        </Basic.PanelHeader>
      </Basic.Panel>
    );
  }

  render() {
    const { rendered, showLoading, className, entity, entityIdentifier, _showLoading, style, showIdentity, face } = this.props;
    //
    if (!rendered) {
      return null;
    }
    let _entity = this.props._entity;
    if (entity) { // entity prop has higher priority
      _entity = entity;
    }
    //
    let username = null;
    if (_entity !== null) {
      username = _entity._embedded.identity.username;
    }
    //
    const classNames = classnames(
      'identity-contract-info',
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
      return (<UuidInfo className={ classNames } value={ entityIdentifier } Identitystyle={style}/>);
    }
    //
    if (!this.showLink()) {
      return (
        <span className={ classNames }>{ manager.getNiceLabel(_entity, showIdentity) }</span>
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
          <Link className={ classNames } to={`/identity/${username}/identity-contract/${entityIdentifier}/detail`}>{manager.getNiceLabel(_entity.identity, showIdentity)}</Link>
        );
      }
      case 'popover': {
        return (
          <Basic.Popover
            trigger="click"
            value={this._renderFull()}
            className="identity-info-popover">
            {
              <span
                className={ classNames }
                style={ style }>
                <a href="#" onClick={ (e) => e.preventDefault() }>{ manager.getNiceLabel( _entity ) }</a>
              </span>
            }
          </Basic.Popover>
        );
      }
      default: {
        return this._renderFull();
      }
    }
  }
}

IdentityContractInfo.propTypes = {
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
   * Show contract's identity
   */
  showIdentity: PropTypes.bool,
  /**
   * Internal entity loaded by given identifier
   */
  _entity: PropTypes.object,
  _showLoading: PropTypes.bool,
  /**
   * Show how to open this link
   */
  face: PropTypes.string
};
IdentityContractInfo.defaultProps = {
  ...AbstractEntityInfo.defaultProps,
  entity: null,
  face: 'link',
  _showLoading: true,
  showIdentity: true,
};

function select(state, component) {
  return {
    _entity: manager.getEntity(state, component.entityIdentifier),
    _showLoading: manager.isShowLoading(state, null, component.entityIdentifier)
  };
}
export default connect(select)(IdentityContractInfo);
