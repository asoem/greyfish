package org.asoem.greyfish.core.actions;

/**
 * Created by IntelliJ IDEA.
 * User: christoph
 * Date: 20.01.11
 * Time: 15:33
 * To change this template use File | Settings | File Templates.
 */
public class GFActionSpec {

    /*
    @RunWith(classOf[JUnitRunner])
class GFActionSpec extends FlatSpec {

  "All classes implementing GFAction which have annotation @ClassGroup(tags=[\"actions\"])" should "correctly clone themselves" in {
    // find all GFActions in classpath org.asoem.greyfish.core.actions with annotation @ClassGroup(tags=["actions"])
    val finder = ClassFinder()
    val classes = finder.getClasses
    val concreteSubclasses = ClassFinder.concreteSubclasses("org.asoem.greyfish.core.actions.GFAction", classes)

    try {
      val classIterator = concreteSubclasses.map(e => Class.forName(e.name))
      val classWithAnnotationIterator = classIterator.filter(e => e.isAnnotationPresent(classOf[ClassGroup]))
      val instanceList = new ListBuffer[java.lang.Object]
      // TODO: val classFieldListMap Map[class, fieldList]


      for (clazz <- classWithAnnotationIterator) {

        val fieldList = new ListBuffer[Field]
        var objOrSuper = clazz.asInstanceOf[java.lang.Class[Object]];
        do {
          for (field <- clazz.getDeclaredFields()) {
            fieldList += field
          }
          objOrSuper = objOrSuper.getSuperclass().asInstanceOf[java.lang.Class[Object]];
        } while (objOrSuper != null)

        // now I have the class and the fields
        // next, I need to create an instance of this class
        // and set the fields to some arbitrary to

        val instance = clazz.newInstance().asInstanceOf[java.lang.Class[Object]];
        instanceList += instance

        for (field <- fieldList) {
          field.set(instance, field.getType().newInstance())
        }

      }
      // clone them
      val cloneList = instanceList.map(e => e.asInstanceOf[GFAction].deepClone())

      // assert all fields annotated with an "SimpleXML" Attribute are properly cloned
      val b = (instanceList zip cloneList).forall(
        e => {
//          val fieldList = Nil // TODO: = classFieldListMap.get(classOf(instance))
//          fieldList.forall(f => f.get(e._1) == f.get(e._2))
//           TODO: does the == relation always hold?
          false
        }
      )

      assert(b, "Cloning failed")
    }
    catch {
      case ex : Exception => println(ex)
    }


  }
}
     */
}
