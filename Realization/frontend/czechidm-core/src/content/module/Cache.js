import React from 'react';
import PropTypes from 'prop-types';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
//
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import { CacheManager, DataManager, SecurityManager } from '../../redux';
import * as Utils from '../../utils';
import ConfigLoader from '../../utils/ConfigLoader';
import ResultCodesModal from './ResultCodesModal';

/**
 * BE modules administration
 *
 * @author Radek Tomiška
 */
class Cache extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      detail: {
        show: false
      }
    };
    this.cacheManager = new CacheManager();
  }

  getContentKey() {
    return 'content.system.cache';
  }

  getNavigationKey() {
    return 'cache';
  }

  componentDidMount() {
    super.componentDidMount();
    //
    this.context.store.dispatch(this.cacheManager.fetchAvailableCaches());
  }

  reload() {
    const { _searchParameters } = this.props;
    //
    this.context.store.dispatch(this.cacheManager.fetchAvailableCaches());
  }

  onEvict(entity, enable, event) {
    if (event) {
      event.preventDefault();
    }
    this.refs[`confirm-evict`].show(
      this.i18n(`action.evict.message`, { count: 1, record: entity.name }),
      this.i18n(`action.evict.header`, { count: 1 })
    ).then(() => {
      this.context.store.dispatch(this.cacheManager.evictCache(entity.id, (patchedEntity, error) => {
        if (!error) {
          this.addMessage({ message: this.i18n(`action.evict.success`, { count: 1, record: entity.name }) });
          this.reload();
        } else {
          this.addError(error);
        }
      }));
    }, () => {
      // Rejected
    });
  }



  render() {
    const { caches, showLoading } = this.props;
    const { detail } = this.state;

    const _caches = [];
    if (caches) {
      caches.forEach(cache => {
        _caches.push(cache);
      });
    }
    _caches.sort((one, two) => one.name > two.name);

    return (
      <div>
        <Helmet title={this.i18n('title')} />
        <Basic.Confirm ref="confirm-evict" level="warning"/>

        <ResultCodesModal detail={ detail } />

        <Basic.Table
          ref="table"
          data={_caches}
          showLoading={showLoading}
          noData={this.i18n('component.basic.Table.noData')}
          rowClass={({rowIndex, data}) => Utils.Ui.getRowClass(data[rowIndex]) }>

          <Basic.Column property="name" header={this.i18n('entity.Cache.name')}/>
          <Basic.Column property="size" header={this.i18n('entity.Cache.size')}/>
          <Basic.Column
            header={this.i18n('label.action')}
            className="action"
            cell={
              ({rowIndex, data}) => {
                return (
                  <Basic.Button
                    level="success"
                    onClick={this.onEvict.bind(this, data[rowIndex], true)}
                    className="btn-xs"
                    title={this.i18n('button.evict')}
                    titlePlacement="bottom">
                    {this.i18n('button.evict')}
                  </Basic.Button>
                );
              }
            }
            rendered={SecurityManager.hasAuthority('MODULE_UPDATE')}/>
        </Basic.Table>
      </div>
    );
  }
}

Cache.propTypes = {
  userContext: PropTypes.object,
  caches: PropTypes.object,
  showLoading: PropTypes.bool
};
Cache.defaultProps = {
  userContext: null,
  caches: null,
  showLoading: true
};

function select(state) {
  return {
    userContext: state.security.userContext,
    caches: DataManager.getData(state, CacheManager.UI_KEY_MODULES),
    showLoading: Utils.Ui.isShowLoading(state, CacheManager.UI_KEY_MODULES)
  };
}

export default connect(select)(Cache);
