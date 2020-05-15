import React from 'react';
import { connect } from 'react-redux';
import classnames from 'classnames';
//
import * as Basic from '../../basic';
import {
  IdentityManager,
  RoleManager,
  SecurityManager
} from '../../../redux';
import SearchParameters from '../../../domain/SearchParameters';

const identityManager = new IdentityManager();
const roleManager = new RoleManager();

/**
 * Search box in navigation.
 * @FIXME: Search single identity and role is supported now only (implement service registration).
 * @FIXME: both READ authorities are required now to show search box - search by permission.
 *
 * @author Radek Tomiška
 * @since 10.3.0
 */
class NavigationSearch extends Basic.AbstractContextComponent {

  constructor(props, context) {
    super(props, context);
    //
    this.state = {
      searchShowLoading: false
    };
  }

  getComponentKey() {
    return 'component.advanced.NavigationSearch';
  }

  /**
   * Seach identity or role:
   * - by codeable identifier
   * - then by text usage.
   */
  search(event) {
    if (event) {
      event.preventDefault();
    }
    //
    const text = this.refs['input-search'].getValue();
    if (!text) {
      return;
    }
    //
    // FIXME: implement service registration
    // FIXME: service vs manager usage - try to rewire na e.g.  Promise.All
    this.setState({
      searchShowLoading: true
    }, () => {
      this.context.store.dispatch(identityManager.fetchEntity(text, 'search', (identity, e1) => {
        if (e1 && e1.statusCode === 404) {
          this.context.store.dispatch(roleManager.fetchEntity(text, 'search2', (role, e2) => {
            if (e2 && e2.statusCode === 404) {
              // text search is used => when at least record is found, then first detial is shown. Warning message is shown otherwise.
              identityManager.getService()
                .search(new SearchParameters().setFilter('text', text).setSort('disabled', true).setSort('username', true))
                .then(json => {
                  // ok
                  const entities = json._embedded[identityManager.getCollectionType()] || [];
                  if (entities.length > 0) {
                    this.context.history.push(identityManager.getDetailLink(entities[0]));
                    this.setState({
                      searchShowLoading: false
                    });
                  } else {
                    roleManager.getService()
                      .search(new SearchParameters().setFilter('text', text).setSort('disabled', true).setSort('code', true))
                      .then(json2 => {
                        // ok
                        const entities2 = json2._embedded[roleManager.getCollectionType()] || [];
                        if (entities2.length > 0) {
                          this.context.history.push(`/role/${ encodeURIComponent(entities2[0].id) }/detail`);
                          this.refs['input-search'].setValue(null);
                        } else {
                          // not found message
                          this.addMessage({
                            level: 'info',
                            title: this.i18n('message.notFound.title'),
                            message: this.i18n('message.notFound.message', { text })
                          });
                        }
                        this.setState({
                          searchShowLoading: false
                        });
                      })
                      .catch(error => {
                        this.addError(error);
                        this.setState({
                          searchShowLoading: false
                        });
                      });
                  }
                })
                .catch(error => {
                  this.addError(error);
                  this.setState({
                    searchShowLoading: false
                  });
                });
            } else if (e2) {
              this.addError(e2);
            } else {
              this.context.history.push(`/role/${ encodeURIComponent(role.id) }/detail`);
              this.refs['input-search'].setValue(null);
            }
            this.setState({
              searchShowLoading: false
            });
          }));
        } else if (e1) {
          this.addError(e1);
          this.setState({
            searchShowLoading: false
          });
        } else {
          this.context.history.push(identityManager.getDetailLink(identity));
          this.refs['input-search'].setValue(null);
          this.setState({
            searchShowLoading: false
          });
        }
      }));
    });
  }

  render() {
    const { rendered, showLoading, className, userContext } = this.props;
    const { searchShowLoading } = this.state;
    //
    if (!rendered) {
      return null;
    }
    // FIXME - by registered services
    if (!SecurityManager.hasAllAuthorities(['IDENTITY_READ', 'ROLE_READ'], userContext)) {
      return null;
    }
    //
    return (
      <form
        className={ classnames(className, 'navigation-search') }
        onSubmit={ this.search.bind(this) }>
        <Basic.Div className="input-group">
          <Basic.TextField
            label={ null }
            ref="input-search"
            placeholder={ this.i18n('search.placeholder') }/>
          <span className="input-group-btn">
            <Basic.Button
              showLoading={ searchShowLoading || showLoading }
              title={ this.i18n('button.title') }
              showLoadingIcon
              level="default"
              type="submit"
              icon="search"/>
          </span>
        </Basic.Div>
      </form>
    );
  }
}

function select(state) {
  //
  return {
    userContext: state.security.userContext,
    i18nReady: state.config.get('i18nReady')
  };
}

export default connect(select)(NavigationSearch);
