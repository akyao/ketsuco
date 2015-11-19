package controllers

import javax.inject.Inject

import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._

/**
 * Created by akyao on 2015/11/19.
 */
class MiniC @Inject()(val messagesApi: MessagesApi) extends Controller with I18nSupport {

  def imageLoad = Action {
    Ok(views.html.mini.image_load())
  }
}
