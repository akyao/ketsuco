package models

import scalikejdbc._
import org.joda.time.{DateTime}

case class HbfSitePage(
  id: Long,
  hbfSiteId: Long,
  url: Long,
  entryAt: Option[DateTime] = None,
  createdAt: Option[DateTime] = None,
  updatedAt: Option[DateTime] = None) {

  def save()(implicit session: DBSession = HbfSitePage.autoSession): HbfSitePage = HbfSitePage.save(this)(session)

  def destroy()(implicit session: DBSession = HbfSitePage.autoSession): Unit = HbfSitePage.destroy(this)(session)

}


object HbfSitePage extends SQLSyntaxSupport[HbfSitePage] {

  override val schemaName = Some("ketsuco")

  override val tableName = "hbf_site_page"

  override val columns = Seq("id", "hbf_site_id", "url", "entry_at", "created_at", "updated_at")

  def apply(hsp: SyntaxProvider[HbfSitePage])(rs: WrappedResultSet): HbfSitePage = apply(hsp.resultName)(rs)
  def apply(hsp: ResultName[HbfSitePage])(rs: WrappedResultSet): HbfSitePage = new HbfSitePage(
    id = rs.get(hsp.id),
    hbfSiteId = rs.get(hsp.hbfSiteId),
    url = rs.get(hsp.url),
    entryAt = rs.get(hsp.entryAt),
    createdAt = rs.get(hsp.createdAt),
    updatedAt = rs.get(hsp.updatedAt)
  )

  val hsp = HbfSitePage.syntax("hsp")

  override val autoSession = AutoSession

  def find(id: Long)(implicit session: DBSession = autoSession): Option[HbfSitePage] = {
    withSQL {
      select.from(HbfSitePage as hsp).where.eq(hsp.id, id)
    }.map(HbfSitePage(hsp.resultName)).single.apply()
  }

  def findAll()(implicit session: DBSession = autoSession): List[HbfSitePage] = {
    withSQL(select.from(HbfSitePage as hsp)).map(HbfSitePage(hsp.resultName)).list.apply()
  }

  def countAll()(implicit session: DBSession = autoSession): Long = {
    withSQL(select(sqls.count).from(HbfSitePage as hsp)).map(rs => rs.long(1)).single.apply().get
  }

  def findBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Option[HbfSitePage] = {
    withSQL {
      select.from(HbfSitePage as hsp).where.append(where)
    }.map(HbfSitePage(hsp.resultName)).single.apply()
  }

  def findAllBy(where: SQLSyntax)(implicit session: DBSession = autoSession): List[HbfSitePage] = {
    withSQL {
      select.from(HbfSitePage as hsp).where.append(where)
    }.map(HbfSitePage(hsp.resultName)).list.apply()
  }

  def countBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Long = {
    withSQL {
      select(sqls.count).from(HbfSitePage as hsp).where.append(where)
    }.map(_.long(1)).single.apply().get
  }

  def create(
    hbfSiteId: Long,
    url: Long,
    entryAt: Option[DateTime] = None,
    createdAt: Option[DateTime] = None,
    updatedAt: Option[DateTime] = None)(implicit session: DBSession = autoSession): HbfSitePage = {
    val generatedKey = withSQL {
      insert.into(HbfSitePage).columns(
        column.hbfSiteId,
        column.url,
        column.entryAt,
        column.createdAt,
        column.updatedAt
      ).values(
        hbfSiteId,
        url,
        entryAt,
        createdAt,
        updatedAt
      )
    }.updateAndReturnGeneratedKey.apply()

    HbfSitePage(
      id = generatedKey,
      hbfSiteId = hbfSiteId,
      url = url,
      entryAt = entryAt,
      createdAt = createdAt,
      updatedAt = updatedAt)
  }

  def batchInsert(entities: Seq[HbfSitePage])(implicit session: DBSession = autoSession): Seq[Int] = {
    val params: Seq[Seq[(Symbol, Any)]] = entities.map(entity => 
      Seq(
        'hbfSiteId -> entity.hbfSiteId,
        'url -> entity.url,
        'entryAt -> entity.entryAt,
        'createdAt -> entity.createdAt,
        'updatedAt -> entity.updatedAt))
        SQL("""insert into hbf_site_page(
        hbf_site_id,
        url,
        entry_at,
        created_at,
        updated_at
      ) values (
        {hbfSiteId},
        {url},
        {entryAt},
        {createdAt},
        {updatedAt}
      )""").batchByName(params: _*).apply()
    }

  def save(entity: HbfSitePage)(implicit session: DBSession = autoSession): HbfSitePage = {
    withSQL {
      update(HbfSitePage).set(
        column.id -> entity.id,
        column.hbfSiteId -> entity.hbfSiteId,
        column.url -> entity.url,
        column.entryAt -> entity.entryAt,
        column.createdAt -> entity.createdAt,
        column.updatedAt -> entity.updatedAt
      ).where.eq(column.id, entity.id)
    }.update.apply()
    entity
  }

  def destroy(entity: HbfSitePage)(implicit session: DBSession = autoSession): Unit = {
    withSQL { delete.from(HbfSitePage).where.eq(column.id, entity.id) }.update.apply()
  }

}
