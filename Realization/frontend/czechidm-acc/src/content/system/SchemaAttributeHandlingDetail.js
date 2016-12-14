import React, { PropTypes } from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
//
import { Basic, Utils, Domain} from 'czechidm-core';
import { SystemEntityHandlingManager, SchemaAttributeHandlingManager, SchemaAttributeManager} from '../../redux';

const uiKey = 'schema-attribute-handling';
const manager = new SchemaAttributeHandlingManager();
const systemEntityHandlingManager = new SystemEntityHandlingManager();
const schemaAttributeManager = new SchemaAttributeManager();

class SchemaAttributeHandlingDetail extends Basic.AbstractTableContent {

  constructor(props, context) {
    super(props, context);
  }

  getManager() {
    return manager;
  }

  getUiKey() {
    return uiKey;
  }

  getContentKey() {
    return 'acc:content.system.attributeHandlingDetail';
  }

  componentWillReceiveProps(nextProps) {
    const { attributeId} = nextProps.params;
    if (attributeId && attributeId !== this.props.params.attributeId) {
      this._initComponent(nextProps);
    }
  }

  // Did mount only call initComponent method
  componentDidMount() {
    this._initComponent(this.props);
  }

  /**
   * Method for init component from didMount method and from willReceiveProps method
   * @param  {properties of component} props For didmount call is this.props for call from willReceiveProps is nextProps.
   */
  _initComponent(props) {
    const { attributeId} = props.params;
    if (this._getIsNew(props)) {
      this.setState({attribute: {systemEntityHandling: props.location.query.entityHandlingId,
        system: props.location.query.systemId}});
    } else {
      this.context.store.dispatch(this.getManager().fetchEntity(attributeId));
    }
    this.selectNavigationItems(['sys-systems', 'system-entities-handling']);
  }

  _getIsNew(nextProps) {
    const { query } = nextProps ? nextProps.location : this.props.location;
    return (query) ? query.new : null;
  }

  save(event) {
    const formEntity = this.refs.form.getData();
    formEntity.systemEntityHandling = systemEntityHandlingManager.getSelfLink(formEntity.systemEntityHandling);
    formEntity.schemaAttribute = schemaAttributeManager.getSelfLink(formEntity.schemaAttribute);
    //
    super.save(formEntity, event);
  }

  afterSave(entity, error) {
    if (!error) {
      if (this._getIsNew()) {
        this.addMessage({ message: this.i18n('create.success', { name: entity.name }) });
      } else {
        this.addMessage({ message: this.i18n('save.success', { name: entity.name }) });
      }
      this.context.router.goBack();
    } else {
      this.addError(error);
    }
    super.afterSave();
  }

  closeDetail() {
    this.refs.form.processEnded();
  }

  _uidChanged(event) {
    const checked = event.currentTarget.checked;
    // I need set value direct to checkbox (this event is run befor state is set, but I need him in render mothod now)
    this.refs.uid.setState({value: checked}, () => {
      if (checked) {
        this.refs.idmPropertyName.setValue(null);
        this.refs.entityAttribute.setValue(false);
        this.refs.confidentialAttribute.setValue(false);
        this.refs.extendedAttribute.setValue(false);
      }
      this.forceUpdate();
    });
  }

  _disabledChanged(key, event) {
    const checked = event.currentTarget.checked;
    // I need set value direct to checkbox (this event is run befor state is set, but I need him in render mothod now)
    this.refs[key].setState({value: checked}, () => {
      this.forceUpdate();
    });
  }

  _checkboxChanged(key, disableKey, event) {
    const checked = event.currentTarget.checked;
    // I need set value direct to checkbox (this event is run befor state is set, but I need him in render mothod now)
    if (checked) {
      this.refs[disableKey].setState({value: false});
    }
    this.refs[key].setState({value: checked}, () => {
      this.forceUpdate();
    });
  }

  _schemaAttributeChange(value) {
    if (!this.refs.name.getValue()) {
      this.refs.name.setValue(value.name);
    }
  }

