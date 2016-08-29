

import RestApiService from './RestApiService';
import SearchParameters from '../domain/SearchParameters';
import * as Utils from '../utils';

/**
 * Contains basic CRUD + search operation
 */
export default class AbstractService {

  constructor() {
    if (this.getApiPath === undefined) {
      throw new TypeError('Must override method getApiPath()');
    }
    this.getById = this.getById.bind(this);
  }

  /**
   * Returns absolute endpoint patch
   *
   * @return {[type]} [description]
   */
  getAbsoluteApiPath() {
    return RestApiService.getUrl(this.getApiPath());
  }

  /**
   * Textual entity reprezentation (~entity.toString())
   * @param  {entity} entity
   * @return {string]}
   */
  getNiceLabel(entity) {
    return entity._links.self.href;
  }

  /**
   * Returns resource by given id
   *
   * @param  {string|number} id resource identifier
   * @return {Promise} promise with response
   */
  getById(id) {
    return RestApiService
      .get(this.getApiPath() + `/${id}`)
      .then(response => {
        if (response.status === 403) {
          throw new Error(403);
        }
        if (response.status === 404) {
          throw new Error(404);
        }
        return response.json();
      })
      .then(json => {
        if (Utils.Response.hasError(json)) {
          throw Utils.Response.getFirstError(json);
        }
        return json;
      });
  }

  create(json) {
    return RestApiService
      .post(this.getApiPath(), json)
      .then(response => {
        return response.json();
      })
      .then(json => {
        if (Utils.Response.hasError(json)) {
          throw Utils.Response.getFirstError(json);
        }
        return json;
      });
  }

  upload(formData) {
    return RestApiService
      .upload(this.getApiPath(), formData)
      .then(response => {
        return response.json();
      })
      .then(json => {
        if (Utils.Response.hasError(json)) {
          throw Utils.Response.getFirstError(json);
        }
        return json;
      });
  }

  deleteById(id) {
    return RestApiService
      .delete(this.getApiPath() + `/${id}`)
      .then(response => {
        if (response.status === 204) {
          return {};
        }
        return response.json();
      })
      .then(json => {
        if (Utils.Response.hasError(json)) {
          throw Utils.Response.getFirstError(json);
        }
        return json;
      });
  }

  updateById(id, json) {
    return RestApiService
      .put(this.getApiPath() + `/${id}`, json)
      .then(response => {
        return response.json();
      })
      .then(json => {
        if (Utils.Response.hasError(json)) {
          throw Utils.Response.getFirstError(json);
        }
        return json;
      });
  }

  patchById(id, json) {
    return RestApiService
      .patch(this.getApiPath() + `/${id}`, json)
      .then(response => {
        return response.json();
      })
      .then(json => {
        if (Utils.Response.hasError(json)) {
          throw Utils.Response.getFirstError(json);
        }
        return json;
      });
  }

  search(searchParameters) {
    if (!searchParameters) {
      searchParameters = this.getDefaultSearchParameters();
    }
    if (!(searchParameters instanceof SearchParameters)) {
      // TODO: try construct SearchParameters.fromJs
      // TODO: log4js warn
      console.log('Search parameters is not instance of SearchParameters - using default', searchParameters);
      searchParameters = this.getDefaultSearchParameters();
    }
    return RestApiService
      .get(this.getApiPath() + searchParameters.toUrl())
      .then(response => {
        return response.json();
      })
      .then(json => {
        if (Utils.Response.hasError(json)) {
          throw Utils.Response.getFirstError(json);
        }
        return json;
      });
  }

  /**
   * Returns default searchParameters for current entity type
   *
   * @return {object} searchParameters
   */
  getDefaultSearchParameters() {
    return new SearchParameters().setSort('id');
  }

  /**
   * Merge search parameters - second sp has higher priority
   *
   * @param  {object} previousSearchParameters
   * @param  {object} newSearchParameters
   * @return {object} resultSearchParameters
   */
  mergeSearchParameters(previousSearchParameters, newSearchParameters) {
    if (!newSearchParameters) { // nothing to merge with
      return previousSearchParameters;
    }
    if (!previousSearchParameters) { // nothing to merge with
      return newSearchParameters;
    }
    // merge filters
    let _searchParameters = previousSearchParameters;
    newSearchParameters.getFilters().forEach((filter, property) => {
      _searchParameters = _searchParameters.setFilter(property, filter);
    });
    // override sorts if needed
    if (!newSearchParameters.getSorts().isEmpty()) {
      _searchParameters = _searchParameters.clearSort();
      newSearchParameters.getSorts().forEach((ascending, property) => {
        _searchParameters = _searchParameters.setSort(property, ascending);
      });
    }
    // override pagination
    if (newSearchParameters.getPage() !== null) {
      _searchParameters = _searchParameters.setPage(newSearchParameters.getPage());
    }
    if (newSearchParameters.getSize() !== null) {
      _searchParameters = _searchParameters.setSize(newSearchParameters.getSize());
    }
    return _searchParameters;
  }

  /**
   * Get given audits revisions for apiPath and username, role name, organizations ids
   *
   * @param apiPath {string}
   * @param entityId {string, number}
   * @return {Promise}
   */
  getRevisions(entityId) {
    return RestApiService
    .get(this.getApiPath() + `/${entityId}/revisions`)
    .then(response => {
      return response.json();
    })
    .then(json => {
      if (Utils.Response.hasError(json)) {
        throw Utils.Response.getFirstError(json);
      }
      return json;
    });
  }

  /**
   * Get single revision for username, role name, organizations ids and id revision
   *
   * @param entityId {string, number}
   * @param revId {number}
   * @return {Promise}
   */
  getRevision(entityId, revId) {
    return RestApiService
    .get(this.getApiPath() + `/${entityId}/revisions/${revId}`)
    .then(response => {
      return response.json();
    })
    .then(json => {
      if (Utils.Response.hasError(json)) {
        throw Utils.Response.getFirstError(json);
      }
      return json;
    });
  }
}
