package helpers

import play.api.templates.Html

object RepeatWithIndex {
  
	def apply(field: play.api.data.Field, min: Int)(f: (Int, Int, play.api.data.Field) => Html) = {
	  
	  val endIndex = math.max(if (field.indexes.isEmpty) 0 else field.indexes.max + 1, min);
	  
	  ( 0 until endIndex).map(i => f(i, endIndex - 1, field("[" + i + "]")))  
	}
}