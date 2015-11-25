package models

import scalikejdbc._
import org.joda.time.{DateTime}

case class HbfSite(
  id: Long,
  url: String,
  rssUrl: Option[String] = None,
  title: Option[String] = None,
  createdAt: Option[DateTime] = None,
  updatedAt: Option[DateTime] = None) {

  def save()(implicit session: DBSession = HbfSite.autoSession): HbfSite = HbfSite.save(this)(session)

  def destroy()(implicit session: DBSession = HbfSite.autoSession): Unit = HbfSite.destroy(this)(session)

}


object HbfSite extends SQLSyntaxSupport[HbfSite] {

  override val schemaName = Some("ketsuco")

  override val tableName = "hbf_site"

  override val columns = Seq("id", "url", "rss_url", "title", "created_at", "updated_at")

  def apply(hs: SyntaxProvider[HbfSite])(rs: WrappedResultSet): HbfSite = apply(hs.resultName)(rs)
  def apply(hs: ResultName[HbfSite])(rs: WrappedResultSet): HbfSite = new HbfSite(
    id = rs.get(hs.id),
    url = rs.get(hs.url),
    rssUrl = rs.get(hs.rssUrl),
    title = rs.get(hs.title),
    createdAt = rs.get(hs.createdAt),
    updatedAt = rs.get(hs.updatedAt)
  )

  val hs = HbfSite.syntax("hs")

  override val autoSession = AutoSession

  def find(id: Long)(implicit session: DBSession = autoSession): Option[HbfSite] = {
    withSQL {
      select.from(HbfSite as hs).where.eq(hs.id, id)
    }.map(HbfSite(hs.resultName)).single.apply()
  }

  def findAll()(implicit session: DBSession = autoSession): List[HbfSite] = {
    withSQL(select.from(HbfSite as hs)).map(HbfSite(hs.resultName)).list.apply()
  }

  def countAll()(implicit session: DBSession = autoSession): Long = {
    withSQL(select(sqls.count).from(HbfSite as hs)).map(rs => rs.long(1)).single.apply().get
  }

  def findBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Option[HbfSite] = {
    withSQL {
      select.from(HbfSite as hs).where.append(where)
    }.map(HbfSite(hs.resultName)).single.apply()
  }

  def findAllBy(where: SQLSyntax)(implicit session: DBSession = autoSession): List[HbfSite] = {
    withSQL {
      select.from(HbfSite as hs).where.append(where)
    }.map(HbfSite(hs.resultName)).list.apply()
  }

  def countBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Long = {
    withSQL {
      select(sqls.count).from(HbfSite as hs).where.append(where)
    }.map(_.long(1)).single.apply().get
  }

  def create(
    url: String,
    rssUrl: Option[String] = None,
    title: Option[String] = None,
    createdAt: Option[DateTime] = None,
    updatedAt: Option[DateTime] = None)(implicit session: DBSession = autoSession): HbfSite = {
    val generatedKey = withSQL {
      insert.into(HbfSite).columns(
        column.url,
        column.rssUrl,
        column.title,
        column.createdAt,
        column.updatedAt
      ).values(
        url,
        rssUrl,
        title,
        createdAt,
        updatedAt
      )
    }.updateAndReturnGeneratedKey.apply()

    HbfSite(
      id = generatedKey,
      url = url,
      rssUrl = rssUrl,
      title = title,
      createdAt = createdAt,
      updatedAt = updatedAt)
  }

  def batchInsert(entities: Seq[HbfSite])(implicit session: DBSession = autoSession): Seq[Int] = {
    val params: Seq[Seq[(Symbol, Any)]] = entities.map(entity => 
      Seq(
        'url -> entity.url,
        'rssUrl -> entity.rssUrl,
        'title -> entity.title,
        'createdAt -> entity.createdAt,
        'updatedAt -> entity.updatedAt))
        SQL("""insert into hbf_site(
        url,
        rss_url,
        title,
        created_at,
        updated_at
      ) values (
        {url},
        {rssUrl},
        {title},
        {createdAt},
        {updatedAt}
      )""").batchByName(params: _*).apply()
    }

  def save(entity: HbfSite)(implicit session: DBSession = autoSession): HbfSite = {
    withSQL {
      update(HbfSite).set(
        column.id -> entity.id,
        column.url -> entity.url,
        column.rssUrl -> entity.rssUrl,
        column.title -> entity.title,
        column.createdAt -> entity.createdAt,
        column.updatedAt -> entity.updatedAt
      ).where.eq(column.id, entity.id)
    }.update.apply()
    entity
  }

  def destroy(entity: HbfSite)(implicit session: DBSession = autoSession): Unit = {
    withSQL { delete.from(HbfSite).where.eq(column.id, entity.id) }.update.apply()
  }

}
