import PropTypes from 'prop-types';
import { connect } from 'react-redux';
//
import { Enums, Managers, Advanced } from 'czechidm-core';
import {ReportManager} from '../../redux';

const manager = new ReportManager();

/**
 * Report info card
 *
 * @author Vít Švanda
 */
export class ReportInfo extends Advanced.AbstractEntityInfo {

  getManager() {
    return manager;
  }

  showLink() {
    if (!super.showLink()) {
      return false;
    }
    if (!Managers.SecurityManager.hasAccess({ type: 'HAS_ANY_AUTHORITY', authorities: ['REPORT_READ'] })) {
      return false;
    }
    return true;
  }

  /**
   * Get link to detail (`url`).
   *
   * @return {string}
   */
  getLink() {
    const entity = this.getEntity();
    return `/rpt/reports/${encodeURIComponent(entity.id)}`;
  }

  /**
   * Returns popover info content
   *
   * @param  {array} table data
   */
  getPopoverContent(entity) {
    return [
      {
        label: this.i18n('rpt:entity.Report.executorName.label'),
        value: entity.executorName
      },
      {
        label: this.i18n('rpt:entity.Report.result.state'),
        value: entity.result && entity.result.state
          ? Enums.OperationStateEnum.getNiceLabel(entity.result.state) : null
      }
    ];
  }

  /**
   * Returns entity icon (null by default - icon will not be rendered)
   *
   * @param  {object} entity
   */
  getEntityIcon() {
    return 'component:report';
  }
}

ReportInfo.propTypes = {
  ...Advanced.AbstractEntityInfo.propTypes,
  /**
   * Selected entity - has higher priority
   */
  entity: PropTypes.object,
  /**
   * Selected entity's id - entity will be loaded automatically
   */
  entityIdentifier: PropTypes.string,
  /**
   * Internal entity loaded by given identifier
   */
  _entity: PropTypes.object,
  _showLoading: PropTypes.bool
};
ReportInfo.defaultProps = {
  ...Advanced.AbstractEntityInfo.defaultProps,
  entity: null,
  showLink: true,
  face: 'link',
  _showLoading: true,
};

function select(state, component) {
  return {
    _entity: manager.getEntity(state, component.entityIdentifier),
    _showLoading: manager.isShowLoading(state, null, component.entityIdentifier)
  };
}
export default connect(select)(ReportInfo);
