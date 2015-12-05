package models

import scalikejdbc.specs2.mutable.AutoRollback
import org.specs2.mutable._
import scalikejdbc._
import org.joda.time.{DateTime}


class HbfUserSpec extends Specification {

  "HbfUser" should {

    val hu = HbfUser.syntax("hu")

    "find by primary keys" in new AutoRollback {
      val maybeFound = HbfUser.find(1L)
      maybeFound.isDefined should beTrue
    }
    "find by where clauses" in new AutoRollback {
      val maybeFound = HbfUser.findBy(sqls.eq(hu.id, 1L))
      maybeFound.isDefined should beTrue
    }
    "find all records" in new AutoRollback {
      val allResults = HbfUser.findAll()
      allResults.size should be_>(0)
    }
    "count all records" in new AutoRollback {
      val count = HbfUser.countAll()
      count should be_>(0L)
    }
    "find all by where clauses" in new AutoRollback {
      val results = HbfUser.findAllBy(sqls.eq(hu.id, 1L))
      results.size should be_>(0)
    }
    "count by where clauses" in new AutoRollback {
      val count = HbfUser.countBy(sqls.eq(hu.id, 1L))
      count should be_>(0L)
    }
    "create new record" in new AutoRollback {
      val created = HbfUser.create()
      created should not beNull
    }
    "save a record" in new AutoRollback {
      val entity = HbfUser.findAll().head
      // TODO modify something
      val modified = entity
      val updated = HbfUser.save(modified)
      updated should not equalTo(entity)
    }
    "destroy a record" in new AutoRollback {
      val entity = HbfUser.findAll().head
      HbfUser.destroy(entity)
      val shouldBeNone = HbfUser.find(1L)
      shouldBeNone.isDefined should beFalse
    }
    "perform batch insert" in new AutoRollback {
      val entities = HbfUser.findAll()
      entities.foreach(e => HbfUser.destroy(e))
      val batchInserted = HbfUser.batchInsert(entities)
      batchInserted.size should be_>(0)
    }
  }

}
