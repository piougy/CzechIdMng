import React from 'react';
//
import * as Basic from '../../../components/basic';
import { AutomaticRoleAttributeManager } from '../../../redux';
import AutomaticRoleAttributeTable from './AutomaticRoleAttributeTable';

/**
 * List of automatic roles by tree nodes
 *
 * @author Ondrej Kopr
 */
export default class AutomaticRoleAttributes extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.manager = new AutomaticRoleAttributeManager();
  }

  getContentKey() {
    return 'content.automaticRoles';
  }

  componentDidMount() {
    this.selectNavigationItems(['system', 'automatic-roles', 'automatic-role-attribute']);
  }

  render() {
    return (
      <div>
        <AutomaticRoleAttributeTable uiKey="automatic-role-attribute-table" manager={this.manager}/>
      </div>
    );
  }
}

AutomaticRoleAttributes.propTypes = {
};
AutomaticRoleAttributes.defaultProps = {
};
