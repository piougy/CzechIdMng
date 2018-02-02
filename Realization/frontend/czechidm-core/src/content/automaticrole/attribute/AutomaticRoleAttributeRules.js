import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
import * as Basic from '../../../components/basic';
import { AutomaticRoleAttributeManager, AutomaticRoleAttributeRuleManager } from '../../../redux';
import AutomaticRoleAttributeRuleTable from './AutomaticRoleAttributeRuleTable';

const manager = new AutomaticRoleAttributeManager();

/**
 * Automatic role detail, update automatic role isn't currently allowed
 *
 * @author Ondrej Kopr
 */
class AutomaticRoleAttributeRules extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    //
    this.automaticRoleAttributeRuleManager = new AutomaticRoleAttributeRuleManager();
  }

  getContentKey() {
    return 'content.automaticRoles.attribute';
  }

  componentDidMount() {
    const { entityId } = this.props.params;
    this.selectNavigationItems(['system', 'automatic-roles', 'automatic-role-attribute-rules']);

    this.getLogger().debug(`[TypeContent] loading entity detail [id:${entityId}]`);
    this.context.store.dispatch(manager.fetchEntity(entityId));
  }

  render() {
    const { entity} = this.props;
    return (
      <div>
        <Basic.ContentHeader style={{ marginBottom: 0 }}>
          {this.i18n('content.automaticRoles.attribute.rules.header')}
        </Basic.ContentHeader>
        <AutomaticRoleAttributeRuleTable
          manager={this.automaticRoleAttributeRuleManager}
          uiKey={entity ? entity.id : null}
          attributeId={entity ? entity.id : null} />

      </div>
    );
  }
}

AutomaticRoleAttributeRules.propTypes = {
  showLoading: PropTypes.bool
};
AutomaticRoleAttributeRules.defaultProps = {
};

function select(state, component) {
  const { entityId } = component.params;
  //
  return {
    entity: manager.getEntity(state, entityId),
    showLoading: manager.isShowLoading(state, null, entityId)
  };
}

export default connect(select)(AutomaticRoleAttributeRules);
