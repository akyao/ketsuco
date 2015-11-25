package controllers

import java.util.Formatter.DateTime

import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.Implicits.global

import java.security.MessageDigest
import javax.inject.Inject


import form.HbfForm
import models.{Cron, CronLine}
import play.api.data.Forms._
import play.api.data._
import play.api.i18n._
import play.api.libs.ws.{WSResponse, WSClient, WSRequest}
import play.api.mvc._
import scalikejdbc._

import scala.concurrent.duration.Duration
import scala.xml.{XML, Elem}
import scala.concurrent.{Await, Future}


// @Inject ()の意味
class HbfC @Inject()(val messagesApi: MessagesApi) (ws: WSClient) extends Controller with I18nSupport {

  def index = Action {
    Ok(views.html.hbf.index())
  }

  def show(url: String) = Action {
    Ok(views.html.hbf.show())
  }

  def create() = Action { request =>
    Ok(views.html.hbf.edit(form))
  }

  def save = Action { implicit request =>

    // RSSを読み込み、記事のURL一覧を返す
    def rssUrl2UrlList(rssUrl:String) :Seq[String] = {
      // TODO 変数名がきたねえよ
      val future = ws.url(rssUrl).get()
      val unko = future.map { res =>
        val gero:Seq[String] = res.xml \ "entry" \ "link" map { feed =>
          feed.attribute("href").get.toString()
        }
        gero
      }
      return Await.result(unko, Duration.Inf)
    }

    // 記事の一覧を取得し、URLとそのブックマーク件数のリストを返す
    def urlList2BookmarkCountList(urlList:Seq[String]) : Map[String, Int] = {
      // http://developer.hatena.ne.jp/ja/documents/bookmark/apis/getcount
      // TODO URLエスケープ

      val paramList = urlList.map(url => "url=" + url)
      val apiQueryString = paramList.mkString("&")
      val apiUrl = "http://api.b.st-hatena.com/entry.counts?"

      val future = ws.url(apiUrl + apiQueryString).get()
      val unko = future.map { res =>
        res.json.as[Map[String, Int]]
      }

      return Await.result(unko, Duration.Inf)
    }

    def url2BookmarkInfo(url:String) : Seq[String]= {
      // http://developer.hatena.ne.jp/ja/documents/bookmark/apis/getinfo
      val apiUrl = "http://b.hatena.ne.jp/entry/jsonlite/?url=" + url
      val future = ws.url(apiUrl).get()
      val unko = future.map { res =>
        val chinko = res.json \ "bookmarks" \\ "user"
        chinko.map(jsv => jsv.toString())
      }

      return Await.result(unko, Duration.Inf)
    }

    // TODO siteUrl -> baseUrl

    // TODO baseUrl -> rssUrl
    val rssUrl = "http://jkondo.hatenablog.com/feed"

    // rssUrl -> urlList
    val urlList = rssUrl2UrlList(rssUrl)

    // urlList ->
    val bookMarkList = urlList2BookmarkCountList(urlList)

    for (tup <- bookMarkList) {
// TODO     if (tup._2 > 0) {
      if (tup._2 == 25) { // TODO test
        val bookmarkInfo = url2BookmarkInfo(tup._1)
        // TODO bookmark情報を保存する
        println(bookmarkInfo)

        // TODO APIコール回数、迷惑かけない頻度にすること
      }
    }

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
