import React from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
import { Link } from 'react-router';
import _ from 'lodash';
//
import { Basic, Advanced, Managers } from 'czechidm-core';
import { ProvisioningOperationManager, ProvisioningArchiveManager } from '../../redux';
import ProvisioningOperationTable from './ProvisioningOperationTable';
import ProvisioningOperationTypeEnum from '../../domain/ProvisioningOperationTypeEnum';
import ProvisioningResultStateEnum from '../../domain/ProvisioningResultStateEnum';
import SchemaAttributeInfo from '../../components/SchemaAttributeInfo';
import EntityInfo from '../../components/EntityInfo';

const manager = new ProvisioningOperationManager();
const archiveManager = new ProvisioningArchiveManager();

class ProvisioningOperations extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      detail: {
        show: false,
        entity: null
      },
      retryDialog: {
        show: false,
        bulkActionValue: null,
        ids: []
      }
    };
  }

  getManager() {
    return manager;
  }

  getContentKey() {
    return 'acc:content.provisioningOperations';
  }

  componentDidMount() {
    this.selectNavigationItems(['audit', 'provisioning-operations']);
  }

  /**
   * Shows retry dialog
   *
   * @return {[type]} [description]
   */
  showRetryDialog(bulkActionValue, ids) {
    this.setState({
      retryDialog: {
        show: true,
        bulkActionValue,
        ids
      }
    });
  }

  onRetry(batch = true) {
    const { retryDialog } = this.state;
    this.closeRetryDialog();
    this.context.store.dispatch(manager.retry(retryDialog.ids, retryDialog.bulkActionValue, batch, () => {
      // clear selected rows and reload
      this.refs.table.getWrappedInstance().clearSelectedRows();
      this.refs.table.getWrappedInstance().reload();
      this.refs.archiveTable.getWrappedInstance().reload();
    }));
  }

  /**
   * Close modal retry dialog
   */
  closeRetryDialog() {
    this.setState({
      retryDialog: {
        show: false,
        bulkActionValue: null,
        ids: []
      }
    });
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
    }, () => {
      this.context.router.replace(`/provisioning?id=${entity.id}`);
      // TODO: back link could open modal window
    });
  }

  /**
   * Close modal detail
   */
  closeDetail() {
    this.setState({
      detail: {
        show: false,
        entity: null
      }
    });
  }

  render() {
    const { detail, retryDialog } = this.state;
    // accountObject to table
    const accountData = [];
    if (detail.entity && detail.entity.provisioningContext.accountObject) {
      const accountObject = detail.entity.provisioningContext.accountObject;
      for (const schemaAttributeId in accountObject) {
        if (!accountObject.hasOwnProperty(schemaAttributeId)) {
          continue;
        }
        let content = '';
        const propertyValue = accountObject[schemaAttributeId];
        if (_.isArray(propertyValue)) {
          content = propertyValue.join(', ');
        } else {
          content = propertyValue;
        }

        accountData.push({
          property: schemaAttributeId,
          value: content
        });
      }
    }
    //
    const connectorData = [];
    if (detail.entity && detail.entity.provisioningContext.connectorObject) {
      const connectorObject = detail.entity.provisioningContext.connectorObject;
      connectorObject.attributes.forEach(attribute => {
        let name = attribute.name;
        if (!name && attribute.password) {
          name = '__PASSWORD__';
        }
        connectorData.push({
          property: name,
          value: attribute.values.join(', ')
        });
      });
    }
    //
    return (
      <div>
        <Helmet title={this.i18n('title')} />

        <Basic.ContentHeader>
          <span dangerouslySetInnerHTML={{ __html: this.i18n('header') }}/>
        </Basic.ContentHeader>

        <Basic.Tabs>
          <Basic.Tab eventKey={1} title={this.i18n('tabs.active.label')}>
            <ProvisioningOperationTable
              ref="table"
              uiKey="provisioning-operations-table"
              manager={manager}
              showDetail={this.showDetail.bind(this)}
              showRowSelection={Managers.SecurityManager.hasAnyAuthority(['APP_ADMIN'])}
              actions={
                [
                  { value: 'retry', niceLabel: this.i18n('action.retry.action'), action: this.showRetryDialog.bind(this) },
                  { value: 'cancel', niceLabel: this.i18n('action.cancel.action'), action: this.showRetryDialog.bind(this) }
                ]
              }/>
          </Basic.Tab>

          <Basic.Tab eventKey={2} title={this.i18n('tabs.archive.label')}>
            <ProvisioningOperationTable
              ref="archiveTable"
              uiKey="provisioning-archive-table"
              manager={archiveManager}
              showDetail={this.showDetail.bind(this)}/>
          </Basic.Tab>
        </Basic.Tabs>

        <Basic.Modal
          bsSize="large"
          show={detail.show}
          onHide={this.closeDetail.bind(this)}
          backdrop="static">
          <Basic.Modal.Header closeButton text={this.i18n('detail.header')}/>
          <Basic.Modal.Body>
            {
              !detail.entity
              ||
              <div>
                <Basic.AbstractForm data={detail.entity} className="form-horizontal" readOnly>
                  <Basic.LabelWrapper label={this.i18n('entity.created')}>
                    <div style={{ margin: '7px 0' }}>
                      <Advanced.DateValue value={detail.entity.created} showTime/>
                    </div>
                  </Basic.LabelWrapper>
                  <Basic.EnumLabel ref="operationType" label={this.i18n('acc:entity.ProvisioningOperation.operationType')} enum={ProvisioningOperationTypeEnum}/>
                  <Basic.LabelWrapper label={this.i18n('acc:entity.ProvisioningOperation.entity')}>
                    <EntityInfo entityType={detail.entity.entityType} entityIdentifier={detail.entity.entityIdentifier} style={{ margin: 0 }}/>
                  </Basic.LabelWrapper>
                  <Basic.LabelWrapper label={this.i18n('acc:entity.System.name')}>
                    <div style={{ margin: '7px 0' }}>
                      <Link to={`/system/${detail.entity._embedded.system.id}/detail`} >{detail.entity._embedded.system.name}</Link>
                    </div>
                  </Basic.LabelWrapper>
                  <Basic.LabelWrapper label={this.i18n('acc:entity.SystemEntity.uid')}>
                    <div style={{ margin: '7px 0' }}>
                      {detail.entity.systemEntityUid}
                    </div>
                  </Basic.LabelWrapper>
                </Basic.AbstractForm>
                <br />

                <h3 style={{ margin: '0 0 10px 0', padding: 0, borderBottom: '1px solid #ddd' }}>{ this.i18n('detail.result') }</h3>
                <div style={{ marginBottom: 15 }}>
                  <Basic.EnumValue value={detail.entity.resultState} enum={ProvisioningResultStateEnum}/>
                  {
                    (!detail.entity.result || !detail.entity.result.code)
                    ||
                    <span style={{ marginLeft: 15 }}>
                      {this.i18n('detail.resultCode')}: {detail.entity.result.code}
                    </span>
                  }
                  <Basic.FlashMessage message={this.getFlashManager().convertFromResultModel(detail.entity.result.model)} style={{ marginTop: 15 }}/>
                </div>
                {
                  (!detail.entity.result || !detail.entity.result.stackTrace)
                  ||
                  <div>
                    <textArea
                      rows="10"
                      value={detail.entity.result.stackTrace}
                      readOnly
                      style={{ width: '100%', marginBottom: 15 }}/>
                  </div>
                }
                <Basic.Row>
                  <div className="col-lg-6">
                    <h3 style={{ margin: '0 0 10px 0', padding: 0, borderBottom: '1px solid #ddd' }}>{this.i18n('detail.accountObject')}</h3>
                    <Basic.Table
                      data={accountData}
                      noData={this.i18n('component.basic.Table.noData')}
                      className="table-bordered">
                      <Basic.Column
                        property="property"
                        header={this.i18n('label.property')}
                        cell={
                          ({rowIndex, data, property}) => {
                            return (
                              <SchemaAttributeInfo id={data[rowIndex][property]}/>
                            );
                          }
                        }/>
                      <Basic.Column property="value" header={this.i18n('label.value')}/>
                    </Basic.Table>
                  </div>
                  <div className="col-lg-6">
                    <h3 style={{ margin: '0 0 10px 0', padding: 0, borderBottom: '1px solid #ddd' }}>{this.i18n('detail.connectorObject')}</h3>
                    <Basic.Table
                      data={connectorData}
                      noData={this.i18n('component.basic.Table.noData')}
                      className="table-bordered">
                      <Basic.Column property="property" header={this.i18n('label.property')}/>
                      <Basic.Column property="value" header={this.i18n('label.value')}/>
                    </Basic.Table>
                  </div>
                </Basic.Row>
              </div>
            }
          </Basic.Modal.Body>

          <Basic.Modal.Footer>
            <Basic.Button
              level="link"
              onClick={this.closeDetail.bind(this)}>
              {this.i18n('button.close')}
            </Basic.Button>
          </Basic.Modal.Footer>
        </Basic.Modal>

        <Basic.Modal
          show={retryDialog.show}
          onHide={this.closeRetryDialog.bind(this)}
          backdrop="static">
          <Basic.Modal.Header closeButton text={this.i18n(`action.${retryDialog.bulkActionValue}.header`, { count: retryDialog.ids.length})}/>
          <Basic.Modal.Body>
            <span dangerouslySetInnerHTML={{__html: this.i18n(`action.${retryDialog.bulkActionValue}.batchMessage`, { count: retryDialog.ids.length, name: manager.getNiceLabel(manager.getEntity(this.context.store.getState(), retryDialog.ids[0])) })}}/>
          </Basic.Modal.Body>

          <Basic.Modal.Footer>
            <Basic.Button
              level="success"
              onClick={this.onRetry.bind(this, true)}>
              {this.i18n(`action.${retryDialog.bulkActionValue}.button.batch`)}
            </Basic.Button>
          </Basic.Modal.Footer>
          <Basic.Modal.Body>
            <span dangerouslySetInnerHTML={{__html: this.i18n(`action.${retryDialog.bulkActionValue}.message`, { count: retryDialog.ids.length, name: manager.getNiceLabel(manager.getEntity(this.context.store.getState(), retryDialog.ids[0])) })}}/>
          </Basic.Modal.Body>
          <Basic.Modal.Footer>
            <Basic.Button
              level="link"
              onClick={this.closeRetryDialog.bind(this)}>
              {this.i18n('button.close')}
            </Basic.Button>
            <Basic.Button
              level="warning"
              onClick={this.onRetry.bind(this, false)}>
              {this.i18n(`action.${retryDialog.bulkActionValue}.button.selected`)}
            </Basic.Button>
          </Basic.Modal.Footer>
        </Basic.Modal>

      </div>
    );
  }
}

ProvisioningOperations.propTypes = {
};
ProvisioningOperations.defaultProps = {
};

function select() {
  return {
  };
}

export default connect(select)(ProvisioningOperations);
