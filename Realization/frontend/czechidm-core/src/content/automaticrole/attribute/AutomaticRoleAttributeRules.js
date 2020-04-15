import PropTypes from 'prop-types';
import React from 'react';
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

  isEmbeddedDetail() {
    const { entityId } = this.props.match.params;
    //
    return !!entityId;
  }

  getNavigationKey() {
    if (this.isEmbeddedDetail()) {
      return 'role-automatic-role-attribute-rules';
    }
    return 'automatic-role-attribute-rules';
  }

  componentDidMount() {
    super.componentDidMount();
    //
    const { automaticRoleId } = this.props.match.params;
    this.getLogger().debug(`[TypeContent] loading entity detail [id:${ automaticRoleId }]`);
    this.context.store.dispatch(manager.fetchEntity(automaticRoleId));
  }

  render() {
    const { entity} = this.props;
    return (
      <div className="tab-pane-table-body">
        <Basic.ContentHeader style={{ marginBottom: 0 }}>
          { this.i18n('content.automaticRoles.attribute.rules.header') }
        </Basic.ContentHeader>
        <Basic.Panel className="no-border last">
          <AutomaticRoleAttributeRuleTable
            manager={ this.automaticRoleAttributeRuleManager }
            uiKey={ entity ? entity.id : null }
            attributeId={ entity ? entity.id : null }
            className={ !this.isEmbeddedDetail() ? 'no-margin' : '' }/>
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
  const { automaticRoleId } = component.match.params;
  //
  return {
    entity: manager.getEntity(state, automaticRoleId),
    showLoading: manager.isShowLoading(state, null, automaticRoleId)
  };
}

export default connect(select)(AutomaticRoleAttributeRules);
