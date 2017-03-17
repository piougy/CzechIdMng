import React, { PropTypes } from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
//
import * as Basic from '../../components/basic';
import * as Utils from '../../utils';
import { IdentityManager, RoleManager, DataManager } from '../../redux';
import AuthoritiesPanel from '../role/AuthoritiesPanel';
import authorityHelp from '../role/AuthoritiesPanel_cs.md';

const uiKeyAuthorities = 'identity-roles';
const identityManager = new IdentityManager();
const roleManager = new RoleManager();

/**
 * Identity's authorities
 *
 * @author Radek Tomiška
 */
class IdentityAuthorities extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
  }

  getContentKey() {
    return 'content.identity.authorities';
  }

  getNavigationKey() {
    return 'profile-authorities';
  }

  componentDidMount() {
    super.componentDidMount();
    const { entityId } = this.props.params;
    this.context.store.dispatch(identityManager.fetchAuthorities(entityId, `${uiKeyAuthorities}-${entityId}`));
  }

  render() {
    const { authorities } = this.props;
    //
    return (
      <div>
        <Basic.Confirm ref="confirm-delete" level="danger"/>
        <Helmet title={this.i18n('title')} />

        <Basic.Panel className="no-border">
          <Basic.PanelHeader help={authorityHelp} style={{ marginBottom: 15 }}>
            <h3><span dangerouslySetInnerHTML={{ __html: this.i18n('header') }}/></h3>
          </Basic.PanelHeader>

          <AuthoritiesPanel
            roleManager={roleManager}
            authorities={authorities}
            disabled/>

        </Basic.Panel>
      </div>
    );
  }
}

IdentityAuthorities.propTypes = {
  _showLoading: PropTypes.bool,
  authorities: PropTypes.arrayOf(React.PropTypes.object)
};
IdentityAuthorities.defaultProps = {
  _showLoading: true,
  authorities: []
};

function select(state, component) {
  return {
    _showLoading: Utils.Ui.isShowLoading(state, `${uiKeyAuthorities}-${component.params.entityId}`),
    authorities: DataManager.getData(state, `${uiKeyAuthorities}-${component.params.entityId}`)
  };
}

export default connect(select)(IdentityAuthorities);
