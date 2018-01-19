import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
import Helmet from 'react-helmet';
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
    const { entityId } = this.props.params;
    this.selectNavigationItems(['system', 'automatic-roles', 'automatic-role-attribute-detail']);

    if (this._getIsNew()) {
      this.context.store.dispatch(manager.receiveEntity(entityId, { }));
    } else {
      this.getLogger().debug(`[TypeContent] loading entity detail [id:${entityId}]`);
      this.context.store.dispatch(manager.fetchEntity(entityId));
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
    const { entity, entityId } = this.props;
    return (
      <div>
        {
          this._getIsNew()
          ?
          <Helmet title={this.i18n('create.title')} />
          :
          <Helmet title={this.i18n('edit.title')} />
        }

        <AutomaticRoleAttributeDetail entity={entity} entityId={entityId} manager={manager} />

      </div>
    );
  }
}

AutomaticRoleAttributeContent.propTypes = {
  showLoading: PropTypes.bool
};
AutomaticRoleAttributeContent.defaultProps = {
};

function select(state, component) {
  const { entityId } = component.params;
  //
  return {
    entity: manager.getEntity(state, entityId),
    showLoading: manager.isShowLoading(state, null, entityId)
  };
}

export default connect(select)(AutomaticRoleAttributeContent);
