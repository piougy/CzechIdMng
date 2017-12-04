import React, { PropTypes } from 'react';
//
import * as Basic from '../../../components/basic';
import * as Utils from '../../../utils';
import { AutomaticRoleAttributeRuleManager, SecurityManager, FormAttributeManager } from '../../../redux';
import AutomaticRoleAttributeRuleTypeEnum from '../../../enums/AutomaticRoleAttributeRuleTypeEnum';
import AutomaticRoleAttributeRuleComparisonEnum from '../../../enums/AutomaticRoleAttributeRuleComparisonEnum';

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
      typeForceSearchParameters: null
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
    if (nextProps.entity.id !== this.props.entity.id) {
      this._initForm(nextProps.entity);
    }
  }

  /**
   * Method for basic initial form
   */
  _initForm(entity) {
    if (entity !== undefined) {
      this.refs.type.focus();
      this.refs.form.setData(entity);
    }
  }

  /**
   * Default save method that catch save event from form.
   */
  save(afterAction = 'CONTINUE', event) {
    const { uiKey, attributeId } = this.props;

    if (event) {
      event.preventDefault();
    }
    if (!this.refs.form.isFormValid()) {
      return;
    }

    this.setState({
      showLoading: true
    }, this.refs.form.processStarted());

    const entity = this.refs.form.getData();
    entity.automaticRoleAttribute = attributeId;
    // edit isn't allowed
    if (entity.id === undefined) {
      this.context.store.dispatch(this.manager.createEntity(entity, `${uiKey}-detail`, (createdEntity, error) => {
        this._afterSave(createdEntity, error, afterAction);
      }));
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
    let typeForceSearchParameters = this.formAttributeManager.getDefaultSearchParameters();
    if (option && option.value === AutomaticRoleAttributeRuleTypeEnum.findKeyBySymbol(AutomaticRoleAttributeRuleTypeEnum.IDENITITY_EAV)) {
      typeForceSearchParameters = typeForceSearchParameters.setFilter(DEFINITION_TYPE_FILTER, IDENTITY_EAV_TYPE);
    } else if (option && option.value === AutomaticRoleAttributeRuleTypeEnum.findKeyBySymbol(AutomaticRoleAttributeRuleTypeEnum.CONTRACT_EAV)) {
      typeForceSearchParameters = typeForceSearchParameters.setFilter(DEFINITION_TYPE_FILTER, CONTRACT_EAV_TYPE);
    } else {
      typeForceSearchParameters = null;
    }
    this.setState({
      typeForceSearchParameters
    });
  }

  render() {
    const { uiKey, entity } = this.props;
    const { showLoading, typeForceSearchParameters } = this.state;
    //
    //
    return (
      <div>
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
            <Basic.TextField
              ref="attributeName"
              label={this.i18n('entity.AutomaticRole.attribute.attributeName')}
              hidden={typeForceSearchParameters !== null}/>
            <Basic.SelectBox
              ref="formAttribute"
              forceSearchParameters={typeForceSearchParameters}
              label={this.i18n('entity.AutomaticRole.attribute.formAttribute')}
              hidden={typeForceSearchParameters === null}
              manager={this.formAttributeManager}/>
            <Basic.Row>
              <div className="col-lg-4">
                <Basic.EnumSelectBox
                  ref="comparison"
                  required
                  useFirst
                  label={this.i18n('entity.AutomaticRole.attribute.comparison')}
                  enum={AutomaticRoleAttributeRuleComparisonEnum}/>
              </div>
              <div className="col-lg-8">
                <Basic.TextField
                  ref="value"
                  label={this.i18n('entity.AutomaticRole.attribute.value.label')}
                  helpBlock={this.i18n('entity.AutomaticRole.attribute.value.help')}/>
              </div>
            </Basic.Row>
          </Basic.AbstractForm>

          <Basic.PanelFooter showLoading={showLoading} >
            <Basic.Button type="button" level="link" onClick={this.context.router.goBack}>{this.i18n('button.back')}</Basic.Button>
            <Basic.SplitButton
              level="success"
              title={ this.i18n('button.saveAndContinue') }
              onClick={ this.save.bind(this, 'CONTINUE') }
              showLoading={ showLoading }
              showLoadingIcon
              showLoadingText={ this.i18n('button.saving') }
              rendered={SecurityManager.hasAuthority(Utils.Entity.isNew(entity) ? 'AUTOMATICROLERULE_CREATE' : 'AUTOMATICROLERULE_UPDATE')}
              dropup>
              <Basic.MenuItem eventKey="1" onClick={this.save.bind(this, 'CLOSE')}>{this.i18n('button.saveAndClose')}</Basic.MenuItem>
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
