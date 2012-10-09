package controllers

import play.mvc.Controller

import play.api.mvc.Action

object Content extends core.LangAwareController {
	def index = Action{
	  Ok("Test demo")
	}
}