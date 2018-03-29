import React, { PropTypes} from 'react';
import { connect } from 'react-redux';
import { Link } from 'react-router';
import classNames from 'classnames';
//
import * as Basic from '../../basic';
import { IdentityManager } from '../../../redux/';
import AbstractEntityInfo from '../EntityInfo/AbstractEntityInfo';

const manager = new IdentityManager();

/**
 * Identity basic information (info card)
 *
 * @author Radek Tomiška
 */
export class IdentityInfo extends AbstractEntityInfo {

  constructor(props, context) {
    super(props, context);
  }

  getManager() {
    return manager;
  }

  componentDidMount() {
    if (!this.state.imageUrl && this.props.entityIdentifier) {
      manager.download(this.props.entityIdentifier, this.receiveImage.bind(this));
    }
  }

  receiveImage(blob) {
    const objectURL = URL.createObjectURL(blob);
    this.setState({imageUrl: objectURL});
  }

  /**
   * Override super.getEntityId() - adds username for backward compatibility
   *
   * @return {string} id
   */
  getEntityId() {
    const { username, entityIdentifier, entity } = this.props;
    // id has higher priority
    if (entityIdentifier) {
      return entityIdentifier;
    }
    if (username) {
      return username;
    }
    if (entity) {
      return entity.id;
    }
    return null;
  }

  showLink() {
    if (!super.showLink()) {
      return false;
    }
    // evaluate authorization policies
    const { _permissions } = this.props;
    if (!this.getManager().canRead(this.getEntity(), _permissions)) {
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
    return `/identity/${encodeURIComponent(this.getEntityId())}/profile`;
  }

  /**
   * Returns entity icon (null by default - icon will not be rendered)
   *
   * @param  {object} entity
   */
  getEntityIcon(entity) {
    if (this.isDisabled(entity)) {
      return 'fa:user-times';
    }
    return 'fa:user';
  }

  /**
   * @Override
   * Renders nicelabel used in text and link face
   */
  _renderNiceLabel() {
    const{showOnlyUsername} = this.props;
    if (!showOnlyUsername) {
      return super._renderNiceLabel();
    }
    const _entity = this.getEntity();
    if (_entity && _entity.username) {
      return _entity.username;
    }
    return '';
  }


  /**
   * Returns popover info content
   *
   * @param  {array} table data
   */
  getPopoverContent(entity) {
    return [
      {
        label: this.i18n('entity.Identity.email'),
        value: entity.email
      },
      {
        label: this.i18n('entity.Identity.phone'),
        value: entity.phone
      }
    ];
  }

  _renderFull() {
    const { className, style } = this.props;
    const _entity = this.getEntity();
    const imageUrl = this.state.imageUrl;
    //
    const panelClassNames = classNames(
      'abstract-entity-info',
      { 'panel-success': _entity && !this.isDisabled(_entity) },
      { 'panel-warning': _entity && this.isDisabled(_entity) },
      className
    );
    //
    return (
      <Basic.Panel className={panelClassNames} style={style}>
        <Basic.PanelHeader>
          <div className="pull-left">
            <Basic.Icon value={ this.getEntityIcon(_entity) } style={{ marginRight: 5 }}/>
            { this.getPopoverTitle(_entity) }
          </div>
          {
            !this.isDisabled(_entity)
            ||
            <div className="pull-right">
              <Basic.Label text={ this.i18n('label.disabled') } className="label-disabled"/>
            </div>
          }
          <div className="clearfix"/>
        </Basic.PanelHeader>

        <Basic.Table
          condensed
          hover={ false }
          noHeader
          data={ this.getPopoverContent(_entity) }
          children={ this.getTableChildren() }/>
          <img src={imageUrl ? imageUrl : null} className="center-block" style={{width: '100%', float: 'none'}}/>
        <Basic.PanelFooter rendered={ this.showLink() }>
          <Link to={ this.getLink() }>
            <Basic.Icon value="fa:angle-double-right"/>
            {' '}
            {this.i18n('component.advanced.EntityInfo.link.detail.label')}
          </Link>
        </Basic.PanelFooter>
      </Basic.Panel>
    );
  }
}

IdentityInfo.propTypes = {
  ...AbstractEntityInfo.propTypes,
  /**
   * Selected entity - has higher priority
   */
  entity: PropTypes.object,
  /**
   * Selected identity's username - identity will be loaded automatically  (username entityIdentifier)
   */
  username: PropTypes.string,
  /**
   * Selected identity's id (username alias) - identity will be loaded automatically
   */
  entityIdentifier: PropTypes.string,
  /**
   * If true, then show only username instead niceLabel
   */
  showOnlyUsername: PropTypes.bool,
  //
  _showLoading: PropTypes.bool,
  _permissions: PropTypes.arrayOf(PropTypes.string)
};
IdentityInfo.defaultProps = {
  ...AbstractEntityInfo.defaultProps,
  entity: null,
  face: 'full',
  showOnlyUsername: false,
  _showLoading: true
};

function select(state, component) {
  const identifier = component.entityIdentifier || component.username;
  const identity = component.entity || manager.getEntity(state, identifier);
  //
  return {
    _entity: identity,
    _showLoading: manager.isShowLoading(state, null, identifier),
    userContext: state.security.userContext, // is needed for refresh after login
    _permissions: manager.getPermissions(state, null, identity)
  };
}
export default connect(select)(IdentityInfo);
