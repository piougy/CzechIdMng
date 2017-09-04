import React, { PropTypes } from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
import _ from 'lodash';
//
import { Basic, Utils, Advanced, Domain } from 'czechidm-core';
import { VsRequestManager } from '../../redux';
import VsRequestInfo from '../../components/advanced/VsRequestInfo/VsRequestInfo';
import VsRequestTable from './VsRequestTable';

const manager = new VsRequestManager();

/**
 * Virtual system request detail
 *
 * @author Vít Švanda
 */
class VsRequestDetail extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    //
    this.state = {
      showLoading: false
    };
  }

  /**
   * "Shorcut" for localization
   */
  getContentKey() {
    return 'vs:content.vs-request.detail';
  }

  componentDidMount() {
    super.componentDidMount();
    //
    const { entity } = this.props;
    if (entity) {
      this._initConnectorObject(entity.id);
    }
  }

  /**
   * Component will receive new props, try to compare with actual,
   * then init form
   */
  componentWillReceiveProps(nextProps) {
    const { entity } = this.props;
    if (entity && nextProps.entity && entity.id !== nextProps.entity.id) {
      this._initConnectorObject(nextProps.entity.id);
    }
  }

  realize(entity) {
    if (!entity) {
      return;
    }
    manager.realizeUi.bind(this)('realize', [entity ? entity.id : null], manager, () => {
      this._initConnectorObject(entity.id);
    });
  }

  cancel(entity) {
    if (!entity) {
      return;
    }
    manager.cancelUi.bind(this)('cancel', [entity ? entity.id : null], manager, () => {
      this._initConnectorObject(entity.id);
    });
  }

  _initConnectorObject(entityId) {
    manager.getService().getConnectorObject(entityId)
    .then(json => {
      this.setState({connectorObject: json});
    })
    .catch(error => {
      this.addError(error);
    });
  }

  _getImplementers(entity) {
    if (!entity || !entity.implementers) {
      return '';
    }
    const identities = [];
    for (const implementer of entity.implementers) {
      identities.push(implementer.id);
    }

    return (
      <Advanced.IdentitiesInfo identities={identities} maxEntry={100} showOnlyUsername={false}/>
    );
  }

  _getRequestAccountData(entity) {
    const {connectorObject} = this.state;
    let compiledConnectorAttributes;
    if (connectorObject) {
      const attributes = connectorObject.attributes;
      compiledConnectorAttributes = this._compileAttributes(attributes);
    }
    if (entity && entity.connectorObject) {
      const attributes = entity.connectorObject.attributes;
      const compiledAttributes = this._compileAttributes(attributes);
      const result = this._compileDiffAttributes(compiledAttributes, compiledConnectorAttributes);
      return result;
    }
  }

  _getAccountData() {
    const {connectorObject} = this.state;
    if (connectorObject) {
      const attributes = connectorObject.attributes;
      const compiledAttributes = this._compileAttributes(attributes);
      for (const property in compiledAttributes) {
        if (compiledAttributes.hasOwnProperty(property)) {
          const propertyValue = compiledAttributes[property].value;
          if (_.isArray(propertyValue)) {
            compiledAttributes[property].value = propertyValue.join(', ');
          }
        }
      }
      // sort by property
      return _(compiledAttributes).sortBy('property').value();
    }
  }

  _compileAttributes(attributes) {
    const accountData = {};
    for (const schemaAttributeId in attributes) {
      if (!attributes.hasOwnProperty(schemaAttributeId)) {
        continue;
      }
      let content = '';
      const attribute = attributes[schemaAttributeId];
      const propertyValue = attribute.values;
      if (attribute.multiValue) {
        content = propertyValue;
      } else {
        content = propertyValue[0];
      }
      accountData[attribute.name] = {property: attribute.name, value: content, multiValue: attribute.multiValue};
    }
    return accountData;
  }

  _compileDiffAttributes(requestAttributes, connectorAttributes) {
    const result = {};
    for (const property in requestAttributes) {
      if (requestAttributes.hasOwnProperty(property)) {
        const propertyValue = requestAttributes[property].value;
        let content;
        let level;
        let multiValue = false;
        if (requestAttributes[property].multiValue) {
          multiValue = true;
          const listResult = {};
          const listConnector = connectorAttributes ? connectorAttributes[property].value : null;

          for (const value of requestAttributes[property].value) {
            let levelItem;
            if (_.indexOf(listConnector, value) !== -1) {
              levelItem = null;
            } else {
              levelItem = 'success';
            }
            listResult[value] = {property, value, level: levelItem};
          }
          content = _(listResult).sortBy('value').value();
        } else {
          content = propertyValue;
          level = this._getLevelForAttribute(connectorAttributes, propertyValue, property);
        }
        result[property] = {property, value: content, level, multiValue};
      }
    }
     // sort by property
    return _(result).sortBy('property').value();
  }

  _getLevelForAttribute(connectorAttributes, propertyValue, property) {
    if (!connectorAttributes || !connectorAttributes.hasOwnProperty(property)) {
      return 'success';
    } else if (connectorAttributes[property].value === propertyValue) {
      return null;
    }
    return 'warning';
  }

  _getValueCell({ rowIndex, data}) {
    const entity = data[rowIndex];
    if (!entity || !entity.value) {
      return '';
    }

    if (entity.multiValue) {
      const listResult = [];
      for (const item of entity.value) {
        if (item.level) {
          listResult.push(<Basic.Label level={item.level} text={item.value}/>);
        } else {
          listResult.push(item.value + ' ');
        }
        listResult.push(' ');
      }
      return listResult;
    }

    if (entity.level) {
      return (<Basic.Label level={entity.level} text={entity.value}/>);
    }
    return entity.value;
  }

  render() {
    const { entity, _permissions, showLoading } = this.props;
    const _showLoading = showLoading || this.state.showLoading;

    const searchBefore = new Domain.SearchParameters()
    .setFilter('uid', entity ? entity.uid : null)
    .setFilter('systemId', entity ? entity.systemId : Domain.SearchParameters.BLANK_UUID)
    .setFilter('createdBefore', entity ? entity.created : null)
    .setFilter('state', 'IN_PROGRESS');

    const searchAfter = new Domain.SearchParameters()
    .setFilter('uid', entity ? entity.uid : null)
    .setFilter('systemId', entity ? entity.systemId : Domain.SearchParameters.BLANK_UUID)
    .setFilter('createdAfter', entity ? entity.created : null)
    .setFilter('state', 'IN_PROGRESS');

    const accountData = this._getAccountData();
    const requestData = this._getRequestAccountData(entity);
    return (
      <div>
        <Basic.Confirm ref="confirm-realize" level="danger"/>
        <Basic.Confirm ref="confirm-cancel" level="danger">
          <div style={{marginTop: '20px'}}>
            <Basic.AbstractForm ref="cancel-form" uiKey="confirm-cancel" >
              <Basic.TextArea
                ref="cancel-reason"
                placeholder={this.i18n('vs:content.vs-requests.cancel-reason.placeholder')}
                required/>
            </Basic.AbstractForm>
          </div>
        </Basic.Confirm>
        <Helmet title={ Utils.Entity.isNew(entity) ? this.i18n('create.header') : this.i18n('edit.title') } />

          <Basic.Panel>
            <Basic.PanelHeader text={ Utils.Entity.isNew(entity) ? this.i18n('create.header') : this.i18n('basic') } />
            <Basic.PanelBody
              showLoading={ _showLoading } >
              <Basic.Row>
                <Basic.Col lg={ 6 }>
                  <VsRequestInfo entityIdentifier={entity ? entity.id : null} entity={entity} face="full" showLink={false}/>
                  <Basic.LabelWrapper readOnly ref="implementers" label={this.i18n('vs:entity.VsRequest.implementers.label') + ':'}>
                    {this._getImplementers(entity)}
                  </Basic.LabelWrapper>
                </Basic.Col>
                <Basic.Col lg={ 6 }>
                  <Basic.TextArea
                    ref="reason"
                    label={this.i18n('vs:entity.VsRequest.reason.label')}
                    readOnly
                    rows={6}
                    rendered={entity && entity.state === 'CANCELED' }
                    value={entity ? entity.reason : null}/>
                </Basic.Col>
              </Basic.Row>
              <Basic.Row>
                <Basic.Col lg={ 6 }>
                  <Basic.LabelWrapper readOnly label={this.i18n('requestAttributes') + ':'}>
                    <Basic.Table
                      data={requestData}
                      noData={this.i18n('component.basic.Table.noData')}
                      className="table-bordered">
                      <Basic.Column property="property" header={this.i18n('label.property')}/>
                      <Basic.Column property="value" header={this.i18n('label.value')} cell={this._getValueCell.bind(this)}/>
                    </Basic.Table>
                  </Basic.LabelWrapper>
                </Basic.Col>
                <Basic.Col lg={ 6 }>
                  <Basic.LabelWrapper readOnly label={this.i18n('accountAttributes') + ':'}>
                    <Basic.Table
                      data={accountData}
                      noData={this.i18n('component.basic.Table.noData')}
                      className="table-bordered">
                      <Basic.Column property="property" header={this.i18n('label.property')}/>
                      <Basic.Column property="value" header={this.i18n('label.value')}/>
                    </Basic.Table>
                  </Basic.LabelWrapper>
                </Basic.Col>
              </Basic.Row>
              <Basic.Row>
                <Basic.Col lg={ 6 }>
                  <Basic.LabelWrapper readOnly ref="vs-request-table-before" label={this.i18n('beforeRequests.label') + ':'}>
                    <VsRequestTable
                      uiKey="vs-request-table-before"
                      columns= {['state', 'operationType', 'created', 'uid']}
                      showFilter={false}
                      forceSearchParameters={searchBefore}
                      showToolbar={false}
                      showPageSize={false}
                      showRowSelection={false}
                      showId={false}
                      filterOpened={false} />
                  </Basic.LabelWrapper>
                </Basic.Col>
                <Basic.Col lg={ 6 }>
                    <Basic.LabelWrapper readOnly ref="vs-request-table-after" label={this.i18n('afterRequests.label') + ':'}>
                      <VsRequestTable
                        uiKey="vs-request-table-after"
                        columns= {['state', 'operationType', 'created', 'uid']}
                        showFilter={false}
                        forceSearchParameters={searchAfter}
                        showToolbar={false}
                        showPageSize={false}
                        showRowSelection={false}
                        showId={false}
                        filterOpened={false} />
                  </Basic.LabelWrapper>
                </Basic.Col>
              </Basic.Row>
            </Basic.PanelBody>

            <Basic.PanelFooter>
              <Basic.Button type="button" level="link" onClick={ this.context.router.goBack }>{ this.i18n('button.back') }</Basic.Button>
              <Basic.SplitButton
                level="success"
                id="request-realize"
                title={ this.i18n('button.request.realize') }
                onClick={this.realize.bind(this, entity) }
                showLoading={ _showLoading }
                showLoadingIcon
                showLoadingText={ this.i18n('button.saving') }
                rendered={ manager.canSave(entity, _permissions) && entity && entity.state === 'IN_PROGRESS' }
                pullRight
                dropup>
                <Basic.MenuItem
                  eventKey="1"
                  onClick={this.cancel.bind(this, entity)}>
                  {this.i18n('button.request.cancel')}
                </Basic.MenuItem>
              </Basic.SplitButton>
            </Basic.PanelFooter>
          </Basic.Panel>
      </div>
    );
  }
}

VsRequestDetail.propTypes = {
  /**
   * Loaded entity
   */
  entity: PropTypes.object,
  /**
   * Entity, permissions etc. fro this content are stored in redux under given key
   */
  uiKey: PropTypes.string.isRequired,
  /**
   * Logged identity permissions - what can do with currently loaded entity
   */
  _permissions: PropTypes.arrayOf(PropTypes.string)
};
VsRequestDetail.defaultProps = {
  _permissions: null
};

function select(state, component) {
  if (!component.entity) {
    return {};
  }
  return {
    _permissions: manager.getPermissions(state, null, component.entity.id)
  };
}

export default connect(select)(VsRequestDetail);
