package controllers

import java.io.StringReader
import javax.inject.Inject

import dtos.BookmarkInfo
import form.HbfForm
import helpers.{RssFeed, HbfRssReader}
import models._
import nu.validator.htmlparser.dom.HtmlDocumentBuilder

import org.joda.time.DateTime
import org.xml.sax.InputSource
import play.api.data.Forms._
import play.api.data._
import play.api.i18n._
import play.api.libs.ws.WSClient
import play.api.mvc._
import scalikejdbc._

import exceptions.ValidatorException

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration


// @Inject ()ってなんすか？
class HbfC @Inject()(val messagesApi: MessagesApi) (ws: WSClient) extends Controller with I18nSupport {

  def index = Action {
    // TODO そのうちトップページ作る
    //Ok(views.html.hbf.index())
    Redirect(routes.HbfC.create())
  }

  def show(siteId: Long) = Action { implicit request =>
    DB localTx { implicit session =>
      val site = HbfSite.find(siteId)
      // TODO 古い時は更新すること
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
          case e: Throwable
            => throw e
        }
      }
    )
  }

  def fetchRssUrl(url:String): Seq[String] = {
    // TODO 入力されたのがいきなりRSSフィードだったらどうするよ

    val rssUrl = ws.url(url).get().map { res =>
      val builder = new HtmlDocumentBuilder()
      val sreader = new StringReader(res.body)
      val dom = builder.parse(new InputSource(sreader))
      val elems = dom.getElementsByTagName("link")

      var rssUrls = ListBuffer[String]()
      for (i <- 0 to elems.getLength) {
        val elem = elems.item(i)
        if (elem != null) {
          val attMap = elem.getAttributes()
          val attType = attMap.getNamedItem("type")
          if (attType != null && attType.getNodeValue.matches("application\\/((atom|rss)\\+)?xml")) {
            val attHref = attMap.getNamedItem("href")
            if( attHref != null ) {
              rssUrls += attHref.getNodeValue
            }
          }
        }
      }
      rssUrls
    }
    return Await.result(rssUrl, Duration.Inf)
  }

  def doFuck(rssUrls:Seq[String]) : HbfSite = {

    /** RSSのURLを元にRSSフィード情報を取得します */
    def loadRss():RssFeed = {
      // TODO クソみたいなコード書きやがって
      rssUrls.foreach(u =>
        try {
          return HbfRssReader.loadRss(ws, u)
        } catch {
          case e: ValidatorException
            =>
        }
      )

      throw new ValidatorException("RSSが読み込めません")
    }

    /** 記事の一覧を取得し、URLとそのブックマーク件数のリストを取得します */
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

    /** ブックマーク情報を取得します */
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

    val rssFeed = loadRss()

    // 更新から時間が立っていない場合はそのままでいい
    var site = HbfSite.findBy(sqls.eq(HbfSite.syntax("hs").rssUrl, rssFeed.rssLink))
    if (site.isDefined && site.get.updatedAt.get.isAfter(now.get.minusDays(1))) {
      return site.get
    }

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
          updateUpdatedAt(HbfSitePage, now.get, sitePage.id)

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
            updateUpdatedAt(HbfUser, now.get, user.id)

            userMap.put(userName, user)

            // bookmark情報を保存する
            HbfBookmark.create(sitePage.id, user.id, now, now)
          }
        }
      }

      updateUpdatedAt(HbfSite, now.get, site.id)

      return site
    }
  }

  def updateUpdatedAt(entity:SQLSyntaxSupport[_], time:DateTime, id:Long)(implicit session:DBSession): Unit = {
    sql"update ${entity.table} set updated_at = now() where id = ${id}".update.apply()
  }

  val form = Form(
    mapping(
      "url" -> nonEmptyText
        .verifying("URLじゃないのでは？", {_.matches("https?://[\\w/:%#\\$&\\?\\(\\)~\\.=\\+\\-]+")})
        .verifying("URL長すぎます。。", {_.length <= 256})
    )(HbfForm.apply)(HbfForm.unapply)
  )
}
