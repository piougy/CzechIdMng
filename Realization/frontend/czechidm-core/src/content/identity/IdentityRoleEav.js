import React from 'react';
//
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import { IdentityRoleManager } from '../../redux';

const uiKey = 'eav-identity-role';
const manager = new IdentityRoleManager();

/**
 * Extended identity role attributes
 *
 * @author Vít Švanda
 */
export default class IdentityRoleEav extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
  }

  getContentKey() {
    return 'content.identityRole.eav';
  }

  getNavigationKey() {
    return 'profile-eav';
  }

  render() {
    const { entityId } = this.props;
    //
    return (
      <Advanced.EavContent
        uiKey={ uiKey }
        formableManager={ manager }
        entityId={ entityId }
        contentKey={ this.getContentKey() }
        showSaveButton />
    );
  }
}
