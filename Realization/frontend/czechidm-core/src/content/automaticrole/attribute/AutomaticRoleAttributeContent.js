import React from 'react';
import { connect } from 'react-redux';
import Helmet from 'react-helmet';
//
import * as Basic from '../../../components/basic';
import { AutomaticRoleAttributeManager } from '../../../redux';
import AutomaticRoleAttributeDetail from './AutomaticRoleAttributeDetail';

const manager = new AutomaticRoleAttributeManager();

/**
 * Automatic role detail, update automatic role isn't currently allowed
 *
 * @author Ondrej Kopr
 */
class AutomaticRoleAttributeContent extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
  }

  getContentKey() {
    return 'content.automaticRoles.attribute';
  }

  componentDidMount() {
    super.componentDidMount();
    const { automaticRoleId, entityId } = this.props.params;
    if (entityId) { // We are on the role
      this.selectNavigationItems(['roles-menu', 'roles', 'role-automatic-roles', 'role-automatic-role-attribute', 'role-automatic-role-attribute-detail']);
    } else {
      this.selectNavigationItems(['roles-menu', 'automatic-roles', 'automatic-role-attribute-detail']);
    }

    if (this._getIsNew()) {
      this.context.store.dispatch(manager.receiveEntity(automaticRoleId, { }));
    } else {
      this.getLogger().debug(`[TypeContent] loading entity detail [id:${automaticRoleId}]`);
      this.context.store.dispatch(manager.fetchEntity(automaticRoleId));
    }
  }

  /**
   * Function check if exist params new
   */
  _getIsNew() {
    const { query } = this.props.location;
    return (query) ? query.new : null;
  }

  render() {
    const { entity} = this.props;
    const { entityId } = this.props.params;
    return (
      <div className={entityId ? 'panel-body' : ''}>
        {
          this._getIsNew()
          ?
          <Helmet title={this.i18n('create.title')} />
          :
          <Helmet title={this.i18n('edit.title')} />
        }

        <AutomaticRoleAttributeDetail entity={entity} manager={manager} />

      </div>
    );
  }
}

AutomaticRoleAttributeContent.propTypes = {
};
AutomaticRoleAttributeContent.defaultProps = {
};

function select(state, component) {
  const { automaticRoleId } = component.params;
  //
  return {
    entity: manager.getEntity(state, automaticRoleId)
  };
}

export default connect(select)(AutomaticRoleAttributeContent);
