import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
import Immutable from 'immutable';
import { Link } from 'react-router';
//
import * as Basic from '../../components/basic';
import * as Utils from '../../utils';
import { IdentityManager } from '../../redux';
import SearchParameters from '../../domain/SearchParameters';

const uiKey = 'managers-info';

/**
 * Notification detail content
 */
class ManagersInfo extends Basic.AbstractContextComponent {

  constructor(props, context) {
    super(props, context);
    this.identityManager = new IdentityManager();
  }

  componentDidMount() {
    const { identityContract } = this.props;
    if (identityContract._embedded && identityContract._embedded.workingPosition) {
      // load managers by Tree
      if (!Utils.Ui.isShowLoading(this.context.store.getState(), `${uiKey}-${identityContract._embedded.workingPosition.treeType.id}`)) {
        const searchParameters = new SearchParameters().setFilter('managersByTreeType', identityContract._embedded.workingPosition.treeType.id);
        this.context.store.dispatch(this.identityManager.fetchEntities(searchParameters, `${uiKey}-${identityContract._embedded.workingPosition.treeType.id}`));
      }
    }
  }

  getAllManagers() {
    const { identityContract, _managers } = this.props;
    //
    let managers = new Immutable.OrderedMap();
    if (identityContract._embedded && identityContract._embedded.guarantee) {
      managers = managers.set(identityContract._embedded.guarantee.username, identityContract._embedded.guarantee);
    }
    _managers.forEach(manager => {
      managers = managers.set(manager.username, manager);
    });
    return managers.toArray();
  }

  render() {
    const { identityContract, _showLoading} = this.props;
    if (!identityContract) {
      return null;
    }

    return (
      <span>
        {
          this.getAllManagers().map(identity => {
            return (
              <Link to={`/identity/${identity.username}/profile`} style={{ marginRight: 10 }}>
                {this.identityManager.getNiceLabel(identity)}
              </Link>
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
  identityContract: PropTypes.object.required,
  _showLoading: PropTypes.bool,
  _managers: PropTypes.arrayOf(PropTypes.object)
};
ManagersInfo.defaultProps = {
  _managers: [],
  _showLoading: false
};

function select(state, component) {
  if (!component.identityContract || !component.identityContract._embedded || !component.identityContract._embedded.workingPosition) {
    return {};
  }
  const uiKeyId = `${uiKey}-${component.identityContract._embedded.workingPosition.treeType.id}`;
  return {
    _showLoading: Utils.Ui.isShowLoading(state, uiKeyId),
    _managers: Utils.Ui.getEntities(state, uiKeyId)
  };
}

export default connect(select)(ManagersInfo);
