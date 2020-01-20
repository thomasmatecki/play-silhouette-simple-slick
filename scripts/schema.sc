
import models._
import slick.jdbc
import slick.jdbc.MySQLProfile.api._
import slick.lifted.TableQuery

val schema: jdbc.MySQLProfile.DDL = (TableQuery[Users].schema ++ TableQuery[Profiles].schema ++ TableQuery[Passwords].schema)

schema.createStatements.foreach(println(_))
