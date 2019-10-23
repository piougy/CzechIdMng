import React from 'react';
//
import * as Basic from '../../../components/basic';
import * as Advanced from '../../../components/advanced';
import { TreeNodeManager, SecurityManager } from '../../../redux';

const manager = new TreeNodeManager();

/**
 * Extended tree node attributes
 *
 * @author Radek Tomiška
 */
export default class NodeEav extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
  }

  getContentKey() {
    return 'content.tree.node.eav';
  }

  getNavigationKey() {
    return 'tree-node-eav';
  }

  render() {
    const { entityId } = this.props.match.params;
    //
    return (
      <Advanced.EavContent
        formableManager={manager}
        entityId={entityId}
        contentKey={this.getContentKey()}
        showSaveButton={SecurityManager.hasAuthority('TREENODE_UPDATE')}/>
    );
  }
}
