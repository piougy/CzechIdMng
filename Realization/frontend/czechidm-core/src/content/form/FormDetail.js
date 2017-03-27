import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
//
import * as Utils from '../../utils';
import * as Basic from '../../components/basic';
import { FormDefinitionManager, SecurityManager, DataManager } from '../../redux';

const manager = new FormDefinitionManager();

const TYPES_UIKEY = 'typesUiKey';

/**
* Form detail
*/
class FormDetail extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      _showLoading: true
    };
  }

  getContentKey() {
    return 'content.formDefinitions';
  }

  componentDidMount() {
    super.componentDidMount();
    const { entityId } = this.props.params;
    const { isNew } = this.props;
    this.context.store.dispatch(manager.fetchTypes(TYPES_UIKEY, (types) => {
      const values = [];
      if (types && types._embedded) {
        types._embedded.resources.forEach((value) => {
          values.push({
            value,
            niceLabel: value
          });
        });
        this.setState({
          types: values,
          _showLoading: false
        });
      }
    }));
    if (isNew) {
      this.context.store.dispatch(manager.receiveEntity(entityId, { unmodifiable: false }));
    } else {
      this.getLogger().debug(`[FormDetail] loading entity detail [id:${entityId}]`);
      this.context.store.dispatch(manager.fetchEntity(entityId, null, () => {
        // set focus into name
        this.refs.name.focus();
      }));
    }
  }

  getNavigationKey() {
    return 'forms-detail';
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
      _showLoading: true
    }, this.refs.form.processStarted());

    const entity = this.refs.form.getData();

    if (entity.id === undefined) {
      this.context.store.dispatch(manager.createEntity(entity, `${uiKey}-detail`, (createdEntity, error) => {
        this._afterSave(createdEntity, error);
      }));
    } else {
      this.context.store.dispatch(manager.patchEntity(entity, `${uiKey}-detail`, this._afterSave.bind(this)));
    }
  }

  /**
  * Just set showloading to false and set processEnded to form.
  * Call after save/create
  */
  _afterSave(entity, error) {
    const { isNew } = this.props;
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
    if (isNew) {
      this.context.router.replace(`/forms`);
    }
  }

  render() {
    const { uiKey, entity, showLoading } = this.props;
    const { _showLoading, types } = this.state;

    return (
      <form onSubmit={this.save.bind(this)}>
        <Basic.Panel className={Utils.Entity.isNew(entity) ? '' : 'no-border last'}>
          <Basic.PanelHeader text={Utils.Entity.isNew(entity) ? this.i18n('create.header') : this.i18n('content.formDefinitions.detail.title')} />
          <Basic.PanelBody style={Utils.Entity.isNew(entity) ? { paddingTop: 0, paddingBottom: 0 } : { padding: 0 }}>
            <Basic.AbstractForm ref="form" uiKey={uiKey} data={entity} rendered={types !== undefined}
              readOnly={!SecurityManager.hasAuthority('EAVFORMDEFINITIONS_WRITE')}>
            <Basic.TextField
              ref="name"
              label={this.i18n('entity.FormDefinition.name')}
              readOnly={!entity || entity.unmodifiable}
              max={255}
              required/>
            <Basic.EnumSelectBox
              ref="type"
              label={this.i18n('entity.FormDefinition.type')}
              placeholder={this.i18n('entity.FormDefinition.type')}
              required
              readOnly={!entity || entity.unmodifiable}
              options={types}/>
            <Basic.Checkbox
              ref="unmodifiable"
              readOnly
              label={this.i18n('entity.FormDefinition.unmodifiable.label')}
              helpBlock={this.i18n('entity.FormDefinition.unmodifiable.help')}/>
          </Basic.AbstractForm>
          </Basic.PanelBody>
          <Basic.PanelFooter showLoading={showLoading} >
            <Basic.Button type="button" level="link" onClick={this.context.router.goBack}>{this.i18n('button.back')}</Basic.Button>
            <Basic.Button
              type="submit"
              level="success"
              showLoadingIcon
              showLoadingText={this.i18n('button.saving')}
              rendered={SecurityManager.hasAuthority('EAVFORMDEFINITIONS_WRITE')}>
              {this.i18n('button.save')}
            </Basic.Button>
          </Basic.PanelFooter>
        </Basic.Panel>
      </form>
    );
  }
}

FormDetail.propTypes = {
  uiKey: PropTypes.string,
  definitionManager: PropTypes.object,
  isNew: PropTypes.bool
};
FormDetail.defaultProps = {
  isNew: false,
};

function select(state, component) {
  const { entityId } = component.params;
  //
  return {
    entity: manager.getEntity(state, entityId),
    showLoading: manager.isShowLoading(state, null, entityId),
    types: DataManager.getData(state, TYPES_UIKEY)
  };
}

export default connect(select)(FormDetail);
