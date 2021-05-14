import React from 'react';
import PropTypes from 'prop-types';
import classNames from 'classnames';
import { Link } from 'react-router-dom';
//
import _ from 'lodash';
import * as Basic from '../../basic';
import * as Utils from '../../../utils';
import { ConfigurationManager } from '../../../redux';
import UuidInfo from '../UuidInfo/UuidInfo';
import AuditableInfo from './AuditableInfo';

/**
 * Entity info renderer - common methods.
 *
 * @author Radek Tomiška
 */
export default class AbstractEntityInfo extends Basic.AbstractContextComponent {

  constructor(props, context) {
    super(props, context);
    const { collapse, collapsable, face } = props;

    let expandInfo = true;
    if (collapsable && face === 'full') {
      expandInfo = !collapse;
    }
    //
    this.state = {
      error: null,
      showAuditableInfo: false,
      expandInfo,
      showSystemInformation: !context.store ? false : ConfigurationManager.showSystemInformation(context.store.getState())
    };
  }

  /**
   * Returns entity manager
   */
  getManager() {
    return this.props.manager;
  }

  componentDidMount() {
    this.loadEntityIfNeeded();
  }

  componentDidUpdate() {
    this.loadEntityIfNeeded();
  }

  /**
   * if entityIdentifier is setted and entity not - then loads entity from BE.
   */
  loadEntityIfNeeded() {
    const { entity, _entity, face } = this.props;
    const manager = this.getManager();
    if (!manager || !this.getEntityId()) {
      // nothing to load
      return;
    }
    //
    const entityId = this.getEntityId();
    if (entityId) {
      const uiKey = manager.resolveUiKey(null, entityId);
      // load entity
      if (!entity && !_entity) {
        const error = Utils.Ui.getError(this.context.store.getState(), uiKey) || this.state.error;
        if (!Utils.Ui.isShowLoading(this.context.store.getState(), uiKey)
            && (!error || error.statusCode === 401)) { // show loading check has to be here - new state is needed
          this.context.store.dispatch(manager.queueAutocompleteEntityIfNeeded(entityId, uiKey, (e, ex) => {
            // TODO: move to other place - is called only when entity is not given
            if (!ex && (face === 'full' || (face === 'link' && this.getLink(e)))) {
              this.onEnter();
            }
            this.setState({
              error: ex
            });
          }));
        }
      }
    }
  }

  /**
   * Load permissions if needed if pover is opened
   *
   * @return {[type]} [description]
   */
  onEnter() {
    const { _permissions } = this.props;
    const manager = this.getManager();
    const entityId = this.getEntityId();
    //
    if (!manager || !entityId || !manager.supportsAuthorization()) {
      // nothing to load
      return;
    }
    if (!_permissions && entityId) {
      const uiKey = manager.resolveUiKey(null, entityId);
      this.context.store.dispatch(manager.queueFetchPermissions(entityId, uiKey));
    }
  }

  /**
   * Returns entity identifier
   *
   * @return {string} entity identifier
   */
  getEntityId(_entity) {
    const { entityIdentifier, entity } = this.props;
    //
    if (_entity) { // propagated entity - higher priority
      return _entity.id;
    }
    if (entityIdentifier) { // given as property - codeable
      return entityIdentifier;
    }
    if (entity) { // given property - basic uuid id
      return entity.id;
    }
    return null;
  }

  getEntity(givenEntity = null) {
    if (givenEntity) {
      return givenEntity;
    }
    // by props
    const { entity, _entity } = this.props;
    //
    if (entity) { // entity is given by props
      return entity;
    }
    return _entity; // loaded by redux
  }

  /**
   * Get link to detail (`url`).
   *
   * @return {string}
   */
  getLink(/* entity */) {
    return null;
  }

  /**
   * Returns true, when link to detail could be shown
   *
   * @return {bool} Returns true, when link to detail could be shown
   */
  showLink() {
    const { showLink } = this.props;
    const { expandInfo } = this.state;

    if (!showLink || !expandInfo) {
      // disabled by props
      return false;
    }
    if (!this.getEntityId()) {
      // disabled by unknown entity id
      return false;
    }
    if (!this.getLink()) {
      // link to detail is not provided
      return false;
    }
    return true;
  }

  /**
   * Show identity detail by configured projection
   *
   * @param  {event} event
   * @since 10.2.0
   */
  showDetail(entity, event) {
    if (event) {
      event.preventDefault();
    }
    //
    this.context.history.push(this.getLink(entity));
  }

  onShowAuditableInfo(show, event) {
    if (event) {
      event.preventDefault();
    }
    //
    this.setState({
      showAuditableInfo: show
    });
  }

  onExpandInfo(show, event) {
    if (event) {
      event.preventDefault();
    }
    //
    this.setState({
      expandInfo: show
    });
  }

