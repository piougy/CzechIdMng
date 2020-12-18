import React from 'react';
//
import { Advanced, Basic } from 'czechidm-core';

/**
 * Icon for LDAP connector.
 *
 * @author Vít Švanda
 * @author Radek Tomiška
 * @since 10.7.0
 */
export default class LdapConnectorIcon extends Advanced.AbstractIcon {

  renderIcon() {
    const { iconSize } = this.props;

    if (iconSize === 'sm') {
      return (
        <Basic.Icon style={{ marginTop: 1, minWidth: 25, height: 25 }} level="primary" iconSize={ iconSize } type="fa" value="users-cog"/>
      );
    }
    if (iconSize === 'lg') {
      return (
        <Basic.Icon style={{ marginTop: 8, minWidth: 120, height: 100 }} level="primary" iconSize={ iconSize } type="fa" value="users-cog"/>
      );
    }
    // default
    return (
      <Basic.Icon level="primary" value="fa:users-cog" iconSize={ iconSize }/>
    );
  }
}
