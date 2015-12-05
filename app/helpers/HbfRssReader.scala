package helpers

import exceptions.ValidatorException
import play.api.libs.ws.WSClient

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by akyao on 2015/12/02.
 */
class HbfRssReader {

}

class HbfRss2Reader extends HbfRssReader{
  def loadRss(ws: WSClient, rssUrl:String) :RssFeed = {
    val future = ws.url(rssUrl).get()
    val rssFeed = future.map { res =>
      val link: String = (res.xml \ "channel" \ "link").head.text
      val title: String = (res.xml \ "channel" \ "title").head.text
      val entryList: Seq[String] = res.xml \ "channel" \ "item" \ "link" map { feed =>
        feed.head.text
      }
      RssFeed(link, rssUrl, title, entryList)
    }
    return Await.result(rssFeed, Duration.Inf)
  }
}

class HbfAtomReader extends HbfRssReader{
  def loadRss(ws: WSClient, rssUrl:String) :RssFeed = {
    val future = ws.url(rssUrl).get()
    val rssFeed = future.map { res =>
      val link:String = (res.xml \ "link").map(_.attribute("href").get.toString()).head
      //        val link:String = (res.xml \ "link" \ "@href").head.toString() エラーになる場合があった
      val title:String = (res.xml \ "title").head.text
      val entryList:Seq[String] = res.xml \ "entry" \ "link" map { feed =>
        feed.attribute("href").get.toString()
      }
      RssFeed(link, rssUrl, title, entryList)
    }
    return Await.result(rssFeed, Duration.Inf)
  }
}

object HbfRssReader {
  def loadRss(ws: WSClient, rssUrl:String) :RssFeed = {
    println(rssUrl)
    val future = ws.url(rssUrl).get()
    val rssFeed = future.map { res =>
      // http://argius.hatenablog.jp/entry/20130830/1377867921
      res.xml.label.toLowerCase match {
        case "rss" =>
          (res.xml \ "@version").text match {
            case "2.0" => new HbfRss2Reader().loadRss(ws, rssUrl)
            case _ => throw new ValidatorException("RSSが読み込めません")
          }
        case "rdf" => throw new ValidatorException("RSS1は未対応です")
        case "feed" => new HbfAtomReader().loadRss(ws, rssUrl)
        case _ => throw new ValidatorException("RSSが読み込めません")
      }
    }

    // RSSの仕様を判断
    return Await.result(rssFeed, Duration.Inf)
  }
}

case class RssFeed(link:String, rssLink:String, title:String, entryList:Seq[String])