package forms

import play.api.data.Form
import play.api.data.Forms._

object SignUpForm {

  val form = Form(
    mapping(
      "firstName" -> nonEmptyText,
      "lastName" -> nonEmptyText,
      "email" -> email,
      "password" -> nonEmptyText
    )(Data.apply)(Data.unapply)
  )

  case class Data(firstName: String,
                  lastName: String,
                  email: String,
                  password: String)

}

