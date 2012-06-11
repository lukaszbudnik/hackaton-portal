package model
import java.util.Date
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.Schema
import org.squeryl.annotations.Column
import org.squeryl.Session
import org.squeryl.SessionFactory
import org.squeryl.adapters.H2Adapter
import org.squeryl.KeyedEntity
import org.squeryl.adapters.PostgreSqlAdapter

class News(
  var title: String,
  var text: String,
  var author: String,
  var published: Date) extends KeyedEntity[Long] {

  val id: Long = 0L

}

object Hackathon extends Schema {
  val news = table[News]

  /** @TODO: how to make squeryl read these settings from config? */ 
  val databaseUsername = ""
  val databasePassword = ""
  val databaseConnection = "jdbc:h2:mem:play"

  on(news)(news => declare(
    news.id is (autoIncremented)))

  def startDatabaseSession(): Unit = {
    /** @TODO and this setting as well? */
    /** @TODO How to switch between adapters when we will deploy on Heroku? new H2Adapter -> new PostgreSqlAdapter */
    Class.forName("org.h2.Driver")
    SessionFactory.concreteFactory = Some(() => Session.create(
      java.sql.DriverManager.getConnection(databaseConnection, databaseUsername, databasePassword),
      new H2Adapter))
  }
}





   