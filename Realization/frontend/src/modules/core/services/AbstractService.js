'use strict';

import _ from 'lodash';
//
import RestApiService from './RestApiService';
import SearchParameters from '../domain/SearchParameters';

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
    return RestApiService.get(this.getApiPath() + `/${id}`);
  }

  create(json) {
    return RestApiService.post(this.getApiPath(), json);
  }

  upload(formData) {
    return RestApiService.upload(this.getApiPath(), formData);
  }

  deleteById(id) {
    return RestApiService.delete(this.getApiPath() + `/${id}`);
  }

  updateById(id, json) {
    return RestApiService.put(this.getApiPath() + `/${id}`, json);
  }

  patchById(id, json) {
    return RestApiService.patch(this.getApiPath() + `/${id}`, json);
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
    return RestApiService.get(this.getApiPath() + searchParameters.toUrl());
  }

  bulkSave(json) {
    return RestApiService.put(this.getApiPath(), json);
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
      return newSearchParameters
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
}
