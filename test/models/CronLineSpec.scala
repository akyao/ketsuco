package models

import scalikejdbc.specs2.mutable.AutoRollback
import org.specs2.mutable._
import scalikejdbc._
import org.joda.time.{LocalDate}


class CronLineSpec extends Specification {

  "CronLine" should {

    val cl = CronLine.syntax("cl")

    "find by primary keys" in new AutoRollback {
      val maybeFound = CronLine.find(1L)
      maybeFound.isDefined should beTrue
    }
    "find by where clauses" in new AutoRollback {
      val maybeFound = CronLine.findBy(sqls.eq(cl.id, 1L))
      maybeFound.isDefined should beTrue
    }
    "find all records" in new AutoRollback {
      val allResults = CronLine.findAll()
      allResults.size should be_>(0)
    }
    "count all records" in new AutoRollback {
      val count = CronLine.countAll()
      count should be_>(0L)
    }
    "find all by where clauses" in new AutoRollback {
      val results = CronLine.findAllBy(sqls.eq(cl.id, 1L))
      results.size should be_>(0)
    }
    "count by where clauses" in new AutoRollback {
      val count = CronLine.countBy(sqls.eq(cl.id, 1L))
      count should be_>(0L)
    }
    "create new record" in new AutoRollback {
      val created = CronLine.create(cronId = 1L)
      created should not beNull
    }
    "save a record" in new AutoRollback {
      val entity = CronLine.findAll().head
      // TODO modify something
      val modified = entity
      val updated = CronLine.save(modified)
      updated should not equalTo(entity)
    }
    "destroy a record" in new AutoRollback {
      val entity = CronLine.findAll().head
      CronLine.destroy(entity)
      val shouldBeNone = CronLine.find(1L)
      shouldBeNone.isDefined should beFalse
    }
    "perform batch insert" in new AutoRollback {
      val entities = CronLine.findAll()
      entities.foreach(e => CronLine.destroy(e))
      val batchInserted = CronLine.batchInsert(entities)
      batchInserted.size should be_>(0)
    }
  }

}
