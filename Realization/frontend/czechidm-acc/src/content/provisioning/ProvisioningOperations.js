import React from 'react';
import PropTypes from 'prop-types';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
import moment from 'moment';
import _ from 'lodash';
//
import { Basic, Advanced, Managers, Utils } from 'czechidm-core';
import { ProvisioningOperationManager, ProvisioningArchiveManager } from '../../redux';
import ProvisioningOperationTableComponent, { ProvisioningOperationTable } from './ProvisioningOperationTable';
import ProvisioningOperationTypeEnum from '../../domain/ProvisioningOperationTypeEnum';
import SystemEntityTypeEnum from '../../domain/SystemEntityTypeEnum';
//
const manager = new ProvisioningOperationManager();
const archiveManager = new ProvisioningArchiveManager();

/**
 * Active and archived provisioning operations
 *
 * @author Radek Tomiška
 * @author Ondrej Husnik
 */
class ProvisioningOperations extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      detail: {
        show: false,
        entity: null,
        activeKey: 1
      }
    };
  }

  getManager() {
    return manager;
  }

  getContentKey() {
    return 'acc:content.provisioningOperations';
  }

  /**
   * Shows modal detail with given entity
   */
  showDetail(entity, isArchive) {
    this.setState({
      detail: {
        show: true,
        entity,
        isArchive
      }
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

  _onChangeSelectTabs(activeKey) {
    this.setState({
      activeKey
    });
  }

  /**
   * Transforma account or connector object value into FE property values
   *
   * @param  {object} objectValue
   * @return {string}
   */
  _toPropertyValue(objectValue) {
    return Utils.Ui.toStringValue(objectValue);
  }

  /**
   * Aggregate and sort detail table data
   * @param {object} detail
   * @returns
   */
  _prepareProvisioningDetail(detail) {
    if (!detail.entity) {
      return null;
    }
    const resultContent = [];
    if (detail.entity.provisioningContext.accountObject) {
      const accountObject = detail.entity.provisioningContext.accountObject;
      for (const schemaAttributeId in accountObject) {
        if (!{}.hasOwnProperty.call(accountObject, schemaAttributeId)) {
          continue;
        }
        let accountVal = '';
        let systemVal = '';
        let changedVal = '';
        if (accountObject.hasOwnProperty(schemaAttributeId)) {
          accountVal = this._toPropertyValue(accountObject[schemaAttributeId]);
        }
        // separate name and strategy
        const strategyArr = schemaAttributeId.match(/\s*\([A-Z_]{3,}\).*$/);
        const strategyStr = strategyArr == null || strategyArr.length === 0 ? '' : strategyArr[0].trim();
        const connectorItemName = schemaAttributeId.replace(strategyStr, '').trim();
        // values from target system
        const systemObject = detail.entity.provisioningContext.systemConnectorObject;
        if (systemObject) {
          systemObject.attributes.forEach(attribute => {
            if (attribute.name === connectorItemName) {
              systemVal = this._toPropertyValue(attribute.values);
            }
          });
        }
        // changed values for provisoning
        const connectorObject = detail.entity.provisioningContext.connectorObject;
        if (connectorObject) {
          connectorObject.attributes.forEach(attribute => {
            if (attribute.name === connectorItemName) {
              changedVal = this._toPropertyValue(attribute.values);
            }
          });
        }
        resultContent.push({
          property: connectorItemName,
          strategy: strategyStr,
          accountVal,
          systemVal,
          changedVal
        });
      }
    }
    // sort by name
    resultContent.sort((lItem, rItem) => {
      const l = lItem.property;
      const r = rItem.property;
      return l < r ? -1 : (r < l ? 1 : 0);
    });
    // order changed attributes at the beginning
    resultContent.sort((lItem, rItem) => {
      const l = _.isEmpty(lItem.changedVal);
      const r = _.isEmpty(rItem.changedVal);
      return !l && r ? -1 : (r && !l ? 1 : 0);
    });
    return resultContent;
  }

  /**
   * Generator of the column label with help
   *
   * @param {*} headerDesc
   * @param {*} helpText
   * @returns
   */
  _renderColumnHelp(headerDesc, helpText) {
    return (
      <div>
        <span> {headerDesc} </span>
        <Basic.Popover
          ref="popover"
          trigger={['click', 'hover']}
          value={ helpText }
          className="abstract-entity-info-popover">
          {
            <span>
              <Basic.Button
                level="link"
                style={{ padding: 0, marginLeft: 3 }}
                icon="fa:question-circle"/>
            </span>
          }
        </Basic.Popover>
      </div>
    );

  }

  /**
   * Highlights the content of cells of attributes to change
   * @param {}
   * @returns
   */
  _highlightCellContent({rowIndex, data, property}) {
    if (_.isEmpty(data[rowIndex].changedVal)) {
      return (`${data[rowIndex][property]}`);
    }
    return (<strong>{data[rowIndex][property]}</strong>);
  }

  /**
   * Formats attribute name and its strategy
   *
   * @param  {}
   * @return {}
   */
  _formatAttributeName({rowIndex, data}) {
    return (<span>{data[rowIndex].property} <small>{data[rowIndex].strategy}</small></span>);
  }

  render() {
    const { forceSearchParameters, columns, uiKey, showDeleteAllButton } = this.props;
    const { detail, activeKey} = this.state;

    const tableContent = this._prepareProvisioningDetail(detail);
    //
    return (
      <Basic.Div>
        <Helmet title={ this.i18n('title') } />

        <Basic.Tabs activeKey={ activeKey } onSelect={ this._onChangeSelectTabs.bind(this) }>
          <Basic.Tab
            eventKey={ 1 }
            title={ this.i18n('tabs.active.label') }
            rendered={ Managers.SecurityManager.hasAnyAuthority(['PROVISIONINGOPERATION_READ']) }>
            <ProvisioningOperationTableComponent
              ref="table"
              key="table"
              uiKey={ uiKey }
              manager={ manager }
              isArchive={ false }
              showDetail={ this.showDetail.bind(this) }
              showRowSelection={ Managers.SecurityManager.hasAnyAuthority(['PROVISIONINGOPERATION_UPDATE']) }
              forceSearchParameters={ forceSearchParameters }
              columns={ columns }
              showDeleteAllButton={ showDeleteAllButton }/>
          </Basic.Tab>

          <Basic.Tab
            eventKey={ 2 }
            title={ this.i18n('tabs.archive.label') }
            rendered={ Managers.SecurityManager.hasAnyAuthority(['PROVISIONINGARCHIVE_READ']) }>
            <ProvisioningOperationTableComponent
              ref="archiveTable"
              key="archiveTable"
              uiKey={ `archive-${uiKey}` }
              manager={ archiveManager }
              isArchive
              showDetail={ this.showDetail.bind(this) }
              forceSearchParameters={ forceSearchParameters }
              columns={ columns }/>
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
                <Basic.AbstractForm data={ detail.entity } readOnly>

                  <Basic.Row>
                    <Basic.Col lg={ 4 }>
                      <Basic.LabelWrapper
                        label={ this.i18n('acc:entity.ProvisioningOperation.created.label') }
                        helpBlock={ this.i18n('acc:entity.ProvisioningOperation.created.help') }>
                        <div style={{ margin: '7px 0' }}>
                          <Advanced.DateValue value={ detail.entity.created } showTime/>
                        </div>
                      </Basic.LabelWrapper>
                    </Basic.Col>
                    <Basic.Col lg={ 8 }>
                      <Basic.LabelWrapper
                        label={
                          detail.isArchive === true
                          ?
                          this.i18n('acc:entity.ProvisioningArchive.modified.label')
                          :
                          this.i18n('acc:entity.ProvisioningOperation.modified.label')
                        }
                        helpBlock={
                          detail.isArchive === true
                          ?
                          this.i18n('acc:entity.ProvisioningArchive.modified.help')
                          :
                          this.i18n('acc:entity.ProvisioningOperation.modified.help')
                        }>
                        <div style={{ margin: '7px 0' }}>
                          <Advanced.DateValue value={ detail.entity.modified } showTime/>
                        </div>
                      </Basic.LabelWrapper>
                    </Basic.Col>
                  </Basic.Row>

                  <Basic.Row>
                    <Basic.Col lg={ 4 }>
                      <Basic.EnumLabel
                        ref="operationType"
                        label={ this.i18n('acc:entity.ProvisioningOperation.operationType') }
                        enum={ ProvisioningOperationTypeEnum }/>
                    </Basic.Col>
                  </Basic.Row>

                  <Basic.Row>
                    <Basic.Col lg={ 4 }>
                      <Basic.EnumLabel ref="entityType" label={ this.i18n('acc:entity.SystemEntity.entityType') } enum={ SystemEntityTypeEnum }/>
                    </Basic.Col>
                    <Basic.Col lg={ 8 }>
                      <Basic.LabelWrapper label={ this.i18n('acc:entity.ProvisioningOperation.entity') }>
                        {
                          !detail.entity.entityIdentifier
                          ?
                          <span>N/A</span>
                          :
                          <Advanced.EntityInfo
                            entityType={ detail.entity.entityType }
                            entityIdentifier={ detail.entity.entityIdentifier }
                            style={{ margin: 0 }}
                            face="popover"
                            showIcon/>
                        }
                      </Basic.LabelWrapper>
                    </Basic.Col>
                  </Basic.Row>

                  <Basic.Row>
                    <Basic.Col lg={ 4 }>
                      <Basic.LabelWrapper label={ this.i18n('acc:entity.System.name') }>
                        <Advanced.EntityInfo
                          entityType="system"
                          entityIdentifier={ detail.entity.system }
                          entity={ detail.entity._embedded.system }
                          style={{ margin: 0 }}
                          face="popover"/>
                      </Basic.LabelWrapper>
                    </Basic.Col>
                    <Basic.Col lg={ 8 }>
                      <Basic.LabelWrapper label={ this.i18n('acc:entity.SystemEntity.uid') }>
                        <div style={{ margin: '7px 0' }}>
                          { detail.isArchive ? detail.entity.systemEntityUid : detail.entity._embedded.systemEntity.uid }
                        </div>
                      </Basic.LabelWrapper>
                    </Basic.Col>
                  </Basic.Row>

                </Basic.AbstractForm>

                <Advanced.OperationResult value={ detail.entity.result } face="full"/>

                { // look out - archive doesn't have batch
                  (!detail.entity._embedded || !detail.entity._embedded.batch || !detail.entity._embedded.batch.nextAttempt)
                  ||
                  <div style={{ marginBottom: 15 }}>
                    <Basic.ContentHeader text={ this.i18n('detail.nextAttempt.header') }/>
                    {
                      this.i18n('detail.nextAttempt.label', {
                        escape: false,
                        currentAttempt: detail.entity.currentAttempt,
                        maxAttempts: detail.entity.maxAttempts,
                        nextAttempt: moment(detail.entity._embedded.batch.nextAttempt).format(this.i18n('format.datetime'))
                      })
                    }
                  </div>
                }
                <Basic.Row>
                  <Basic.Col lg={ 12 }>
                    <Basic.Table
                      data={ tableContent }
                      noData={ this.i18n('component.basic.Table.noData') }
                      className="table-bordered"
                      rowClass={({rowIndex, data}) => {
                        return _.isEmpty(data[rowIndex].changedVal) ? '' : 'warning';
                      }}>
                      <Basic.Column
                        property="property"
                        header={ this.i18n('detail.attributeNameCol') }
                        cell={this._formatAttributeName}
                      />
                      <Basic.Column
                        property="systemVal"
                        header={ this._renderColumnHelp(this.i18n('detail.systemObject.label'), this.i18n('detail.systemObject.help')) }
                      />
                      <Basic.Column
                        property="accountVal"
                        header={ this._renderColumnHelp(this.i18n('detail.accountObject.label'), this.i18n('detail.accountObject.help')) }
                        cell={this._highlightCellContent}
                      />
                    </Basic.Table>
                  </Basic.Col>
                </Basic.Row>
              </div>
            }
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

ProvisioningOperations.propTypes = {
  uiKey: PropTypes.string.isRequired,
  /**
   * Force searchparameters - system id
   */
  forceSearchParameters: PropTypes.object,
  /**
   * Show delete all button
   */
  showDeleteAllButton: PropTypes.bool
};
ProvisioningOperations.defaultProps = {
  forceSearchParameters: null,
  columns: ProvisioningOperationTable.defaultProps.columns,
  showDeleteAllButton: true
};

function select(state) {
  return {
    i18nReady: state.config.get('i18nReady')
  };
}

export default connect(select)(ProvisioningOperations);
