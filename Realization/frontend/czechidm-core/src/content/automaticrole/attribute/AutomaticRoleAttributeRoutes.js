import React from 'react';
import { connect } from 'react-redux';
//
import * as Basic from '../../../components/basic';
import * as Advanced from '../../../components/advanced';
import AutomaticRoleAttributeDetail from './AutomaticRoleAttributeDetail';
import { AutomaticRoleAttributeManager } from '../../../redux';

const manager = new AutomaticRoleAttributeManager();

/**
 * Default content (routes diff) for automatic roles attribue
 *
 * @author Adamec Petr
 * @author Radek Tomiška
 */
class AutomaticRoleAttributeRoutes extends Basic.AbstractContent {

  getContentKey() {
    return 'content.automaticRoles';
  }

  componentDidMount() {
    const { entityId } = this.props.params;
    //
    if (!this._getIsNew()) {
      this.context.store.dispatch(manager.fetchEntityIfNeeded(entityId));
    }
  }

  /**
   * Method check if exist params new
   */
  _getIsNew() {
    const { query } = this.props.location;
    if (query) {
      return query.new ? true : false;
    }
    return false;
  }

  render() {
    const { entity} = this.props;
    //
    return (
      <div>
        <Basic.PageHeader>
          <Basic.Icon value="fa:universal-access"/> {this.i18n('content.automaticRoles.attribute.header')}
        </Basic.PageHeader>

        <Basic.Alert
          level="warning"
          text={ this.i18n('entity.AutomaticRole.attribute.concept.help') }
          rendered={ entity && entity.concept }/>
        <Advanced.TabPanel position="left" parentId="automatic-role-attribute" params={this.props.params}>
          {this.props.children}
        </Advanced.TabPanel>
      </div>
    );
  }
}

AutomaticRoleAttributeRoutes.propTypes = {
};
AutomaticRoleAttributeRoutes.defaultProps = {
};

function select(state, component) {
  const { entityId } = component.params;
  //
  return {
    entity: manager.getEntity(state, entityId)
  };
}

export default connect(select)(AutomaticRoleAttributeRoutes);
