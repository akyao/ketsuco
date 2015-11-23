package controllers

import java.security.MessageDigest
import javax.inject.Inject


import form.HbfForm
import models.{Cron, CronLine}
import play.api.data.Forms._
import play.api.data._
import play.api.i18n._
import play.api.mvc._
import scalikejdbc._


class HbfC @Inject()(val messagesApi: MessagesApi) extends Controller with I18nSupport {

  def index = Action {
    Ok(views.html.hbf.index())
  }

  def show(url: String) = Action {
    Ok(views.html.hbf.show())
  }

  def create() = Action {
    Ok(views.html.hbf.edit(form))
  }

  def save = Action { implicit request =>
    Redirect(routes.HbfC.show("fff"))
  }

  val form = Form(
    mapping(
      "url" -> nonEmptyText
        .verifying("16384文字以内で入力してください。", {
        _.length <= 16384
      })
    )(HbfForm.apply)(HbfForm.unapply)
  )
}
