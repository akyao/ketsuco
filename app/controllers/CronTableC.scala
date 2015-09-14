package controllers

import play.api.mvc._

class CronTableC extends Controller {

  def index = Action {
    Ok(views.html.cron_table.index())
  }

  def create = Action {
    Ok(views.html.cron_table.edit())
  }

  def save = Action {
    // TODO redirect
    Ok(views.html.cron_table.show())
  }

  def show(cronHash: String) = Action {
    Ok(views.html.cron_table.show())
  }
}
