import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
import Helmet from 'react-helmet';
import * as Basic from '../../components/basic';
import { RoleCatalogueManager } from '../../redux';
import RoleCatalogueDetail from './RoleCatalogueDetail';

const roleCatalogueManager = new RoleCatalogueManager();

/**
 * Role Catalogue detail - first tab on role detail
 */
class RoleCatalogueContent extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.setState({
      _showLoading: true
    });
  }

  getContentKey() {
    return 'content.roleCatalogues';
  }

  componentDidMount() {
    const { entityId } = this.props.params;
    this.selectNavigationItems(['role-catalogues', 'role-catalogue-detail']);
    const isNew = this._getIsNew();

    if (isNew) {
      this.context.store.dispatch(roleCatalogueManager.receiveEntity(entityId, { }));
    } else {
      this.getLogger().debug(`[RoleCatalogueContent] loading entity detail [id:${entityId}]`);
      this.context.store.dispatch(roleCatalogueManager.fetchEntity(entityId), entityId, () => {
        this.setState({
          _showLoading: false
        });
      });
    }
  }

  _getIsNew() {
    const { query } = this.props.location;
    return (query) ? query.new : null;
  }

  render() {
    const { entity, showLoading } = this.props;
    return (
      <div>
        <Helmet title={this.i18n('title')} rendered={!this._getIsNew()} />
        <Basic.Confirm ref="confirm-delete" level="danger"/>
        {
          !entity
          ||
          <Basic.PageHeader>
            <Basic.Icon value="fa:list-alt"/>
            {' '}
            {
              this._getIsNew()
              ?
              this.i18n('create.title')
              :
              <span>{entity.name} <small>{this.i18n('edit')}</small></span>
            }
          </Basic.PageHeader>
        }
        <Basic.Panel>
          <Basic.Loading isStatic showLoading={showLoading} />
          {
            !entity
            ||
            <RoleCatalogueDetail entity={entity} />
          }
        </Basic.Panel>

      </div>
    );
  }
}

RoleCatalogueContent.propTypes = {
  entity: PropTypes.object,
  showLoading: PropTypes.bool
};
RoleCatalogueContent.defaultProps = {
};

function select(state, component) {
  const { entityId } = component.params;
  //
  return {
    entity: roleCatalogueManager.getEntity(state, entityId),
    showLoading: roleCatalogueManager.isShowLoading(state, null, entityId)
  };
}

export default connect(select)(RoleCatalogueContent);
