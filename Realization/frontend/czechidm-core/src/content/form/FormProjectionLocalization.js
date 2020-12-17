import React from 'react';
import { connect } from 'react-redux';
import _ from 'lodash';
//
import * as Basic from '../../components/basic';
import * as Utils from '../../utils';
import { FormProjectionManager, FormDefinitionManager } from '../../redux';

const manager = new FormProjectionManager();

/**
 * Form projection - how to add localization
 *
 * TODO: work with locales - now fallback locale is counted as "exists".
 * TODO: check label / help for definition itself exists
 *
 * @author Radek Tomiška
 * @since 10.2.0
 */
class FormProjectionLocalization extends Basic.AbstractContent {

  getContentKey() {
    return 'content.form-projections.localization';
  }

  getNavigationKey() {
    return 'form-projection-localization';
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

  _getLocalePrefix(projection) {
    return FormDefinitionManager.getLocalizationPrefix({ ...projection, type: 'form-projection' });
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

  render() {
    const { entity, showLoading } = this.props;
    if (!entity || showLoading) {
      return (
        <Basic.Loading show isStatic />
      );
    }
    //
    return (
      <Basic.Panel className="no-border last">
        <Basic.PanelHeader text={ this.i18n('title') } />

        <Basic.Alert level="success" text={ this.i18n('check.found')} icon="ok" rendered={ this._localeExists(entity) }/>
        <Basic.Alert level="warning" text={ this.i18n('check.not-found')} icon="info-sign" rendered={ !this._localeExists(entity) }/>

        <Basic.ContentHeader text={ this.i18n('how-to.header') } />

        <Basic.PanelBody style={{ padding: 0 }}>

          { this.i18n('how-to.message', { name: entity.module ? entity.module : 'core', escape: false }) }
          {/* it's ugly, but works */}
          <pre style={{ marginTop: 15 }}>
            ...<br/>
            {'  '}"eav": {'\u007b'} <br/>
            {'    '}"form-projection": {'\u007b'}<br/>
            {'      '}"{ Utils.Ui.spinalCase(entity.code) }": {'\u007b'}<br/>
            {'        '}"label": "{ entity.code }",<br/>
            {'        '}"help": "{ _.trim(entity.description) }",<br/>
            {'        '}"icon": "fa:user-plus",<br/>
            {'        '}"level": "success"<br/>
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

FormProjectionLocalization.propTypes = {
};
FormProjectionLocalization.defaultProps = {
};

function select(state, component) {
  const { entityId } = component.match.params;
  //
  return {
    entity: manager.getEntity(state, entityId),
    showLoading: manager.isShowLoading(state, null, entityId),
  };
}

export default connect(select)(FormProjectionLocalization);
