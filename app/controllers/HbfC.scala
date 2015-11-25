package controllers

import java.util.Formatter.DateTime

import org.joda.time.DateTime

import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.Implicits.global

import java.security.MessageDigest
import javax.inject.Inject


import form.HbfForm
import models._
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
    case class RssFeed(link:String, rssLink:String, title:String, entryList:Seq[String])
    // RSSのURLを元にRSSフィード情報を取得します
    def loadRss(rssUrl:String) :RssFeed = {
      val future = ws.url(rssUrl).get()
      val rssFeed = future.map { res =>
        val link:String = (res.xml \ "link" \ "@href").head.toString()
        val title:String = (res.xml \ "title").head.text
        val entryList:Seq[String] = res.xml \ "entry" \ "link" map { feed =>
          feed.attribute("href").get.toString()
        }
        RssFeed(link, rssUrl, title, entryList)
      }
      return Await.result(rssFeed, Duration.Inf)
    }

    // 記事の一覧を取得し、URLとそのブックマーク件数のリストを取得します
    def fetchBookmarkCountList(urlList:Seq[String]) : Map[String, Int] = {
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

    def fetchBookmarkInfo(url:String) : Seq[String]= {
      // http://developer.hatena.ne.jp/ja/documents/bookmark/apis/getinfo
      val apiUrl = "http://b.hatena.ne.jp/entry/jsonlite/?url=" + url
      val future = ws.url(apiUrl).get()
      val unko = future.map { res =>
        val chinko = res.json \ "bookmarks" \\ "user"
        chinko.map(jsv => jsv.toString().replaceAll("\"", ""))
      }

      return Await.result(unko, Duration.Inf)
    }

    val now = Some(DateTime.now())

    // TODO siteUrl -> baseUrl
    // TODO baseUrl -> rssUrl

    val rssUrl = "http://jkondo.hatenablog.com/feed"

    // rssUrl -> urlList
    val rssFeed = loadRss(rssUrl)

    // urlList ->
    val bookMarkList = fetchBookmarkCountList(rssFeed.entryList)

    // サイト情報の保存
    // Optionでくるんで渡さないといけないとか馬鹿らしいな
    val site = HbfSite.create(rssFeed.link,
      Some(rssFeed.rssLink),
      Some(rssFeed.title),
      now,
      now)

    // TODO サイトやページ、ユーザーがすでにある場合

    for (tup <- bookMarkList) {
      // TODO     if (tup._2 > 0) {
      if (tup._2 == 25) { // TODO test
        // TODO 記事公開日を保存したい
        val sitePage = HbfSitePage.create(
          site.id,
          tup._1,
          createdAt = now,
          updatedAt = now)

        // TODO APIコール回数、迷惑かけない頻度にすること
        val bookmarkInfo = fetchBookmarkInfo(tup._1)
        // TODO ユーザーはまとめて検索しような？
        for (userName <- bookmarkInfo) {
          val user:HbfUser = HbfUser.findBy(sqls.eq(HbfUser.syntax("hu").userName, userName))
            .getOrElse(HbfUser.create(Some(userName), now, now))

          // TODO bookmarkは全消ししてぶっこむ
          // TODO bookmark情報を保存する
          HbfBookmark.create(sitePage.id, user.id, now, now)
        }

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
