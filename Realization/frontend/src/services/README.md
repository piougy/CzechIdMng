# Services

Services simply make calls to rest api - isomorphic-fetch is used.

Base services are:
* RestApiService - calls http methods to given endpoint and wraps authentication tokens (xsrf).
* AbstractService - basic CRUD method, whid are commons for all endpoints
* Other services define concrete rest endpoint and add custom endpoint methods
