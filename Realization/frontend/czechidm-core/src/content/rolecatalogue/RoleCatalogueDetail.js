import React, { PropTypes } from 'react';
import Joi from 'joi';
import * as Basic from '../../components/basic';
import { RoleCatalogueManager, SecurityManager } from '../../redux';
import _ from 'lodash';

/**
 * Role catalogue detail.
 * Combined node detail and role detail.
 */
export default class RoleCatalogueDetail extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.roleCatalogueManager = new RoleCatalogueManager();
    this.state = {
      showLoading: false,
    };
  }

  getContentKey() {
    return 'content.roleCatalogues';
  }

  componentDidMount() {
    const { entity } = this.props;
    this.selectNavigationItem('role-catalogues');
    if (entity !== undefined) {
      const loadedEntity = _.merge({ }, entity);
      // if exist _embedded - edit role catalogue, if not exist create new
      if (entity._embedded) {
        loadedEntity.parent = entity._embedded.parent.id;
      }
      this.refs.form.setData(loadedEntity);
      // TODO: ?? where put focus?? this.refs.parent.focus();
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
      entity.parent = this.roleCatalogueManager.getSelfLink(entity.parent);
    }

    if (entity.id === undefined) {
      this.context.store.dispatch(this.roleCatalogueManager.createEntity(entity, `${uiKey}-detail`, (createdEntity, error) => {
        this._afterSave(createdEntity, error);
      }));
    } else {
      this.context.store.dispatch(this.roleCatalogueManager.patchEntity(entity, `${uiKey}-detail`, this._afterSave.bind(this)));
    }
  }

  /**
   * Just set showloading to false and set processEnded to form.
   * Call after save/create
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
    this.context.router.replace(`role-catalogues`);
  }

  closeDetail() {
  }

  render() {
    const { uiKey } = this.props;
    const { showLoading } = this.state;

    return (
      <div>
        <form onSubmit={this.save.bind(this)}>
          <Basic.AbstractForm showLoading={showLoading} ref="form" uiKey={uiKey} className="form-horizontal" readOnly={!SecurityManager.hasAuthority('ROLECATALOGUE_WRITE')} >
            <Basic.TextField
              ref="name"
              label={this.i18n('entity.RoleCatalogue.name')}
              required
              validation={Joi.string().min(0).max(255)}/>
              <Basic.SelectBox
                ref="parent"
                label={this.i18n('entity.RoleCatalogue.parent.name')}
                manager={this.roleCatalogueManager}/>
              <Basic.TextArea
                  ref="description"
                  label={this.i18n('entity.RoleCatalogue.description')}
                  validation={Joi.string().max(255)}/>
          </Basic.AbstractForm>

          <Basic.PanelFooter showLoading={showLoading}>
            <Basic.Button type="button" level="link" onClick={this.context.router.goBack}>{this.i18n('button.back')}</Basic.Button>
            <Basic.Button
              type="submit"
              level="success"
              showLoadingIcon
              showLoadingText={this.i18n('button.saving')}
              rendered={SecurityManager.hasAuthority('ROLECATALOGUE_WRITE')}>
              {this.i18n('button.save')}
            </Basic.Button>
          </Basic.PanelFooter>
        </form>
      </div>
    );
  }
}

RoleCatalogueDetail.propTypes = {
  entity: PropTypes.object,
  uiKey: PropTypes.string.isRequired,
};
