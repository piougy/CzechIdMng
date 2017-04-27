import React, { PropTypes } from 'react';
//
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import * as Utils from '../../utils';
import { ScriptManager, SecurityManager, ScriptAuthorityManager } from '../../redux';
import ScriptCategoryEnum from '../../enums/ScriptCategoryEnum';
import EntityUtils from '../../utils/EntityUtils';
import AbstractEnum from '../../enums/AbstractEnum';
import ScriptAuthorityTable from './ScriptAuthorityTable';

/**
 * Detail for sript
 * * name
 * * description
 * * script area
 * * category
 * * script authorities (table)
 *
 */
export default class ScriptDetail extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.scriptManager = new ScriptManager();
    this.scriptAuthorityManager = new ScriptAuthorityManager();
    this.state = {
      showLoading: false
    };
  }

  getContentKey() {
    return 'content.scripts';
  }

  componentDidMount() {
    const { entity } = this.props;
    this.selectNavigationItem('scripts');
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
        entity.category = AbstractEnum.findKeyBySymbol(ScriptCategoryEnum, ScriptCategoryEnum.DEFAULT);
      }
      this.refs.code.focus();
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
    // entity.category = AbstractEnum.findKeyBySymbol(ScriptCategoryEnum, entity.category);
    if (entity.id === undefined) {
      this.context.store.dispatch(this.scriptManager.createEntity(entity, `${uiKey}-detail`, (createdEntity, error) => {
        this._afterSave(createdEntity, error);
      }));
    } else {
      this.context.store.dispatch(this.scriptManager.updateEntity(entity, `${uiKey}-detail`, this._afterSave.bind(this)));
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
    const { uiKey, entity } = this.props;
    const { showLoading } = this.state;
    return (
      <div>
        <form onSubmit={this.save.bind(this)}>
          <Basic.AbstractForm
            ref="form"
            uiKey={uiKey}
            readOnly={!SecurityManager.hasAuthority(Utils.Entity.isNew(entity) ? 'SCRIPT_CREATE' : 'SCRIPT_UPDATE')}
            style={{ padding: '15px 15px 0 15px' }}>
          <Basic.Row>
            <div className="col-lg-2">
              <Basic.TextField
                ref="code"
                label={this.i18n('entity.Script.code')}
                required
                max={255}/>
            </div>
            <div className="col-lg-10">
              <Basic.TextField
                ref="name"
                label={this.i18n('entity.Script.name')}
                required
                max={255}/>
            </div>
          </Basic.Row>
          <Basic.EnumSelectBox
            ref="category"
            label={this.i18n('entity.Script.category')}
            enum={ScriptCategoryEnum}
            max={255}
            required/>
          <Advanced.RichTextArea ref="description" label={this.i18n('entity.Script.description')} />
          <Basic.ScriptArea
            ref="script"
            mode="groovy"
            height="25em"
            helpBlock={this.i18n('entity.Script.script.help')}
            label={this.i18n('entity.Script.script.label')}/>
          </Basic.AbstractForm>

          {
            Utils.Entity.isNew(entity)
            ||
            <Basic.Panel style={{display: 'block', borderColor: '#fff'}} showLoading={showLoading}>
              <Basic.PanelHeader text={this.i18n('scriptAuthorities')}/>
                <ScriptAuthorityTable uiKey={entity.id} scriptId={entity.id} rendered={!Utils.Entity.isNew(entity)} />
            </Basic.Panel>
          }

          <Basic.PanelFooter showLoading={showLoading} >
            <Basic.Button type="button" level="link" onClick={this.context.router.goBack}>{this.i18n('button.back')}</Basic.Button>
            <Basic.Button
              type="submit"
              level="success"
              showLoadingIcon
              showLoadingText={this.i18n('button.saving')}
              rendered={SecurityManager.hasAuthority(Utils.Entity.isNew(entity) ? 'SCRIPT_CREATE' : 'SCRIPT_UPDATE')}>
              {this.i18n('button.save')}
            </Basic.Button>
          </Basic.PanelFooter>
        </form>
      </div>
    );
  }
}

ScriptDetail.propTypes = {
  entity: PropTypes.object,
  uiKey: PropTypes.string.isRequired,
};
ScriptDetail.defaultProps = {
};
