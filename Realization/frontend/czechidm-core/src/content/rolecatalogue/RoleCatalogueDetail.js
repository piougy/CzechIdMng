import React, { PropTypes } from 'react';
//
import * as Basic from '../../components/basic';
import * as Utils from '../../utils';
import { RoleCatalogueManager, SecurityManager } from '../../redux';


/**
* Role catalogue detail.
* Combined node detail and role detail.
*
* @author Odřej Kopr
* @author Radek Tomiška
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
      this.refs.form.setData(entity);
      this.refs.code.focus();
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
    //
    const entity = this.refs.form.getData();
    if (entity.id === undefined) {
      this.context.store.dispatch(this.roleCatalogueManager.createEntity(entity, `${uiKey}-detail`, (createdEntity, error) => {
        this._afterSave(createdEntity, error);
      }));
    } else {
      this.context.store.dispatch(this.roleCatalogueManager.updateEntity(entity, `${uiKey}-detail`, this._afterSave.bind(this)));
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
    this.context.store.dispatch(this.roleCatalogueManager.clearEntities());
    this.addMessage({ message: this.i18n('save.success', { name: entity.name }) });
    this.context.router.replace(`role-catalogues`);
  }

  render() {
    const { uiKey, entity } = this.props;
    const { showLoading } = this.state;

    return (
      <div>
        <form onSubmit={this.save.bind(this)}>
          <Basic.PanelBody>
            <Basic.AbstractForm
              showLoading={showLoading}
              ref="form"
              uiKey={uiKey} r
              eadOnly={!SecurityManager.hasAuthority(Utils.Entity.isNew(entity) ? 'ROLECATALOGUE_CREATE' : 'ROLECATALOGUE_UPDATE')}
              style={{ padding: 0 }} >
              <Basic.Row>
                <div className="col-lg-2">
                  <Basic.TextField
                    ref="code"
                    label={this.i18n('entity.RoleCatalogue.code.name')}
                    helpBlock={this.i18n('entity.RoleCatalogue.code.help')}
                    required
                    min={0}
                    max={255}/>
                </div>
                <div className="col-lg-10">
                  <Basic.TextField
                    ref="name"
                    label={this.i18n('entity.RoleCatalogue.name.name')}
                    helpBlock={this.i18n('entity.RoleCatalogue.name.help')}
                    required
                    min={0}
                    max={255}/>
                </div>
              </Basic.Row>
              <Basic.Row>
                <div className="col-lg-4">
                  <Basic.TextField
                    ref="urlTitle"
                    label={this.i18n('entity.RoleCatalogue.urlTitle')}/>
                </div>
                <div className="col-lg-8">
                  <Basic.TextField
                    ref="url"
                    label={this.i18n('entity.RoleCatalogue.url')}/>
                </div>
              </Basic.Row>
              <Basic.SelectBox
                ref="parent"
                label={this.i18n('entity.RoleCatalogue.parent.name')}
                manager={this.roleCatalogueManager}/>
              <Basic.TextArea
                ref="description"
                label={this.i18n('entity.RoleCatalogue.description')}
                max={255}/>
            </Basic.AbstractForm>
          </Basic.PanelBody>

          <Basic.PanelFooter showLoading={showLoading}>
            <Basic.Button type="button" level="link" onClick={this.context.router.goBack}>{this.i18n('button.back')}</Basic.Button>
            <Basic.Button
              type="submit"
              level="success"
              showLoadingIcon
              showLoadingText={this.i18n('button.saving')}
              rendered={SecurityManager.hasAuthority(Utils.Entity.isNew(entity) ? 'ROLECATALOGUE_CREATE' : 'ROLECATALOGUE_UPDATE')}>
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
