import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
import Helmet from 'react-helmet';
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
    const { entity, showLoading } = this.props;
    return (
      <div>
        QWDW
        <Helmet title={this.i18n('edit.title')} />

        <Basic.Panel showLoading={showLoading}>
          <Basic.PanelHeader text={this.i18n('content.automaticRoles.attribute.rules.header')}/>
            <AutomaticRoleAttributeRuleTable
              manager={this.automaticRoleAttributeRuleManager}
              uiKey={entity ? entity.id : null}
              attributeId={entity ? entity.id : null} />
        </Basic.Panel>

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
