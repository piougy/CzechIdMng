import React from 'react';
//
import { Advanced, Basic } from 'czechidm-core';

/**
 * Icon for PostgreSQL connector.
 *
 * @author Vít Švanda
 * @since 10.7.0
 */
export default class PostgreSqlConnectorIcon extends Advanced.AbstractIcon {

  renderIcon() {
    const {iconStyle} = this.props;
    if (iconStyle === 'sm') {
      return (
        <Basic.Div>
          <img style={{maxWidth: 40}} src={'dist/images/postgresql.png'}/>
        </Basic.Div>
      );
    }
    return (
      <Basic.Div>
        <img style={{maxWidth: 100}} src={'dist/images/postgresql.png'}/>
      </Basic.Div>
    );
  }
}
