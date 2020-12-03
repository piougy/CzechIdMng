import React from 'react';
//
import { Advanced, Basic } from 'czechidm-core';

/**
 * Icon for VS connector.
 *
 * @author Vít Švanda
 * @since 10.7.0
 */
export default class VsConnectorIcon extends Advanced.AbstractIcon {

  renderIcon() {
    const {iconStyle} = this.props;
    if (iconStyle === 'sm') {
      return (
        <Basic.Div>
          <Basic.Icon style={{color: '#008AFF', marginTop: '1px', minWidth: '25px', height: '25px'}} className="fa-2x" value="link"/>
        </Basic.Div>
      );
    }
    return (
      <Basic.Div>
        <Basic.Icon style={{color: '#008AFF', marginTop: '8px', minWidth: '120px', height: '100px'}} className="fa-6x" value="link"/>
      </Basic.Div>
    );
  }
}
