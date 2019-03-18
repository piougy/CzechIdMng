import React from 'react';
import classNames from 'classnames';
//
import Icon from '../Icon/Icon';
import ShortText from '../ShortText/ShortText';
import AbstractComponent from '../AbstractComponent/AbstractComponent';

/**
 * Base selectbox option decorator. Reuses react-select component behavior.
 * - disabled option cannot be selected (TODO: move to props, method overriding can be used now).
 * - if entity has a description, then will be shown (smaller + italic) - max length 100 (TODO: move to props, method overriding can be used now).
 * - getEntityIcon can be overriden - then icon for the option can be rendered (TODO: move to props, method overriding can be used now).
 *
 * @see https://github.com/JedWatson/react-select/blob/v1.2.1/src/Option.js
 * @author Radek Tomiška
 * @since 9.5.0
 */
export default class OptionDecorator extends AbstractComponent {

  /**
   * react-select method
   */
  handleMouseDown(event) {
    if (event) {
      event.preventDefault();
      event.stopPropagation();
    }
    //
    const { onSelect, option } = this.props;
    if (onSelect) {
      onSelect(option, event);
    }
  }

  /**
   * react-select method
   */
  handleMouseEnter(event) {
    const { onFocus, option } = this.props;
    //
    if (onFocus) {
      onFocus(option, event);
    }
  }

  /**
   * react-select method
   */
  handleMouseMove(event) {
    const { isFocused, onFocus, option } = this.props;
    //
    if (isFocused) {
      return;
    }
    if (onFocus) {
      onFocus(option, event);
    }
  }

  /**
   * react-select method
   */
  handleTouchEnd(event) {
		// Check if the view is being dragged, In this case
		// we don't want to fire the click event (because the user only wants to scroll)
    if (this.dragging) {
      return;
    }
    this.handleMouseDown(event);
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
   * Shorten the description.
   *
   * @return {int}
   */
  getDescriptionMaxLength() {
    return 100;
  }

  /**
   * Entity is disabled.
   *
   * @param  {dto}  entity
   * @return {Boolean} Return undefined, when disabled option hasn't be controlled.
   */
  isDisabled(entity) {
    if (!entity || entity.disabled === true) {
      return true;
    }
    return false;
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

  /**
   * Render description
   *
   * @param  {dto} entity
   * @return {element}
   */
  renderDescription(entity) {
    if (!entity || !entity.description) {
      return null;
    }
    //
    return (
      <ShortText
        value={ entity.description }
        maxLength={ this.getDescriptionMaxLength() }
        style={{ color: this.isDisabled(entity) ? '#ccc' : '#555', fontSize: '0.95em', fontStyle: 'italic' }}/>
    );
  }

  render() {
    const { option, children } = this.props;
    const disabled = this.isDisabled(option);
    const className = classNames(this.props.className, option.className);
    //
    return (
      <div
        className={ className }
        style={ option.style }
        role="option"
        aria-label={ option.label }
				onMouseDown={ disabled === true ? undefined : this.handleMouseDown.bind(this) }
				onMouseEnter={ disabled === true ? undefined : this.handleMouseEnter.bind(this) }
				onMouseMove={ disabled === true ? undefined : this.handleMouseMove.bind(this) }
        onTouchStart={ disabled === true ? undefined : this.handleTouchStart.bind(this) }
				onTouchMove={ disabled === true ? undefined : this.handleTouchMove.bind(this) }
				onTouchEnd={ disabled === true ? undefined : this.handleTouchEnd.bind(this) }
				title={option.title}>
				<div>
          { this.renderIcon(option) }
          { children }
        </div>
        { this.renderDescription(option) }
			</div>
		);
  }
}
