import React from 'react';
import classnames from 'classnames';
//
import { Advanced } from 'czechidm-core';

/**
 * Icon for PostgreSQL connector.
 *
 * @author Vít Švanda
 * @author Radek Tomiška
 * @since 10.7.0
 */
export default class PostgreSqlConnectorIcon extends Advanced.AbstractIcon {

  renderIcon() {
    const { iconSize } = this.props;

    return (
      <span className={
        classnames(
          'connector-icon',
          'postgresql-icon',
          { 'img-lg': iconSize === 'lg' },
          { 'img-sm': iconSize === 'sm' }
        )
      }/>
    );
  }
}
