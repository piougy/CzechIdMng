import React, { PropTypes } from 'react';
//
import * as Basic from '../../../components/basic';
import * as Advanced from '../../../components/advanced';
import * as Utils from '../../../utils';
import { AutomaticRoleAttributeManager, AutomaticRoleAttributeRuleManager, SecurityManager } from '../../../redux';
import AutomaticRoleAttributeRuleTable from './AutomaticRoleAttributeRuleTable';

/**
 * Detail automatic role by attribute
 *
 * @author Ondrej Kopr
 */
export default class AutomaticRoleAttributeDetail extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.manager = new AutomaticRoleAttributeManager();
    this.automaticRoleAttributeRuleManager = new AutomaticRoleAttributeRuleManager();
    this.state = {
      showLoading: false
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
    const { entity } = this.props;
    if (!nextProps.entity) {
      return;
    }
    // check id of old and new entity
    if (!entity || (nextProps.entity.id !== entity.id)) {
      this._initForm(nextProps.entity);
    }
  }

  /**
   * Method for basic initial form
   */
  _initForm(entity) {
    if (entity !== undefined) {
      this.refs.name.focus();
      this.refs.form.setData(entity);
    }
  }

  /**
   * Default save method that catch save event from form.
   */
  save(afterAction = 'CONTINUE', event) {
    const { uiKey } = this.props;

    if (event) {
      event.preventDefault();
    }
    if (!this.refs.form.isFormValid()) {
      return;
    }
    const entity = this.refs.form.getData();
    //
    if (entity.id) {
      return;
    }
    //
    this.setState({
      showLoading: true
    }, this.refs.form.processStarted());

    // edit isn't allowed
    if (entity.id === undefined) {
      this.context.store.dispatch(this.manager.createEntity(entity, `${uiKey}-detail`, (createdEntity, error) => {
        if (error && error.status === 'ACCEPTED') {
          // approving
          this.addMessage({ message: this.i18n('error.ACCEPTED.message'), title: this.i18n('error.ACCEPTED.title') });
          this.context.router.goBack();
        } else {
          this._afterSave(createdEntity, error, afterAction);
        }
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
      this.context.router.replace('/automatic-role/attributes/' + entity.id);
    }
  }

  _showConceptWarning() {
    const { entity } = this.props;
    //
    if (entity && entity.concept) {
      return (
        <Basic.Alert level="warning" text={this.i18n('entity.AutomaticRole.attribute.concept.help')}/>
      );
    }
    return null;
  }

  _recalculate() {
    const { entity } = this.props;
    this.setState({
      showLoading: true
    }, this.refs.form.processStarted());
    //
    // modal window with information about recalculate automatic roles
    this.refs['recalculate-automatic-role'].show(
      this.i18n(`content.automaticRoles.recalculate.message`),
      this.i18n(`content.automaticRoles.recalculate.header`)
    ).then(() => {
      this.manager.recalculate(entity.id, (automaticRole) => {
        // refresh concept state
        // there must be fetch only init form doesn't enough
        this.context.store.dispatch(this.manager.fetchEntity(automaticRole.id));
        this._initForm(automaticRole);
        this.setState({
          showLoading: false
        }, this.refs.form.processEnded());
      });
      this.addMessage({ message: this.i18n('save.recalculate', { name: entity.name }), level: 'info' });
    }, () => {
      // reject
      this.setState({
        showLoading: false
      }, this.refs.form.processEnded());
    });
  }

  render() {
    const { uiKey, entity } = this.props;
    const { showLoading } = this.state;
    //
    return (
      <div>
        <Basic.Confirm ref="recalculate-automatic-role" level="warning"/>
        <form onSubmit={this.save.bind(this, 'CONTINUE')}>
          <Basic.AbstractForm
            ref="form"
            uiKey={uiKey}
            showLoading={entity === null}
            readOnly={!(SecurityManager.hasAuthority('AUTOMATICROLE_CREATE') && Utils.Entity.isNew(entity))}
            style={{ padding: '15px 15px 0 15px' }}>
            <Basic.TextField
              ref="name"
              label={this.i18n('entity.AutomaticRole.name.label')}
              helpBlock={this.i18n('entity.AutomaticRole.name.help')}
              required/>
            <Advanced.EntitySelectBox
              ref="role"
              readOnly={!(SecurityManager.hasAuthority('AUTOMATICROLE_CREATE') && Utils.Entity.isNew(entity))}
              label={this.i18n('entity.AutomaticRole.role.label')}
              helpBlock={this.i18n('entity.AutomaticRole.role.help')}
              entityType="role"
              required/>
          </Basic.AbstractForm>

          {
            this._showConceptWarning()
          }

          <div style={{ margin: '0 15px'}}>
            <Basic.ContentHeader className="marginable" style={{ marginBottom: 0 }}>
              {this.i18n('rules')}
            </Basic.ContentHeader>
          </div>

          <Basic.Alert level="info" text={this.i18n('automaticRoleAttributeSaveFirst')} rendered={Utils.Entity.isNew(entity)} />

          <AutomaticRoleAttributeRuleTable
            manager={this.automaticRoleAttributeRuleManager}
            uiKey={entity ? entity.id : null}
            rendered={!Utils.Entity.isNew(entity)}
            attributeId={entity ? entity.id : null} />

          <Basic.PanelFooter showLoading={showLoading} >
            <Basic.Button type="button" level="link" onClick={this.context.router.goBack}>{this.i18n('button.back')}</Basic.Button>
            <Basic.Button type="button"
              level="warning"
              onClick={this._recalculate.bind(this)}
              rendered={entity && entity.concept === true}>
              { this.i18n('content.automaticRoles.recalculate.label') }
            </Basic.Button>
            <Basic.SplitButton
              level="success"
              title={ this.i18n('button.saveAndContinue') }
              onClick={ this.save.bind(this, 'CONTINUE') }
              showLoading={ showLoading }
              showLoadingIcon
              showLoadingText={ this.i18n('button.saving') }
              rendered={ entity && Utils.Entity.isNew(entity) && SecurityManager.hasAuthority('AUTOMATICROLE_CREATE') }
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

AutomaticRoleAttributeDetail.propTypes = {
  entity: PropTypes.object,
  uiKey: PropTypes.string.isRequired
};
AutomaticRoleAttributeDetail.defaultProps = {
};
