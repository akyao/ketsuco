package models

import scalikejdbc.specs2.mutable.AutoRollback
import org.specs2.mutable._
import scalikejdbc._
import org.joda.time.{DateTime}


class HbfSiteSpec extends Specification {

  "HbfSite" should {

    val hs = HbfSite.syntax("hs")

    "find by primary keys" in new AutoRollback {
      val maybeFound = HbfSite.find(1L)
      maybeFound.isDefined should beTrue
    }
    "find by where clauses" in new AutoRollback {
      val maybeFound = HbfSite.findBy(sqls.eq(hs.id, 1L))
      maybeFound.isDefined should beTrue
    }
    "find all records" in new AutoRollback {
      val allResults = HbfSite.findAll()
      allResults.size should be_>(0)
    }
    "count all records" in new AutoRollback {
      val count = HbfSite.countAll()
      count should be_>(0L)
    }
    "find all by where clauses" in new AutoRollback {
      val results = HbfSite.findAllBy(sqls.eq(hs.id, 1L))
      results.size should be_>(0)
    }
    "count by where clauses" in new AutoRollback {
      val count = HbfSite.countBy(sqls.eq(hs.id, 1L))
      count should be_>(0L)
    }
    "create new record" in new AutoRollback {
      val created = HbfSite.create(url = 1L)
      created should not beNull
    }
    "save a record" in new AutoRollback {
      val entity = HbfSite.findAll().head
      // TODO modify something
      val modified = entity
      val updated = HbfSite.save(modified)
      updated should not equalTo(entity)
    }
    "destroy a record" in new AutoRollback {
      val entity = HbfSite.findAll().head
      HbfSite.destroy(entity)
      val shouldBeNone = HbfSite.find(1L)
      shouldBeNone.isDefined should beFalse
    }
    "perform batch insert" in new AutoRollback {
      val entities = HbfSite.findAll()
      entities.foreach(e => HbfSite.destroy(e))
      val batchInserted = HbfSite.batchInsert(entities)
      batchInserted.size should be_>(0)
    }
  }

}
