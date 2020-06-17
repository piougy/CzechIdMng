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
      back,
      children,
      buttons,
      small,
      additionalOptions
    } = this.props;
    //
    if (!rendered) {
      return null;
    }
    const HeaderComponent = small ? Basic.ContentHeader : Basic.PageHeader;
    //
    return (
      <HeaderComponent
        icon={ icon }
        showLoading={ showLoading }>
        <Basic.Div style={{ display: 'flex', alignItems: 'center' }}>
          <Basic.Div style={{ flex: 1 }}>
            { children }
          </Basic.Div>
          <Basic.Div style={{ fontSize: '0.85em' }}>
            { buttons }
            <AuditableInfo entity={ entity } additionalOptions={additionalOptions}/>
            <CloseButton to={ back } />
          </Basic.Div>
        </Basic.Div>
      </HeaderComponent>
    );
  }
}

DetailHeader.propTypes = {
  // ...Basic.AbstractContextComponent.propTypes, FIXME: showLoading in page header works, but test is broken, why?
  /**
   * Entity for show system information.
   */
  entity: PropTypes.object,
  /**
   * Header left icon
   */
  icon: PropTypes.string,
  /**
   * Close button path. Close button will not be shown, if empty.
   */
  back: PropTypes.string,
  /**
   * Additional Buttons are shown before close detail button.
   */
  buttons: PropTypes.arrayOf(PropTypes.element),
  /**
   * True - page header, false - content header.
   *
   * @since 10.3.0
   */
  small: PropTypes.bool
};

DetailHeader.defaultProps = {
  ...Basic.AbstractContextComponent.defaultProps,
  buttons: [],
  small: false
};
