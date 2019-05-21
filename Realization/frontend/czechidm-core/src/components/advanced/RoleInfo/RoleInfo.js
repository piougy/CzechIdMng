import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
//
import * as Basic from '../../basic';
import { RoleManager } from '../../../redux/';
import AbstractEntityInfo from '../EntityInfo/AbstractEntityInfo';
import RolePriorityEnum from '../../../enums/RolePriorityEnum';
import Tree from '../Tree/Tree';
import CodeListValue from '../CodeListValue/CodeListValue';

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

  getNiceLabel(entity) {
    const { showEnvironment, showCode } = this.props;
    //
    return this.getManager().getNiceLabel(entity, showEnvironment, showCode);
  }

  showLink() {
    if (!super.showLink()) {
      return false;
    }
    //
    // evaluate authorization policies
    const { _permissions } = this.props;
    if (!manager.canRead(this.getEntity(), _permissions)) {
      return false;
    }
    return true;
  }

  /**
   * Get link to detail (`url`).
   *
   * @return {string}
   */
  getLink(entity) {
    return `/role/${encodeURIComponent(this.getEntityId(entity))}/detail`;
  }

  /**
   * Returns entity icon (null by default - icon will not be rendered)
   *
   * @param  {object} entity
   */
  getEntityIcon(entity) {
    if (entity && entity.childrenCount > 0) {
      return 'component:business-role';
    }
    return 'component:role';
  }

  /**
   * Returns popovers title
   *
   * @param  {object} entity
   */
  getPopoverTitle() {
    return this.i18n('entity.Role._type');
  }

  getTableChildren() {
    // component are used in #getPopoverContent => skip default column resolving
    return [
      <Basic.Column property="label"/>,
      <Basic.Column property="value"/>
    ];
  }

  _renderIcon() {
    return null;
  }

  _renderPopover() {
    const _entity = this.getEntity();
    //
    return (
      <Tree
        uiKey={ `role-info-${ this.getEntityId() }` }
        manager={ this.getManager() }
        roots={[ _entity ]}
        header={ null }
        className="role-info-tree"
        bodyClassName="role-info-tree-body"
        onChange={ () => false }
        nodeIcon={ ({ node }) => this.props.showIcon ? this.getEntityIcon(node) : null }
        nodeStyle={{ paddingLeft: 0 }}
        nodeIconClassName={ null }
        nodeContent={ ({ node }) => {
          // FIXME: maxWidth + inline-block for IE - find a way, how to fix overflowX
          // TODO: maxWidth configurable
          return (
            <span style={{ whiteSpace: 'normal', maxWidth: 350, display: 'inline-block' }}>{ super._renderPopover(node) }</span>
          );
        }}
        />
    );
  }

  /**
   * Returns popover info content
   *
   * @param  {array} table data
   */
  getPopoverContent(entity) {
    const content = [
      {
        label: this.i18n('entity.name'),
        value: entity.name
      },
      {
        label: this.i18n('entity.Role.code.label'),
        value: entity.baseCode
      }
    ];
    if (entity.environment) {
      content.push({
        label: this.i18n('entity.Role.environment.label'),
        value: (<CodeListValue code="environment" value={ entity.environment }/>)
      });
    }
    //
    content.push({
      label: this.i18n('entity.Role.priorityEnum'),
      value: (<Basic.EnumValue enum={ RolePriorityEnum } value={ RolePriorityEnum.findKeyBySymbol(RolePriorityEnum.getKeyByPriority(entity.priority)) } />)
    });
    //
    if (entity.description) {
      content.push({
        label: this.i18n('entity.Role.description'),
        value: (
          <Basic.ShortText value={ entity.description } maxLength={ 100 }/>
        )
      });
    }
    //
    return content;
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
  //
  _showLoading: PropTypes.bool,
  _permissions: PropTypes.oneOfType([
    PropTypes.bool,
    PropTypes.arrayOf(PropTypes.string)
  ])
};
RoleInfo.defaultProps = {
  ...AbstractEntityInfo.defaultProps,
  entity: null,
  face: 'link',
  _showLoading: true,
};

function select(state, component) {
  const { entityIdentifier, entity } = component;
  let entityId = entityIdentifier;
  if (!entityId && entity) {
    entityId = entity.id;
  }
  return {
    _entity: manager.getEntity(state, entityId),
    _showLoading: manager.isShowLoading(state, null, entityId),
    _permissions: manager.getPermissions(state, null, entityId)
  };
}
export default connect(select)(RoleInfo);
