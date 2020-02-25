import React from 'react';
import PropTypes from 'prop-types';
//
import * as Basic from '../../basic';
import AuditableInfo from '../EntityInfo/AuditableInfo';
import CloseButton from '../Button/CloseButton';

/**
 * Detail header with title, system information and close button.
 *
 * @author Radek Tomiška
 * @since 10.2.0
 */
export default class DetailHeader extends Basic.AbstractContextComponent {

  getComponentKey() {
    return 'component.advanced.DetailHeader';
  }

  render() {
    const {
      rendered,
      showLoading,
      icon,
      entity,
      to,
      children
    } = this.props;
    //
    if (!rendered) {
      return null;
    }
    //
    return (
      <Basic.PageHeader
        icon={ icon }
        showLoading={ showLoading }>
        <Basic.Div style={{ display: 'flex', alignItems: 'center' }}>
          <Basic.Div style={{ flex: 1 }}>
            { children }
          </Basic.Div>
          <Basic.Div style={{ fontSize: '0.85em' }}>
            <AuditableInfo entity={ entity }/>
            <CloseButton to={ to } />
          </Basic.Div>
        </Basic.Div>
      </Basic.PageHeader>
    );
  }
}

DetailHeader.propTypes = {
  // ...Basic.AbstractContextComponent.propTypes,
  to: PropTypes.string
};

DetailHeader.defaultProps = {
  ...Basic.AbstractContextComponent.defaultProps
};
