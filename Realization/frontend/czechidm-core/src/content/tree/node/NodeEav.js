import React from 'react';
//
import * as Basic from '../../../components/basic';
import * as Advanced from '../../../components/advanced';
import { TreeNodeManager, SecurityManager } from '../../../redux';

const uiKey = 'eav-tree-node';
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
    const { entityId } = this.props.params;
    //
    return (
      <Advanced.EavContent
        uiKey={uiKey}
        formableManager={manager}
        entityId={entityId}
        contentKey={this.getContentKey()}
        showSaveButton={SecurityManager.hasAuthority('TREENODE_WRITE')}/>
    );
  }
}
