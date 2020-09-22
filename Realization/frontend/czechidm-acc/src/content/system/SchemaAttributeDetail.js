import PropTypes from 'prop-types';
import React from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
//
import { Basic, Advanced, Utils, Managers } from 'czechidm-core';
import { SchemaObjectClassManager, SchemaAttributeManager } from '../../redux';

const uiKey = 'schema-attribute';
const manager = new SchemaAttributeManager();
const schemaObjectClassManager = new SchemaObjectClassManager();

class SchemaAttributeDetail extends Advanced.AbstractTableContent {

  getManager() {
    return manager;
  }

  getUiKey() {
    return uiKey;
  }

  getContentKey() {
    return 'acc:content.system.attributeDetail';
  }

  getNavigationKey() {
    return 'schema-object-classes';
  }

  // @Deprecated - since V10 ... replaced by dynamic key in Route
  // UNSAFE_componentWillReceiveProps(nextProps) {
  //   const { attributeId} = nextProps.match.params;
  //   if (attributeId && attributeId !== this.props.match.params.attributeId) {
  //     this._initComponent(nextProps);
  //   }
  // }

  // Did mount only call initComponent method
  componentDidMount() {
    super.componentDidMount();
    //
    this._initComponent(this.props);
  }

  /**
   * Method for init component from didMount method and from willReceiveProps method
   * @param  {properties of component} props For didmount call is this.props for call from willReceiveProps is nextProps.
   */
  _initComponent(props) {
    const { attributeId} = props.match.params;
    if (this._getIsNew(props)) {
      this.setState({attribute: {objectClass: props.location.query.objectClassId}});
    } else {
      this.context.store.dispatch(this.getManager().fetchEntity(attributeId));
    }
  }

  _getIsNew(nextProps) {
    const { query } = nextProps ? nextProps.location : this.props.location;
    return (query) ? query.new : null;
  }

  save(event) {
    const formEntity = this.refs.form.getData();
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
      // Complete wizard step.
      // Set new entity to the wizard context and go to next step.
      if ( this.isWizard() ) {
        const activeStep = this.context.wizardContext.activeStep;
        if (activeStep) {
          activeStep.id = 'schema';
          activeStep.objectClass = entity._embedded.objectClass;
          this.context.wizardContext.wizardForceUpdate();
        }
      } else {
        const systemId = this.props.match.params.entityId;
        this.context.history.replace(`/system/${systemId}/object-classes/${entity._embedded.objectClass.id}/detail`, {attributeId: entity.id});
      }
    } else {
      this.addError(error);
    }
    super.afterSave();
  }

  goBack() {
    if ( this.isWizard() ) {
      // If is component in the wizard, then set new ID (master component)
      // to the active action and render wizard.
      const activeStep = this.context.wizardContext.activeStep;
      if (activeStep) {
        activeStep.id = 'schema';
        this.context.wizardContext.wizardForceUpdate();
      }
    } else {
      this.context.history.goBack();
    }
  }

  wizardAddButtons(showLoading) {
    return this.renderButtons(showLoading);
  }

  renderButtons(_showLoading) {
    return <span>
      <Basic.Button
        type="button"
        level="link"
        onClick={this.goBack.bind(this)}
        showLoading={_showLoading}>
        {this.i18n('button.back')}
      </Basic.Button>
      <Basic.Button
        onClick={this.save.bind(this)}
        level="success"
        type="submit"
        showLoading={_showLoading}
        rendered={Managers.SecurityManager.hasAnyAuthority(['SYSTEM_UPDATE'])}>
        {this.i18n('button.save')}
      </Basic.Button>
    </span>;
  }

  render() {
    const { _showLoading, _attribute} = this.props;
    const isNew = this._getIsNew();
    const attribute = isNew ? this.state.attribute : _attribute;
    return (
      <div>
        <Helmet title={this.i18n('title')}/>
        <Basic.Confirm ref="confirm-delete" level="danger"/>

        <Basic.ContentHeader>
          <Basic.Icon value="list"/>
          {' '}
          <span dangerouslySetInnerHTML={{__html: this.i18n('header', attribute ? {name: manager.getNiceLabel(attribute)} : {})}}/>
        </Basic.ContentHeader>
        <form onSubmit={this.save.bind(this)}>
          <Basic.Panel className="no-border last">
            <Basic.AbstractForm ref="form" data={attribute} showLoading={_showLoading}>
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
                hidden
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
                tooltip={this.i18n('returnedByDefaultTooltip')}
                label={this.i18n('acc:entity.SchemaAttribute.returned_by_default')}/>
            </Basic.AbstractForm>
            <Basic.PanelFooter rendered={!this.isWizard()}>
              {this.renderButtons(_showLoading)}
            </Basic.PanelFooter>
          </Basic.Panel>
        </form>
      </div>
    );
  }
}

SchemaAttributeDetail.propTypes = {
  system: PropTypes.object,
  _showLoading: PropTypes.bool,
};
SchemaAttributeDetail.defaultProps = {
  system: null,
  _showLoading: false,
};

function select(state, component) {
  const entity = Utils.Entity.getEntity(state, manager.getEntityType(), component.match.params.attributeId);
  if (entity) {
    const objectClass = entity._embedded && entity._embedded.objectClass ? entity._embedded.objectClass.id : null;
    entity.objectClass = objectClass;
  }
  return {
    _attribute: entity,
    _showLoading: Utils.Ui.isShowLoading(state, `${uiKey}-detail`),
  };
}

export default connect(select)(SchemaAttributeDetail);
