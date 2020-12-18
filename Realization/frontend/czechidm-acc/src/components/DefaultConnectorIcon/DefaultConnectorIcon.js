import React from 'react';
import classnames from 'classnames';
//
import { Advanced } from 'czechidm-core';

/**
 * Icon for default connector.
 *
 * @author Vít Švanda
 * @author Radek Tomiška
 * @since 10.7.0
 */
export default class DefaultConnectorIcon extends Advanced.AbstractIcon {

  renderIcon() {
    const { iconSize } = this.props;

    return (
      <span className={
        classnames(
          'connector-icon',
          'default-icon',
          { 'img-lg': iconSize === 'lg' },
          { 'img-sm': iconSize === 'sm' }
        )
      }/>
    );
  }
}
