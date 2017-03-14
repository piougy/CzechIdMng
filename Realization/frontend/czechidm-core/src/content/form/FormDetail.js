import React, { PropTypes } from 'react';
import * as Basic from '../../components/basic';
import { FormAttributeManager } from '../../redux';
import _ from 'lodash';
//
import FormAttributeTable from './FormAttributeTable';

/**
 * Form attribute manager for saving attributes
 * @type {FormAttributeManager}
 */
const attributeManager = new FormAttributeManager();

/**
* Form detail
*/
export default class FormDetail extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      showLoading: false,
    };
  }

  getContentKey() {
    return 'content.formDefinitions';
  }

  componentDidMount() {
    const { entity } = this.props;
    this.selectNavigationItem('forms');
    if (entity !== undefined) {
      const loadedEntity = _.merge({ }, entity);
      this.refs.form.setData(loadedEntity);
      // focus is not neccessary
      // this.refs.code.focus();
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
    this.context.store.dispatch(this.roleCatalogueManager.clearEntities());
    this.addMessage({ message: this.i18n('save.success', { name: entity.name }) });
    this.context.router.replace(`role-catalogues`);
  }

  closeDetail() {
  }

  render() {
    const { uiKey, entity } = this.props;
    const { showLoading } = this.state;

    return (
      <div>
        <form onSubmit={this.save.bind(this)}>
          <Basic.AbstractForm showLoading={showLoading} ref="form" uiKey={uiKey} readOnly style={{ padding: '15px 15px 0 15px' }}>
            <Basic.TextField
              ref="name"
              label={this.i18n('entity.FormDefinition.name')}
              readOnly
              required
              max={255}/>
            <Basic.TextField
              ref="type"
              readOnly
              label={this.i18n('entity.FormDefinition.type')}
              max={255}
              required/>
          </Basic.AbstractForm>

          <FormAttributeTable
            attributeManager={attributeManager}
            uiKey="form-attributes-tables" formDefinition={entity} />

          <Basic.PanelFooter showLoading={showLoading} >
            <Basic.Button type="button" level="link" onClick={this.context.router.goBack}>{this.i18n('button.back')}</Basic.Button>
          </Basic.PanelFooter>
        </form>
      </div>
    );
  }
}

FormDetail.propTypes = {
  entity: PropTypes.object,
  uiKey: PropTypes.string.isRequired,
  definitionManager: PropTypes.object
};
