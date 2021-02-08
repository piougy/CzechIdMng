import React from 'react';
import PropTypes from 'prop-types';
//
import { Advanced, Basic, Domain, Managers, Utils } from 'czechidm-core';
import { SystemManager } from '../../redux';
import SystemTable from '../system/SystemTable';

const systemManager = new SystemManager();

/**
* Available Connectors.
*
* @author Radek Tomiška
* @since 10.8.0
*/
export default class ConnectorTable extends Advanced.AbstractTableContent {

  constructor(props, context) {
    super(props, context);
    //
    this.state = {
      detail: {
        show: false,
        entity: {}
      }
    };
  }

  getContentKey() {
    return 'acc:content.connector';
  }

  /**
   * Shows modal detail with given entity
   */
  showDetail(entity) {
    this.setState({
      detail: {
        show: true,
        entity
      }
    });
  }

  /**
   * Close modal detail
   */
  closeDetail() {
    const { detail } = this.state;
    //
    this.setState({
      detail: {
        show: false,
        entity: {}
      }
    }, () => {
      this.context.store.dispatch(systemManager.setSearchParameters(null, `${ this.getUiKey() }-${ Utils.Ui.spinalCase(detail.entity.id) }-systems`));
    });
  }

  render() {
    const { showLoading, connectorFrameworks, className, defaultSearchParameters } = this.props;
    const { detail} = this.state;
    const availableConnectors = [];
    const forceSearchParameters = new Domain.SearchParameters()
      .setFilter('connectorFramework', detail.entity.framework)
      .setFilter('connectorName', detail.entity.connectorName)
      .setFilter('connectorBundleName', detail.entity.bundleName)
      .setFilter('connectorVersion', detail.entity.version);
    //
    if (connectorFrameworks) {
      connectorFrameworks.forEach((connectors, framework) => {
        connectors.forEach((connector, fullName) => {
          availableConnectors.push({
            id: fullName,
            connectorDisplayName: connector.connectorDisplayName,
            connectorName: connector.connectorKey.connectorName,
            framework,
            version: connector.connectorKey.bundleVersion,
            bundleName: connector.connectorKey.bundleName,
            fullName
          });
        });
      });
    }
    //
    return (
      <Basic.Div>
        <Basic.Table
          showLoading={ showLoading }
          data={ availableConnectors }
          className={ className }
          uiKey={ this.getUiKey() }>
          <Basic.Column
            header=""
            className="detail-button"
            cell={
              ({ rowIndex, data }) => {
                return (
                  <Advanced.DetailButton
                    title={ this.i18n('button.detail') }
                    onClick={ this.showDetail.bind(this, data[rowIndex]) }/>
                );
              }
            }/>
          <Basic.Column
            property="connectorDisplayName"
            header={ this.i18n('acc:entity.System.connectorKey.connectorDisplayName.label') }/>
          <Basic.Column
            property="version"
            header={ this.i18n('acc:entity.System.connectorKey.bundleVersion') }/>
          <Basic.Column
            property="framework"
            header={ this.i18n('acc:entity.System.connectorKey.framework') }/>
        </Basic.Table>

        <Basic.Modal
          bsSize="large"
          show={ detail.show }
          onHide={ this.closeDetail.bind(this) }
          backdrop="static">
          <Basic.Modal.Header
            closeButton
            icon="component:default-connector"
            text={ this.i18n('detail.header', { record: detail.entity.connectorDisplayName, version: detail.entity.version, escape: false }) }/>
          <Basic.Modal.Body>
            <Basic.Tabs>
              <Basic.Tab
                eventKey={ 1 }
                title={ this.i18n('detail.tabs.basic.title') }
                style={{ padding: 15 }}>
                <form>
                  <Basic.AbstractForm
                    ref="form"
                    data={ detail.entity }
                    readOnly>
                    <Basic.TextField
                      ref="connectorDisplayName"
                      label={ this.i18n('acc:entity.System.connectorKey.connectorDisplayName.label') }/>
                    <Basic.TextField
                      ref="version"
                      label={ this.i18n('acc:entity.System.connectorKey.bundleVersion') }/>
                    <Basic.TextField
                      ref="framework"
                      label={ this.i18n('acc:entity.System.connectorKey.framework') }/>
                    <Basic.TextField
                      ref="connectorName"
                      label={ this.i18n('acc:entity.System.connectorKey.connectorName') }/>
                    <Basic.TextField
                      ref="bundleName"
                      label={ this.i18n('acc:entity.System.connectorKey.bundleName') }/>
                    <Basic.TextField
                      ref="fullName"
                      label={ this.i18n('acc:entity.System.connectorKey.fullName.label') }/>
                  </Basic.AbstractForm>
                </form>
              </Basic.Tab>

              <Basic.Tab
                eventKey={ 2 }
                title={ this.i18n('detail.tabs.systems.title') }
                rendered={ Managers.SecurityManager.hasAnyAuthority(['SYSTEM_READ']) }>

                <SystemTable
                  columns={ ['name', 'description', 'state', 'blockedOperation'] }
                  showAddButton={ false }
                  showRowSelection
                  uiKey={ `${ this.getUiKey() }-${ Utils.Ui.spinalCase(detail.entity.id) }-systems` }
                  showFilterVirtual={ false }
                  filterOpened
                  forceSearchParameters={ forceSearchParameters }
                  defaultSearchParameters={ defaultSearchParameters }
                  match={ this.props.match }
                  className="no-margin"
                  rendered={ Utils.Ui.isNotEmpty(detail.entity.id) }/>
              </Basic.Tab>
            </Basic.Tabs>


          </Basic.Modal.Body>

          <Basic.Modal.Footer>
            <Basic.Button
              level="link"
              onClick={ this.closeDetail.bind(this) }>
              { this.i18n('button.close') }
            </Basic.Button>
          </Basic.Modal.Footer>
        </Basic.Modal>
      </Basic.Div>
    );
  }
}

ConnectorTable.propTypes = {
  uiKey: PropTypes.string.isRequired,
  connectorFrameworks: PropTypes.arrayOf(PropTypes.object)
};
