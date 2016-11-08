import React, { PropTypes } from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
//
import { Basic, Utils} from 'czechidm-core';
import { SchemaObjectClassManager, SchemaAttributeManager } from '../../redux';

const uiKey = 'schema-attribute';
const manager = new SchemaAttributeManager();
const schemaObjectClassManager = new SchemaObjectClassManager();

class SchemaAttribute extends Basic.AbstractTableContent {

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
    return 'acc:content.schema.attribute.detail';
  }

  componentWillReceiveProps(nextProps) {
    const { entityId} = nextProps.params;
    if (entityId && entityId !== this.props.params.entityId) {
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
    const { entityId} = props.params;
    if (this._getIsNew(props)) {
      this.setState({attribute: {objectClass: props.location.query.objectClassId}});
    } else {
      this.context.store.dispatch(this.getManager().fetchEntity(entityId));
    }
    this.selectNavigationItems(['sys-systems']);
  }

  _getIsNew(nextProps) {
    const { query } = nextProps ? nextProps.location : this.props.location;
    return (query) ? query.new : null;
  }

  save(entity, event) {
    const formEntity = this.refs.form.getData();
    formEntity.objectClass = schemaObjectClassManager.getSelfLink(formEntity.objectClass);
    //
    super.save(formEntity, event);
  }

  afterSave(entity, error) {
    if (!error) {
      if (this._getIsNew()) {
        this.addMessage({ message: this.i18n('create.success', { name: entity.name }) });
        this.context.router.replace(`/schema-attributes/${entity.id}/detail`, {entityId: entity.id});
      } else {
        this.addMessage({ message: this.i18n('save.success', { name: entity.name }) });
      }
    }
    super.afterSave();
  }

  closeDetail() {
    this.refs.form.processEnded();
  }

  render() {
    const { _showLoading, _attribute} = this.props;
    const isNew = this._getIsNew();
    const attribute = isNew ? this.state.attribute : _attribute;
    return (
      <div>
        <Helmet title={this.i18n('title')} />
        <Basic.Confirm ref="confirm-delete" level="danger"/>

        <Basic.ContentHeader>
          <Basic.Icon value="compressed"/>
          {' '}
          <span dangerouslySetInnerHTML={{ __html: this.i18n('header', attribute ? { name: manager.getNiceLabel(attribute)} : {})}}/>
        </Basic.ContentHeader>

        <Basic.Panel>
          <Basic.AbstractForm ref="form" data={attribute} showLoading={_showLoading} className="form-horizontal">
            <Basic.SelectBox
              ref="objectClass"
              manager={schemaObjectClassManager}
              label={this.i18n('acc:entity.SchemaAttribute.objectClass')}
              readOnly
              required/>
            <Basic.TextField
              ref="name"
              label={this.i18n('acc:entity.SchemaAttribute.name')}
              required
              max={255}/>
            <Basic.TextField
              ref="classType"
              label={this.i18n('acc:entity.SchemaAttribute.classType')}
              required
              max={255}/>
            <Basic.TextField
              ref="nativeName"
              label={this.i18n('acc:entity.SchemaAttribute.nativeName')}
              max={255}/>
            <Basic.Checkbox
              ref="required"
              label={this.i18n('acc:entity.SchemaAttribute.required')}/>
            <Basic.Checkbox
              ref="readable"
              label={this.i18n('acc:entity.SchemaAttribute.readable')}/>
            <Basic.Checkbox
              ref="multivalued"
              label={this.i18n('acc:entity.SchemaAttribute.multivalued')}/>
            <Basic.Checkbox
              ref="createable"
              label={this.i18n('acc:entity.SchemaAttribute.createable')}/>
            <Basic.Checkbox
              ref="updateable"
              label={this.i18n('acc:entity.SchemaAttribute.updateable')}/>
            <Basic.Checkbox
              ref="returnedByDefault"
              label={this.i18n('acc:entity.SchemaAttribute.returned_by_default')}/>
          </Basic.AbstractForm>
          <Basic.PanelFooter>
            <Basic.Button type="button" level="link"
              onClick={this.context.router.goBack}
              showLoading={_showLoading}>
              {this.i18n('button.back')}
            </Basic.Button>
            <Basic.Button
              onClick={this.save.bind(this)}
              level="success" showLoading={_showLoading}>
              {this.i18n('button.save')}
            </Basic.Button>
          </Basic.PanelFooter>
        </Basic.Panel>
      </div>
    );
  }
}

SchemaAttribute.propTypes = {
  system: PropTypes.object,
  _showLoading: PropTypes.bool,
};
SchemaAttribute.defaultProps = {
  system: null,
  _showLoading: false,
};

function select(state, component) {
  const entity = Utils.Entity.getEntity(state, manager.getEntityType(), component.params.entityId);
  if (entity) {
    const objectClass = entity._embedded && entity._embedded.objectClass ? entity._embedded.objectClass.id : null;
    entity.objectClass = objectClass;
  }
  return {
    _attribute: entity,
    _showLoading: Utils.Ui.isShowLoading(state, `${uiKey}-detail`),
  };
}

export default connect(select)(SchemaAttribute);
