import React from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
//
import * as Basic from '../../../components/basic';
import * as Utils from '../../../utils';
import { IdentityManager, DataManager } from '../../../redux';
import AuthoritiesPanel from '../../role/AuthoritiesPanel';

const uiKeyAuthorities = 'identity-authorities';
const identityManager = new IdentityManager();

/**
 * Identity's authorities.
 *
 * @author Radek Tomiška
 */
class IdentityAuthorities extends Basic.AbstractContent {

  getContentKey() {
    return 'content.identity.authorities';
  }

  getNavigationKey() {
    return 'profile-authorities';
  }

  componentDidMount() {
    super.componentDidMount();
    const { entityId } = this.props.match.params;
    this.context.store.dispatch(identityManager.fetchAuthorities(entityId, `${ uiKeyAuthorities }-${ entityId }`));
  }

  render() {
    const { authorities } = this.props;
    //
    return (
      <Basic.Div className="tab-pane-panel-body">
        <AuthoritiesPanel authorities={ authorities } />
      </Basic.Div>
    );
  }
}

IdentityAuthorities.propTypes = {
  _showLoading: PropTypes.bool,
  authorities: PropTypes.arrayOf(PropTypes.object)
};
IdentityAuthorities.defaultProps = {
  _showLoading: true,
  authorities: []
};

function select(state, component) {
  return {
    _showLoading: Utils.Ui.isShowLoading(state, `${ uiKeyAuthorities }-${ component.match.params.entityId }`),
    authorities: DataManager.getData(state, `${ uiKeyAuthorities }-${ component.match.params.entityId }`)
  };
}

export default connect(select)(IdentityAuthorities);
