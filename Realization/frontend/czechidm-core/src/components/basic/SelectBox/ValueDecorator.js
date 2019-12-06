import React from 'react';
import classNames from 'classnames';
//
import AbstractContextComponent from '../AbstractContextComponent/AbstractContextComponent';
import Icon from '../Icon/Icon';

/**
 * Base selectbox value decorator (~selected value). Reuses react-select component behavior.
 * - getEntityIcon can be overriden - then icon for the option can be rendered (TODO: move to props, method overriding can be used now).
 *
 * @see https://github.com/JedWatson/react-select/blob/v1.2.1/src/Value.js
 * @author Radek Tomiška
 * @since 9.5.0
 */
export default class ValueDecorator extends AbstractContextComponent {

  /**
   * react-select method
   */
  handleMouseDown(event) {
    if (event.type === 'mousedown' && event.button !== 0) {
      return;
    }
    if (this.props.onClick) {
      event.stopPropagation();
      this.props.onClick(this.props.value, event);
      return;
    }
    if (this.props.value.href) {
      event.stopPropagation();
    }
  }

  /**
   * react-select method
   */
  onRemove(event) {
    event.preventDefault();
    event.stopPropagation();
    this.props.onRemove(this.props.value);
  }

  /**
   * react-select method
   */
  handleTouchEndRemove(event) {
    // Check if the view is being dragged, In this case
    // we don't want to fire the click event (because the user only wants to scroll)
    if (this.dragging) {
      return;
    }

    // Fire the mouse events
    this.onRemove(event);
  }

  /**
   * react-select method
   */
  handleTouchMove() {
    // Set a flag that the view is being dragged
    this.dragging = true;
  }

  /**
   * react-select method
   */
  handleTouchStart() {
    // Set a flag that the view is not being dragged
    this.dragging = false;
  }

  /**
   * Returns entity icon (null by default - icon will not be rendered)
   *
   * @param  {object} entity
   */
  getEntityIcon(/* entity*/) {
    return null;
  }

  /**
   * Render icon
   *
   * @param  {dto} entity
   * @return {element}
   */
  renderIcon(entity) {
    return (
      <Icon value={ this.getEntityIcon(entity) } style={{ marginRight: 5, fontSize: '0.9em' }}/>
    );
  }

  renderRemoveIcon() {
    const { disabled, onRemove } = this.props;
    //
    if (disabled || !onRemove) {
      return null;
    }
    //
    return (
      <span
        className="Select-value-icon"
        aria-hidden="true"
        onMouseDown={ this.onRemove.bind(this) }
        onTouchEnd={ this.handleTouchEndRemove.bind(this) }
        onTouchStart={ this.handleTouchStart.bind(this) }
        onTouchMove={ this.handleTouchMove.bind(this) }>
        &times;
      </span>
    );
  }

  render() {
    const { children, value } = this.props;
    //
    return (
      <div
        className={ classNames('Select-value', this.props.value.className) }
        style={ this.props.value.style }
        title={ this.props.value.title }>
        { this.renderRemoveIcon() }
        <span className="Select-value-label">
          { this.renderIcon(value) }
          { children }
        </span>
      </div>
    );
  }
}
