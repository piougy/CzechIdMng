import React, { PropTypes } from 'react';
import classNames from 'classnames';
import { connect } from 'react-redux';
import { Link } from 'react-router';
//
import { Basic, Utils } from 'czechidm-core';
import { SchemaAttributeManager } from '../redux';

const manager = new SchemaAttributeManager();

export class SchemaAttributeInfo extends Basic.AbstractContextComponent {

  constructor(props, context) {
    super(props, context);
  }

  componentDidMount() {
    this._loadEntityIfNeeded();
  }

  componentDidUpdate() {
    this._loadEntityIfNeeded();
  }

  /**
   * if id is setted and entity is not - then load entity
   */
  _loadEntityIfNeeded() {
    const { entity, _entity, id } = this.props;
    if (id && !entity && !_entity) {
      if (!Utils.Ui.isShowLoading(this.context.store.getState(), manager.resolveUiKey(null, id))) { // show loading check has to be here - new state is needed
        this.context.store.dispatch(manager.fetchEntityIfNeeded(id));
      }
    }
  }

  render() {
    const { rendered, showLoading, className, id, entity, ...others } = this.props;
    //
    if (!rendered) {
      return null;
    }
    let _entity = this.props._entity;
    if (entity) { // entity prop has higher priority
      _entity = entity;
    }
    //
    const panelClassNames = classNames(
      className
    );
    if (showLoading || (id && !_entity)) {
      return (
        <Basic.Icon value="refresh" showLoading/>
      );
    }
    if (!_entity) {
      return null;
    }
    //
    const systemId = _entity._embedded.objectClass.system.id;
    return (
      <Link to={`/system/${systemId}/schema-attributes/${id}/detail`} className={panelClassNames} title="Zobrazit detail atributu schématu">{_entity.name}</Link>
    );
  }
}

SchemaAttributeInfo.propTypes = {
  ...Basic.AbstractContextComponent.propTypes,
  /**
   * Selected entity
   */
  entity: PropTypes.object,
  /**
   * Selected entity's id - entity will be loaded automatically
   */
  id: PropTypes.string,
  /**
   * Internal identity loaded by given username
   */
  _entity: PropTypes.object
};
SchemaAttributeInfo.defaultProps = {
  ...Basic.AbstractContextComponent.defaultProps,
  entity: null
};

function select(state, component) {
  return {
    _entity: manager.getEntity(state, component.id)
  };
}
export default connect(select)(SchemaAttributeInfo);
