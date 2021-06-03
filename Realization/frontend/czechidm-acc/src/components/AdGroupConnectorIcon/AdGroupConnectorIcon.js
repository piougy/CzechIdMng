import React from 'react';
//
import { Advanced, Basic } from 'czechidm-core';

/**
 * Icon for AD group connector.
 *
 * @author Vít Švanda
 * @since 11.1.0
 */
export default class AdGroupConnectorIcon extends Advanced.AbstractIcon {

  getIcon() {
    return 'fa:key';
  }

  getLevel() {
    return 'warning';
  }

  renderIcon() {
    const { iconSize } = this.props;

    if (iconSize === 'sm') {
      return (
        <Basic.Icon style={{ marginTop: 1, minWidth: 25, height: 25 }} level={ this.getLevel() } iconSize={ iconSize } value={ this.getIcon() }/>
      );
    }
    if (iconSize === 'lg') {
      return (
        <Basic.Icon style={{ marginTop: 8, minWidth: 120, height: 100 }} level={ this.getLevel() } iconSize={ iconSize } value={ this.getIcon() }/>
      );
    }
    // default
    return (
      <Basic.Icon level={ this.getLevel() } value={ this.getIcon() }/>
    );
  }
}
