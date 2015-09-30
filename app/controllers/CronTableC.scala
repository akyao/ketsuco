package controllers

import models.Cron
import org.joda.time.{DateTime, LocalDate}
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import javax.inject.Inject
import play.api.i18n._

import java.security.MessageDigest

import form.CronForm

class CronTableC @Inject()(val messagesApi: MessagesApi) extends Controller with I18nSupport{

  def index = Action {
    Ok(views.html.cron_table.index())
  }

  def create = Action {
    Ok(views.html.cron_table.edit(form))
  }

  val form = Form(
    mapping(
      "cronText" -> text
    )(CronForm.apply)(CronForm.unapply)
  )

  def save = Action {implicit request =>

    val cronForm = form.bindFromRequest.get
    val cronText = cronForm.cronText
    val cronTextLines = cronText.split("\n")

// TODO validation
//    if len(cron_text) > 16384:
//      raise Exception("too big text")
//
    if (cronTextLines.length > 1000){
      //      raise Exception("too big text")
    }

    val body = Some(cronText)
    val now = Some(DateTime.now())
    val hashBytes = MessageDigest.getInstance("SHA-256").digest(now.toString.getBytes)
    val hash = Some(hashBytes.map("%02x".format(_)).mkString)

    val cron = Cron.create(body, hash, now, now)
//    cron.hash = hashlib.sha256(str(datetime.now())).hexdigest()

    Ok(views.html.cron_table.show())
  }

  def show(cronHash: String) = Action {
    Ok(views.html.cron_table.show())
  }
}
