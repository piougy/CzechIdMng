import PropTypes from 'prop-types';
import React from 'react';
import _ from 'lodash';
//
import * as Basic from '../../../components/basic';
import * as Advanced from '../../../components/advanced';
import * as Utils from '../../../utils';
import { NotificationTemplateManager, SecurityManager } from '../../../redux';

const manager = new NotificationTemplateManager();

/**
* Notification emplate detail.
*
* @author Ondřej Kopr
* @author Radek Tomiška
*/
export default class TemplateDetail extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    //
    this.state = {
      showLoading: false,
      bodyHtml: null
    };
  }

  getContentKey() {
    return 'content.notificationTemplate';
  }

  getNavigationKey() {
    return 'notification-templates';
  }

  componentDidMount() {
    super.componentDidMount();
    //
    if (this.refs.form) {
      this.refs.codeable.focus();
    }
  }

  // @Deprecated - since V10 ... replaced by dynamic key in Route
  // UNSAFE_componentWillReceiveProps(nextProps) {
  //   if (!this.props.entity || nextProps.entity.id !== this.props.entity.id || nextProps.entity.id !== this.refs.form.getData().id) {
  //     // this._initForm(nextProps.entity);
  //   }
  // }

  _getIsNew() {
    return this.props.isNew;
  }

  /**
  * @Deprecated - since V10 ... replaced by dynamic key in Route
  * Method for basic initial form
  _initForm(entity) {
    if (entity && this.refs.form) {
      const loadedEntity = _.merge({}, entity);
      //
      this.refs.form.setData(loadedEntity);
    }
  }
  */

  /**
  * Default save method that catch save event from form.
  */
  save(operation, event) {
    const { uiKey } = this.props;
    const { bodyHtml } = this.state;

    if (event) {
      event.preventDefault();
    }
    if (!this.refs.form.isFormValid()) {
      return;
    }

    this.setState({
      showLoading: true
    }, () => {
      this.refs.form.processStarted();
      // get data from form
      const entity = this.refs.form.getData();
      entity.code = entity.codeable.code;
      entity.name = entity.codeable.name;
      if (bodyHtml) {
        entity.bodyHtml = bodyHtml;
      }
      //
      if (entity.id === undefined) {
        this.context.store.dispatch(manager.createEntity(entity, `${ uiKey }-detail`, (createdEntity, error) => {
          this._afterSave(createdEntity, error);
        }));
      } else if (manager.supportsPatch()) {
        this.context.store.dispatch(manager.patchEntity(entity, `${ uiKey }-detail`, (savedEntity, error) => {
          this._afterSave(entity, error);
        }));
      } else {
        this.context.store.dispatch(manager.updateEntity(entity, `${ uiKey }-detail`, (savedEntity, error) => {
          this._afterSave(entity, error);
        }));
      }
    });
  }

  /**
  * Method set showLoading to false and if is'nt error then show success message
  */
  _afterSave(entity, error) {
    if (error) {
      this.setState({
        showLoading: false
      }, () => {
        this.refs.form.processEnded();
        this.addError(error);
      });
      return;
    }
    //
    this.setState({
      showLoading: false
    }, () => {
      this.refs.form.processEnded();
      this.addMessage({ message: this.i18n('save.success', { name: entity.name }) });
      this.context.history.replace('/notification/templates');
    });
  }

  onRedeployOrBackup(actionValue, event) {
    const { uiKey, entity } = this.props;
    if (event) {
      event.preventDefault();
    }
    const entities = [];
    entities.push(entity);
    this.refs[`confirm-${ actionValue }`].show(
      this.i18n(`action.${ actionValue }.message`, { count: 1, record: manager.getNiceLabel(entity), records: manager.getNiceLabel(entity) }),
      this.i18n(`action.${ actionValue }.header`, { count: 1, records: manager.getNiceLabel(entity) })
    ).then(() => {
      // TODO: same method as bulk operation? Or create new for one?
      this.context.store.dispatch(manager.notificationBulkOperationForEntities(entities, actionValue, uiKey));
    }, () => {
      // nothing
    });
  }

  getParameters() {
    const { entity } = this.props;
    let components = {};
    if (entity.parameter) {
      const suggestions = [];
      const parameters = _.split(entity.parameter, ',');
      parameters.forEach((parameter) => {
        suggestions.push({ text: _.trim(parameter), value: _.trim(parameter) });
      });
      components = {
        separator: ' ',
        trigger: '$',
        caseSensitive: true,
        mentionClassName: 'mention-className',
        dropdownClassName: 'dropdown-className',
        optionClassName: 'option-className',
        suggestions
      };
    }
    return components;
  }

  onBodyHtmlChange(event) {
    this.setState({
      bodyHtml: event.currentTarget.value
    });
  }

  render() {
    const { uiKey, entity } = this.props;
    const { showLoading, bodyHtml } = this.state;
    return (
      <Basic.Div>
        <Basic.Confirm ref="confirm-backup" level="danger"/>
        <Basic.Confirm ref="confirm-redeploy" level="danger"/>
        <form onSubmit={ this.save.bind(this) }>
          <Basic.Panel showLoading={ this.props.showLoading }>
            <Basic.PanelBody style={{ paddingTop: 0, paddingBottom: 0 }}>
              <Basic.AbstractForm
                data={ entity }
                ref="form"
                uiKey={ uiKey }
                readOnly={
                  !SecurityManager.hasAuthority(Utils.Entity.isNew(entity) ? 'NOTIFICATIONTEMPLATE_CREATE' : 'NOTIFICATIONTEMPLATE_UPDATE')
                }>

                <Advanced.CodeableField
                  ref="codeable"
                  codeLabel={ this.i18n('entity.NotificationTemplate.code') }
                  nameLabel={ this.i18n('entity.NotificationTemplate.name') }
                  codeReadOnly={ entity.unmodifiable } />

                <Basic.TextField
                  ref="module"
                  readOnly={ entity.unmodifiable }
                  max={ 255 }
                  label={ this.i18n('entity.NotificationTemplate.module.label') }
                  helpBlock={ this.i18n('entity.NotificationTemplate.module.help') } />
                <Basic.TextField
                  ref="sender"
                  label={ this.i18n('entity.NotificationTemplate.sender') }
                  max={ 255 } />
                <Basic.TextField
                  ref="subject"
                  label={ this.i18n('entity.NotificationTemplate.subject') }
                  required
                  max={ 255 }/>
                <Basic.Checkbox
                  readOnly={entity.unmodifiable}
                  ref="unmodifiable"
                  label={ this.i18n('entity.NotificationTemplate.unmodifiable.name') }
                  helpBlock={ this.i18n('entity.NotificationTemplate.unmodifiable.help') }/>
                <Basic.TextField
                  ref="parameter"
                  readOnly={ entity.unmodifiable }
                  max={ 255 }
                  label={ this.i18n('entity.NotificationTemplate.parameter.name') }
                  helpBlock={ this.i18n('entity.NotificationTemplate.parameter.help') } />
                <Basic.TextArea
                  ref="bodyText"
                  label={ this.i18n('entity.NotificationTemplate.bodyText') } />

                <Basic.Tabs>
                  <Basic.Tab
                    eventKey={ 1 }
                    title={ this.i18n('entity.NotificationTemplate.bodyHtml.name') }
                    className="bordered">
                    <Basic.Div style={{ padding: 10 }}>
                      <Basic.TextArea
                        ref="bodyHtml"
                        value={ bodyHtml || entity.bodyHtml }
                        rows={ 20 }
                        onChange={ this.onBodyHtmlChange.bind(this) }/>
                    </Basic.Div>
                  </Basic.Tab>
                  <Basic.Tab
                    eventKey={ 2 }
                    title={ this.i18n('entity.Notification.message.renderedHtmlMessage') }
                    className="bordered">
                    <Basic.Div style={{ padding: 10, minHeight: 450 }}>
                      <span dangerouslySetInnerHTML={{ __html: bodyHtml || entity.bodyHtml || '' }}/>
                    </Basic.Div>
                  </Basic.Tab>
                </Basic.Tabs>

              </Basic.AbstractForm>
            </Basic.PanelBody>
            <Basic.PanelFooter showLoading={ showLoading } >
              <Basic.Button type="button" level="link" onClick={ this.context.history.goBack }>
                { this.i18n('button.back') }
              </Basic.Button>

              <Basic.SplitButton
                level="success"
                title={ this.i18n('button.save') }
                onClick={ this.save.bind(this, 'CONTINUE') }
                showLoadingIcon
                showLoadingText={ this.i18n('button.saving') }
                rendered={ SecurityManager.hasAuthority(Utils.Entity.isNew(entity) ? 'NOTIFICATIONTEMPLATE_CREATE' : 'NOTIFICATIONTEMPLATE_UPDATE') }
                pullRight
                dropup>
                <Basic.MenuItem eventKey="1" onClick={ this.onRedeployOrBackup.bind(this, 'redeploy') }>
                  { this.i18n('action.redeploy.action') }
                </Basic.MenuItem>
                <Basic.MenuItem eventKey="2" onClick={ this.onRedeployOrBackup.bind(this, 'backup') }>
                  { this.i18n('action.backup.action') }
                </Basic.MenuItem>
              </Basic.SplitButton>

            </Basic.PanelFooter>
            {/* onEnter action - is needed because SplitButton is used instead standard submit button */}
            <input type="submit" className="hidden"/>
          </Basic.Panel>
        </form>
      </Basic.Div>
    );
  }
}

TemplateDetail.propTypes = {
  entity: PropTypes.object,
  uiKey: PropTypes.string.isRequired,
  isNew: PropTypes.bool
};
TemplateDetail.defaultProps = {
};
