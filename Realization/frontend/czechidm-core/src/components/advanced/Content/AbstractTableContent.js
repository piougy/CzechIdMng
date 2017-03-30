import * as Basic from '../../basic';

/**
* Advance table content with entity CRUD methods id modal
*
* @author Radek Tomiška
*/
export default class AbstractTableContent extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    // prepare state for modal detail
    this.state = {
      detail: {
        show: false,
        entity: {}
      }
    };
  }

  /**
   * Returns main content / table manager
   */
  getManager() {
    return null;
  }

  /**
   * Returns main content uiKey
   */
  getUiKey() {
    if (this.getManager() !== null) {
      return this.getManager().resolveUiKey();
    }
    return null;
  }

  /**
   * Shows modal detail with given entity
   */
  showDetail(entity, cb) {
    this.setState({
      detail: {
        show: true,
        entity
      }
    }, () => {
      this.refs.form.setData(entity);
      cb();
    });
  }

  /**
   * Close modal detail
   */
  closeDetail() {
    this.setState({
      detail: {
        show: false,
        entity: {}
      }
    });
  }

  /**
   * Saves given entity
   */
  save(entity, event) {
    if (event) {
      event.preventDefault();
    }
    if (!this.refs.form.isFormValid()) {
      return;
    }
    //
    if (entity.id === undefined) {
      this.context.store.dispatch(this.getManager().createEntity(entity, `${this.getUiKey()}-detail`, (createdEntity, error) => {
        this.afterSave(createdEntity, error);
        if (!error && this.refs.table) {
          this.refs.table.getWrappedInstance().reload();
        }
      }));
    } else if (this.getManager().supportsPatch()) {
      this.context.store.dispatch(this.getManager().patchEntity(entity, `${this.getUiKey()}-detail`, this.afterSave.bind(this)));
    } else {
      this.context.store.dispatch(this.getManager().updateEntity(entity, `${this.getUiKey()}-detail`, this.afterSave.bind(this)));
    }
  }

  /**
   * Callback after save
   */
  afterSave(entity, error) {
    if (error) {
      this.addError(error);
      this.refs.form.processEnded();
      if (error.statusCode !== 202) {
        return;
      }
    }
    this.closeDetail();
  }

  /**
   * Bulk delete operation
   */
  onDelete(bulkActionValue, selectedRows) {
    const selectedEntities = this.getManager().getEntitiesByIds(this.context.store.getState(), selectedRows);
    //
    this.refs['confirm-' + bulkActionValue].show(
      this.i18n(`action.${bulkActionValue}.message`, { count: selectedEntities.length, record: this.getManager().getNiceLabel(selectedEntities[0]), records: this.getManager().getNiceLabels(selectedEntities).join(', ') }),
      this.i18n(`action.${bulkActionValue}.header`, { count: selectedEntities.length, records: this.getManager().getNiceLabels(selectedEntities).join(', ') })
    ).then(() => {
      this.context.store.dispatch(this.getManager().deleteEntities(selectedEntities, this.getUiKey(), (entity, error) => {
        if (entity && error) {
          if (error.statusCode !== 202) {
            this.addErrorMessage({ title: this.i18n(`action.delete.error`, { record: this.getManager().getNiceLabel(entity) }) }, error);
          } else {
            this.addError(error);
          }
        } else {
          this.refs.table.getWrappedInstance().reload();
        }
      }));
    }, () => {
      // nothing
    });
  }

  /**
   * Use filter form
   */
  useFilter(event) {
    if (event) {
      event.preventDefault();
    }
    this.refs.table.getWrappedInstance().useFilterForm(this.refs.filterForm);
  }

  /**
   * Cancel filter form
   */
  cancelFilter(event) {
    if (event) {
      event.preventDefault();
    }
    this.refs.table.getWrappedInstance().cancelFilter(this.refs.filterForm);
  }
}

AbstractTableContent.propTypes = {
  ...Basic.AbstractContent.propTypes
};

AbstractTableContent.defaultProps = {
  ...Basic.AbstractContent.defaultProps
};

AbstractTableContent.contextTypes = {
  ...Basic.AbstractContent.contextTypes
};
