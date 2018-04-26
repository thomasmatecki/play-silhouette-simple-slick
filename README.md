# play-silhouette-simple-slick
Simple Play! application Using [Silhouette](https://www.silhouette.rocks/) and [Slick](http://slick.lightbend.com/). This app is meant to demo a simple form based login, that persists user identity to a relational database, with fewest possible dependencies and components. It is configured to use bcrypt, so it is (ostensibly) secure.

## Configuration :
The following environmental variables should be defined:
- dbname
- dbuser
- dbpass

... as defined in conf/application.conf:

```
slick.dbs.default.profile = ${JDBC_PROFILE}
slick.dbs.default.db.driver = ${DB_DRIVER}
slick.dbs.default.db.url = ${DB_URL}
slick.dbs.default.db.user = ${DB_USER}
slick.dbs.default.db.password = ${DB_PASSWORD}
```
