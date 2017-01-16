import React, { PropTypes } from 'react';
import * as Basic from '../../components/basic';
import { RuleManager, SecurityManager } from '../../redux';
import RuleCategoryEnum from '../../enums/RuleCategoryEnum';
import EntityUtils from '../../utils/EntityUtils';
import AbstractEnum from '../../enums/AbstractEnum';

/**
 * Detail for rules
 * * name
 * * description
 * * script area
 * * category
 *
 */
export default class RuleDetail extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.ruleManager = new RuleManager();
    this.state = {
      showLoading: false
    };
  }

  getContentKey() {
    return 'content.rules';
  }

  componentDidMount() {
    const { entity } = this.props;
    this.selectNavigationItem('rules');
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
      if (EntityUtils.isNew(entity)) {
        entity.description = '';
        entity.category = AbstractEnum.findKeyBySymbol(RuleCategoryEnum, RuleCategoryEnum.DEFAULT);
        this.refs.name.focus();
      }
      this.refs.form.setData(entity);
    }
  }

  /**
   * Default save method that catch save event from form.
   */
  save(event) {
    const { uiKey } = this.props;

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
    if (entity.id === undefined) {
      this.context.store.dispatch(this.ruleManager.createEntity(entity, `${uiKey}-detail`, (createdEntity, error) => {
        this._afterSave(createdEntity, error);
      }));
    } else {
      this.context.store.dispatch(this.ruleManager.patchEntity(entity, `${uiKey}-detail`, this._afterSave.bind(this)));
    }
  }

  /**
   * Method set showLoading to false and if is'nt error then show success message
   */
  _afterSave(entity, error) {
    if (error) {
      this.setState({
        showLoading: false
      }, this.refs.form.processEnded());
      this.addError(error);
      return;
    }
    this.addMessage({ message: this.i18n('save.success', { name: entity.name }) });
    this.context.router.goBack();
  }

  closeDetail() {
  }

  render() {
    const { uiKey } = this.props;
    const { showLoading } = this.state;
    return (
      <div>
        <form onSubmit={this.save.bind(this)}>
            <Basic.AbstractForm ref="form" uiKey={uiKey} className="form-horizontal" readOnly={!SecurityManager.hasAuthority('RULE_WRITE')} >
              <Basic.TextField
                ref="name"
                label={this.i18n('entity.Rule.name')}
                required
                max={255}/>
              <Basic.EnumSelectBox
                ref="category"
                label={this.i18n('entity.Rule.category')}
                enum={RuleCategoryEnum}
                required/>
              <Basic.RichTextArea ref="description" label={this.i18n('entity.Rule.description')} />
              <Basic.ScriptArea
                ref="script"
                mode="groovy"
                height="25em"
                helpBlock={this.i18n('entity.Rule.script.help')}
                label={this.i18n('entity.Rule.script.label')}/>
            </Basic.AbstractForm>

            <Basic.PanelFooter showLoading={showLoading} >
              <Basic.Button type="button" level="link" onClick={this.context.router.goBack}>{this.i18n('button.back')}</Basic.Button>
              <Basic.Button
                type="submit"
                level="success"
                showLoadingIcon
                showLoadingText={this.i18n('button.saving')}
                rendered={SecurityManager.hasAuthority('RULE_WRITE')}>
                {this.i18n('button.save')}
              </Basic.Button>
            </Basic.PanelFooter>
          </form>
      </div>
    );
  }
}

RuleDetail.propTypes = {
  entity: PropTypes.object,
  uiKey: PropTypes.string.isRequired,
};
RuleDetail.defaultProps = {
};
