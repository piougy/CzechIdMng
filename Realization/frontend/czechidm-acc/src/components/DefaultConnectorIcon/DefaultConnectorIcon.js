import React from 'react';
//
import { Advanced, Basic } from 'czechidm-core';

/**
 * Icon for default connector.
 *
 * @author Vít Švanda
 * @since 10.7.0
 */
export default class DefaultConnectorIcon extends Advanced.AbstractIcon {

  renderIcon() {
    const {iconStyle} = this.props;
    if (iconStyle === 'sm') {
      return (
        <Basic.Div>
          <img style={{maxWidth: 40}} src={'dist/images/default-connector.png'}/>
        </Basic.Div>
      );
    }
    return (
      <Basic.Div>
        <img style={{maxWidth: 80, marginTop: 30}} src={'dist/images/default-connector.png'}/>
      </Basic.Div>
    );
  }
}
