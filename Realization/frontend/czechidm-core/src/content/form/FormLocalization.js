import React from 'react';
import { connect } from 'react-redux';
//
import * as Basic from '../../components/basic';
import * as Utils from '../../utils';
import { FormDefinitionManager, FormAttributeManager } from '../../redux';

const manager = new FormDefinitionManager();

/**
 * Form definition - how to add localization
 *
 * TODO: work with locales - now fallback locale is counted as "exists".
 * TODO: check label / help for definition itself exists
 *
 * @author Radek Tomiška
 */
class FormLocalization extends Basic.AbstractContent {

  getContentKey() {
    return 'content.formDefinitions.localization';
  }

  componentDidMount() {
    super.componentDidMount();
  }

  getNavigationKey() {
    return 'forms-localization';
  }

  /**
   * Check key exist in localization
   *
   * TODO: move to LocalizationService
   */
  _localeKeyExists(key) {
    if (!key) {
      return undefined;
    }
    // when key is object, then returned value is undefined, but key exists
    return this.i18n(key) === undefined;
  }

  _getLocalePrefix(formDefinition) {
    return FormDefinitionManager.getLocalizationPrefix(formDefinition);
  }

  /**
   * Returns true when form definition localization was found in localization files (current or fallback locale).
   */
  _localeExists(formDefinition) {
    const key = this._getLocalePrefix(formDefinition);
    if (!key) {
      return undefined;
    }
    //
    return this._localeKeyExists(key);
  }

  /**
   * Returns list of missing attributes (codes) in localization files
   */
  _localeMissingAttributeCodes(formDefinition) {
    const missingAttributeCodes = [];
    if (!formDefinition) {
      return [];
    }
    formDefinition.formAttributes.forEach(attribute => {
      if (!this._localeKeyExists(FormAttributeManager.getLocalizationPrefix(formDefinition, attribute))) {
        missingAttributeCodes.push(Utils.Ui.spinalCase(attribute.code));
      }
    });
    return missingAttributeCodes;
  }

  render() {
    const { entity, showLoading } = this.props;
    if (!entity || showLoading) {
      return (
        <Basic.Loading show isStatic />
      );
    }
    const missingAttributeCodes = this._localeMissingAttributeCodes(entity);
    //
    return (
      <Basic.Panel className={'no-border last'}>
        <Basic.PanelHeader text={this.i18n('title')} />

        <Basic.Alert level="success" text={ this.i18n('check.found')} icon="ok" rendered={ this._localeExists(entity) }/>
        <Basic.Alert level="warning" text={ this.i18n('check.not-found')} icon="info-sign" rendered={ !this._localeExists(entity) }/>

        <Basic.Alert
          level="warning"
          text={ this.i18n('check.attributes-not-found', { attributes: missingAttributeCodes.join(', '), escape: false } )}
          icon="info-sign"
          rendered={ this._localeExists(entity) && missingAttributeCodes.length > 0 }/>


        <Basic.ContentHeader text={ this.i18n('how-to.header' ) } />

        <Basic.PanelBody style={{ padding: 0 }}>

          { this.i18n('how-to.message', { name: entity.module, escape: false } ) }

          <pre style={{ marginTop: 15 }}>
            ...<br/>
            {'  '}"eav": {'\u007b'} <br/>
            {'    '}"{ Utils.Ui.spinalCase(entity.type) }": {'\u007b'}<br/>
            {'      '}"{ Utils.Ui.spinalCase(entity.code) }": {'\u007b'}<br/>
            {'        '}"label": "{ entity.name }",<br/>
            {'        '}"help": "{ entity.description }",<br/>
            {'        '}"attributes": {'\u007b'}<br/>
            {
              entity.formAttributes.map((attribute, index) => {
                return (
                  <div>
                    {'          '}"{ Utils.Ui.spinalCase(attribute.code) }": {'\u007b'}<br/>
                    {'            '}"label": "{ Utils.Ui.escapeDoubleQuotes(attribute.name) }",<br/>
                    {'            '}"help": "{ Utils.Ui.escapeDoubleQuotes(attribute.description) }",<br/>
                    {'            '}"placeholder": "{ Utils.Ui.escapeDoubleQuotes(attribute.placeholder) }"<br/>
                    {'          '}{'\u007d'}{ index + 1 === entity.formAttributes.length ? '' : ','}<br/>
                  </div>
                );
              })
            }
            {'        '}{'\u007d'}<br/>
            {'      '}{'\u007d'}<br/>
            {'    '}{'\u007d'}<br/>
            {'  '}{'\u007d'},<br/>
            ...
          </pre>
        </Basic.PanelBody>
      </Basic.Panel>
    );
  }
}

FormLocalization.propTypes = {
};
FormLocalization.defaultProps = {
};

function select(state, component) {
  const { entityId } = component.params;
  //
  return {
    entity: manager.getEntity(state, entityId),
    showLoading: manager.isShowLoading(state, null, entityId),
  };
}

export default connect(select)(FormLocalization);
