import React from 'react';
//
import { Basic } from 'czechidm-core';
import ProvisioningOperations from '../provisioning/ProvisioningOperations';

export default class SystemProvisioningOparationContent extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
  }

  getContentKey() {
    return 'acc:content.provisioningOperations';
  }

  componentDidMount() {
    this.selectNavigationItems(['sys-systems', 'system-provisioning-operations']);
  }

  render() {
    return (
      <div>
        <Basic.ContentHeader>
          <span dangerouslySetInnerHTML={{ __html: this.i18n('header') }}/>
        </Basic.ContentHeader>

        <ProvisioningOperations systemId={this.props.params.entityId}/>
      </div>
    );
  }
}
