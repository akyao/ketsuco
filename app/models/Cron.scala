package models

import org.joda.time.DateTime
import scalikejdbc._

case class Cron(
  id: Long,
  body: Option[String] = None,
  hash: Option[String] = None,
  createdAt: Option[DateTime] = None,
  updatedAt: Option[DateTime] = None) {

  def save()(implicit session: DBSession = Cron.autoSession): Cron = Cron.save(this)(session)

  def destroy()(implicit session: DBSession = Cron.autoSession): Unit = Cron.destroy(this)(session)

}


object Cron extends SQLSyntaxSupport[Cron] {

  override val schemaName = Some("ketsuco")

  override val tableName = "cron"

  override val columns = Seq("id", "body", "hash", "created_at", "updated_at")

  def apply(c: SyntaxProvider[Cron])(rs: WrappedResultSet): Cron = apply(c.resultName)(rs)
  def apply(c: ResultName[Cron])(rs: WrappedResultSet): Cron = new Cron(
    id = rs.get(c.id),
    body = rs.get(c.body),
    hash = rs.get(c.hash),
    createdAt = rs.get(c.createdAt),
    updatedAt = rs.get(c.updatedAt)
  )

  val c = Cron.syntax("c")

  override val autoSession = AutoSession

  def find(id: Long)(implicit session: DBSession = autoSession): Option[Cron] = {
    withSQL {
      select.from(Cron as c).where.eq(c.id, id)
    }.map(Cron(c.resultName)).single.apply()
  }

  def findAll()(implicit session: DBSession = autoSession): List[Cron] = {
    withSQL(select.from(Cron as c)).map(Cron(c.resultName)).list.apply()
  }

  def countAll()(implicit session: DBSession = autoSession): Long = {
    withSQL(select(sqls.count).from(Cron as c)).map(rs => rs.long(1)).single.apply().get
  }

  def findBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Option[Cron] = {
    withSQL {
      select.from(Cron as c).where.append(where)
    }.map(Cron(c.resultName)).single.apply()
  }

  def findAllBy(where: SQLSyntax)(implicit session: DBSession = autoSession): List[Cron] = {
    withSQL {
      select.from(Cron as c).where.append(where)
    }.map(Cron(c.resultName)).list.apply()
  }

  def countBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Long = {
    withSQL {
      select(sqls.count).from(Cron as c).where.append(where)
    }.map(_.long(1)).single.apply().get
  }

  def create(
    body: Option[String] = None,
    hash: Option[String] = None,
    createdAt: Option[DateTime] = None,
    updatedAt: Option[DateTime] = None)(implicit session: DBSession = autoSession): Cron = {
    val generatedKey = withSQL {
      insert.into(Cron).columns(
        column.body,
        column.hash,
        column.createdAt,
        column.updatedAt
      ).values(
        body,
        hash,
        createdAt,
        updatedAt
      )
    }.updateAndReturnGeneratedKey.apply()

    Cron(
      id = generatedKey,
      body = body,
      hash = hash,
      createdAt = createdAt,
      updatedAt = updatedAt)
  }

  def batchInsert(entities: Seq[Cron])(implicit session: DBSession = autoSession): Seq[Int] = {
    val params: Seq[Seq[(Symbol, Any)]] = entities.map(entity => 
      Seq(
        'body -> entity.body,
        'hash -> entity.hash,
        'createdAt -> entity.createdAt,
        'updatedAt -> entity.updatedAt))
        SQL("""insert into cron(
        body,
        hash,
        created_at,
        updated_at
      ) values (
        {body},
        {hash},
        {createdAt},
        {updatedAt}
      )""").batchByName(params: _*).apply()
    }

  def save(entity: Cron)(implicit session: DBSession = autoSession): Cron = {
    withSQL {
      update(Cron).set(
        column.id -> entity.id,
        column.body -> entity.body,
        column.hash -> entity.hash,
        column.createdAt -> entity.createdAt,
        column.updatedAt -> entity.updatedAt
      ).where.eq(column.id, entity.id)
    }.update.apply()
    entity
  }

  def destroy(entity: Cron)(implicit session: DBSession = autoSession): Unit = {
    withSQL { delete.from(Cron).where.eq(column.id, entity.id) }.update.apply()
  }

}
