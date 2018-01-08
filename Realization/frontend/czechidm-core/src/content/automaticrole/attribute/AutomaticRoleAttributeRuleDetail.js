import React, { PropTypes } from 'react';
//
import * as Basic from '../../../components/basic';
import * as Utils from '../../../utils';
import { AutomaticRoleAttributeRuleManager, SecurityManager, FormAttributeManager } from '../../../redux';
import AutomaticRoleAttributeRuleTypeEnum from '../../../enums/AutomaticRoleAttributeRuleTypeEnum';
import AutomaticRoleAttributeRuleComparisonEnum from '../../../enums/AutomaticRoleAttributeRuleComparisonEnum';
import ContractAttributeEnum from '../../../enums/ContractAttributeEnum';
import IdentityAttributeEnum from '../../../enums/IdentityAttributeEnum';
/**
 * Constant for get eav attribute for identity contract
 * @type {String}
 */
const CONTRACT_EAV_TYPE = 'eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract';
/**
 * Constatn for get eav attribute for identity
 * @type {String}
 */
const IDENTITY_EAV_TYPE = 'eu.bcvsolutions.idm.core.model.entity.IdmIdentity';

const DEFINITION_TYPE_FILTER = 'definitionType';
/**
 * Detail rules of automatic role attribute
 *
 */
