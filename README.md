# play-silhouette-simple-slick
Simple Play Application Using Silhouette Library


## Technologies :
- Play! 2.6
- Sbt 0.13
- Scala 2.12
- Scala guice runtime DI
- Silhouette authentication librarie
- Slick database librarie
- MySql 5.6

## Installation :
1. Download the project as Zip file
2. Extract the Zip to a folder `my-project` for example
3. `cd my-project`
4. `sbt` to install dependencies

## Configuration :
In this section we will set application configurations in the file `conf/application.conf` :  
You should update :
- dbname
- dbuser
- dbpass
by your correcte DB informations

```HOCON
slick.dbs.default.profile = "slick.jdbc.MySQLProfile$"
slick.dbs.default.db.driver = "com.mysql.cj.jdbc.Driver"
slick.dbs.default.db.url = "jdbc:mysql://localhost:3306/dbname?serverTimezone=UTC&useSSL=false"
slick.dbs.default.db.user = dbuser
slick.dbs.default.db.password = dbpass
```

NB : I added `serverTimezone=UTC&useSSL=false` to avoid timezone and ssl mysql Errors when running the application  

## Run :
Go to the sbt console opened in the Installation step and do : `run`. This will run the play application so you can play with using the URL : `http://localhost:9000`


## Troubles :
If the signin does'nt work, you should may be :
- Disable the `secureCookie` config
- Set a duration for the  `maxAge` of the cookie. default in this repository is `None` which mean a transient cookie. 

I use this config in local and works well `app/modules/SilhouetteModule.scala` file :
```scala
val config = CookieAuthenticatorSettings(
      cookieName = "id",
      cookiePath = "/",
      cookieDomain = None,
      secureCookie = false,
      httpOnlyCookie = true,
      useFingerprinting = true,
      cookieMaxAge = Some(10.minutes),
      authenticatorIdleTimeout = None,
      authenticatorExpiry = 12.hours
    )

```

