

import CrtIdentityManager from './CrtIdentityManager';
import CrtCertificateTaskManager from './CrtCertificateTaskManager';
import CrtCertificateManager from './CrtCertificateManager';


const ManagerRoot = {
  CrtIdentityManager: CrtIdentityManager,
  CrtCertificateTaskManager: CrtCertificateTaskManager,
  CrtCertificateManager: CrtCertificateManager
};

ManagerRoot.version = '0.0.1';
module.exports = ManagerRoot;
