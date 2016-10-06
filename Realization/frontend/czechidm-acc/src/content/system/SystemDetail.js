import React, { PropTypes } from 'react';
import Helmet from 'react-helmet';
//
import { Basic, Managers, Utils } from 'czechidm-core';
import { SystemManager } from '../../redux';

/**
 * Target system detail content
 */
export default class SystemDetail extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.manager = new SystemManager();
  }

  getContentKey() {
    return 'acc:content.system.detail';
  }

  componentDidMount() {
    const { entity } = this.props;
    if (entity !== undefined) {
      this.refs.form.setData(entity);
      this.refs.name.focus();
    }
  }

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

    if (entity.parent) {
      entity.parent = this.manager.getSelfLink(entity.parent);
    }

    if (entity.id === undefined) {
      this.context.store.dispatch(this.manager.createEntity(entity, `${uiKey}-detail`, (createdEntity, error) => {
        this._afterSave(createdEntity, error);
      }));
    } else {
      this.context.store.dispatch(this.manager.patchEntity(entity, `${uiKey}-detail`, this._afterSave.bind(this)));
    }
  }

  _afterSave(entity, error) {
    if (error) {
      this.addError(error);
      return;
    }
    this.addMessage({ message: this.i18n('save.success', { name: entity.name }) });
    this.context.router.replace(`systems`);
  }

  closeDetail() {
  }

  render() {
    const { uiKey, entity } = this.props;
    return (
      <div>
        <Helmet title={Utils.Entity.isNew(entity) ? this.i18n('create.header') : this.i18n('edit.title')} />

        <form onSubmit={this.save.bind(this)}>
          <Basic.Panel className={Utils.Entity.isNew(entity) ? '' : 'no-border last'}>
            <Basic.PanelHeader text={Utils.Entity.isNew(entity) ? this.i18n('create.header') : this.i18n('basic')} />

            <Basic.PanelBody style={Utils.Entity.isNew(entity) ? { paddingTop: 0, paddingBottom: 0 } : { padding: 0 }}>
              <Basic.AbstractForm ref="form" uiKey={uiKey} className="form-horizontal" readOnly={!Managers.SecurityManager.hasAuthority('SYSTEM_WRITE')} >
                <Basic.TextField
                  ref="name"
                  label={this.i18n('acc:entity.System.name')}
                  required/>
                <Basic.TextArea
                  ref="description"
                  label={this.i18n('acc:entity.System.description')}/>
                <Basic.Checkbox
                  ref="virtual"
                  label={this.i18n('acc:entity.System.virtual')}/>
                <Basic.Checkbox
                  ref="disabled"
                  label={this.i18n('acc:entity.System.disabled')}/>
              </Basic.AbstractForm>
            </Basic.PanelBody>
            <Basic.PanelFooter>
              <Basic.Button type="button" level="link" onClick={this.context.router.goBack}>{this.i18n('button.back')}</Basic.Button>
              <Basic.Button
                type="submit"
                level="success"
                showLoadingIcon
                showLoadingText={this.i18n('button.saving')}
                rendered={Managers.SecurityManager.hasAuthority('SYSTEM_WRITE')}>
                {this.i18n('button.save')}
              </Basic.Button>
            </Basic.PanelFooter>
          </Basic.Panel>
        </form>
      </div>
    );
  }
}

SystemDetail.propTypes = {
  entity: PropTypes.object,
  uiKey: PropTypes.string.isRequired,
};
SystemDetail.defaultProps = {
};
