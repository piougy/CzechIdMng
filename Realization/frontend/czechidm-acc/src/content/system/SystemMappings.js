import PropTypes from 'prop-types';
import React from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
//
import uuid from 'uuid';
import { Basic, Advanced, Domain, Managers, Utils } from 'czechidm-core';
import { SystemMappingManager, SystemManager } from '../../redux';
import SystemEntityTypeEnum from '../../domain/SystemEntityTypeEnum';
import SystemOperationTypeEnum from '../../domain/SystemOperationTypeEnum';

const uiKey = 'system-mappings-table';
const manager = new SystemMappingManager();
const systemManager = new SystemManager();

/**
 * System mapping list.
 *
 * @author Vít Švanda
 */
class SystemMappings extends Advanced.AbstractTableContent {

  getManager() {
    return manager;
  }

  getUiKey() {
    return uiKey;
  }

  getContentKey() {
    return 'acc:content.system.mappings';
  }

  getNavigationKey() {
    return 'system-mappings';
  }

  showDetail(entity, add) {
    const systemId = entity._embedded && entity._embedded.system ? entity._embedded.system.id : this.props.match.params.entityId;
    if (add) {
      // When we add new object class, then we need use "new" url
      const uuidId = uuid.v1();
      if (this.isWizard()) {
        const activeStep = this.context.wizardContext.activeStep;
        if (activeStep) {
          activeStep.id = '/system/:entityId/mappings/:mappingId/new';
          this.context.wizardContext.wizardForceUpdate();
        }
      } else {
        this.context.history.push(`/system/${systemId}/mappings/${uuidId}/new?new=1`);
      }
    } else if (this.isWizard()) {
      const activeStep = this.context.wizardContext.activeStep;
      if (activeStep) {
        activeStep.id = '/system/:entityId/mappings/:mappingId/detail';
        activeStep.mapping = entity;
        this.context.wizardContext.wizardForceUpdate();
      }
    } else {
      this.context.history.push(`/system/${systemId}/mappings/${entity.id}/detail`);
    }
  }

  render() {
    const { entityId } = this.props.match.params;
    const forceSearchParameters = new Domain.SearchParameters().setFilter('systemId', entityId);
    //
    return (
      <div>
        <Helmet title={ this.i18n('title') } />
        <Basic.Confirm ref="confirm-delete" level="danger"/>

        <Basic.ContentHeader text={ this.i18n('header', { escape: false }) } style={{ marginBottom: 0 }}/>

        <Advanced.Table
          ref="table"
          uiKey={ uiKey }
          manager={ this.getManager() }
          forceSearchParameters={ forceSearchParameters }
          showRowSelection={ Managers.SecurityManager.hasAnyAuthority(['SYSTEM_UPDATE']) }
          className="no-margin"
          buttons={
            [
              <Basic.Button
                level="success"
                key="add_button"
                className="btn-xs"
                onClick={ this.showDetail.bind(this, { }, true) }
                rendered={ Managers.SecurityManager.hasAnyAuthority(['SYSTEM_UPDATE']) }
                icon="fa:plus">
                { this.i18n('button.add') }
              </Basic.Button>
            ]
          }>
          <Advanced.Column
            property=""
            header=""
            className="detail-button"
            cell={
              ({ rowIndex, data }) => {
                return (
                  <Advanced.DetailButton
                    title={ this.i18n('button.detail') }
                    onClick={ this.showDetail.bind(this, data[rowIndex], false) }/>
                );
              }
            }/>
          <Advanced.Column
            property="operationType"
            width={ 100 }
            face="enum"
            enumClass={ SystemOperationTypeEnum }
            header={ this.i18n('acc:entity.SystemMapping.operationType') }
            sort/>
          <Advanced.ColumnLink
            to={ `/system/${entityId}/mappings/:id/detail` }
            property="name"
            face="text"
            header={ this.i18n('acc:entity.SystemMapping.name') }
            sort/>
          <Advanced.Column
            property="_embedded.objectClass.objectClassName"
            face="text"
            header={ this.i18n('acc:entity.SystemMapping.objectClass') }
            sort/>
          <Advanced.Column
            property="entityType"
            face="enum"
            enumClass={ SystemEntityTypeEnum }
            header={ this.i18n('acc:entity.SystemMapping.entityType') }
            sort/>
        </Advanced.Table>
      </div>
    );
  }
}

SystemMappings.propTypes = {
  system: PropTypes.object,
  _showLoading: PropTypes.bool,
};
SystemMappings.defaultProps = {
  system: null,
  _showLoading: false,
};

function select(state, component) {
  return {
    system: Utils.Entity.getEntity(state, systemManager.getEntityType(), component.match.params.entityId),
    _showLoading: Utils.Ui.isShowLoading(state, `${uiKey}-detail`),
  };
}

export default connect(select)(SystemMappings);
