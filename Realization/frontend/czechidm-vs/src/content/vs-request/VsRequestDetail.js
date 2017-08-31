import React, { PropTypes } from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
import _ from 'lodash';
//
import { Basic, Utils, Advanced } from 'czechidm-core';
import { VsRequestManager } from '../../redux';
import VsRequestInfo from '../../components/advanced/VsRequestInfo/VsRequestInfo';

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

  /**
   * Component will receive new props, try to compare with actual,
   * then init form
   */
  componentWillReceiveProps(nextProps) {
    const { entity } = this.props;
    if (entity && entity.id !== nextProps.entity.id) {
      this.refs.form.setData(nextProps.entity);
    }
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

  _getAccountData(entity) {
    const accountData = [];
    if (entity && entity.connectorObject) {
      const attributes = entity.connectorObject.attributes;
      for (const schemaAttributeId in attributes) {
        if (!attributes.hasOwnProperty(schemaAttributeId)) {
          continue;
        }
        let content = '';
        const attribute = attributes[schemaAttributeId];
        const propertyValue = attribute.values;
        if (_.isArray(propertyValue)) {
          content = propertyValue.join(', ');
        } else {
          content = propertyValue;
        }

        accountData.push({
          property: attribute.name,
          value: content
        });
      }
    }
    return accountData;
  }

  render() {
    const { entity, _permissions } = this.props;
    const { showLoading } = this.state;
    //
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
              showLoading={ showLoading } >
              <VsRequestInfo entityIdentifier={entity ? entity.id : null} entity={entity} face="full" showLink={false}/>
              <Basic.AbstractForm ref="vs-request-detail" uiKey="vs-request-detail" >
                <Basic.LabelWrapper readOnly ref="implementers" label={this.i18n('vs:entity.VsRequest.implementers.label') + ':'}>
                  {this._getImplementers(entity)}
                </Basic.LabelWrapper>
              </Basic.AbstractForm>

              <Basic.Row>
                <Basic.Col lg={ 6 }>
                  <Basic.Table
                    data={this._getAccountData(entity)}
                    noData={this.i18n('component.basic.Table.noData')}
                    className="table-bordered">
                    <Basic.Column property="property" header={this.i18n('label.property')}/>
                    <Basic.Column property="value" header={this.i18n('label.value')}/>
                  </Basic.Table>
                </Basic.Col>
                <Basic.Col lg={ 6 }>
                  <Basic.Table
                    data={this._getAccountData(entity)}
                    noData={this.i18n('component.basic.Table.noData')}
                    className="table-bordered">
                    <Basic.Column property="property" header={this.i18n('label.property')}/>
                    <Basic.Column property="value" header={this.i18n('label.value')}/>
                  </Basic.Table>
                </Basic.Col>
              </Basic.Row>
            </Basic.PanelBody>

            <Basic.PanelFooter>
              <Basic.Button type="button" level="link" onClick={ this.context.router.goBack }>{ this.i18n('button.back') }</Basic.Button>

              <Basic.SplitButton
                level="success"
                id="request-realize"
                title={ this.i18n('button.request.realize') }
                onClick={manager.realizeUi.bind(this, 'realize', [entity ? entity.id : null], manager) }
                showLoading={ showLoading }
                showLoadingIcon
                showLoadingText={ this.i18n('button.saving') }
                rendered={ manager.canSave(entity, _permissions) && entity && entity.state === 'IN_PROGRESS' }
                pullRight
                dropup>
                <Basic.MenuItem
                  eventKey="1"
                  onClick={ manager.cancelUi.bind(this, 'cancel', [entity ? entity.id : null], manager)}>
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
