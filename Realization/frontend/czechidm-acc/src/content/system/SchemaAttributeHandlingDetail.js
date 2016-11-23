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
      this.setState({attribute: {systemEntityHandling: props.location.query.entityHandlingId,
        system: props.location.query.systemId}});
    } else {
      this.context.store.dispatch(this.getManager().fetchEntity(entityId));
    }
    this.selectNavigationItems(['sys-systems']);
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
        this.addMessage({ message: this.i18n('create.success', { name: entity.idmPropertyName }) });
        this.context.router.goBack();
      } else {
        this.addMessage({ message: this.i18n('save.success', { name: entity.idmPropertyName }) });
      }
    } else {
      this.addError(error);
    }
    super.afterSave();
  }

  closeDetail() {
    this.refs.form.processEnded();
  }

  _uidChange(event) {
    const checked = event.currentTarget.checked;
    this.setState({isUid: checked}, () => {
      if (checked) {
        this.refs.idmPropertyName.setValue('UID');
      }
    });
  }

  render() {
    const { _showLoading, _attribute} = this.props;
    const { isUid} = this.state;
    const isNew = this._getIsNew();
    const attribute = isNew ? this.state.attribute : _attribute;
    const forceSearchParameters = new Domain.SearchParameters().setFilter('systemId', attribute && attribute.system ? attribute.system : Domain.SearchParameters.BLANK_UUID);
    let _isUid = (isUid != null ? isUid : null);
    if (_isUid == null) {
      _isUid = attribute ? attribute.uid : false;
    }
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
          <Basic.Panel>
            <Basic.AbstractForm ref="form" data={attribute} showLoading={_showLoading} className="form-horizontal">
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
                label={this.i18n('acc:entity.SchemaAttributeHandling.schemaAttribute')}
                required/>
              <Basic.Checkbox
                ref="uid"
                onChange={this._uidChange.bind(this)}
                tooltip={this.i18n('acc:entity.SchemaAttributeHandling.uidTooltip')}
                label={this.i18n('acc:entity.SchemaAttributeHandling.uid')}/>
              <Basic.TextField
                ref="idmPropertyName"
                readOnly = {_isUid}
                label={this.i18n('acc:entity.SchemaAttributeHandling.idmPropertyName')}
                required = {!_isUid}
                max={255}/>
              <Basic.Checkbox
                ref="extendedAttribute"
                label={this.i18n('acc:entity.SchemaAttributeHandling.extendedAttribute')}/>
              <Basic.ScriptArea
                ref="transformFromResourceScript"
                mode="javascript"
                helpBlock={this.i18n('acc:entity.SchemaAttributeHandling.transformFromResourceScript.help')}
                label={this.i18n('acc:entity.SchemaAttributeHandling.transformFromResourceScript.label')}/>
              <Basic.ScriptArea
                ref="transformToResourceScript"
                helpBlock={this.i18n('acc:entity.SchemaAttributeHandling.transformToResourceScript.help')}
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
  const entity = Utils.Entity.getEntity(state, manager.getEntityType(), component.params.entityId);
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
