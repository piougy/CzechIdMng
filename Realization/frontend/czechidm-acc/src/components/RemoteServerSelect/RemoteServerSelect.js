import React from 'react';
//
import { Advanced } from 'czechidm-core';

import RemoteServerOptionDecorator from './RemoteServerOptionDecorator';
import RemoteServerValueDecorator from './RemoteServerValueDecorator';

/**
* Component for select remote server.
*
* @author Radek Tomiška
* @since 10.8.0
*/
export default class RemoteServerSelect extends Advanced.EntitySelectBox {

  render() {
    const { rendered, entityType, ...others } = this.props;
    //
    if (!rendered) {
      return null;
    }
    if (entityType && entityType !== 'remoteServer') {
      LOGGER.warn(`RemoteServerSelect supports remoteServer entity type only, given [${ entityType }] type will be ignored.`);
    }
    //
    return (
      <Advanced.EntitySelectBox
        ref="selectComponent"
        entityType="remoteServer"
        { ...others }/>
    );
  }
}

RemoteServerSelect.propTypes = {
  ...Advanced.EntitySelectBox.propTypes
};
RemoteServerSelect.defaultProps = {
  ...Advanced.EntitySelectBox.defaultProps,
  optionComponent: RemoteServerOptionDecorator,
  valueComponent: RemoteServerValueDecorator
};
