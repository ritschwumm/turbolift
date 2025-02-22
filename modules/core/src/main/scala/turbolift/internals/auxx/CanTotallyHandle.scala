package turbolift.internals.auxx
import scala.annotation.implicitNotFound
import turbolift.!!


@implicitNotFound(msg =
  "Effect leak in total handler (implicit not found: CanTotallyHandle)"+
  "\n  Effects requested by the computation:"+
  "\n    ${U}"+
  "\n  Effects handled by the handler:"+
  "\n    ${V}"
)
//// asserts U <= V
private[turbolift] sealed trait CanTotallyHandle[U, V]:
  def apply[A](comp: A !! U): A !! V

private[turbolift] object CanTotallyHandle:
  private[turbolift] val singleton = new CanTotallyHandle[Any, Any]:
    override def apply[A](comp: A !! Any): A !! Any = comp

  implicit def CanTotallyHandle_evidence[U, V](implicit ev: V <:< U): CanTotallyHandle[U, V] =
    CanTotallyHandle.singleton.asInstanceOf[CanTotallyHandle[U, V]]
