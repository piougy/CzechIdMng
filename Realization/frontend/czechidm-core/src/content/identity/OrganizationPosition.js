import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
import { Link } from 'react-router';
//
import * as Basic from '../../components/basic';
import * as Utils from '../../utils';
import { IdentityManager, SecurityManager, DataManager, TreeNodeManager } from '../../redux';

const uiKey = 'organization-position';

/**
 * Identity position in organization structure
 * * renders first identity's contract working position from root to position
 *
 * @author Radek Tomiška
 */
class OrganizationPosition extends Basic.AbstractContextComponent {

  constructor(props, context) {
    super(props, context);
    this.identityManager = new IdentityManager();
    this.treeNodeManager = new TreeNodeManager();
  }

  componentDidMount() {
    const { identity, rendered } = this.props;
    if (!rendered || !identity) {
      return;
    }
    this.context.store.dispatch(this.identityManager.fetchOrganizationPosition(identity, `${uiKey}-${identity}`));
  }

  render() {
    const { identity, rendered, showLoading, _showLoading, _positions } = this.props;
    if (!rendered || !identity) {
      return null;
    }
    if (showLoading || _showLoading) {
      return (
        <ol className="breadcrumb">
          <li><Basic.Icon value="refresh" showLoading/></li>
        </ol>
      );
    }
    if (!_positions || _positions.length === 0) {
      return null;
    }
    //
    return (
      <ol className="breadcrumb" title={ this.i18n('content.identity.profile.organizationPosition.title') } style={{ marginBottom: 10 }}>
        {
          _positions.map(treeNode => {
            if (!SecurityManager.hasAccess({ 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['TREETYPE_READ']})) {
              return (
                <li>{ this.treeNodeManager.getNiceLabel(treeNode) }</li>
              );
            }
            // link to tree node detail
            return (
              <li key={`op-${treeNode.id}`}>
                <Link to={`/tree/nodes/${treeNode.id}/detail`}>
                  { this.treeNodeManager.getNiceLabel(treeNode) }
                </Link>
              </li>
            );
          })
        }
      </ol>
    );
  }
}

OrganizationPosition.propTypes = {
  ...Basic.AbstractContextComponent.propTypes,
  identity: PropTypes.string,
  _positions: PropTypes.arrayOf(PropTypes.object),
  defaultTreeType: PropTypes.object,
};
OrganizationPosition.defaultProps = {
  ...Basic.AbstractContextComponent.defaultProps,
  identity: null,
  _positions: null,
  _showLoading: true,
  defaultTreeType: null
};

function select(state, component) {
  const uiKeyId = `${uiKey}-${component.identity}`;
  return {
    _showLoading: Utils.Ui.isShowLoading(state, uiKeyId),
    _positions: DataManager.getData(state, uiKeyId)
  };
}

export default connect(select)(OrganizationPosition);