export default class AutomaticRoleAttributeRuleDetail extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.manager = new AutomaticRoleAttributeRuleManager();
    this.formAttributeManager = new FormAttributeManager();
    this.state = {
      showLoading: false,
      typeForceSearchParameters: null,
      type: AutomaticRoleAttributeRuleTypeEnum.findKeyBySymbol(AutomaticRoleAttributeRuleTypeEnum.IDENTITY),
      valueRequired: true
    };
  }

  getContentKey() {
    return 'content.automaticRoles.attribute';
  }

  componentDidMount() {
    const { entity } = this.props;
    this._initForm(entity);
  }

  /**
   * Method check if props in this component is'nt different from new props.
   */
  componentWillReceiveProps(nextProps) {
    // check id of old and new entity
    if (nextProps.entity.id !== this.props.entity.id || nextProps.entity.attributeName !== this.props.entity.attributeName) {
      this._initForm(nextProps.entity);
    }
  }

  /**
   * Method for basic initial form
   */
  _initForm(entity) {
    if (entity !== undefined) {
      if (!entity.id) {
        entity.type = AutomaticRoleAttributeRuleTypeEnum.findKeyBySymbol(AutomaticRoleAttributeRuleTypeEnum.IDENTITY);
        entity.comparison = AutomaticRoleAttributeRuleComparisonEnum.findKeyBySymbol(AutomaticRoleAttributeRuleComparisonEnum.EQUALS);
        entity.attributeName = IdentityAttributeEnum.USERNAME;
      } else {
        this.setState({
          typeForceSearchParameters: this._getForceSearchParametersForType(entity.type),
          type: entity.type
        });
        if (entity.attributeName) {
          if (entity.type === AutomaticRoleAttributeRuleTypeEnum.findKeyBySymbol(AutomaticRoleAttributeRuleTypeEnum.IDENTITY)) {
            entity.attributeName = IdentityAttributeEnum.getEnum(entity.attributeName);
          } else {
            entity.attributeName = ContractAttributeEnum.getEnum(entity.attributeName);
          }
        }
      }
      this.refs.type.focus();
      this.refs.form.setData(entity);
    }
  }

  _getForceSearchParametersForType(type) {
    let typeForceSearchParameters = this.formAttributeManager.getDefaultSearchParameters();
    if (type === AutomaticRoleAttributeRuleTypeEnum.findKeyBySymbol(AutomaticRoleAttributeRuleTypeEnum.IDENITITY_EAV)) {
      typeForceSearchParameters = typeForceSearchParameters.setFilter(DEFINITION_TYPE_FILTER, IDENTITY_EAV_TYPE);
    } else if (type === AutomaticRoleAttributeRuleTypeEnum.findKeyBySymbol(AutomaticRoleAttributeRuleTypeEnum.CONTRACT_EAV)) {
      typeForceSearchParameters = typeForceSearchParameters.setFilter(DEFINITION_TYPE_FILTER, CONTRACT_EAV_TYPE);
    } else {
      typeForceSearchParameters = null;
    }
    return typeForceSearchParameters;
  }

  /**
   * Default save method that catch save event from form.
   */
  save(afterAction = 'CLOSE', event) {
    const { uiKey, attributeId } = this.props;

    if (event) {
      event.preventDefault();
    }
    if (!this.refs.form.isFormValid()) {
      return;
    }
    // modal window with information about recalculate automatic roles
    this.refs['recalculate-automatic-role'].show(
      this.i18n(`content.automaticRoles.recalculate.message`),
      this.i18n(`content.automaticRoles.recalculate.header`)
    ).then(() => {
      this._saveInternal(true, attributeId, uiKey, afterAction);
    }, () => {
      this._saveInternal(false, attributeId, uiKey, afterAction);
    });
  }

  _saveInternal(recalculatedRoles, attributeId, uiKey, afterAction) {
    this.setState({
      showLoading: true
    }, this.refs.form.processStarted());

    const entity = this.refs.form.getData();
    entity.automaticRoleAttribute = attributeId;
    // we must transform attribute name with case sensitive letters
    if (entity.attributeName) {
      if (entity.type === AutomaticRoleAttributeRuleTypeEnum.findKeyBySymbol(AutomaticRoleAttributeRuleTypeEnum.IDENTITY)) {
        entity.attributeName = IdentityAttributeEnum.getField(IdentityAttributeEnum.findKeyBySymbol(entity.attributeName));
      } else {
        entity.attributeName = ContractAttributeEnum.getField(ContractAttributeEnum.findKeyBySymbol(entity.attributeName));
      }
    }
    //
    if (entity.id === undefined) {
      if (recalculatedRoles) {
        this.context.store.dispatch(this.manager.createAndRecalculateEntity(entity, `${uiKey}-detail`, (createdEntity, error) => {
          this._afterSave(createdEntity, error, afterAction);
        }));
      } else {
        this.context.store.dispatch(this.manager.createEntity(entity, `${uiKey}-detail`, (createdEntity, error) => {
          this._afterSave(createdEntity, error, afterAction);
        }));
      }
    } else {
      if (recalculatedRoles) {
        this.context.store.dispatch(this.manager.updateAndRecalculateEntity(entity, `${uiKey}-detail`, (updatedEntity, error) => {
          this._afterSave(updatedEntity, error, afterAction);
        }));
      } else {
        this.context.store.dispatch(this.manager.updateEntity(entity, `${uiKey}-detail`, (updatedEntity, error) => {
          this._afterSave(updatedEntity, error, afterAction);
        }));
      }
    }
  }

  /**
   * Method set showLoading to false and if is'nt error then show success message
   */
  _afterSave(entity, error, afterAction) {
    if (error) {
      this.setState({
        showLoading: false
      }, this.refs.form.processEnded());
      this.addError(error);
      return;
    }
    this.addMessage({ message: this.i18n('save.success', { name: entity.name }) });
    if (afterAction !== 'CONTINUE') {
      this.context.router.goBack();
    } else {
      this.setState({
        showLoading: false
      }, this.refs.form.processEnded());
      //
      this.context.router.replace('/automatic-role/attributes/' + entity.automaticRoleAttribute + '/rule/' + entity.id);
    }
  }

  _typeChange(option) {
    let typeForceSearchParameters = null;
    if (option) {
      typeForceSearchParameters = this._getForceSearchParametersForType(option.value);
    }
    //
    // clear values in specific fields
    this.refs.attributeName.setValue(null);
    this.refs.formAttribute.setValue(null);
    //
    this.setState({
      typeForceSearchParameters,
      type: option.value
    });
  }

  _comparsionChange(option) {
    let valueRequired = false;
    if (option.value === AutomaticRoleAttributeRuleComparisonEnum.findKeyBySymbol(AutomaticRoleAttributeRuleComparisonEnum.EQUALS)) {
      valueRequired = true;
    }
    //
    this.setState({
      valueRequired
    });
  }

  render() {
    const { uiKey, entity } = this.props;
    const { showLoading, typeForceSearchParameters, type, valueRequired } = this.state;
    //
    return (
      <div>
        <Basic.Confirm ref="recalculate-automatic-role" level="danger"/>
        <form onSubmit={this.save.bind(this, 'CONTINUE')}>
          <Basic.AbstractForm
            ref="form"
            uiKey={uiKey}
            readOnly={!SecurityManager.hasAuthority(Utils.Entity.isNew(entity) ? 'AUTOMATICROLERULE_CREATE' : 'AUTOMATICROLERULE_UPDATE')}
            style={{ padding: '15px 15px 0 15px' }}>
            <Basic.EnumSelectBox
              ref="type"
              required
              label={this.i18n('entity.AutomaticRole.attribute.type.label')}
              helpBlock={this.i18n('entity.AutomaticRole.attribute.type.help')}
              enum={AutomaticRoleAttributeRuleTypeEnum}
              onChange={this._typeChange.bind(this)}/>
            <Basic.EnumSelectBox
              ref="attributeName"
              label={this.i18n('entity.AutomaticRole.attribute.attributeName')}
              enum={type === AutomaticRoleAttributeRuleTypeEnum.findKeyBySymbol(AutomaticRoleAttributeRuleTypeEnum.IDENTITY) ? IdentityAttributeEnum : ContractAttributeEnum}
              hidden={typeForceSearchParameters !== null}
              required={!(typeForceSearchParameters !== null)}/>
            <Basic.SelectBox
              ref="formAttribute"
              forceSearchParameters={typeForceSearchParameters}
              label={this.i18n('entity.AutomaticRole.attribute.formAttribute')}
              hidden={typeForceSearchParameters === null}
              required={!(typeForceSearchParameters === null)}
              manager={this.formAttributeManager}/>
            <Basic.Row>
              <div className="col-lg-4">
                <Basic.EnumSelectBox
                  ref="comparison"
                  required
                  useFirst
                  onChange={this._comparsionChange.bind(this)}
                  label={this.i18n('entity.AutomaticRole.attribute.comparison')}
                  enum={AutomaticRoleAttributeRuleComparisonEnum}/>
              </div>
              <div className="col-lg-8">
                <Basic.TextField
                  ref="value"
                  required={valueRequired}
                  label={this.i18n('entity.AutomaticRole.attribute.value.label')}
                  helpBlock={this.i18n('entity.AutomaticRole.attribute.value.help')}/>
              </div>
            </Basic.Row>
          </Basic.AbstractForm>

          <Basic.PanelFooter showLoading={showLoading} >
            <Basic.Button type="button" level="link" onClick={this.context.router.goBack}>{this.i18n('button.back')}</Basic.Button>
            <Basic.SplitButton
              level="success"
              title={ this.i18n('button.saveAndClose') }
              onClick={ this.save.bind(this, 'CLOSE') }
              showLoading={ showLoading }
              showLoadingIcon
              showLoadingText={ this.i18n('button.saving') }
              rendered={SecurityManager.hasAuthority(Utils.Entity.isNew(entity) ? 'AUTOMATICROLERULE_CREATE' : 'AUTOMATICROLERULE_UPDATE')}
              dropup>
              <Basic.MenuItem
                eventKey="1"
                onClick={this.save.bind(this, 'CONTINUE')}>
                {this.i18n('button.saveAndContinue')}
              </Basic.MenuItem>
            </Basic.SplitButton>
          </Basic.PanelFooter>
          {/* onEnter action - is needed because SplitButton is used instead standard submit button */}
          <input type="submit" className="hidden"/>
        </form>
      </div>
    );
  }
}

AutomaticRoleAttributeRuleDetail.propTypes = {
  entity: PropTypes.object,
  uiKey: PropTypes.string.isRequired,
  attributeId: PropTypes.string
};
AutomaticRoleAttributeRuleDetail.defaultProps = {
};
