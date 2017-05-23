import React from 'react';
import { connect } from 'react-redux';
import Helmet from 'react-helmet';
import Joi from 'joi';
//
import * as Basic from '../../components/basic';
import * as Utils from '../../utils';
import { SecurityManager, FormAttributeManager, FormDefinitionManager } from '../../redux';
import PersistentTypeEnum from '../../enums/PersistentTypeEnum';

/**
 * Form attribute manager for saving attributes
 * @type {FormAttributeManager}
 *
 * @author Ondřej Kopr
 * @author Radek Tomiška
 */
const attributeManager = new FormAttributeManager();

const formDefinitionManager = new FormDefinitionManager();

/**
* Form attribute detail
*/
class FormAttributeDetail extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      _showLoading: false,
    };
  }

  getContentKey() {
    return 'content.formAttributes';
  }

  componentDidMount() {
    super.componentDidMount();
    const { entityId } = this.props.params;

    if (this._getIsNew()) {
      this.context.store.dispatch(attributeManager.receiveEntity(entityId,
        {
          persistentType: PersistentTypeEnum.TEXT,
          seq: 0,
          unmodifiable: false,
          formDefinition: this._getFormDefinitionId()
        }, null, () => {
          this.refs.name.focus();
        }));
    } else {
      this.getLogger().debug(`[FormAttributeDetail] loading entity detail [id:${entityId}]`);
      this.context.store.dispatch(attributeManager.fetchEntity(entityId, null, () => {
        this.refs.code.focus();
      }));
    }
  }

  getNavigationKey() {
    return 'forms';
  }

  /**
   * Function check if exist params new
   */
  _getIsNew() {
    const { query } = this.props.location;
    return (query) ? query.new : null;
  }

  _getFormDefinitionId() {
    const { query } = this.props.location;
    return (query) ? query.formDefinition : null;
  }

  /**
   * Default save method that catch save event from form.
   */
  save(event) {
    const { uiKey } = this.props;

    if (event) {
      event.preventDefault();
    }
    if (!this.refs.form.isFormValid()) {
      return;
    }

    this.setState({
      _showLoading: true
    }, this.refs.form.processStarted());

    const entity = this.refs.form.getData();
    const saveEntity = {
      ...entity
    };

    if (entity.id === undefined) {
      saveEntity.formDefinition = formDefinitionManager.getSelfLink(this._getFormDefinitionId());
      this.context.store.dispatch(attributeManager.createEntity(saveEntity, `${uiKey}-detail`, (createdEntity, error) => {
        this._afterSave(createdEntity, error);
      }));
    } else {
      this.context.store.dispatch(attributeManager.patchEntity(saveEntity, `${uiKey}-detail`, this._afterSave.bind(this)));
    }
  }

  _isUnmodifiable() {
    const { entity } = this.props;
    return entity ? entity.unmodifiable : false;
  }

  /**
   * Method set showLoading to false and if is'nt error then show success message
   */
  _afterSave(entity, error) {
    if (error) {
      this.setState({
        _showLoading: false
      }, this.refs.form.processEnded());
      this.addError(error);
      return;
    }
    this.setState({
      _showLoading: false
    });
    this.addMessage({ message: this.i18n('save.success', { name: entity.name }) });
    if (this._getIsNew()) {
      this.context.router.goBack();
    }
  }

  render() {
    const { entity, showLoading } = this.props;
    const { _showLoading } = this.state;
    let loadedEntity = null;
    if (entity) {
      loadedEntity = {
        ...entity,
        seq: '' + entity.seq
        // persistentType: PersistentTypeEnum.findSymbolForKey(entity.persistentType)
      };
    }

    return (
      <div>
        {
          this._getIsNew()
          ?
          <Helmet title={this.i18n('create.title')} />
          :
          <Helmet title={this.i18n('edit.title')} />
        }
        <Basic.Confirm ref="confirm-delete" level="danger"/>
        {
          !entity
          ||
          <Basic.PageHeader>
            <Basic.Icon value="fa:wpforms"/>
            {' '}
            {
              this._getIsNew()
              ?
              this.i18n('create.header')
              :
              <span>{entity.name} <small>{this.i18n('edit.header')}</small></span>
            }
          </Basic.PageHeader>
        }

        <Basic.Panel>
            <form onSubmit={this.save.bind(this)}>
              <Basic.AbstractForm ref="form" data={loadedEntity} showLoading={showLoading || _showLoading}
                readOnly={!SecurityManager.hasAuthority(Utils.Entity.isNew(entity) ? 'EAVFORMATTRIBUTES_CREATE' : 'EAVFORMATTRIBUTES_UPDATE')} style={{ padding: '15px 15px 0 15px' }}>
                <Basic.Row>
                  <div className="col-lg-4">
                    <Basic.TextField
                      ref="code"
                      label={this.i18n('entity.FormAttribute.code')}
                      readOnly={this._isUnmodifiable()}
                      required
                      max={255}/>
                  </div>
                  <div className="col-lg-8">
                    <Basic.TextField
                      ref="name"
                      label={this.i18n('entity.FormAttribute.name')}
                      max={255}
                      required/>
                  </div>
                </Basic.Row>
                <Basic.Row>
                  <div className="col-lg-4">
                    <Basic.EnumSelectBox
                      ref="persistentType"
                      enum={PersistentTypeEnum}
                      readOnly={this._isUnmodifiable()}
                      label={this.i18n('entity.FormAttribute.persistentType')}
                      max={255}
                      useSymbol={false}
                      required/>
                  </div>
                  <div className="col-lg-8">
                    <Basic.TextField
                      ref="placeholder"
                      label={this.i18n('entity.FormAttribute.placeholder')}
                      max={255}/>
                  </div>
                </Basic.Row>
                <Basic.Row>
                  <div className="col-lg-4">
                    <Basic.TextField
                      ref="seq"
                      label={this.i18n('entity.FormAttribute.seq')}
                      validation={Joi.number().required().integer().min(0).max(99999)}/>
                  </div>
                  <div className="col-lg-8">
                    <Basic.TextField
                      ref="defaultValue"
                      label={this.i18n('entity.FormAttribute.defaultValue')}
                      max={255}/>
                  </div>
                </Basic.Row>
                <Basic.Row>
                  <div className="col-lg-12">
                    <Basic.TextArea
                      ref="description"
                      label={this.i18n('entity.FormAttribute.description')}
                      max={2000}/>
                  </div>
                </Basic.Row>
                <Basic.Checkbox
                  ref="required"
                  readOnly={this._isUnmodifiable()}
                  label={this.i18n('entity.FormAttribute.required')}/>
                <Basic.Checkbox
                  ref="readonly"
                  readOnly={this._isUnmodifiable()}
                  label={this.i18n('entity.FormAttribute.readonly')}/>
                <Basic.Checkbox
                  ref="confidential"
                  readOnly={this._isUnmodifiable()}
                  label={this.i18n('entity.FormAttribute.confidential')}/>
                <Basic.Checkbox
                  ref="multiple"
                  readOnly={this._isUnmodifiable()}
                  label={this.i18n('entity.FormAttribute.multiple')}/>
                <Basic.Checkbox
                  ref="unmodifiable"
                  readOnly
                  label={this.i18n('entity.FormAttribute.unmodifiable.label')}
                  helpBlock={this.i18n('entity.FormAttribute.unmodifiable.help')}/>
              </Basic.AbstractForm>
              <Basic.PanelFooter showLoading={showLoading || _showLoading} >
                <Basic.Button type="button" level="link" onClick={this.context.router.goBack}>{this.i18n('button.back')}</Basic.Button>
                <Basic.Button
                  type="submit"
                  level="success"
                  showLoadingIcon
                  showLoadingText={this.i18n('button.saving')}
                  rendered={SecurityManager.hasAuthority(Utils.Entity.isNew(entity) ? 'EAVFORMATTRIBUTES_CREATE' : 'EAVFORMATTRIBUTES_UPDATE')}>
                  {this.i18n('button.save')}
                </Basic.Button>
              </Basic.PanelFooter>
            </form>
        </Basic.Panel>

      </div>
    );
  }
}

FormAttributeDetail.propTypes = {
};
FormAttributeDetail.defaultProps = {
};

function select(state, component) {
  const { entityId } = component.params;

  //
  return {
    entity: attributeManager.getEntity(state, entityId),
    showLoading: attributeManager.isShowLoading(state, null, entityId)
  };
}

export default connect(select)(FormAttributeDetail);
