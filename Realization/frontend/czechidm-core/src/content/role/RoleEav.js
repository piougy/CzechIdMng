import React from 'react';
//
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import { RoleManager, SecurityManager } from '../../redux';

const uiKey = 'eav-role';
const manager = new RoleManager();

/**
 * Extended role attributes
 *
 * @author Radek Tomiška
 */
export default class RoleEav extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
  }

  getContentKey() {
    return 'content.role.eav';
  }

  getNavigationKey() {
    return 'role-eav';
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
        showSaveButton={SecurityManager.hasAuthority('ROLE_UPDATE')}/>
    );
  }
}
