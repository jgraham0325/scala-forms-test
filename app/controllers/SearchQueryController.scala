package controllers

import javax.inject.Inject
import models.SearchQueryResult
import play.api.data._
import play.api.mvc._

/**
  * The classic SearchQueryController using MessagesAbstractController.
  *
  * Instead of MessagesAbstractController, you can use the I18nSupport trait,
  * which provides implicits that create a Messages instance from a request
  * using implicit conversion.
  *
  * See https://www.playframework.com/documentation/2.6.x/ScalaForms#passing-messagesprovider-to-form-helpers
  * for details.
  */
class SearchQueryController @Inject()(cc: MessagesControllerComponents) extends MessagesAbstractController(cc) {

  import SearchQueryForm._

  private val searchQueryResults = scala.collection.mutable.ArrayBuffer(
    SearchQueryResult("mytestKey", "CLASS1")
  )

  // The URL to the widget.  You can call this directly from the template, but it
  // can be more convenient to leave the template completely stateless i.e. all
  // of the "SearchQueryController" references are inside the .scala file.
  private val postUrl = routes.SearchQueryController.createSearchQuery()

  def index = Action {
    Ok(views.html.index())
  }

  def listSearchQueries = Action { implicit request: MessagesRequest[AnyContent] =>
    // Pass an unpopulated form to the template
    Ok(views.html.listSearchQueries(searchQueryResults, form, postUrl))
  }

  // This will be the action that handles our form post
  def createSearchQuery = Action { implicit request: MessagesRequest[AnyContent] =>
    val errorFunction = { formWithErrors: Form[Data] =>
      // This is the bad case, where the form had validation errors.
      // Let's show the user the form again, with the errors highlighted.
      // Note how we pass the form with errors to the template.
      BadRequest(views.html.listSearchQueries(searchQueryResults, formWithErrors, postUrl))
    }

    val successFunction = { data: Data =>
      // This is the good case, where the form was successfully parsed as a Data object.
      //TODO: Call the persistence layer to get result from redis, use this for classification
      val searchQueryResult = SearchQueryResult(key = data.searchKey, classification = "CLASS1")
      searchQueryResults.append(searchQueryResult)
      Redirect(routes.SearchQueryController.listSearchQueries())
    }

    val formValidationResult = form.bindFromRequest
    formValidationResult.fold(errorFunction, successFunction)
  }
}
