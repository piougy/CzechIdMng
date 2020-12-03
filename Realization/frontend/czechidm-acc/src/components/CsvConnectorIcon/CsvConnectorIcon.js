import React from 'react';
//
import { Advanced, Basic } from 'czechidm-core';

/**
 * Icon for CSV connector.
 *
 * @author Vít Švanda
 * @since 10.7.0
 */
export default class CsvConnectorIcon extends Advanced.AbstractIcon {

  renderIcon() {
    const {iconStyle} = this.props;
    if (iconStyle === 'sm') {
      return (
        <Basic.Div>
          <img style={{maxWidth: 35}} src={'dist/images/csv.png'}/>
        </Basic.Div>
      );
    }
    return (
      <Basic.Div>
        <img style={{maxWidth: 100}} src={'dist/images/csv.png'}/>
      </Basic.Div>
    );
  }
}
