package tools.helper

/**
 * Created with IntelliJ IDEA.
 * User: mario
 * Date: 15.01.13
 * Time: 01:14
 * To change this template use File | Settings | File Templates.
 */
object ObjectHelper {
  def deepCopy[A](a: A)(implicit m: reflect.Manifest[A]): A =
    util.Marshal.load[A](util.Marshal.dump(a))
}
