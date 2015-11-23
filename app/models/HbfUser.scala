package models

import scalikejdbc._
import org.joda.time.{DateTime}

case class HbfUser(
  id: Long,
  userName: Option[String] = None,
  createdAt: Option[DateTime] = None,
  updatedAt: Option[DateTime] = None) {

  def save()(implicit session: DBSession = HbfUser.autoSession): HbfUser = HbfUser.save(this)(session)

  def destroy()(implicit session: DBSession = HbfUser.autoSession): Unit = HbfUser.destroy(this)(session)

}


object HbfUser extends SQLSyntaxSupport[HbfUser] {

  override val schemaName = Some("ketsuco")

  override val tableName = "hbf_user"

  override val columns = Seq("id", "user_name", "created_at", "updated_at")

  def apply(hu: SyntaxProvider[HbfUser])(rs: WrappedResultSet): HbfUser = apply(hu.resultName)(rs)
  def apply(hu: ResultName[HbfUser])(rs: WrappedResultSet): HbfUser = new HbfUser(
    id = rs.get(hu.id),
    userName = rs.get(hu.userName),
    createdAt = rs.get(hu.createdAt),
    updatedAt = rs.get(hu.updatedAt)
  )

  val hu = HbfUser.syntax("hu")

  override val autoSession = AutoSession

  def find(id: Long)(implicit session: DBSession = autoSession): Option[HbfUser] = {
    withSQL {
      select.from(HbfUser as hu).where.eq(hu.id, id)
    }.map(HbfUser(hu.resultName)).single.apply()
  }

  def findAll()(implicit session: DBSession = autoSession): List[HbfUser] = {
    withSQL(select.from(HbfUser as hu)).map(HbfUser(hu.resultName)).list.apply()
  }

  def countAll()(implicit session: DBSession = autoSession): Long = {
    withSQL(select(sqls.count).from(HbfUser as hu)).map(rs => rs.long(1)).single.apply().get
  }

  def findBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Option[HbfUser] = {
    withSQL {
      select.from(HbfUser as hu).where.append(where)
    }.map(HbfUser(hu.resultName)).single.apply()
  }

  def findAllBy(where: SQLSyntax)(implicit session: DBSession = autoSession): List[HbfUser] = {
    withSQL {
      select.from(HbfUser as hu).where.append(where)
    }.map(HbfUser(hu.resultName)).list.apply()
  }

  def countBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Long = {
    withSQL {
      select(sqls.count).from(HbfUser as hu).where.append(where)
    }.map(_.long(1)).single.apply().get
  }

  def create(
    userName: Option[String] = None,
    createdAt: Option[DateTime] = None,
    updatedAt: Option[DateTime] = None)(implicit session: DBSession = autoSession): HbfUser = {
    val generatedKey = withSQL {
      insert.into(HbfUser).columns(
        column.userName,
        column.createdAt,
        column.updatedAt
      ).values(
        userName,
        createdAt,
        updatedAt
      )
    }.updateAndReturnGeneratedKey.apply()

    HbfUser(
      id = generatedKey,
      userName = userName,
      createdAt = createdAt,
      updatedAt = updatedAt)
  }

  def batchInsert(entities: Seq[HbfUser])(implicit session: DBSession = autoSession): Seq[Int] = {
    val params: Seq[Seq[(Symbol, Any)]] = entities.map(entity => 
      Seq(
        'userName -> entity.userName,
        'createdAt -> entity.createdAt,
        'updatedAt -> entity.updatedAt))
        SQL("""insert into hbf_user(
        user_name,
        created_at,
        updated_at
      ) values (
        {userName},
        {createdAt},
        {updatedAt}
      )""").batchByName(params: _*).apply()
    }

  def save(entity: HbfUser)(implicit session: DBSession = autoSession): HbfUser = {
    withSQL {
      update(HbfUser).set(
        column.id -> entity.id,
        column.userName -> entity.userName,
        column.createdAt -> entity.createdAt,
        column.updatedAt -> entity.updatedAt
      ).where.eq(column.id, entity.id)
    }.update.apply()
    entity
  }

  def destroy(entity: HbfUser)(implicit session: DBSession = autoSession): Unit = {
    withSQL { delete.from(HbfUser).where.eq(column.id, entity.id) }.update.apply()
  }

}
