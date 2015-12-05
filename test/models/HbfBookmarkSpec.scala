package models

import scalikejdbc.specs2.mutable.AutoRollback
import org.specs2.mutable._
import scalikejdbc._
import org.joda.time.{DateTime}


class HbfBookmarkSpec extends Specification {

  "HbfBookmark" should {

    val hb = HbfBookmark.syntax("hb")

    "find by primary keys" in new AutoRollback {
      val maybeFound = HbfBookmark.find(1L)
      maybeFound.isDefined should beTrue
    }
    "find by where clauses" in new AutoRollback {
      val maybeFound = HbfBookmark.findBy(sqls.eq(hb.id, 1L))
      maybeFound.isDefined should beTrue
    }
    "find all records" in new AutoRollback {
      val allResults = HbfBookmark.findAll()
      allResults.size should be_>(0)
    }
    "count all records" in new AutoRollback {
      val count = HbfBookmark.countAll()
      count should be_>(0L)
    }
    "find all by where clauses" in new AutoRollback {
      val results = HbfBookmark.findAllBy(sqls.eq(hb.id, 1L))
      results.size should be_>(0)
    }
    "count by where clauses" in new AutoRollback {
      val count = HbfBookmark.countBy(sqls.eq(hb.id, 1L))
      count should be_>(0L)
    }
    "create new record" in new AutoRollback {
      val created = HbfBookmark.create(hbfSitePageId = 1L, hbfUserId = 1L)
      created should not beNull
    }
    "save a record" in new AutoRollback {
      val entity = HbfBookmark.findAll().head
      // TODO modify something
      val modified = entity
      val updated = HbfBookmark.save(modified)
      updated should not equalTo(entity)
    }
    "destroy a record" in new AutoRollback {
      val entity = HbfBookmark.findAll().head
      HbfBookmark.destroy(entity)
      val shouldBeNone = HbfBookmark.find(1L)
      shouldBeNone.isDefined should beFalse
    }
    "perform batch insert" in new AutoRollback {
      val entities = HbfBookmark.findAll()
      entities.foreach(e => HbfBookmark.destroy(e))
      val batchInserted = HbfBookmark.batchInsert(entities)
      batchInserted.size should be_>(0)
    }
  }

}
