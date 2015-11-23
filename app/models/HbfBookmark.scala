package models

import scalikejdbc._
import org.joda.time.{DateTime}

case class HbfBookmark(
  id: Long,
  hbfSitePageId: Long,
  hbfUserId: Long,
  createdAt: Option[DateTime] = None,
  updatedAt: Option[DateTime] = None) {

  def save()(implicit session: DBSession = HbfBookmark.autoSession): HbfBookmark = HbfBookmark.save(this)(session)

  def destroy()(implicit session: DBSession = HbfBookmark.autoSession): Unit = HbfBookmark.destroy(this)(session)

}


object HbfBookmark extends SQLSyntaxSupport[HbfBookmark] {

  override val schemaName = Some("ketsuco")

  override val tableName = "hbf_bookmark"

  override val columns = Seq("id", "hbf_site_page_id", "hbf_user_id", "created_at", "updated_at")

  def apply(hb: SyntaxProvider[HbfBookmark])(rs: WrappedResultSet): HbfBookmark = apply(hb.resultName)(rs)
  def apply(hb: ResultName[HbfBookmark])(rs: WrappedResultSet): HbfBookmark = new HbfBookmark(
    id = rs.get(hb.id),
    hbfSitePageId = rs.get(hb.hbfSitePageId),
    hbfUserId = rs.get(hb.hbfUserId),
    createdAt = rs.get(hb.createdAt),
    updatedAt = rs.get(hb.updatedAt)
  )

  val hb = HbfBookmark.syntax("hb")

  override val autoSession = AutoSession

  def find(id: Long)(implicit session: DBSession = autoSession): Option[HbfBookmark] = {
    withSQL {
      select.from(HbfBookmark as hb).where.eq(hb.id, id)
    }.map(HbfBookmark(hb.resultName)).single.apply()
  }

  def findAll()(implicit session: DBSession = autoSession): List[HbfBookmark] = {
    withSQL(select.from(HbfBookmark as hb)).map(HbfBookmark(hb.resultName)).list.apply()
  }

  def countAll()(implicit session: DBSession = autoSession): Long = {
    withSQL(select(sqls.count).from(HbfBookmark as hb)).map(rs => rs.long(1)).single.apply().get
  }

  def findBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Option[HbfBookmark] = {
    withSQL {
      select.from(HbfBookmark as hb).where.append(where)
    }.map(HbfBookmark(hb.resultName)).single.apply()
  }

  def findAllBy(where: SQLSyntax)(implicit session: DBSession = autoSession): List[HbfBookmark] = {
    withSQL {
      select.from(HbfBookmark as hb).where.append(where)
    }.map(HbfBookmark(hb.resultName)).list.apply()
  }

  def countBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Long = {
    withSQL {
      select(sqls.count).from(HbfBookmark as hb).where.append(where)
    }.map(_.long(1)).single.apply().get
  }

  def create(
    hbfSitePageId: Long,
    hbfUserId: Long,
    createdAt: Option[DateTime] = None,
    updatedAt: Option[DateTime] = None)(implicit session: DBSession = autoSession): HbfBookmark = {
    val generatedKey = withSQL {
      insert.into(HbfBookmark).columns(
        column.hbfSitePageId,
        column.hbfUserId,
        column.createdAt,
        column.updatedAt
      ).values(
        hbfSitePageId,
        hbfUserId,
        createdAt,
        updatedAt
      )
    }.updateAndReturnGeneratedKey.apply()

    HbfBookmark(
      id = generatedKey,
      hbfSitePageId = hbfSitePageId,
      hbfUserId = hbfUserId,
      createdAt = createdAt,
      updatedAt = updatedAt)
  }

  def batchInsert(entities: Seq[HbfBookmark])(implicit session: DBSession = autoSession): Seq[Int] = {
    val params: Seq[Seq[(Symbol, Any)]] = entities.map(entity => 
      Seq(
        'hbfSitePageId -> entity.hbfSitePageId,
        'hbfUserId -> entity.hbfUserId,
        'createdAt -> entity.createdAt,
        'updatedAt -> entity.updatedAt))
        SQL("""insert into hbf_bookmark(
        hbf_site_page_id,
        hbf_user_id,
        created_at,
        updated_at
      ) values (
        {hbfSitePageId},
        {hbfUserId},
        {createdAt},
        {updatedAt}
      )""").batchByName(params: _*).apply()
    }

  def save(entity: HbfBookmark)(implicit session: DBSession = autoSession): HbfBookmark = {
    withSQL {
      update(HbfBookmark).set(
        column.id -> entity.id,
        column.hbfSitePageId -> entity.hbfSitePageId,
        column.hbfUserId -> entity.hbfUserId,
        column.createdAt -> entity.createdAt,
        column.updatedAt -> entity.updatedAt
      ).where.eq(column.id, entity.id)
    }.update.apply()
    entity
  }

  def destroy(entity: HbfBookmark)(implicit session: DBSession = autoSession): Unit = {
    withSQL { delete.from(HbfBookmark).where.eq(column.id, entity.id) }.update.apply()
  }

}