  /**
   * Returns true, when disabled decorator has to be used
   *
   * @param  {object} entity
   * @return {bool}
   */
  isDisabled(entity) {
    return Utils.Entity.isDisabled(entity);
  }

  /**
   * Returns entity icon (null by default - icon will not be rendered)
   *
   * @param  {object} entity
   */
  getEntityIcon(/* entity*/) {
    return null;
  }

  /**
   * Returns popovers title
   *
   * @param  {object} entity
   */
  getPopoverTitle(entity) {
    return this.getManager().getNiceLabel(entity);
  }

  /**
   * Returns popover info content
   *
   * @param  {object} entity
   * @return {arrayOf(object)} table data
   */
  getPopoverContent(/* entity*/) {
    return null;
  }

  getNiceLabel(entity) {
    const _entity = entity || this.getEntity();
    //
    let value = this.getManager().getNiceLabel(_entity);
    if (value.length > 60) {
      value = `${ Utils.Ui.substringBegin(value, 60, '', '...') }`;
    }

    return value;
  }

  /**
   * Allows define columns for table in children Info component.
   */
  getTableChildren() {
    return null;
  }

  /**
   * Render icon (rendered only if props showIcon is true)
   * @return {[type]} [description]
   */
  _renderIcon(entity) {
    const { showIcon } = this.props;
    if (!showIcon) {
      return '';
    }
    const _entity = entity || this.getEntity();
    return (
      <span className="pull-left">
        <Basic.Icon
          value={ this.getEntityIcon(_entity) }
          title={ this.getPopoverTitle(_entity) }
          style={{ marginRight: 5 }}/>
      </span>
    );
  }

  _renderSystemInformationIcon(entity) {
    const { showSystemInformation, showAuditableInfo } = this.state;
    const _entity = entity || this.getEntity();
    //
    // auditable info will be hidden, when no audit info is available
    if (!_entity || (!_entity.id && !_entity.created && !_entity.modified && !_entity.transactionId)) {
      return null;
    }
    //
    return (
      <Basic.Icon
        value="fa:cog"
        style={{
          color: showAuditableInfo ? '#000' : '#ccc',
          marginLeft: 10,
          cursor: 'pointer'
        }}
        title={ this.i18n('component.advanced.AuditableInfo.link.title') }
        onClick={ this.onShowAuditableInfo.bind(this, !showAuditableInfo) }
        rendered={ showSystemInformation }/>
    );
  }

  _renderSystemCollapsIcon() {
    const { expandInfo } = this.state;
    const { collapsable, face } = this.props;
    //
    return (
      <Basic.Icon
        value={ expandInfo ? 'fa:angle-down arrow-icon' : 'fa:angle-left arrow-icon' }
        style={{
          color: '#000',
          marginLeft: 10,
          cursor: 'pointer'
        }}
        onClick={ this.onExpandInfo.bind(this, !expandInfo) }
        rendered={ collapsable && face === 'full'}/>
    );
  }

  /**
   * Renders nicelabel used in text and link face
   */
  _renderNiceLabel(entity) {
    const { className, style } = this.props;
    const _entity = entity || this.getEntity();
    //
    return (
      <span className={ className } style={ style }>{ this.getNiceLabel(_entity) }</span>
    );
  }

  /**
   * Renders text face
   */
  _renderText(entity) {
    return (
      <span>
        { this._renderIcon(entity) }
        { this._renderNiceLabel(entity) }
      </span>
    );
  }

  /**
   * Renders link face
   */
  _renderLink() {
    if (!this.showLink()) {
      return this._renderNiceLabel();
    }
    return (
      <span>
        { this._renderIcon() }
        <Link
          to={ this.getLink() }
          title={ this.i18n('component.advanced.EntityInfo.link.detail.label') }>
          { this.getNiceLabel() }
        </Link>
      </span>
    );
  }

  /**
   * Renders popover info card
   */
  _renderPopover(entity) {
    const { style } = this.props;
    //
    return (
      <Basic.Popover
        trigger={ ['click'] }
        value={ this._renderFull(entity) }
        className="abstract-entity-info-popover"
        onEnter={ this.onEnter.bind(this) }>
        {
          <span
            style={ style }
            onClick={
              (event) => {
                if (event && event.ctrlKey) {
                  const link = this.getLink(entity);
                  if (link) {
                    event.preventDefault();
                    event.stopPropagation();
                    this.showDetail(entity, event);
                  }
                }
              }
            }>
            { this._renderIcon(entity) }
            <span
              className="popover-link"
              title={ this.i18n('component.advanced.EntityInfo.link.popover.title') }>
              { this._renderNiceLabel(entity) }
            </span>
          </span>
        }
      </Basic.Popover>
    );
  }

