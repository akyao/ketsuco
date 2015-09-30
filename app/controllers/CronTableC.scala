package controllers

import java.security.MessageDigest
import javax.inject.Inject

import form.CronForm
import models.{Cron, CronLine}
import org.joda.time.DateTime
import play.api.data.Forms._
import play.api.data._
import play.api.i18n._
import play.api.mvc._

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

    val cronBody = Some(cronText)
    val now = Some(DateTime.now())
    val hashBytes = MessageDigest.getInstance("SHA-256").digest(now.toString.getBytes)
    val hash = Some(hashBytes.map("%02x".format(_)).mkString)

    val cron = Cron.create(cronBody, hash, now, now)

    for ((lineRaw, i) <- cronTextLines.zipWithIndex){
      // 連続した空白はタブを一つのスペースに置換
      val lineText = lineRaw.trim.replaceAll("\\s{1,}", " ")
      if (!lineText.isEmpty) {
        println(lineText)
        val isComment = lineText.startsWith("#")
        val isSetting = lineText.contains("=")
        val isCronLine = !isComment && !isSetting
        // TODO コメントも直後の行を説明するものとして保存しておきたい。(DSL)
        if (isCronLine) {
          val elements = lineText.split(" ")
          CronLine.create(
            cronId = cron.id,
            line = Some(i),
            body = Some(lineText),
            command = Some(elements.slice(5, 1000).mkString(" ")),
            month = Some(elements(3)),
            day = Some(elements(2)),
            week = Some(elements(4)),
            hour = Some(elements(1)),
            minute = Some(elements(0)),
            createdAt = now,
            updatedAt = now
          )
        }
      }
    }

    Ok(views.html.cron_table.show())
  }

  def show(cronHash: String) = Action {
    Ok(views.html.cron_table.show())
  }
}
