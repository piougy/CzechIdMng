import React from 'react';
//
import * as Basic from '../../../components/basic';
import { RoleTreeNodeManager } from '../../../redux';
import RoleTreeNodeTable from '../../role/RoleTreeNodeTable';

/**
 * List of automatic roles by tree node
 *
 * @author Ondrej Kopr
 */
export default class AutomaticRoleTrees extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.manager = new RoleTreeNodeManager();
  }

  getContentKey() {
    return 'content.automaticRoles';
  }

  componentDidMount() {
    this.selectNavigationItems(['system', 'automatic-roles', 'automatic-role-tree']);
  }

  render() {
    return (
      <div>
        <RoleTreeNodeTable uiKey="automatic-role-tree-table" manager={this.manager} forceSearchParameters={this.manager.getDefaultSearchParameters()}/>
      </div>
    );
  }
}

AutomaticRoleTrees.propTypes = {
};
AutomaticRoleTrees.defaultProps = {
};
