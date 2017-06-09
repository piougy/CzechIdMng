import * as Basic from '../../basic';
import * as Utils from '../../../utils';

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

  componentDidMount() {
    super.componentDidMount();
    // loads filter from redux state
    this.loadFilter();
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
    if (Utils.Entity.isNew(entity)) {
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
   * Init default filter values
   */
  getDefaultSearchParameters() {
    return null;
  }

  /**
   * Returns filter from redux state or default
   *
   * @return {SearchParameters}
   */
  getSearchParameters() {
    return this.props._searchParameters || this.getDefaultSearchParameters();
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

  /**
   * Loads filter from redux state or default
   */
  loadFilter() {
    if (!this.refs.filterForm) {
      return;
    }
    //  filters from redux
    const _searchParameters = this.getSearchParameters();
    if (_searchParameters) {
      const filterData = {};
      _searchParameters.getFilters().forEach((v, k) => {
        filterData[k] = v;
      });
      this.refs.filterForm.setData(filterData);
    }
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
