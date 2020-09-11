import EntityManager from './EntityManager';
import { FormAttributeService } from '../../services';
import ComponentService from '../../services/ComponentService';
import FormDefinitionManager from './FormDefinitionManager';
import * as Utils from '../../utils';

/**
 * Eav form attributes.
 *
 * @author Ondřej Kopr
 * @author Radek Tomiška
 */
export default class FormAttributeManager extends EntityManager {

  constructor() {
    super();
    this.service = new FormAttributeService();
    this.componentService = new ComponentService();
  }

  getService() {
    return this.service;
  }

  getEntityType() {
    return 'FormAttribute';
  }

  getCollectionType() {
    return 'formAttributes';
  }

  /**
   * Extended nice label.
   *
   * @param  {entity} entity
   * @param  {boolean} showDefinition form definition label will be rendered.
   * @return {string}
   * @since 10.2.0
   */
  getNiceLabel(entity, showDefinition = false) {
    return this.getService().getNiceLabel(entity, showDefinition);
  }

  /**
   * Returns form value component by form attribute's face and persistent type
   *
   * @param  {FormAttribute} attribute
   * @return {object} component or null
   */
  getFormComponent(attribute) {
    if (!attribute) {
      return null;
    }
    //
    return this.componentService.getComponentDefinitions(ComponentService.FORM_ATTRIBUTE_RENDERER).find(component => {
      if (!component.persistentType) {
        // persistent type is required
        return false;
      }
      // component and attribute face type can be empty => default face by persistent type is used
      const _attributeFaceType = attribute.faceType || attribute.persistentType;
      const _componentFaceType = component.faceType || component.persistentType;
      // persistent and face type has to fit
      return attribute.persistentType.toLowerCase() === component.persistentType.toLowerCase()
          && (_attributeFaceType.toLowerCase() === _componentFaceType.toLowerCase() || component.faceType === undefined);
    });
  }

  getLocalization(formDefinition, formAttribute, property, defaultValue = null) {
    const _formDefinition = formDefinition || (formAttribute._embedded ? formAttribute._embedded.formDefinition : null);
    //
    let key = null;
    let keyWithModule = null;
    let localizeMessage = null;
    if (_formDefinition) {
      key = `${ FormAttributeManager.getLocalizationPrefix(_formDefinition, formAttribute, false) }.${ property }`;
      keyWithModule = `${ FormAttributeManager.getLocalizationPrefix(_formDefinition, formAttribute, true) }.${ property }`;
      localizeMessage = this.i18n(keyWithModule);
      //
      // try to find common attributes localization in core by default
      if (formAttribute !== null && (key === null || key === localizeMessage || keyWithModule === localizeMessage)) {
        key = `eav.attributes.${ Utils.Ui.spinalCase(formAttribute.code) }.${ property }`;
        localizeMessage = this.i18n(key);
      }
    }
    //
    // if localized message is exactly same as key, that means message isn't localized
    if (key === null || key === localizeMessage || keyWithModule === localizeMessage) {
      return defaultValue;
    }
    return localizeMessage;
  }

  /**
   * Returns prefix for localization
   * ''
   * @param  {object} formDefinition
   * @param  {string} formAttribute optional - if attribute is not given, then formDefinition prefix is returned
   * @return {string}
   */
  static getLocalizationPrefix(formDefinition, formAttribute, withModule = true) {
    if (!formDefinition && !formAttribute) {
      return undefined;
    }
    const _formDefinition = formDefinition || (formAttribute._embedded ? formAttribute._embedded.formDefinition : null);
    if (!_formDefinition) {
      return undefined;
    }
    //
    if (!formAttribute) {
      // definition prefix only
      return FormDefinitionManager.getLocalizationPrefix(_formDefinition, withModule);
    }
    const definitionPrefix = FormDefinitionManager.getLocalizationPrefix(_formDefinition, false);
    const resolvedModule = formAttribute.module || _formDefinition.module; // attribute module has higher priority
    const modulePrefix = (withModule && resolvedModule) ? `${ resolvedModule }:` : '';
    //
    return `${ modulePrefix }${ definitionPrefix }.attributes.${ Utils.Ui.spinalCase(formAttribute.code) }`;
  }
}
