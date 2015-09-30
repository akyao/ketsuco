package controllers

import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import javax.inject.Inject
import play.api.i18n._

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
    // TODO form postにしようとすると bindFromRequestじゃなくなって辛い
    val cronText = form.bindFromRequest.get


//    if len(cron_text) > 16384:
//      raise Exception("too big text")
//
//    cron_text_lines = cron_text.splitlines()
//    if len(cron_text_lines) > 1000:
//      raise Exception("too big text")
//
//    cron = Cron(body = cron_text)
//    cron.hash = hashlib.sha256(str(datetime.now())).hexdigest()
//    cron.save()


    Ok(views.html.cron_table.show())
  }

  def show(cronHash: String) = Action {
    Ok(views.html.cron_table.show())
  }
}
