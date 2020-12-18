import React from 'react';
import classnames from 'classnames';
//
import { Advanced } from 'czechidm-core';

/**
 * Icon for CSV connector.
 *
 * @author Vít Švanda
 * @author Radek Tomiška
 * @since 10.7.0
 */
export default class CsvConnectorIcon extends Advanced.AbstractIcon {

  renderIcon() {
    const { iconSize } = this.props;

    return (
      <span className={
        classnames(
          'connector-icon',
          'csv-icon',
          { 'img-lg': iconSize === 'lg' },
          { 'img-sm': iconSize === 'sm' }
        )
      }/>
    );
  }
}
