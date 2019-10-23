import React from 'react';
//
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import { IdentityManager } from '../../redux';

const manager = new IdentityManager();

/**
 * Extended identity attributes
 *
 * @author Radek Tomiška
 */
export default class IdentityEav extends Basic.AbstractContent {

  getContentKey() {
    return 'content.identity.eav';
  }

  getNavigationKey() {
    return 'profile-eav';
  }

  render() {
    const { entityId } = this.props.match.params;
    //
    return (
      <Advanced.EavContent
        formableManager={ manager }
        entityId={ entityId }
        contentKey={ this.getContentKey() }
        showSaveButton />
    );
  }
}
