package controllers

import javax.inject.Inject

import dtos.BookmarkInfo
import form.HbfForm
import models._
import org.joda.time.DateTime
import play.api.data.Forms._
import play.api.data._
import play.api.i18n._
import play.api.libs.ws.WSClient
import play.api.mvc._
import scalikejdbc._

import exceptions.ValidatorException

import scala.collection.mutable
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration


// @Inject ()ってなんすか？
class HbfC @Inject()(val messagesApi: MessagesApi) (ws: WSClient) extends Controller with I18nSupport {

  def index = Action {
    Ok(views.html.hbf.index())
  }

  def show(siteId: Long) = Action { implicit request =>
    DB localTx { implicit session =>
      val site = HbfSite.find(siteId)
      val sitePageList:List[HbfSitePage] = HbfSitePage.findAllBy(sqls.eq(HbfSitePage.syntax("hsp").hbfSiteId, siteId))
      val bookmarkList:List[BookmarkInfo] = findBookmarkInfoList(siteId)
      Ok(views.html.hbf.show(site.get, sitePageList, bookmarkList))
    }
  }

  def findBookmarkInfoList(siteId:Long): List[BookmarkInfo] = {
    // TODO txがネストしていてきもい
    DB localTx { implicit session =>
      sql"""
      select
        hb.hbf_user_id as userId,
        hu.user_name as userName,
        count(1) as bookmarkCount,
        group_concat(hb.hbf_site_page_id) as sitePageIds
      from hbf_site_page hsp
      inner join hbf_bookmark hb on hsp.id = hb.hbf_site_page_id
      inner join hbf_user hu on hb.hbf_user_id = hu.id
      where hsp.hbf_site_id = ${siteId}
      group by hb.hbf_user_id
      order by bookmarkCount desc
      limit 100
      """.map(rs => BookmarkInfo(rs.string("userId"), rs.string("userName"), rs.int("bookmarkCount"), rs.string("sitePageIds"))).list.apply()
    }
  }

  def create() = Action { implicit request =>
    Ok(views.html.hbf.edit(form))
  }

  def save = Action { implicit request =>
    form.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(views.html.hbf.edit(formWithErrors))
      }, formData => {
        try {
          val rssUrl = fetchRssUrl(formData.url)
          val site = doFuck(rssUrl)
          Redirect(routes.HbfC.show(site.id))
        } catch {
          // バリデーションエラーの対応
          case e: ValidatorException
            => BadRequest(views.html.hbf.edit(form.bindFromRequest.withError("url", e.getMessage)))
        }
      }
    )
  }

  def fetchRssUrl(url:String): String = {
    // TODO 入力されたのがいきなりRSSフィードだったらどうするよ
    try {
      val rssUrl = ws.url(url).get().map { res =>

        val elems = res.body.split("\n")
          .filter(_.matches(".+application/((atom|rss)\\+)?xml.+"))
          .map(fuck => scala.xml.XML.loadString(fuck.trim).attribute("href"))

        if (elems.length == 0) {
          throw new ValidatorException("RSSフィードが見つからないっす")
        }

        elems(0).get.toString()
      }

      return Await.result(rssUrl, Duration.Inf)
    } catch {
      case e: java.io.IOException
        => throw new ValidatorException("URL読み込みに失敗しました")
    }
  }

  def doFuck(rssUrl:String) : HbfSite = {
    case class RssFeed(link:String, rssLink:String, title:String, entryList:Seq[String])

    // RSSのURLを元にRSSフィード情報を取得します
    def loadRss() :RssFeed = {
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

    // TODO rssUrlで検索。更新から時間が立っていない場合はそれを表示。更新はしない

    // rssUrl -> urlList
    val rssFeed = loadRss()

    // urlList ->
    val bookMarkList = fetchBookmarkCountList(rssFeed.entryList)

    DB localTx { implicit session =>
      // サイト情報の保存
      // Optionでくるんで渡さないといけないとか馬鹿らしいな
      val site = HbfSite.findBy(sqls.eq(HbfSite.syntax("hs").url, rssFeed.link))
        .getOrElse(HbfSite.create(rssFeed.link, Some(rssFeed.rssLink), Some(rssFeed.title), now, now))

      val userMap = mutable.HashMap[String, HbfUser]()

      for (tup <- bookMarkList) {
        if (tup._2 > 0) {

          // TODO 記事公開日を保存したい
          val sitePage = HbfSitePage.findBy(sqls.eq(HbfSitePage.syntax("hsp").url, tup._1))
            .getOrElse(HbfSitePage.create(site.id, tup._1, createdAt = now, updatedAt = now))

          // bookmarkは全消ししてぶっこむ
          // TODO HbfBookmarkのメソッドに追加したい。HbfBookmarkファイルは編集したくない
          sql"DELETE FROM hbf_bookmark WHERE hbf_site_page_id = ${sitePage.id}".update().apply()

          // TODO APIコール回数、迷惑かけない頻度にすること
          val bookmarkInfo = fetchBookmarkInfo(tup._1)
          // TODO ユーザーはまとめて検索しような？
          for (userName <- bookmarkInfo) {
            // mapから検索して、なければDB検索、それもなければ生成
            val user: HbfUser = userMap.getOrElse(userName, HbfUser.findBy(sqls.eq(HbfUser.syntax("hu").userName, userName))
              .getOrElse(HbfUser.create(Some(userName), now, now)))

            userMap.put(userName, user)

            // bookmark情報を保存する
            HbfBookmark.create(sitePage.id, user.id, now, now)
          }
        }
      }

      return site
    }
  }

  val form = Form(
    mapping(
      "url" -> nonEmptyText
        .verifying("URLじゃなくね？", {_.matches("https?://[\\w/:%#\\$&\\?\\(\\)~\\.=\\+\\-]+")})
        .verifying("URLなが杉です", {_.length <= 256})
    )(HbfForm.apply)(HbfForm.unapply)
  )
}
