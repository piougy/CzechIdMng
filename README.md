# CzechIdM 7

CzechIdM is an opensource identity management tool for user accounts that automates the operations associated with establishing and managing identities.

CzechIdM manages user accounts, roles, groups, organizational structures, and their relationships to individual applications and data. All performed operations are recorded, so you always have a complete overview of who has what access and why.

![IdM base structure](https://wiki.czechidm.com/_media/idm_schema.png)

## Main usecases
  - **Manage identities from one place**: Use our unique console to create, approve, sync and audit all of your user identities.
  - **Improve company security**: Always know who has access to your systems and why. Be able to block access to anyone. Enforce a secure password policy.
  - **Automate identity lifecycle**: Synchronize identities in your systems. Simplify HR processes, grant users access and provision them automatically.

## Important links
  - **CzechIDM documentation - admins and developers guide**: https://wiki.czechidm.com/start
  - Stable release: https://github.com/bcvsolutions/CzechIdMng/releases
  - Nightly build: [idm.war](http://download.czechidm.com/CzechIdM/nightly/current/idm.war)
  - Online demo: http://demo.czechidm.com/
  - BugTrack tool Redmine: https://redmine.czechidm.com/projects/czechidmng
  - Contact options, Google groups and more: https://wiki.czechidm.com/support

## Used technologies
The seventh version of [CzechIdM](http://www.czechidm.com/) will be based on technologies:
* Backend (java):
  * jdbc db (primary PostgreSQL)
  * Hibernate ORM
  * Spring (data, hateoas, security ...)
  * Activiti (workflow)

* Frontend (javascript)
  * React
  * Redux


## Instalation

* [Backend](./Realization/backend)
* [Frontend](./Realization/frontend)

## License

[MIT License](./LICENSE)

## Screenshots
### Login page
![Login page](https://wiki.czechidm.com/_media/login_page.png)
### User's dashboard
![User's dashboard](https://wiki.czechidm.com/_media/basic_info.png)
### Role detail
![Role detail](https://wiki.czechidm.com/_media/role.png)
### List of users
![List of users](https://wiki.czechidm.com/_media/users.png)
### Workflow history
![Workflow history](https://wiki.czechidm.com/_media/wf_history.png)
