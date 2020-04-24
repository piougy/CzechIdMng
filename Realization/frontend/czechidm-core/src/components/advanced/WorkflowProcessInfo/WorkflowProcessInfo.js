import PropTypes from 'prop-types';
import React from 'react';
import classnames from 'classnames';
import { connect } from 'react-redux';
import { Link } from 'react-router-dom';
//
import * as Basic from '../../basic';
import { WorkflowHistoricProcessInstanceManager } from '../../../redux';
import UuidInfo from '../UuidInfo/UuidInfo';
import UiUtils from '../../../utils/UiUtils';
import AbstractEntityInfo from '../EntityInfo/AbstractEntityInfo';

const manager = new WorkflowHistoricProcessInstanceManager();

/**
 * WorkflowProcess basic information (info card)
 *
 * @author Švanda
 */
export class WorkflowProcessInfo extends AbstractEntityInfo {

  getManager() {
    return manager;
  }

  showLink() {
    if (!super.showLink()) {
      return false;
    }
    return true;
  }

  /**
   * Get link to detail (`url`).
   *
   * @return {string}
   */
  getLink() {
    const { entityIdentifier, entity } = this.props;
    if (entity && entity.id) {
      return `/workflow/history/processes/${entity.id}`;
    }
    return `/workflow/history/processes/${entityIdentifier}`;
  }

  /**
   * TODO: implement different face
   */
  render() {
    const { rendered, showLoading, className, entity, entityIdentifier, _showLoading, style, maxLength } = this.props;
    //
    if (!rendered) {
      return null;
    }
    let _entity = this.props._entity;
    if (entity) { // entity prop has higher priority
      _entity = entity;
    }
    //
    const classNames = classnames(
      'wf-info',
      className
    );
    if (showLoading || (_showLoading && entityIdentifier && !_entity)) {
      return (
        <Basic.Icon className={ classNames } value="refresh" showLoading style={style}/>
      );
    }
    if (!_entity) {
      if (!entityIdentifier) {
        return null;
      }
      return (<UuidInfo className={ classNames } value={ entityIdentifier } style={style}/>);
    }
    //
    const niceLabelFull = this.getManager().localize(_entity, 'name');
    const niceLabel = UiUtils.substringBegin(niceLabelFull, maxLength, ' ', '...');
    if (!this.showLink()) {
      return (
        <span title={niceLabelFull} className={ classNames }>{ niceLabel }</span>
      );
    }
    return (
      <Link title={niceLabelFull} className={ classNames } to={ this.getLink() }>{ niceLabel }</Link>
    );
  }
}

WorkflowProcessInfo.propTypes = {
  ...AbstractEntityInfo.propTypes,
  /**
   * Selected entity - has higher priority
   */
  entity: PropTypes.object,
  /**
   * Selected entity's id - entity will be loaded automatically
   */
  entityIdentifier: PropTypes.string,
  /**
   * Internal entity loaded by given identifier
   */
  _entity: PropTypes.object,
  _showLoading: PropTypes.bool
};
WorkflowProcessInfo.defaultProps = {
  ...AbstractEntityInfo.defaultProps,
  entity: null,
  face: 'link',
  _showLoading: true,
  maxLength: 35
};

function select(state, component) {
  return {
    _entity: manager.getEntity(state, component.entityIdentifier),
    _showLoading: manager.isShowLoading(state, null, component.entityIdentifier)
  };
}
export default connect(select)(WorkflowProcessInfo);
