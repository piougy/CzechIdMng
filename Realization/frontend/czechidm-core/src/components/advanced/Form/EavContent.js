import React from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import classnames from 'classnames';
import Immutable from 'immutable';
//
import * as Basic from '../../basic';
import * as Utils from '../../../utils';
import { DataManager, FormDefinitionManager, SecurityManager } from '../../../redux';
import FormInstance from '../../../domain/FormInstance';
import EavForm from './EavForm';

const formDefinitionManager = new FormDefinitionManager();
const dataManager = new DataManager();

/**
 * Content with eav form
 *
 * @author Radek Tomiška
 */
class EavContent extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      error: null,
      validationErrors: null,
      showLoadingKeys: new Immutable.Set() // currenrt save form
    };
  }

  getComponentKey() {
    return this.props.contentKey;
  }

  getUiKey() {
    const { entityId, uiKey, formableManager } = this.props;
    //
    if (uiKey) {
      return `${uiKey}-${entityId}`;
    }
    return formableManager.getFormableUiKey(null, entityId);
  }

  componentDidMount() {
    super.componentDidMount();
    // load definition and values
    const { entityId, formableManager, formInstances } = this.props;
    //
    if (!formInstances) {
      this.context.store.dispatch(formableManager.fetchFormInstances(entityId, this.getUiKey(), (_formInstances, error) => {
        if (error) {
          this.addErrorMessage({ hidden: true, level: 'info' }, error);
          this.setState({ error });
        } else {
          this.getLogger().debug(`[EavContent]: Loaded [${ _formInstances.size }] form definitions`);
        }
      }));
    } else {
      let _formInstances = new Immutable.Map();
      formInstances.forEach(formInstance => {
        _formInstances = _formInstances.set(formInstance.formDefinition.code, new FormInstance(formInstance));
      });
      this.context.store.dispatch(dataManager.receiveData(this.getUiKey(), _formInstances));
    }
  }

  /**
   * Focus attribute (or first attribute) in eav form of given owner (or first).
   *
   * @param  {string} [attributeCode=null] [optional] - attribute code in form definition
   * @param  {string} [ownerId=null]       [optional] - form instance of given owner
   * @since 10.7.0
   */
  focus(attributeCode = null, ownerId = null) {
    const { _formInstances } = this.props;
    if (!_formInstances || _formInstances.length === 0) {
      return;
    }
    //
    let focusFormInstance = null;
    for (const formInstance of _formInstances.values()) {
      if (ownerId == null) {
        focusFormInstance = formInstance;
        break;
      } else if (formInstance.getOwnerId() === ownerId) {
        focusFormInstance = formInstance;
        break;
      }
    }
    //
    if (focusFormInstance) {
      const eavFormRef = this._createFormRef(focusFormInstance.getDefinition().code);
      if (eavFormRef) {
        this.refs[eavFormRef].focus(attributeCode);
      }
    }
  }

  _createFormRef(definitionCode) {
    return `eav-form-${definitionCode}`;
  }

  save(definitionCode, event) {
    if (event) {
      event.preventDefault();
    }
    const eavFormRef = this._createFormRef(definitionCode);
    if (!this.refs[eavFormRef].isValid()) {
      return;
    }
    //
    const { entityId, formableManager, _formInstances } = this.props;
    const { showLoadingKeys } = this.state;
    //
    const filledFormValues = this.refs[eavFormRef].getValues();
    this.getLogger().debug(`[EavContent]: Saving form [${ definitionCode }]`);

    this.setState({
      showLoadingKeys: showLoadingKeys.add(definitionCode)
    }, () => {
      //
      // push new values int redux - prevent to lost new values after submit
      const formInstance = _formInstances.get(definitionCode).setValues(filledFormValues);
      this.context.store.dispatch(dataManager.receiveData(this.getUiKey(), _formInstances.set(definitionCode, formInstance)));
      // save values
      this.context.store.dispatch(
        formableManager.saveFormValues(
          entityId,
          definitionCode,
          filledFormValues,
          this.getUiKey(),
          (savedFormInstance, error) => {
            const _showLoadingKeys = this.state.showLoadingKeys;
            if (error) {
              let validationErrors = null;
              if (error.statusEnum === 'FORM_INVALID') {
                validationErrors = error.parameters ? error.parameters.attributes : null;
              }
              this.setState({
                validationErrors,
                showLoadingKeys: _showLoadingKeys.delete(definitionCode)
              }, () => {
                // focus the first invalid component
                if (validationErrors && validationErrors.length > 0) {
                  const firstValidationError = validationErrors[0];
                  this.refs[eavFormRef].focus(firstValidationError.attributeCode);
                }
                this.addError(error);
              });
            } else {
              this.setState({
                validationErrors: null,
                showLoadingKeys: _showLoadingKeys.delete(definitionCode)
              }, () => {
                const entity = formableManager.getEntity(this.context.store.getState(), entityId);
                this.addMessage({ message: this.i18n('save.success', { name: formableManager.getNiceLabel(entity) }) });
                this.getLogger().debug(`[EavForm]: Form [${definitionCode}] saved`);
              });
            }
          }
        )
      );
    });
  }

  isValid() {
    const { _formInstances, showDefinitions, _showLoading } = this.props;
    if (_showLoading) {
      return false;
    }
    //
    let isAllValid = true;
    _formInstances.forEach(_formInstance => {
      const definition = _formInstance.getDefinition();
      //
      if (!showDefinitions || showDefinitions.has(definition.code) || showDefinitions.has(definition.id)) {
        const eavFormRef = this._createFormRef(definition.code);
        if (!this.refs[eavFormRef].isValid()) {
          isAllValid = false;
        }
      }
    });
    //
    return isAllValid;
  }

  getValues() {
    const { _formInstances, showDefinitions } = this.props;
    //
    const filledFormInstances = [];
    _formInstances.forEach(_formInstance => {
      const definitionCode = _formInstance.getDefinition().code;
      //
      if (!showDefinitions || showDefinitions.has(definitionCode) || showDefinitions.has(_formInstance.getDefinition().id)) {
        const eavFormRef = this._createFormRef(definitionCode);
        //
        const filledFormValues = this.refs[eavFormRef].getValues();
        this.getLogger().debug(`[EavContent]: Get form instance [${ definitionCode }] values`);
        //
        // push new values int redux - prevent to lost new values after submit
        filledFormInstances.push({
          formDefinition: {
            id: _formInstance.getDefinition().id,
            code: definitionCode
          },
          values: filledFormValues
        });
      }
    });
    //
    return filledFormInstances;
  }

  render() {
    const {
      rendered,
      formableManager,
      showSaveButton,
      showAttributesOnly,
      _formInstances,
      showDefinitions,
      userContext
    } = this.props;
    const { error, showLoadingKeys } = this.state;
    const validationErrors = this.state.validationErrors || this.props.validationErrors;
    //
    if (!rendered) {
      return null;
    }
    //
    let content = null;
    if (error && error.statusEnum !== 'FORM_INVALID') {
      // loading eav definition failed
      content = (
        <div style={{ paddingTop: 15 }}>
          <Basic.Alert level="info" text={ this.i18n('error.notFound') } className="no-margin"/>
        </div>
      );
    } else if (!_formInstances) {
      // eav form is loaded from BE
      content = (
        <Basic.Loading isStatic showLoading/>
      );
    } else if (_formInstances.size === 0) {
      content = (
        <Basic.Div style={{ paddingTop: 15 }} rendered={ showDefinitions === null || showDefinitions === undefined }>
          <Basic.Alert level="info" text={this.i18n('error.notFound')} className="no-margin"/>
        </Basic.Div>
      );
    } else {
      // form instances are ready
      let index = 0;
      content = [..._formInstances.map(_formInstance => {
        const definitionCode = _formInstance.getDefinition().code;
        //
        let _showSaveButton = false; // some attribute is editable
        _formInstance.getAttributes().forEach(attribute => {
          if (!attribute.readonly) { // TODO: hidden
            _showSaveButton = true;
          }
        });

        let _renderAttributes = null;
        let renderDefinition = true;
        if (showDefinitions) {
          if (showDefinitions.has(definitionCode)) {
            _renderAttributes = showDefinitions.get(definitionCode);
          } else if (showDefinitions.has(_formInstance.getDefinition().id)) {
            _renderAttributes = showDefinitions.get(_formInstance.getDefinition().id);
          } else {
            renderDefinition = false;
          }
        }
        if (!renderDefinition) {
          // form definition is not rendered
          return null;
        }
        // resolve definition name
        let formDefinitionName = this.i18n('header'); //  RT: back compatibilty header
        if (_formInstance.getDefinition().name !== 'default') {
          // by locale
          formDefinitionName = formDefinitionManager.getLocalization(_formInstance.getDefinition(), 'label', _formInstance.getDefinition().name);
        }
        //
        if (showAttributesOnly) {
          const eavForm = (
            <EavForm
              ref={ this._createFormRef(definitionCode) }
              formInstance={ _formInstance }
              readOnly={ !showSaveButton }
              validationErrors={ validationErrors }
              formableManager={ formableManager }
              showAttributes={ _renderAttributes }
              showLoading={ showLoadingKeys.has(definitionCode) }/>
          );
          if (!SecurityManager.hasAllAuthorities(['FORMDEFINITION_UPDATE', 'FORMATTRIBUTE_UPDATE'], userContext)
              || !this.isDevelopment()
              || !_formInstance.getDefinition()
              || !_formInstance.getDefinition().id) {
            return eavForm;
          }
          //
          return (
            <Basic.Panel style={{ border: '1px dashed #ccc' }}>
              <Basic.PanelHeader
                text={ formDefinitionName }
                style={{ color: '#ccc', borderBottom: '1px dashed #ccc' }}/>
              <Basic.PanelBody>
                { eavForm }
              </Basic.PanelBody>
              <Basic.PanelFooter style={{ color: '#ccc', borderTop: '1px dashed #ccc' }}>
                <Basic.Button
                  type="button"
                  level="link"
                  onClick={ () => this.context.history.push(`/form-definitions/${ _formInstance.getDefinition().id }/attributes`) }
                  title={ this.i18n('component.advanced.EavForm.attributes.link.title') }>
                  { this.i18n('component.advanced.EavForm.attributes.link.label') }
                </Basic.Button>
              </Basic.PanelFooter>
            </Basic.Panel>
          );
        }
        index += 1;
        //
        return (
          <form className="abstract-form" onSubmit={ this.save.bind(this, definitionCode) }>
            <Basic.Panel className={
              classnames({
                last: index === _formInstances.size
              })}>

              <Basic.PanelHeader text={ formDefinitionName }/>

              <Basic.Alert
                icon="info-sign"
                text={ formDefinitionManager.getLocalization(_formInstance.getDefinition(), 'help', _formInstance.getDefinition().description) }
                style={{ marginBottom: 0 }}/>

              <Basic.PanelBody showLoading={ showLoadingKeys.has(definitionCode) }>
                <EavForm
                  ref={ this._createFormRef(definitionCode) }
                  formInstance={ _formInstance }
                  readOnly={ !showSaveButton }
                  validationErrors={ validationErrors }
                  formableManager={ formableManager }
                  showAttributes={ _renderAttributes }/>
              </Basic.PanelBody>

              <Basic.PanelFooter rendered={ _showSaveButton && showSaveButton && _formInstance.getAttributes().size > 0 }>
                <Basic.Button
                  type="button"
                  level="link"
                  rendered={
                    SecurityManager.hasAllAuthorities(['FORMDEFINITION_UPDATE', 'FORMATTRIBUTE_UPDATE'], userContext)
                    &&
                    this.isDevelopment()
                  }
                  onClick={ () => this.context.history.push(`/form-definitions/${ _formInstance.getDefinition().id }/attributes`) }
                  title={ this.i18n('component.advanced.EavForm.attributes.link.title') }>
                  { this.i18n('component.advanced.EavForm.attributes.link.label') }
                </Basic.Button>
                <Basic.Button
                  type="submit"
                  level="success"
                  rendered={ _formInstance.getAttributes().size > 0 }
                  showLoadingIcon
                  showLoadingText={ this.i18n('button.saving') }
                  showLoading={ showLoadingKeys.has(definitionCode) }>
                  { this.i18n('button.save') }
                </Basic.Button>
              </Basic.PanelFooter>
            </Basic.Panel>
          </form>
        );
      }).values()];
    }

    return (
      <Basic.Div>
        { content }
      </Basic.Div>
    );
  }
}

