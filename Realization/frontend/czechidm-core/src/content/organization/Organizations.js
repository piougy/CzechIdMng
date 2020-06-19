import PropTypes from 'prop-types';
import React from 'react';
import { connect } from 'react-redux';
import uuid from 'uuid';
//
import * as Basic from '../../components/basic';
import * as Utils from '../../utils';
import { TreeTypeManager, TreeNodeManager, DataManager } from '../../redux';

/**
 * Preselect default organizations for list of organizations.
 *
 * @author Radek Tomiška
 */
class Organizations extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    //
    this.treeTypeManager = new TreeTypeManager();
    this.treeNodeManager = new TreeNodeManager();
  }

  getContentKey() {
    return 'content.organizations';
  }

  getNavigationKey() {
    return 'organizations';
  }

  getManager() {
    return this.treeNodeManager;
  }

  getTypeManager() {
    return this.treeTypeManager;
  }

  componentDidMount() {
    super.componentDidMount();
    //
    const selectedTypeId = this._getTypeIdFromParam();
    if (selectedTypeId) {
      this.context.history.push(`/tree/nodes?type=${ selectedTypeId }`);
    } else {
      this.context.store.dispatch(this.getTypeManager().fetchDefaultTreeType((defaultTreeType, error) => {
        if (error) {
          this.addError(error);
        } else if (defaultTreeType) {
          this.context.history.push(`/tree/nodes?type=${ defaultTreeType.id }`);
        } else {
          this.context.history.push('/tree/nodes');
        }
      }));
    }
  }

  _getTypeIdFromParam() {
    const { query } = this.props.location;
    return (query) ? query.type : null;
  }

  onCreateType(event) {
    if (event) {
      event.preventDefault();
    }
    //
    const uuidId = uuid.v1();
    this.context.history.push(`/tree/types/${ uuidId }?new=1`);
  }

  showTreeTypes() {
    this.context.history.push(`/tree/types`);
  }

  render() {
    return (
      <Basic.Div>
        { this.renderPageHeader() }

        <Basic.Panel>
          <Basic.Loading isStatic show/>
        </Basic.Panel>
      </Basic.Div>
    );
  }
}


Organizations.propTypes = {
  defaultTreeType: PropTypes.object,
  showLoading: PropTypes.bool
};
Organizations.defaultProps = {
  defaultTreeType: null,
  showLoading: true
};

function select(state) {
  return {
    defaultTreeType: DataManager.getData(state, TreeTypeManager.UI_KEY_DEFAULT_TREE_TYPE),
    showLoading: Utils.Ui.isShowLoading(state, TreeTypeManager.UI_KEY_DEFAULT_TREE_TYPE)
  };
}

export default connect(select)(Organizations);