  render() {
    const { _showLoading, _attribute} = this.props;
    const isNew = this._getIsNew();
    const attribute = isNew ? this.state.attribute : _attribute;
    const forceSearchParameters = new Domain.SearchParameters().setFilter('systemId', attribute && attribute.system ? attribute.system : Domain.SearchParameters.BLANK_UUID);

    const _isDisabled = this.refs.disabledAttribute ? this.refs.disabledAttribute.getValue() : false;
    const _isUid = this.refs.uid ? this.refs.uid.getValue() : false;
    const _isEntityAttribute = this.refs.entityAttribute ? this.refs.entityAttribute.getValue() : false;
    const _isExtendedAttribute = this.refs.extendedAttribute ? this.refs.extendedAttribute.getValue() : false;

    const _isRequiredIdmField = (_isEntityAttribute || _isExtendedAttribute) && !_isUid && !_isDisabled;

    return (
      <div>
        <Helmet title={this.i18n('title')} />
        <Basic.Confirm ref="confirm-delete" level="danger"/>

        <Basic.ContentHeader>
          <Basic.Icon value="list-alt"/>
          {' '}
          <span dangerouslySetInnerHTML={{ __html: this.i18n('header', attribute ? { name: attribute.idmPropertyName} : {})}}/>
        </Basic.ContentHeader>
        <form onSubmit={this.save.bind(this)}>
          <Basic.Panel className="no-border last">
            <Basic.AbstractForm ref="form" data={attribute} showLoading={_showLoading} className="form-horizontal">
              <Basic.Checkbox
                ref="disabledAttribute"
                onChange={this._disabledChanged.bind(this, 'disabledAttribute')}
                tooltip={this.i18n('acc:entity.SchemaAttributeHandling.disabledAttribute.tooltip')}
                label={this.i18n('acc:entity.SchemaAttributeHandling.disabledAttribute.label')}/>
              <Basic.SelectBox
                ref="systemEntityHandling"
                manager={systemEntityHandlingManager}
                label={this.i18n('acc:entity.SchemaAttributeHandling.systemEntityHandling')}
                readOnly
                required/>
              <Basic.SelectBox
                ref="schemaAttribute"
                manager={schemaAttributeManager}
                forceSearchParameters={forceSearchParameters}
                onChange={this._schemaAttributeChange.bind(this)}
                label={this.i18n('acc:entity.SchemaAttributeHandling.schemaAttribute')}
                required/>
              <Basic.TextField
                ref="name"
                label={this.i18n('acc:entity.SchemaAttributeHandling.name.label')}
                helpBlock={this.i18n('acc:entity.SchemaAttributeHandling.name.help')}
                required
                max={255}/>
              <Basic.Checkbox
                ref="uid"
                onChange={this._uidChanged.bind(this)}
                tooltip={this.i18n('acc:entity.SchemaAttributeHandling.uid.tooltip')}
                label={this.i18n('acc:entity.SchemaAttributeHandling.uid.label')}
                readOnly = {_isDisabled}/>
              <Basic.Checkbox
                ref="entityAttribute"
                onChange={this._checkboxChanged.bind(this, 'entityAttribute', 'extendedAttribute')}
                label={this.i18n('acc:entity.SchemaAttributeHandling.entityAttribute')}
                readOnly = {_isDisabled || _isUid}/>
              <Basic.Checkbox
                ref="extendedAttribute"
                onChange={this._checkboxChanged.bind(this, 'extendedAttribute', 'entityAttribute')}
                label={this.i18n('acc:entity.SchemaAttributeHandling.extendedAttribute')}
                readOnly = {_isDisabled || _isUid}/>
              <Basic.Checkbox
                ref="confidentialAttribute"
                label={this.i18n('acc:entity.SchemaAttributeHandling.confidentialAttribute')}
                readOnly = {_isDisabled || _isUid || !_isRequiredIdmField}/>
              <Basic.TextField
                ref="idmPropertyName"
                readOnly = {_isUid || _isDisabled || !_isRequiredIdmField}
                label={this.i18n('acc:entity.SchemaAttributeHandling.idmPropertyName.label')}
                helpBlock={this.i18n('acc:entity.SchemaAttributeHandling.idmPropertyName.help')}
                required = {_isRequiredIdmField}
                max={255}/>
              <Basic.ScriptArea
                ref="transformFromResourceScript"
                helpBlock={this.i18n('acc:entity.SchemaAttributeHandling.transformFromResourceScript.help')}
                readOnly = {_isDisabled}
                label={this.i18n('acc:entity.SchemaAttributeHandling.transformFromResourceScript.label')}/>
              <Basic.ScriptArea
                ref="transformToResourceScript"
                helpBlock={this.i18n('acc:entity.SchemaAttributeHandling.transformToResourceScript.help')}
                readOnly = {_isDisabled}
                label={this.i18n('acc:entity.SchemaAttributeHandling.transformToResourceScript.label')}/>
            </Basic.AbstractForm>
            <Basic.PanelFooter>
              <Basic.Button type="button" level="link"
                onClick={this.context.router.goBack}
                showLoading={_showLoading}>
                {this.i18n('button.back')}
              </Basic.Button>
              <Basic.Button
                onClick={this.save.bind(this)}
                level="success"
                type="submit"
                showLoading={_showLoading}>
                {this.i18n('button.save')}
              </Basic.Button>
            </Basic.PanelFooter>
          </Basic.Panel>
        </form>
      </div>
    );
  }
}

SchemaAttributeHandlingDetail.propTypes = {
  _showLoading: PropTypes.bool,
};
SchemaAttributeHandlingDetail.defaultProps = {
  _showLoading: false,
};

function select(state, component) {
  const entity = Utils.Entity.getEntity(state, manager.getEntityType(), component.params.attributeId);
  if (entity) {
    const systemEntityHandling = entity._embedded && entity._embedded.systemEntityHandling ? entity._embedded.systemEntityHandling.id : null;
    const system = entity._embedded && entity._embedded.systemEntityHandling && entity._embedded.systemEntityHandling.system ? entity._embedded.systemEntityHandling.system.id : null;
    const schemaAttribute = entity._embedded && entity._embedded.schemaAttribute ? entity._embedded.schemaAttribute.id : null;
    entity.systemEntityHandling = systemEntityHandling;
    entity.schemaAttribute = schemaAttribute;
    entity.system = system;
  }
  return {
    _attribute: entity,
    _showLoading: Utils.Ui.isShowLoading(state, `${uiKey}-detail`),
  };
}

export default connect(select)(SchemaAttributeHandlingDetail);