EavContent.propTypes = {
  /**
   * UI identifier - it's used as key in store (saving, loading ...)
   * Manager's formable uiKey is used as default.
   */
  uiKey: PropTypes.string,
  /**
   * Parent entity identifier
   */
  entityId: PropTypes.string.isRequired,
  /**
   * Manager controlls owners extended attributes, e.g. identityManager, roleManager.
   * Enable additional features, which depends on concrete manager (e.g. download attachment).
   */
  formableManager: PropTypes.object.isRequired,
  /**
   * Show / hide save button
   */
  showSaveButton: PropTypes.bool,
  /**
   * Render given definitions and attributes only. Render all definitions and atributes otherwise.
   */
  showDefinitions: PropTypes.arrayOf(PropTypes.object),
  /**
   * List of InvalidFormAttributeDto
   */
  validationErrors: PropTypes.arrayOf(PropTypes.object),
  /**
   * Internal properties (loaded by redux)
   */
  _formInstances: PropTypes.object, // immutable map
  _showLoading: PropTypes.bool
};
EavContent.defaultProps = {
  showSaveButton: false,
  showAttributesOnly: false,
  _formInstances: null,
  _showLoading: false,
  rendered: true
};

function select(state, component) {
  const entityId = component.entityId;
  const uiKey = component.uiKey ? `${component.uiKey}-${entityId}` : component.formableManager.getFormableUiKey(null, entityId);
  //
  return {
    userContext: state.security.userContext,
    _showLoading: Utils.Ui.isShowLoading(state, uiKey),
    _formInstances: DataManager.getData(state, uiKey)
  };
}

export default connect(select, null, null, { forwardRef: true })(EavContent);
