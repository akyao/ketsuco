package dtos

/**
 * Created by akyao on 2015/11/27.
 */
case class BookmarkInfo(userId:String, userName:String, bookmarkCount:Int, sitePageIds:String) {

  var sitePageIdList:Seq[Int] = null

  def getSitePageIdList(): Seq[Int] = {
    if (sitePageIdList == null ) {
      sitePageIdList = sitePageIds.split(",").map(pageId => pageId.toInt)
    }
    return sitePageIdList;
  }

  def getBookmarkCount(): Int = {
    return getSitePageIdList().size
  }
}
