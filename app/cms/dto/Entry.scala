package cms.dto

object EntryType extends Enumeration {
  val Message = Value(1, "Message")
  val HTML = Value(2, "HTML")
}

case class Entry(key: String, entryType: EntryType.Value, contents: List[Content])

case class Content(lang: String, value: String)