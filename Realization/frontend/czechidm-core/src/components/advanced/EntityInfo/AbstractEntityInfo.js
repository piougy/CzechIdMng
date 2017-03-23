import { PropTypes} from 'react';
//
import * as Basic from '../../basic';
import * as Utils from '../../../utils';

/**
 * Entity info renderer - common methods.
 *
 * @author Radek Tomiška
 */
export default class AbstractEntityInfo extends Basic.AbstractContextComponent {

  constructor(props, context) {
    super(props, context);
  }

  componentDidMount() {
    this.loadEntityIfNeeded();
  }

  componentDidUpdate() {
    this.loadEntityIfNeeded();
  }

  /**
   * Returns entity manager
   */
  getManager() {
    return null;
  }

  /**
   * if entityIdentifier is setted and entity not - then loads entity from BE.
   */
  loadEntityIfNeeded() {
    const { entity, entityIdentifier, _entity } = this.props;
    const manager = this.getManager();
    //
    if (manager && entityIdentifier && !entity && !_entity) {
      const uiKey = manager.resolveUiKey(null, entityIdentifier);
      const error = Utils.Ui.getError(this.context.store.getState(), uiKey);
      if (!Utils.Ui.isShowLoading(this.context.store.getState(), uiKey)
          && (!error || error.statusCode === 401)) { // show loading check has to be here - new state is needed
        this.context.store.dispatch(manager.fetchEntityIfNeeded(entityIdentifier, null, () => {}));
      }
    }
  }

  showLink() {
    const { showLink, entityIdentifier } = this.props;
    if (!showLink || !entityIdentifier) {
      return false;
    }
    return true;
  }
}

AbstractEntityInfo.propTypes = {
  ...Basic.AbstractContextComponent.propTypes,
  /**
   * Decorator
   */
  face: PropTypes.oneOf(['text', 'link', 'full']),
  /**
   * Shows link to full identity detail (if currently logged user has appropriate permission)
   */
  showLink: PropTypes.bool
};
AbstractEntityInfo.defaultProps = {
  ...Basic.AbstractContextComponent.defaultProps,
  face: 'full',
  showLink: true
};
