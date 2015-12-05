package helpers

import javax.xml.XMLConstants
import javax.xml.parsers.SAXParserFactory

import exceptions.ValidatorException
import org.apache.xerces.impl.Constants
import play.api._
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
  def XML = scala.xml.XML.withSAXParser(xercesSaxParserFactory.newSAXParser())
  val xercesSaxParserFactory =
    SAXParserFactory.newInstance("org.apache.xerces.jaxp.SAXParserFactoryImpl", Play.getClass.getClassLoader)
  xercesSaxParserFactory.setFeature(Constants.SAX_FEATURE_PREFIX + Constants.EXTERNAL_GENERAL_ENTITIES_FEATURE, false)
  xercesSaxParserFactory.setFeature(Constants.SAX_FEATURE_PREFIX + Constants.EXTERNAL_PARAMETER_ENTITIES_FEATURE, false)
  xercesSaxParserFactory.setFeature(Constants.XERCES_FEATURE_PREFIX + Constants.DISALLOW_DOCTYPE_DECL_FEATURE, true)
  xercesSaxParserFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true)

  // TODO An invalid XML character (Unicode: 0x8) was found in the element content of the document
  // TODO https://moneyforward.com/engineers_blog/
  def loadRss(ws: WSClient, rssUrl:String) :RssFeed = {

    val future = ws.url(rssUrl).get()
    val rssFeed = future.map { res =>
      // http://argius.hatenablog.jp/entry/20130830/1377867921
      val xml = XML.loadString(res.body.trim)
      xml.label.toLowerCase match {
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