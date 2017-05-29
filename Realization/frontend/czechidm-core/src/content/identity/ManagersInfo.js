import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
import Immutable from 'immutable';
import { Link } from 'react-router';
//
import * as Basic from '../../components/basic';
import * as Utils from '../../utils';
import { IdentityManager, SecurityManager } from '../../redux';
import SearchParameters from '../../domain/SearchParameters';

const uiKey = 'managers-info';

/**
 * Renders all managers by identity contract
 *
 * @author Radek Tomiška
 */
class ManagersInfo extends Basic.AbstractContextComponent {

  constructor(props, context) {
    super(props, context);
    this.identityManager = new IdentityManager();
  }

  componentDidMount() {
    const { managersFor, identityContractId } = this.props;
    if (identityContractId) {
      // load managers by Tree
      const uiKeyId = `${uiKey}-${identityContractId}`;
      if (!Utils.Ui.isShowLoading(this.context.store.getState(), uiKeyId)) {
        const searchParameters = new SearchParameters().setFilter('managersFor', managersFor).setFilter('managersByContract', identityContractId).setFilter('includeGuarantees', true);
        this.context.store.dispatch(this.identityManager.fetchEntities(searchParameters, uiKeyId));
      }
    }
  }

  getAllManagers() {
    const { _managers } = this.props;
    //
    let managers = new Immutable.OrderedMap();
    _managers.forEach(manager => {
      managers = managers.set(manager.username, manager);
    });
    return managers.toArray();
  }

  render() {
    const { identityContractId, _showLoading} = this.props;
    if (!identityContractId) {
      return null;
    }

    return (
      <span>
        {
          this.getAllManagers().map(identity => {
            if (!SecurityManager.hasAccess({ 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['IDENTITY_READ']})) {
              return this.identityManager.getNiceLabel(identity);
            }
            return (
              <span>
                <Link to={`/identity/${identity.username}/profile`} style={{ marginRight: 7 }}>
                  {this.identityManager.getFullName(identity)}
                  {' '}
                  <small>({identity.username})</small>
                </Link>
              </span>
            );
          })
        }
        {
          !_showLoading
          ||
          <Basic.Icon value="refresh" showLoading />
        }
      </span>
    );
  }
}

ManagersInfo.propTypes = {
  identityContractId: PropTypes.string.required,
  managersFor: PropTypes.string.required,
  _showLoading: PropTypes.bool,
  _managers: PropTypes.arrayOf(PropTypes.object)
};
ManagersInfo.defaultProps = {
  _managers: [],
  _showLoading: false
};

function select(state, component) {
  if (!component.identityContractId) {
    return {};
  }
  const uiKeyId = `${uiKey}-${component.identityContractId}`;
  return {
    _showLoading: Utils.Ui.isShowLoading(state, uiKeyId),
    _managers: Utils.Ui.getEntities(state, uiKeyId)
  };
}

export default connect(select)(ManagersInfo);
