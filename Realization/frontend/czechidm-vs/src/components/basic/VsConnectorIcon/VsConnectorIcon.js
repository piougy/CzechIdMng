import React from 'react';
//
import { Advanced, Basic } from 'czechidm-core';

/**
 * Icon for VS connector.
 *
 * @author Vít Švanda
 * @author Radek Tomiška
 * @since 10.7.0
 */
export default class VsConnectorIcon extends Advanced.AbstractIcon {

  renderIcon() {
    const { iconSize } = this.props;

    if (iconSize === 'sm') {
      return (
        <Basic.Icon color="#008AFF" style={{ marginTop: 1, minWidth: 25, height: 25 }} iconSize={ iconSize } value="link"/>
      );
    }

    if (iconSize === 'lg') {
      return (
        <Basic.Icon color="#008AFF" style={{ marginTop: 8, minWidth: 120, height: 100 }} iconSize={ iconSize } value="link"/>
      );
    }
    // default
    return (
      <Basic.Icon color="#008AFF" iconSize={ iconSize } value="link"/>
    );
  }
}
