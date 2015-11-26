package dtos

/**
 * Created by akyao on 2015/11/27.
 */
case class BookmarkInfo(userId:String, userName:String, bookmarkCount:Int, sitePageIds:String) {
  def getSitePageIdList(): Seq[Int] = {
    sitePageIds.split(",").map(pageId => pageId.toInt)
  }
}
