import PropTypes from 'prop-types';
import React from 'react';
import { connect } from 'react-redux';
import Immutable from 'immutable';
import _ from 'lodash';
//
import * as Utils from '../../utils';
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import * as Domain from '../../domain';
import { FormProjectionManager, FormDefinitionManager, FormAttributeManager, DataManager } from '../../redux';
import AbstractEnum from '../../enums/AbstractEnum';

const manager = new FormProjectionManager();
const formDefinitionManager = new FormDefinitionManager();
const formAttributeManager = new FormAttributeManager();

/**
 * Modified IdentityAttributeEnum - singular properties
 *
 * TODO: DRY, but how to generalize enum + static methods ...
 *
 * @author Radek Tomiška
 */
class IdentityAttributeEnum extends AbstractEnum {

  static getNiceLabel(key) {
    return super.getNiceLabel(`core:enums.IdentityAttributeEnum.${key}`);
  }

  static getHelpBlockLabel(key) {
    return super.getNiceLabel(`core:enums.IdentityAttributeEnum.helpBlock.${key}`);
  }

  static findKeyBySymbol(sym) {
    return super.findKeyBySymbol(this, sym);
  }

  static findSymbolByKey(key) {
    return super.findSymbolByKey(this, key);
  }

  static getField(key) {
    if (!key) {
      return null;
    }

    const sym = super.findSymbolByKey(this, key);

    switch (sym) {
      case this.USERNAME: {
        return 'username';
      }
      case this.EXTERNAL_CODE: {
        return 'externalCode';
      }
      case this.DISABLED: {
        return 'disabled';
      }
      case this.FIRSTNAME: {
        return 'firstName';
      }
      case this.LASTNAME: {
        return 'lastName';
      }
      case this.EMAIL: {
        return 'email';
      }
      case this.PHONE: {
        return 'phone';
      }
      case this.TITLE_BEFORE: {
        return 'titleBefore';
      }
      case this.TITLE_AFTER: {
        return 'titleAfter';
      }
      case this.DESCRIPTION: {
        return 'description';
      }
      case this.STATE: {
        return 'state';
      }
      case this.PASSWORD: {
        return 'password';
      }
      default: {
        return null;
      }
    }
  }

  static getEnum(field) {
    if (!field) {
      return null;
    }

    switch (field) {
      case 'username': {
        return this.USERNAME;
      }
      case 'externalCode': {
        return this.EXTERNAL_CODE;
      }
      case 'disabled': {
        return this.DISABLED;
      }
      case 'firstName': {
        return this.FIRSTNAME;
      }
      case 'lastName': {
        return this.LASTNAME;
      }
      case 'email': {
        return this.EMAIL;
      }
      case 'phone': {
        return this.PHONE;
      }
      case 'titleBefore': {
        return this.TITLE_BEFORE;
      }
      case 'titleAfter': {
        return this.TITLE_AFTER;
      }
      case 'description': {
        return this.DESCRIPTION;
      }
      case 'state': {
        return this.STATE;
      }
      case 'password': {
        return this.PASSWORD;
      }
      default: {
        return null;
      }
    }
  }

  static getLevel(key) {
    if (!key) {
      return null;
    }

    const sym = super.findSymbolByKey(this, key);

    switch (sym) {
      default: {
        return 'default';
      }
    }
  }
}

IdentityAttributeEnum.USERNAME = Symbol('USERNAME');
IdentityAttributeEnum.EXTERNAL_CODE = Symbol('EXTERNAL_CODE');
IdentityAttributeEnum.DISABLED = Symbol('DISABLED');
IdentityAttributeEnum.FIRSTNAME = Symbol('FIRSTNAME');
IdentityAttributeEnum.LASTNAME = Symbol('LASTNAME');
IdentityAttributeEnum.EMAIL = Symbol('EMAIL');
IdentityAttributeEnum.PHONE = Symbol('PHONE');
IdentityAttributeEnum.TITLE_BEFORE = Symbol('TITLE_BEFORE');
IdentityAttributeEnum.TITLE_AFTER = Symbol('TITLE_AFTER');
IdentityAttributeEnum.DESCRIPTION = Symbol('DESCRIPTION');
IdentityAttributeEnum.STATE = Symbol('STATE');
IdentityAttributeEnum.PASSWORD = Symbol('PASSWORD');

