import _ from 'lodash';
import Immutable from 'immutable';

/**
 * Immutable search representation - filter, sort, pageable.
 * Every modify operation returns new cloned SearchParameters with new values
 *
 * @author Radek Tomiška
 */
export default class SearchParameters {

  constructor(name = null, page = 0, size = SearchParameters.DEFAUT_SIZE) {
    this.name = name;
    this.page = page;
    this.size = size;
    this.filters = new Immutable.Map({});
    this.sorts = new Immutable.OrderedMap({});
  }

  _clone() {
    return _.clone(this);
  }

  /**
   * Search name
   *
   * @return {string}
   */
  getName() {
    return this.name;
  }

  /**
   * Sets search name
   *
   * @param {[type]} name [description]
   */
  setName(name) {
    const newState = this._clone();
    newState.name = name;
    return newState;
  }

  /**
   * Pageable size
   *
   * @return {number}
   */
  getSize() {
    return this.size;
  }

  /**
   * Pageable size
   *
   * @param {number} size = SearchParameters.DEFAUT_SIZE
   * @return {SearchParameters} new copy
   */
  setSize(size = SearchParameters.DEFAUT_SIZE) {
    const newState = this._clone();
    newState.size = size;
    return newState;
  }

  /**
   * Current Pageable (counted from zero)
   *
   * @return {Number}
   */
  getPage() {
    return this.page;
  }

  /**
   * Sets current page (counted from zero)
   *
   * @param {number} page = 0
   * @return {SearchParameters} new copy
   */
  setPage(page = 0) {
    const newState = this._clone();
    newState.page = page;
    return newState;
  }

  /**
   * Sets sort by property
   *
   * @param {string} property
   * @param {bool} ascending = true
   * @return {SearchParameters} new copy
   */
  setSort(property, ascending = true) {
    if (!property) {
      return this;
    }
    if (typeof ascending === 'string') {
      ascending = ascending.toLowerCase() === 'asc';
    }
    const newState = this._clone();
    newState.sorts = newState.sorts.set(property, ascending);
    return newState;
  }

  /**
   * Clears sort by property. If propety is null, then all sorts ic cleared
   *
   * @param  {string} property
   * @return {SearchParameters} new copy
   */
  clearSort(property = null) {
    const newState = this._clone();
    //
    if (property) {
      if (newState.sorts.has(property)) {
        newState.sorts = newState.sorts.delete(property);
      }
    } else { // clear all
      newState.sorts = newState.sorts.clear();
    }
    return newState;
  }

  /**
   * Returns true, when sort for given property is setted ascending.
   * Returns false, when sort for given property is setted descending.
   * If sort for given property is not setted, then returns null.
   *
   * @param  {string} property
   * @return {bool} true - asc | false - desc | null - not setted
   */
  getSort(property) {
    if (!property || !this.sorts.has(property)) {
      return null;
    }
    return this.sorts.get(property);
  }

  /**
   * Returns setted sorts as immutable ordered map
   *
   * @return {Immutable.OrderedMap<property, bool>} true = asc | false = desc
   */
  getSorts() {
    return this.sorts;
  }

  /**
   * Sets filter for given property
   *
   * @param {string} property
   * @param {string} filter value
   * @return {SearchParameters} new copy
   */
  setFilter(property, filter) {
    if (!property) {
      return this;
    }
    const newState = this._clone();
    newState.filters = newState.filters.set(property, filter);
    return newState;
  }

  /**
   * Sets filters - immutable map is needed
   *
   * @param {Immutable.Map} filters
   */
  setFilters(filters) {
    const newState = this._clone();
    if (!filters) {
      newState.clearFilter();
    } else if (!(filters instanceof Immutable.Map)) {
      // console.log('#setFilters supports immutable maps only');
      newState.clearFilter();
    } else {
      newState.filters = filters;
    }
    return newState;
  }

  /**
   * Returns setted filters as immutable map
   *
   * @return {Immutable.Map<property, filter>}
   */
  getFilters() {
    return this.filters;
  }

  /**
   * Clears filter by property. If propety is null, then all filter is cleared
   *
   * @param  {string} property
   * @return {SearchParameters} new copy
   */
  clearFilter(property = null) {
    const newState = this._clone();
    //
    if (property) {
      if (newState.filters.has(property)) {
        newState.filters = newState.filters.delete(property);
      }
    } else { // clear all
      newState.filters = newState.filters.clear();
    }
    return newState;
  }

  /**
   * Converts from json
   *
   * @param  {object} json
   * @return {SearchParameters}
   */
  static fromJS(json) {
    if (!json) {
      return new SearchParameters();
    }
    // TODO: convert from json
    throw new Error('unsupported operation');
  }

  /**
   * Converts to json
   *
   * @return {object}
   */
  toJs() {
    // TODO: convert to json
    throw new Error('unsupported operation');
  }

  /**
   * Returns search url (search suffix with name, page, size, sorts, filters)
   *
   * @return {string} url part
   */
  toUrl() {
    let url = '';
    if (this.name) { // if search name is setted - else default endpoint is used
      url += `/search/${this.name}`;
    }
    url += `?size=${this.size}&page=${this.page}`;
    // pageable
    this.sorts.forEach((ascending, property) => {
      url += `&sort=${property},` + (ascending ? 'asc' : 'desc');
    });
    // filterable
    this.filters.forEach((filter, property) => {
      if (filter !== null && filter !== undefined) {
        url += `&${property}=${encodeURIComponent(filter)}`;
      }
    });
    return url;
  }

  /**
   * Returs true, if searchparameters are equals
   *
   * @param  {SearchParameters} other
   * @return {bool}
   */
  equals(other) {
    return this.name === other.getName()
      && this.page === other.getPage()
      && this.size === other.getSize()
      && Immutable.is(this.filters, other.getFilters())
      && Immutable.is(this.sorts, other.getSorts());
  }

  /**
   * Returs true, if searchparameters are equals
   *
   * @param  {SearchParameters} one
   * @param  {SearchParameters} two
   * @return {bool}
   */
  static is(one, two) {
    if (!one && !two) {
      return true;
    }
    if ((one && !two) || (!one && two)) {
      return false;
    }
    return one.equals(two);
  }
}

/**
 * Default search name
 * @type {String}
 */
SearchParameters.NAME_QUICK = 'quick';
/**
 * Search name for autocomplete (select box etc.)
 * @type {String}
 */
SearchParameters.NAME_AUTOCOMPLETE = 'autocomplete';
/**
 * Default page size
 * @type {Number}
 */
SearchParameters.DEFAUT_SIZE = 10;
/**
 * Maximum page size
 * @type {Number}
 */
SearchParameters.MAX_SIZE = 500;
/**
 * Blank UUID. Can be use for sitiuations when we don't have ID for some entity, but we need use some "default" value.
 * @type {String}
 */
SearchParameters.BLANK_UUID = '00000000-0000-0000-0000-000000000000';
