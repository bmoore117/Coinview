import com.google.inject.AbstractModule
import tasks.FetchPriceTask

class Module extends AbstractModule {

  override def configure(): Unit = {
    bind(classOf[FetchPriceTask]).asEagerSingleton()
  }
}