/**
* Form projection detail.
*
* @author Radek Tomiška
* @since 10.2.0
*/
class FormProjectionDetail extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
    };
  }

  getContentKey() {
    return 'content.form-projections';
  }

  componentDidMount() {
    super.componentDidMount();
    //
    const { entityId } = this.props.match.params;
    const { isNew, entity } = this.props;
    //
    if (isNew) {
      this._initProjection(entity || {
        route: '/form/identity-projection',
        ownerType: 'eu.bcvsolutions.idm.core.model.entity.IdmIdentity'
      });
    } else {
      this.getLogger().debug(`[FormDetail] loading entity detail [id:${ entityId }]`);
      this.context.store.dispatch(manager.fetchEntity(entityId, null, (projection) => {
        this._initProjection(projection);
      }));
    }
    this.context.store.dispatch(manager.fetchSupportedRoutes());
  }

  _initProjection(projection) {
    const { supportedRoutes } = this.props;
    let _formDefinitions = new Immutable.OrderedMap();
    //
    if (projection) {
      // basic fields
      try {
        projection.basicFields =
          !projection.basicFields
          ||
          JSON.parse(projection.basicFields).map(f => IdentityAttributeEnum.findKeyBySymbol(IdentityAttributeEnum.getEnum(f)));
      } catch (syntaxError) {
        // nothing
      }
      // form definitions and attributes
      try {
        // immutable map
        const formDefinitions = JSON.parse(projection.formDefinitions);
        if (formDefinitions && formDefinitions.length > 0) {
          formDefinitions.forEach(formDefinition => {
            if (formDefinition.definition) {
              _formDefinitions = _formDefinitions.set(formDefinition.definition, new Immutable.OrderedSet());
              if (formDefinition.attributes) {
                formDefinition.attributes.forEach(attribute => {
                  _formDefinitions = _formDefinitions.set(formDefinition.definition, _formDefinitions.get(formDefinition.definition).add(attribute));
                });
              }
            }
          });
        }
      } catch (syntaxError) {
        // nothing
      }
    }
    //
    this.setState({
      projection,
      formDefinitions: _formDefinitions,
      formProjectionRoute: !supportedRoutes || !supportedRoutes.has(projection.route) ? null : supportedRoutes.get(projection.route)
    }, () => {
      this.refs.code.focus();
    });
  }

  getNavigationKey() {
    return 'form-projection-detail';
  }

  save(event) {
    const { uiKey } = this.props;
    const { formDefinitions } = this.state;
    //
    if (event) {
      event.preventDefault();
    }
    if (!this.refs.form.isFormValid()) {
      return;
    }
    if (this.refs.formInstance) {
      if (!this.refs.formInstance.isValid()) {
        return;
      }
    }
    //
    this.refs.form.processStarted();
    const entity = this.refs.form.getData();
    // basic fields
    if (entity.basicFields) {
      if (entity.basicFields.length === 0) {
        entity.basicFields = null;
      } else {
        entity.basicFields = JSON.stringify(entity.basicFields.map(f => IdentityAttributeEnum.getField(IdentityAttributeEnum.findSymbolByKey(f))));
      }
    }
    // form definitions and attributes
    const _formDefinitions = [];
    formDefinitions.forEach((formDefinition, definitionId) => {
      if (definitionId) {
        _formDefinitions.push({
          definition: definitionId,
          attributes: formDefinitions.get(definitionId).toArray()
        });
      }
    });
    entity.formDefinitions = JSON.stringify(_formDefinitions);
    //
    // transform properties
    if (this.refs.formInstance) {
      entity.properties = this.refs.formInstance.getProperties();
    }
    // save
    if (entity.id === undefined) {
      this.context.store.dispatch(manager.createEntity(entity, `${ uiKey }-detail`, (createdEntity, error) => {
        this._afterSave(createdEntity, error);
      }));
    } else {
      this.context.store.dispatch(manager.patchEntity(entity, `${ uiKey }-detail`, this._afterSave.bind(this)));
    }
  }

  /**
  * Just set showloading to false and set processEnded to form.
  * Call after save/create
  */
  _afterSave(entity, error) {
    const { isNew } = this.props;
    if (error) {
      this.refs.form.processEnded();
      this.addError(error);
      return;
    }
    //
    this.refs.form.processEnded();
    this.addMessage({ message: this.i18n('save.success', { code: entity.code }) });
    if (isNew) {
      this.context.history.replace(`/forms/form-projections`);
    }
  }

  onChangeFormDefinition(id, formDefinition) {
    const { formDefinitions } = this.state;
    //
    this.setState({
      formDefinitions: formDefinitions.remove(id).set(formDefinition.id, new Immutable.OrderedSet())
    });
  }

  onChangeFormAttributes(id, attributes) {
    const { formDefinitions } = this.state;
    //
    this.setState({
      formDefinitions: formDefinitions.set(id, new Immutable.OrderedSet(!attributes ? [] : attributes.map(a => a.id)))
    });
  }

  addFormDefinition() {
    const { formDefinitions } = this.state;
    //
    this.setState({
      formDefinitions: formDefinitions.set(null, new Immutable.OrderedSet())
    });
  }

  removeFormDefinition(id) {
    const { formDefinitions } = this.state;
    //
    this.setState({
      formDefinitions: formDefinitions.remove(id)
    });
  }

  onChangeRoute(formProjectionRoute) {
    this.setState({
      formProjectionRoute
    });
  }

  _getSupportedRoutes() {
    const { supportedRoutes } = this.props;
    //
    const _supportedRoutes = [];
    if (!supportedRoutes) {
      return [];
    }
    //
    supportedRoutes.forEach(route => {
      _supportedRoutes.push(this._toRouteOption(route));
    });
    //
    return _supportedRoutes;
  }

  _toRouteOption(route) {
    return {
      niceLabel: formAttributeManager.getLocalization(route.formDefinition, null, 'label', route.id),
      value: route.id,
      description: formAttributeManager.getLocalization(route.formDefinition, null, 'help', route.description),
      formDefinition: route.formDefinition
    };
  }

  render() {
    const { uiKey, showLoading, _permissions } = this.props;
    const { projection, formDefinitions, formProjectionRoute } = this.state;
    //
    const _supportedRoutes = this._getSupportedRoutes();
    let formInstance = new Domain.FormInstance({});
    if (formProjectionRoute && formProjectionRoute.formDefinition && projection) {
      formInstance = new Domain.FormInstance(formProjectionRoute.formDefinition).setProperties(projection.properties);
    }
    const showProperties = formInstance
      && formProjectionRoute
      && formProjectionRoute.formDefinition
      && formProjectionRoute.formDefinition.formAttributes.length > 0;
    //
    return (
      <form onSubmit={ this.save.bind(this) }>
        <Basic.Panel className={ Utils.Entity.isNew(projection) ? '' : 'no-border last' }>
          <Basic.PanelHeader text={
              Utils.Entity.isNew(projection)
              ?
              this.i18n('create.header')
              :
              this.i18n('content.form-projections.detail.title')
          }/>
          <Basic.PanelBody style={ Utils.Entity.isNew(projection) ? { paddingTop: 0, paddingBottom: 0 } : { padding: 0 } }>
            <Basic.AbstractForm
              ref="form"
              uiKey={ uiKey }
              data={ projection }
              readOnly={ !manager.canSave(projection, _permissions) }>
              <Basic.TextField
                ref="ownerType"
                label={ this.i18n('entity.FormProjection.ownerType.label') }
                max={ 255 }
                required
                hidden/>
              <Basic.TextField
                ref="code"
                label={ this.i18n('entity.FormProjection.code.label') }
                helpBlock={ this.i18n('entity.FormProjection.code.help') }
                max={ 255 }
                required/>
              <Basic.TextField
                ref="module"
                label={ this.i18n('entity.FormProjection.module.label') }
                max={ 255 }
                helpBlock={ this.i18n('entity.FormProjection.module.help') }/>
              <Basic.EnumSelectBox
                enum={ IdentityAttributeEnum }
                ref="basicFields"
                label={ this.i18n('entity.FormProjection.basicFields.label') }
                helpBlock={ this.i18n('entity.FormProjection.basicFields.help') }
                placeholder={ this.i18n('entity.FormProjection.basicFields.placeholder') }
                multiSelect/>
              <Basic.LabelWrapper
                label={ this.i18n('entity.FormProjection.formDefinitions.label') }
                helpBlock={ this.i18n('entity.FormProjection.formDefinitions.help') }>
                {
                  !formDefinitions
                  ||
                  [
                    ...formDefinitions.map((attributes, definitionId) => (
                      <Basic.Div style={{ display: 'flex' }}>
                        <Basic.Div style={{ flex: 2, paddingRight: 5 }}>
                          <Basic.SelectBox
                            label={ null }
                            manager={ formDefinitionManager }
                            forceSearchParameters={
                              new Domain.SearchParameters()
                                .setFilter('type', [
                                  'eu.bcvsolutions.idm.core.model.entity.IdmIdentity',
                                  'eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract'
                                ])
                                .setSort('code')
                            }
                            value={ definitionId }
                            placeholder={ this.i18n('entity.FormProjection.formDefinitions.placeholder') }
                            style={{ marginBottom: 5 }}
                            onChange={ (f) => this.onChangeFormDefinition(definitionId, f) }
                            readOnly={ definitionId !== null }
                            clearable={ false }/>
                        </Basic.Div>
                        <Basic.Div style={{ flex: 3, paddingRight: 5 }}>
                          <Basic.SelectBox
                            label={ null }
                            manager={ formAttributeManager }
                            value={ attributes.toArray() }
                            forceSearchParameters={ new Domain.SearchParameters().setFilter('definitionId', definitionId) }
                            placeholder={ this.i18n('entity.FormProjection.formDefinitions.attributes.placeholder') }
                            multiSelect
                            style={{ marginBottom: 5 }}
                            onChange={ (attrs) => this.onChangeFormAttributes(definitionId, attrs) }
                            readOnly={ definitionId === null || !manager.canSave(projection, _permissions) }/>
                        </Basic.Div>
                        <Basic.Button
                          level="danger"
                          icon="fa:trash"
                          title={ this.i18n('entity.FormProjection.formDefinitions.button.remove.title') }
                          titlePlacement="left"
                          onClick={ () => this.removeFormDefinition(definitionId) }
                          disabled={ !manager.canSave(projection, _permissions) }/>
                      </Basic.Div>
                    )).values()
                  ]
                }

                <Basic.Button
                  level="success"
                  className="btn-xs"
                  icon="fa:plus"
                  title={ this.i18n('entity.FormProjection.formDefinitions.button.add.title') }
                  style={{ marginBottom: 5 }}
                  onClick={ () => this.addFormDefinition() }
                  disabled={
                    !manager.canSave(projection, _permissions)
                    ||
                    (formDefinitions && _.includes(formDefinitions.keySeq().toArray(), null))
                  }>
                  { this.i18n('entity.FormProjection.formDefinitions.button.add.label') }
                </Basic.Button>
              </Basic.LabelWrapper>

              <Basic.EnumSelectBox
                ref="route"
                options={ _supportedRoutes }
                onChange={ this.onChangeRoute.bind(this) }
                label={ this.i18n('entity.FormProjection.route.label') }
                helpBlock={ formProjectionRoute ? formProjectionRoute.description : this.i18n('entity.FormProjection.route.help') }
                clearable={ false }
                required/>

              <Basic.Div style={ showProperties ? {} : { display: 'none' }}>
                <Advanced.EavForm
                  ref="formInstance"
                  formInstance={ formInstance }
                  useDefaultValue={ Utils.Entity.isNew(projection) }
                  readOnly={ !manager.canSave(projection, _permissions) }
                  showAttributes/>
              </Basic.Div>

              <Basic.TextArea
                ref="description"
                label={ this.i18n('entity.FormProjection.description.label') }
                rows={ 4 }
                max={ 1000 }/>
              <Basic.Checkbox
                ref="disabled"
                label={ this.i18n('entity.FormProjection.disabled.label') }
                helpBlock={ this.i18n('entity.FormProjection.disabled.help') }/>
            </Basic.AbstractForm>
          </Basic.PanelBody>
          <Basic.PanelFooter showLoading={ showLoading } >
            <Basic.Button type="button" level="link" onClick={ this.context.history.goBack }>
              { this.i18n('button.back') }
            </Basic.Button>
            <Basic.Button
              type="submit"
              level="success"
              showLoading={ showLoading }
              showLoadingIcon
              showLoadingText={ this.i18n('button.saving') }
              rendered={ manager.canSave(projection, _permissions) }>
              { this.i18n('button.save') }
            </Basic.Button>
          </Basic.PanelFooter>
        </Basic.Panel>
      </form>
    );
  }
}

FormProjectionDetail.propTypes = {
  uiKey: PropTypes.string,
  definitionManager: PropTypes.object,
  isNew: PropTypes.bool,
  _permissions: PropTypes.arrayOf(PropTypes.string)
};
FormProjectionDetail.defaultProps = {
  isNew: false,
  _permissions: null
};

function select(state, component) {
  const { entityId } = component.match.params;
  //
  return {
    entity: manager.getEntity(state, entityId),
    showLoading: manager.isShowLoading(state, null, entityId),
    _permissions: manager.getPermissions(state, null, entityId),
    supportedRoutes: DataManager.getData(state, FormProjectionManager.UI_KEY_SUPPORTED_ROUTES)
  };
}

export default connect(select)(FormProjectionDetail);
