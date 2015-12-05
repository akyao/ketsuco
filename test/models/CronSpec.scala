package models

import scalikejdbc.specs2.mutable.AutoRollback
import org.specs2.mutable._
import scalikejdbc._
import org.joda.time.{LocalDate}


class CronSpec extends Specification {

  "Cron" should {

    val c = Cron.syntax("c")

    "find by primary keys" in new AutoRollback {
      val maybeFound = Cron.find(1L)
      maybeFound.isDefined should beTrue
    }
    "find by where clauses" in new AutoRollback {
      val maybeFound = Cron.findBy(sqls.eq(c.id, 1L))
      maybeFound.isDefined should beTrue
    }
    "find all records" in new AutoRollback {
      val allResults = Cron.findAll()
      allResults.size should be_>(0)
    }
    "count all records" in new AutoRollback {
      val count = Cron.countAll()
      count should be_>(0L)
    }
    "find all by where clauses" in new AutoRollback {
      val results = Cron.findAllBy(sqls.eq(c.id, 1L))
      results.size should be_>(0)
    }
    "count by where clauses" in new AutoRollback {
      val count = Cron.countBy(sqls.eq(c.id, 1L))
      count should be_>(0L)
    }
    "create new record" in new AutoRollback {
      val created = Cron.create()
      created should not beNull
    }
    "save a record" in new AutoRollback {
      val entity = Cron.findAll().head
      // TODO modify something
      val modified = entity
      val updated = Cron.save(modified)
      updated should not equalTo(entity)
    }
    "destroy a record" in new AutoRollback {
      val entity = Cron.findAll().head
      Cron.destroy(entity)
      val shouldBeNone = Cron.find(1L)
      shouldBeNone.isDefined should beFalse
    }
    "perform batch insert" in new AutoRollback {
      val entities = Cron.findAll()
      entities.foreach(e => Cron.destroy(e))
      val batchInserted = Cron.batchInsert(entities)
      batchInserted.size should be_>(0)
    }
  }

}
