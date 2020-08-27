import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import moment from 'moment';
//
import { Advanced, Managers } from 'czechidm-core';

/**
 * Info card for echo items stored in cache
 *
 * @author Ondrej Kopr
 */
export class EchoItemInfo extends Advanced.AbstractEntityInfo {

  getManager() {
    // Echo item hasn't manager yet. Echos are stored in cache
    return null;
  }

  showLink() {
    if (!super.showLink()) {
      return false;
    }
    if (!Managers.SecurityManager.hasAccess({ type: 'HAS_ANY_AUTHORITY', authorities: ['ACCOUNT_READ'] })) {
      return false;
    }
    return true;
  }

  getNiceLabel() {
    return this.i18n('acc:entity.Account.echo.label');
  }

  /**
   * Get link to detail (`url`).
   *
   * @return {string}
   */
  getLink() {
    return null;
  }

  /**
   * Returns entity icon (null by default - icon will not be rendered)
   *
   * @param  {object} entity
   */
  getEntityIcon() {
    return 'fa:assistive-listening-systems';
  }

  /**
   * Returns popovers title
   *
   * @param  {object} entity
   */
  getPopoverTitle() {
    return this.i18n('acc:entity.Account.echo.label');
  }

  /**
   * Returns popover info content
   *
   * @param  {array} table data
   */
  getPopoverContent(entity) {
    //
    return [
      {
        label: this.i18n('acc:entity.Account.echo.changed.label'),
        value: this.i18n(`acc:entity.Account.echo.changed.${entity.changed}`)
      },
      {
        label: this.i18n('acc:entity.Account.echo.changeDate'),
        value: entity.changeDate ? moment(entity.changeDate).format(this.i18n('format.datetime')) : null
      },
      {
        label: this.i18n('acc:entity.Account.echo.validityChecked.label'),
        value: this.i18n(`acc:entity.Account.echo.validityChecked.${entity.validityChecked}`)
      },
      {
        label: this.i18n('acc:entity.Account.echo.validateDate'),
        value: entity.validateDate ? moment(entity.validateDate).format(this.i18n('format.datetime')) : null
      }
    ];
  }
}

EchoItemInfo.propTypes = {
  ...Advanced.AbstractEntityInfo.propTypes,
  /**
   * Selected entity - has higher priority.
   */
  entity: PropTypes.object
};

EchoItemInfo.defaultProps = {
  ...Advanced.AbstractEntityInfo.defaultProps,
  entity: null,
  face: 'popover',
};

function select() {
}

export default connect(select)(EchoItemInfo);
