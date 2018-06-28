import React from 'react';
//
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import { IdentityManager } from '../../redux';

const uiKey = 'eav-identity';
const manager = new IdentityManager();

/**
 * Extended identity attributes
 *
 * @author Radek Tomiška
 */
export default class IdentityEav extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
  }

  getContentKey() {
    return 'content.identity.eav';
  }

  getNavigationKey() {
    return 'profile-eav';
  }

  render() {
    const { entityId } = this.props.params;
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
