import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
import Helmet from 'react-helmet';
import * as Basic from '../../../components/basic';
import { AutomaticRoleAttributeRuleManager } from '../../../redux';
import AutomaticRoleAttributeRuleDetail from './AutomaticRoleAttributeRuleDetail';

const manager = new AutomaticRoleAttributeRuleManager();

/**
 * Detail rule for automatic role by attribute
 *
 * @author Ondrej Kopr
 */
class AutomaticRoleAttributeRuleContent extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
  }

  getContentKey() {
    return 'content.automaticRoles.attribute.rule';
  }

  componentDidMount() {
    const { ruleId } = this.props.params;
    this.selectNavigationItems(['system', 'automatic-roles']);

    if (this._getIsNew()) {
      this.context.store.dispatch(manager.receiveEntity(ruleId, { }));
    } else {
      this.getLogger().debug(`[TypeContent] loading entity detail [id:${ruleId}]`);
      this.context.store.dispatch(manager.fetchEntity(ruleId));
    }
  }

  /**
   * Function check if exist params new
   */
  _getIsNew() {
    const { query } = this.props.location;
    return (query) ? query.new : null;
  }

  render() {
    const { entity, showLoading } = this.props;
    const { entityId } = this.props.params;
    return (
      <div>
        {
          this._getIsNew()
          ?
          <Helmet title={this.i18n('create.title')} />
          :
          <Helmet title={this.i18n('edit.title')} />
        }
        <Basic.Confirm ref="confirm-delete" level="danger"/>
        {
          !entity
          ||
          <Basic.PageHeader>
            <Basic.Icon value="fa:universal-access"/>
            {' '}
            {
              this._getIsNew()
              ?
              this.i18n('create.header')
              :
              <span>{this.i18n('edit.header')}</span>
            }
          </Basic.PageHeader>
        }

        <Basic.Panel>
          <Basic.Loading isStatic showLoading={showLoading} />
          {
            !entity
            ||
            <AutomaticRoleAttributeRuleDetail entity={entity} manager={manager} attributeId={entityId} />
          }
        </Basic.Panel>

      </div>
    );
  }
}

AutomaticRoleAttributeRuleContent.propTypes = {
  showLoading: PropTypes.bool
};
AutomaticRoleAttributeRuleContent.defaultProps = {
};

function select(state, component) {
  const { ruleId } = component.params;
  //
  return {
    entity: manager.getEntity(state, ruleId),
    showLoading: manager.isShowLoading(state, null, ruleId)
  };
}

export default connect(select)(AutomaticRoleAttributeRuleContent);
