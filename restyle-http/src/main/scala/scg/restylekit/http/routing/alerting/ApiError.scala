package scg.restylekit.http.routing.alerting

class ApiError(val alerts : List[Alert]) extends RuntimeException

object ApiError {

  def apply(alerts : List[Alert]) : ApiError = new ApiError(alerts)

  def apply(alert : Alert) : ApiError = ApiError(List(alert))

  def unapply(e : ApiError) : Option[List[Alert]] = Some(e.alerts)

}
