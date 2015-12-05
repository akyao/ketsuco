package models

import scalikejdbc.specs2.mutable.AutoRollback
import org.specs2.mutable._
import scalikejdbc._
import org.joda.time.{DateTime}


class HbfSitePageSpec extends Specification {

  "HbfSitePage" should {

    val hsp = HbfSitePage.syntax("hsp")

    "find by primary keys" in new AutoRollback {
      val maybeFound = HbfSitePage.find(1L)
      maybeFound.isDefined should beTrue
    }
    "find by where clauses" in new AutoRollback {
      val maybeFound = HbfSitePage.findBy(sqls.eq(hsp.id, 1L))
      maybeFound.isDefined should beTrue
    }
    "find all records" in new AutoRollback {
      val allResults = HbfSitePage.findAll()
      allResults.size should be_>(0)
    }
    "count all records" in new AutoRollback {
      val count = HbfSitePage.countAll()
      count should be_>(0L)
    }
    "find all by where clauses" in new AutoRollback {
      val results = HbfSitePage.findAllBy(sqls.eq(hsp.id, 1L))
      results.size should be_>(0)
    }
    "count by where clauses" in new AutoRollback {
      val count = HbfSitePage.countBy(sqls.eq(hsp.id, 1L))
      count should be_>(0L)
    }
    "create new record" in new AutoRollback {
      val created = HbfSitePage.create(hbfSiteId = 1L, url = 1L)
      created should not beNull
    }
    "save a record" in new AutoRollback {
      val entity = HbfSitePage.findAll().head
      // TODO modify something
      val modified = entity
      val updated = HbfSitePage.save(modified)
      updated should not equalTo(entity)
    }
    "destroy a record" in new AutoRollback {
      val entity = HbfSitePage.findAll().head
      HbfSitePage.destroy(entity)
      val shouldBeNone = HbfSitePage.find(1L)
      shouldBeNone.isDefined should beFalse
    }
    "perform batch insert" in new AutoRollback {
      val entities = HbfSitePage.findAll()
      entities.foreach(e => HbfSitePage.destroy(e))
      val batchInserted = HbfSitePage.batchInsert(entities)
      batchInserted.size should be_>(0)
    }
  }

}
