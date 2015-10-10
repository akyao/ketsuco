package controllers

import java.security.MessageDigest
import javax.inject.Inject

import form.CronForm
import helpers.CronTimeHelper
import models.{Cron, CronLine}
import org.joda.time.DateTime
import play.api.data.Forms._
import play.api.data._
import play.api.i18n._
import play.api.mvc._
import scalikejdbc._

class CronTableC @Inject()(val messagesApi: MessagesApi) extends Controller with I18nSupport {

  def index = Action {
    Ok(views.html.cron_table.index())
  }

  def show(cronHash: String) = Action {
    val cron = Cron.findBy(sqls.eq(Cron.syntax("c").hash, cronHash))
    if (cron.isEmpty) {
      BadRequest("ないよ")
    } else {
      val cronLines = CronLine.findAllBy(sqls.eq(CronLine.syntax("cl").cronId, cron.get.id))
      Ok(views.html.cron_table.show(cron.get, cronLines))
    }
  }

  def create = Action {
    Ok(views.html.cron_table.edit(form))
  }

  val form = Form(
    mapping(
      "cronText" -> nonEmptyText
        .verifying("16384文字以内で入力してください。", {
        _.length <= 16384
      })
        .verifying("1行あたり200文字まで入力してください", {
        _.split("\n").exists(n => n.length <= 200)
      })
    )(CronForm.apply)(CronForm.unapply)
  )

  def save = Action { implicit request =>

    form.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(views.html.cron_table.edit(formWithErrors))
      },
      formData => {
        val cronText = formData.cronText
        try {
          val cron = save2(cronText)
          val cronLines = CronLine.findAllBy(sqls.eq(CronLine.syntax("cl").cronId, cron.id))
          Ok(views.html.cron_table.show(cron, cronLines))
        } catch {
          // バリデーションエラーの対応
          case e: ValidatorException
          => BadRequest(views.html.cron_table.edit(form.bindFromRequest.withError("cronText", e.getMessage)))
        }
      }
    )
  }

  def save2(cronText: String): Cron = {

    val cronTextLines = cronText.split("\n")
    val cronBody = Some(cronText)
    val now = Some(DateTime.now())
    val hashBytes = MessageDigest.getInstance("SHA-256").digest(now.toString.getBytes)
    val hash = Some(hashBytes.map("%02x".format(_)).mkString)

    DB localTx { implicit session =>

      val cron = Cron.create(cronBody, hash, now, now)

      for ((lineRaw, i) <- cronTextLines.zipWithIndex) {
        // 連続した空白はタブを一つのスペースに置換
        val lineText = lineRaw.trim.replaceAll("\\s{1,}", " ")
        if (!lineText.isEmpty) {
          val isComment = lineText.startsWith("#")
          val isSetting = lineText.contains("=")
          val isCronLine = !isComment && !isSetting
          // TODO コメントも直後の行を説明するものとして保存しておきたい。(DSL)

          if (isCronLine) {
            val elements = lineText.split(" ")
            if (elements.length < 6) {
              throw new ValidatorException("%d行目(%s)がcronとして解釈不可能です.".format(i+1, lineText))
            }
            // 解釈不可能な入力ははじく
            val minute = elements(0)
            val hour = elements(1)
            if (CronTimeHelper.calcTimes(minute, 60)._2) {
              throw new ValidatorException("%d行目の(%s)が分の設定として解釈不可能です".format(i+1, minute))
            }
            if (CronTimeHelper.calcTimes(hour, 24)._2) {
              throw new ValidatorException("%d行目の(%s)が時間の設定として解釈不可能です".format(i+1, hour))
            }

            CronLine.create(
              cronId = cron.id,
              line = Some(i),
              body = Some(lineText),
              command = Some(elements.slice(5, 1000).mkString(" ")),
              month = Some(elements(3)),
              day = Some(elements(2)),
              week = Some(elements(4)),
              hour = Some(hour),
              minute = Some(minute),
              createdAt = now,
              updatedAt = now
            )
          }
        }
      }

      return cron
    }
  }

  class ValidatorException(msg: String) extends RuntimeException(msg)

}
