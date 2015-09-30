package models

import org.joda.time.DateTime
import scalikejdbc._

case class CronLine(
  id: Long,
  cronId: Long,
  line: Option[Int] = None,
  body: Option[String] = None,
  command: Option[String] = None,
  month: Option[String] = None,
  day: Option[String] = None,
  week: Option[String] = None,
  hour: Option[String] = None,
  minute: Option[String] = None,
  createdAt: Option[DateTime] = None,
  updatedAt: Option[DateTime] = None) {

  def save()(implicit session: DBSession = CronLine.autoSession): CronLine = CronLine.save(this)(session)

  def destroy()(implicit session: DBSession = CronLine.autoSession): Unit = CronLine.destroy(this)(session)

}


object CronLine extends SQLSyntaxSupport[CronLine] {

  override val schemaName = Some("ketsuco")

  override val tableName = "cron_line"

  override val columns = Seq("id", "cron_id", "line", "body", "command", "month", "day", "week", "hour", "minute", "created_at", "updated_at")

  def apply(cl: SyntaxProvider[CronLine])(rs: WrappedResultSet): CronLine = apply(cl.resultName)(rs)
  def apply(cl: ResultName[CronLine])(rs: WrappedResultSet): CronLine = new CronLine(
    id = rs.get(cl.id),
    cronId = rs.get(cl.cronId),
    line = rs.get(cl.line),
    body = rs.get(cl.body),
    command = rs.get(cl.command),
    month = rs.get(cl.month),
    day = rs.get(cl.day),
    week = rs.get(cl.week),
    hour = rs.get(cl.hour),
    minute = rs.get(cl.minute),
    createdAt = rs.get(cl.createdAt),
    updatedAt = rs.get(cl.updatedAt)
  )

  val cl = CronLine.syntax("cl")

  override val autoSession = AutoSession

  def find(id: Long)(implicit session: DBSession = autoSession): Option[CronLine] = {
    withSQL {
      select.from(CronLine as cl).where.eq(cl.id, id)
    }.map(CronLine(cl.resultName)).single.apply()
  }

  def findAll()(implicit session: DBSession = autoSession): List[CronLine] = {
    withSQL(select.from(CronLine as cl)).map(CronLine(cl.resultName)).list.apply()
  }

  def countAll()(implicit session: DBSession = autoSession): Long = {
    withSQL(select(sqls.count).from(CronLine as cl)).map(rs => rs.long(1)).single.apply().get
  }

  def findBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Option[CronLine] = {
    withSQL {
      select.from(CronLine as cl).where.append(where)
    }.map(CronLine(cl.resultName)).single.apply()
  }

  def findAllBy(where: SQLSyntax)(implicit session: DBSession = autoSession): List[CronLine] = {
    withSQL {
      select.from(CronLine as cl).where.append(where)
    }.map(CronLine(cl.resultName)).list.apply()
  }

  def countBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Long = {
    withSQL {
      select(sqls.count).from(CronLine as cl).where.append(where)
    }.map(_.long(1)).single.apply().get
  }

  def create(
    cronId: Long,
    line: Option[Int] = None,
    body: Option[String] = None,
    command: Option[String] = None,
    month: Option[String] = None,
    day: Option[String] = None,
    week: Option[String] = None,
    hour: Option[String] = None,
    minute: Option[String] = None,
    createdAt: Option[DateTime] = None,
    updatedAt: Option[DateTime] = None)(implicit session: DBSession = autoSession): CronLine = {
    val generatedKey = withSQL {
      insert.into(CronLine).columns(
        column.cronId,
        column.line,
        column.body,
        column.command,
        column.month,
        column.day,
        column.week,
        column.hour,
        column.minute,
        column.createdAt,
        column.updatedAt
      ).values(
        cronId,
        line,
        body,
        command,
        month,
        day,
        week,
        hour,
        minute,
        createdAt,
        updatedAt
      )
    }.updateAndReturnGeneratedKey.apply()

    CronLine(
      id = generatedKey,
      cronId = cronId,
      line = line,
      body = body,
      command = command,
      month = month,
      day = day,
      week = week,
      hour = hour,
      minute = minute,
      createdAt = createdAt,
      updatedAt = updatedAt)
  }

  def batchInsert(entities: Seq[CronLine])(implicit session: DBSession = autoSession): Seq[Int] = {
    val params: Seq[Seq[(Symbol, Any)]] = entities.map(entity => 
      Seq(
        'cronId -> entity.cronId,
        'line -> entity.line,
        'body -> entity.body,
        'command -> entity.command,
        'month -> entity.month,
        'day -> entity.day,
        'week -> entity.week,
        'hour -> entity.hour,
        'minute -> entity.minute,
        'createdAt -> entity.createdAt,
        'updatedAt -> entity.updatedAt))
        SQL("""insert into cron_line(
        cron_id,
        line,
        body,
        command,
        month,
        day,
        week,
        hour,
        minute,
        created_at,
        updated_at
      ) values (
        {cronId},
        {line},
        {body},
        {command},
        {month},
        {day},
        {week},
        {hour},
        {minute},
        {createdAt},
        {updatedAt}
      )""").batchByName(params: _*).apply()
    }

  def save(entity: CronLine)(implicit session: DBSession = autoSession): CronLine = {
    withSQL {
      update(CronLine).set(
        column.id -> entity.id,
        column.cronId -> entity.cronId,
        column.line -> entity.line,
        column.body -> entity.body,
        column.command -> entity.command,
        column.month -> entity.month,
        column.day -> entity.day,
        column.week -> entity.week,
        column.hour -> entity.hour,
        column.minute -> entity.minute,
        column.createdAt -> entity.createdAt,
        column.updatedAt -> entity.updatedAt
      ).where.eq(column.id, entity.id)
    }.update.apply()
    entity
  }

  def destroy(entity: CronLine)(implicit session: DBSession = autoSession): Unit = {
    withSQL { delete.from(CronLine).where.eq(column.id, entity.id) }.update.apply()
  }

}