  /**
   * Renders full info card - its used ass popover content too
   */
  _renderFull(entity) {
    const { className, style, level, titleStyle } = this.props;
    const { showAuditableInfo, expandInfo } = this.state;
    const _entity = entity || this.getEntity();
    //
    const panelClassNames = classNames(
      'abstract-entity-info',
      { 'panel-success': level === 'success' || (_entity && !this.isDisabled(_entity)) },
      { 'panel-warning': level === 'warning' || (_entity && this.isDisabled(_entity)) },
      { 'panel-info': level === 'info' },
      className
    );
    let _titleStyle = _.clone(titleStyle, true);
    if (!_titleStyle) {
      _titleStyle = {};
    }
    _titleStyle.flex = 1;
    //
    return (
      <Basic.Panel className={ panelClassNames } style={ style }>
        <Basic.PanelHeader>
          <Basic.Div style={{ display: 'flex', alignItems: 'center' }}>
            <Basic.Div style={ _titleStyle }>
              <Basic.Icon value={ this.getEntityIcon(_entity) } style={{ marginRight: 5 }}/>
              <Basic.ShortText value={ this.getPopoverTitle(_entity) } maxLength={ 60 } cutChar="" />
            </Basic.Div>
            <Basic.Div>
              {
                !this.isDisabled(_entity)
                ||
                <Basic.Label text={ this.i18n('label.disabled') } className="label-disabled"/>
              }
              { this._renderSystemInformationIcon(_entity) }
              { this._renderSystemCollapsIcon() }
            </Basic.Div>
          </Basic.Div>
        </Basic.PanelHeader>

        {
          showAuditableInfo
          ?
          <AuditableInfo entity={ _entity } face="content"/>
          :
          <Basic.Table
            condensed
            hover={ false }
            rendered={ expandInfo }
            noHeader
            data={ this.getPopoverContent(_entity) }>
            { this.getTableChildren() }
          </Basic.Table>
        }

        {
          !this.showLink()
          ||
          <Basic.PanelFooter>
            <Link to={ this.getLink(_entity) }>
              <Basic.Icon value="fa:angle-double-right"/>
              { ' ' }
              { this.i18n('component.advanced.EntityInfo.link.detail.label') }
            </Link>
          </Basic.PanelFooter>
        }
      </Basic.Panel>
    );
  }

  render() {
    const { rendered, showLoading, className, entity, face, _showLoading, style } = this.props;
    //
    if (!rendered) {
      return null;
    }
    let _entity = this.props._entity;
    if (entity) { // identity prop has higher priority
      _entity = entity;
    }
    const entityId = this.getEntityId();
    //
    if (showLoading || (_showLoading && entityId && !_entity)) {
      switch (face) {
        case 'text':
        case 'link':
        case 'popover': {
          return (
            <Basic.Icon value="refresh" showLoading className={ className } style={ style } title={ entityId }/>
          );
        }
        default: {
          return (
            <Basic.Well showLoading className={ classNames('abstract-entity-info', className) } style={ style }/>
          );
        }
      }
    }
    if (!_entity) {
      if (!this.getEntityId()) {
        return null;
      }
      return (
        <UuidInfo className={ className } value={ this.getEntityId() } style={ style } />
      );
    }
    //
    switch (face) {
      case 'text': {
        return this._renderText();
      }
      case 'link': {
        return this._renderLink();
      }
      case 'popover': {
        return this._renderPopover();
      }
      default: {
        return this._renderFull();
      }
    }
  }
}

AbstractEntityInfo.propTypes = {
  ...Basic.AbstractContextComponent.propTypes,
  /**
   * Internal entity loaded by given identifier
   */
  _entity: PropTypes.object,
  /**
   * Decorator
   */
  face: PropTypes.oneOf(['text', 'link', 'popover', 'full']),
  /**
   * Shows link to full identity detail (if currently logged user has appropriate permission)
   */
  showLink: PropTypes.bool,
  /**
   * Shows icon for text', 'link', 'popover' face
   */
  showIcon: PropTypes.bool,
  /**
   * Allow collapsing of this info.
   */
  collapsable: PropTypes.bool,
  /**
   * Collapse content of this info.
   *
   * Collapseable property must be true and face = "full"!
   */
  collapse: PropTypes.bool,
  /**
   * Entity manager
   */
  manager: PropTypes.object,
  /**
   * Custom style for main title in full mode.
   */
  titleStyle: PropTypes.object,
  /**
   * Directly set level.
   */
  level: PropTypes.oneOf(['warning', 'success', 'info'])
};
AbstractEntityInfo.defaultProps = {
  ...Basic.AbstractContextComponent.defaultProps,
  face: 'full',
  showLink: true,
  showIcon: false,
  collapsable: false,
  collapse: false
};
