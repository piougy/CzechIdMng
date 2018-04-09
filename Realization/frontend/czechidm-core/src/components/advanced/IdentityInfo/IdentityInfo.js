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

  renderImage() {
    const imageUrl = this.state.imageUrl;
    const style = {
      height: '100px',
      width: '100px',
      borderRadius: '50%',
      marginTop: '5px',
      marginBottom: '10px',
      border: '2px solid white',
      backgroundColor: 'white',
    };
    if (imageUrl) {
      style.boxShadow = '0px 2px 10px rgba(0, 0, 0, 0.2)';
      return (
        <img
        src={imageUrl}
        className="center-block"
        style={ style } />
      );
    }
    return (
        <div
        className="center-block"
        style={ style }>
          <Basic.Icon
          className=""
          value={ "fa:user-circle" }
          color="#D6EEF8"
          style={{ fontSize: '92px', margin: '2px', height: '100px'}}/>
        </div>
      );
  }

  /**
   * Returns popover info content
   *
   * @param  {array} table data
   */
  getPopoverContent(entity) {
    return [
      {
        value: '.'
      },
      {
        value: (
          <div>
            <Basic.Icon value="fa:envelope" style={{ marginRight: 5 }}/>
            {' '}
            { entity.email }
          </div>
        )
      },
      {
        value: (
          <div>
            <Basic.Icon value="fa:phone" style={{ marginRight: 3 }}/>
            {' '}
            { entity.phone }
          </div>
        )
      },
    ];
  }

  _renderFull() {
    const { className, style } = this.props;
    const _entity = this.getEntity();
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
        <Basic.PanelHeader className="text-center" style={{ padding: '8px 0' }}>
            { this.renderImage() }

              <Basic.Icon value={ this.getEntityIcon(_entity) } style={{ marginRight: 5 }}/>
              { this.getPopoverTitle(_entity) }
            {
              !this.isDisabled(_entity)
              ||
              <div className="pull-right">
                <Basic.Label text={ this.i18n('label.disabled') } className="label-disabled"/>
              </div>
            }
            <div className="clearfix"/>
        </Basic.PanelHeader>

          <table className="table table-condensed text-center" style={{ marginBottom: 0 }}>
            <tbody>
              <tr>
                <td>
                  <Basic.Icon value="fa:envelope" style={{ marginRight: 5 }}/>
                  {' '}
                  { _entity.email }
                </td>
              </tr>
              <tr>
                <td>
                  <Basic.Icon value="fa:phone" style={{ marginRight: 5 }}/>
                  {' '}
                  { _entity.phone }
                </td>
              </tr>
            </tbody>
          </table>

        <Basic.PanelFooter rendered={ this.showLink() }>
          <div className="text-center">
            <Link to={ this.getLink() }>
              <Basic.Icon value="fa:angle-double-right"/>
              {' '}
              {this.i18n('component.advanced.EntityInfo.link.detail.label')}
            </Link>
          </div>
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
