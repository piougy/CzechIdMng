import React, { PropTypes } from 'react';
import * as Basic from 'app/components/basic';
import { OrganizationManager, SecurityManager } from 'core/redux';

/**
 * Organization detail content
 */
export default class OrganizationDetail extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.organizationManager = new OrganizationManager();
  }

  getContentKey() {
    return 'content.organizations';
  }

  componentDidMount() {
    const { organization } = this.props;
    this.selectNavigationItem('organizations');

    if (organization !== undefined) {
      const loadedOrganization = organization;
      if (organization._embedded) {
        loadedOrganization.parent = organization._embedded.parent.id;
      }
      this.refs.form.setData(loadedOrganization);
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
    const entity = this.refs.form.getData();

    if (entity.parent) {
      entity.parent = this.organizationManager.getSelfLink(entity.parent);
    }

    if (entity.id === undefined) {
      this.context.store.dispatch(this.organizationManager.createEntity(entity, `${uiKey}-detail`, (createdEntity, error) => {
        this._afterSave(createdEntity, error);
      }));
    } else {
      this.context.store.dispatch(this.organizationManager.patchEntity(entity, `${uiKey}-detail`, this._afterSave.bind(this)));
    }
  }

  _afterSave(entity, error) {
    if (error) {
      this.addError(error);
      return;
    }
    this.addMessage({ message: this.i18n('save.success', { name: entity.name }) });
    this.context.router.replace(`organizations/`);
  }

  closeDetail() {
  }

  render() {
    const { uiKey } = this.props;
    return (
      <div>
        <form onSubmit={this.save.bind(this)}>
            <Basic.AbstractForm ref="form" uiKey={uiKey} className="form-horizontal" readOnly={!SecurityManager.hasAuthority('ORGANIZATION_WRITE')} >
              <Basic.TextField
                ref="name"
                label={this.i18n('entity.Organization.name')}
                required/>
              <Basic.SelectBox
                ref="parent"
                label={this.i18n('entity.Organization.parent.name')}
                manager={this.organizationManager}
                required/>
              <Basic.Checkbox
                ref="disabled"
                label={this.i18n('entity.Organization.disabled')}/>
            </Basic.AbstractForm>

            <Basic.PanelFooter>
              <Basic.Button type="button" level="link" onClick={this.context.router.goBack}>{this.i18n('button.back')}</Basic.Button>
              <Basic.Button
                type="submit"
                level="success"
                showLoadingIcon
                showLoadingText={this.i18n('button.saving')}
                rendered={SecurityManager.hasAuthority('ORGANIZATION_WRITE')}>
                {this.i18n('button.save')}
              </Basic.Button>
            </Basic.PanelFooter>
          </form>
      </div>
    );
  }
}

OrganizationDetail.propTypes = {
  organization: PropTypes.object,
  uiKey: PropTypes.string.isRequired,
};
OrganizationDetail.defaultProps = {
};
