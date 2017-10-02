import { PropTypes} from 'react';
import { connect } from 'react-redux';
//
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
  _showLoading: PropTypes.bool
};
IdentityInfo.defaultProps = {
  ...AbstractEntityInfo.defaultProps,
  entity: null,
  face: 'full',
  showOnlyUsername: false,
  _showLoading: true,
  _permissions: PropTypes.arrayOf(PropTypes.string)
};

function select(state, component) {
  const identifier = component.entityIdentifier || component.username;
  const identity = manager.getEntity(state, identifier);
  //
  return {
    _entity: identity,
    _showLoading: manager.isShowLoading(state, null, identifier),
    userContext: state.security.userContext, // is needed for refresh after login
    _permissions: manager.getPermissions(state, null, identity)
  };
}
export default connect(select)(IdentityInfo);
