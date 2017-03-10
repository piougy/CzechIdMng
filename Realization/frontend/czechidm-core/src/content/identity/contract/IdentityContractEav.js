import React from 'react';
//
import * as Basic from '../../../components/basic';
import * as Advanced from '../../../components/advanced';
import { IdentityContractManager, SecurityManager } from '../../../redux';

const uiKey = 'eav-identity-contract';
const manager = new IdentityContractManager();

/**
 * Extended identity contract's attributes
 *
 * @author Radek Tomiška
 */
export default class IdentityContractEav extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
  }

  componentDidMount() {
    this.selectSidebarItem('identity-contract-eav');
  }

  getContentKey() {
    return 'content.identity-contract.eav';
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
        showSaveButton={SecurityManager.hasAuthority('APP_ADMIN')}/>
    );
  }
}
