import _ from 'lodash';

/**
 * Complex help content. Immutable.
 *
 * @author Radek Tomiška
 */
export default class HelpContent {

  constructor(header = null, body = null) {
    this.header = header;
    this.body = body;
  }

  _clone() {
    return _.clone(this);
  }

  /**
   * Help header
   *
   * @return {string}
   */
  getHeader() {
    return this.header;
  }

  /**
   * Sets help header
   *
   * @param {string} title
   */
  setHeader(header) {
    const newState = this._clone();
    newState.header = header;
    return newState;
  }

  /**
   * Help body
   *
   * @return {string}
   */
  getBody() {
    return this.body;
  }

  /**
   * Sets help body
   *
   * @param {string} body
   */
  setBody(body) {
    const newState = this._clone();
    newState.body = body;
    return newState;
  }

}
