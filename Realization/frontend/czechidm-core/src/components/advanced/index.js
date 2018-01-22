import Table from './Table/Table';
import Column from './Table/Column';
import ColumnLink from './Table/ColumnLink';
import RefreshButton from './Table/RefreshButton';
import IdentityInfo from './IdentityInfo/IdentityInfo';
import Navigation from './Navigation/Navigation';
import TabPanel from './TabPanel/TabPanel';
import Filter from './Filter/Filter';
import DateValue from './DateValue/DateValue';
import Tree from './Tree/Tree';
import DetailButton from './Table/DetailButton';
import ModalProgressBar from './ModalProgressBar/ModalProgressBar';
import EavForm from './Form/EavForm';
import EavContent from './Form/EavContent';
import AbstractFormAttributeRenderer from './Form/AbstractFormAttributeRenderer';
import PasswordField from './PasswordField/PasswordField';
import ProgressBar from './ProgressBar/ProgressBar';
import RichTextArea from './RichTextArea/RichTextArea';
import AbstractTableContent from './Content/AbstractTableContent';
import EntityInfo from './EntityInfo/EntityInfo';
import AbstractEntityInfo from './EntityInfo/AbstractEntityInfo';
import UuidInfo from './UuidInfo/UuidInfo';
import RoleInfo from './RoleInfo/RoleInfo';
import RoleCatalogueInfo from './RoleCatalogueInfo/RoleCatalogueInfo';
import IdentityContractInfo from './IdentityContractInfo/IdentityContractInfo';
import WorkflowProcessInfo from './WorkflowProcessInfo/WorkflowProcessInfo';
import NotificationTemplateInfo from './NotificationTemplateInfo/NotificationTemplateInfo';
import ScriptArea from './ScriptArea/ScriptArea';
import RoleSelect from './RoleSelect/RoleSelect';
import Recaptcha from './Recaptcha/Recaptcha';
import IdentitiesInfo from './IdentitiesInfo/IdentitiesInfo';
import SchedulerTaskInfo from './SchedulerTaskInfo/SchedulerTaskInfo';
import EntitySelectBox from './EntitySelectBox/EntitySelectBox';
import Dropzone from './Dropzone/Dropzone';
import PasswordChangeComponent from './PasswordChangeComponent/PasswordChangeComponent';
import ValidationMessage from './ValidationMessage/ValidationMessage';

const Components = {
  Table,
  Column,
  ColumnLink,
  RefreshButton,
  IdentityInfo,
  Navigation,
  TabPanel,
  Filter,
  _ToogleButton: Filter.ToogleButton,
  _FilterButtons: Filter.FilterButtons,
  DateValue,
  ProgressBar,
  ModalProgressBar,
  Tree,
  DetailButton,
  EavForm,
  EavContent,
  AbstractFormAttributeRenderer,
  PasswordField,
  RichTextArea,
  AbstractTableContent,
  EntityInfo,
  AbstractEntityInfo,
  UuidInfo,
  RoleInfo,
  RoleCatalogueInfo,
  IdentityContractInfo,
  WorkflowProcessInfo,
  NotificationTemplateInfo,
  ScriptArea,
  RoleSelect,
  Recaptcha,
  IdentitiesInfo,
  SchedulerTaskInfo,
  EntitySelectBox,
  Dropzone,
  PasswordChangeComponent,
  ValidationMessage
};

Components.version = '0.0.1';
module.exports = Components;
