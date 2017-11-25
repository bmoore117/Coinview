/*
 * Copyright (C) 2009-2017 Lightbend Inc. <https://www.lightbend.com>
 */
package modules

import play.api.inject.{SimpleModule, _}
import tasks.FetchPriceTask

class TasksModule extends SimpleModule(bind[FetchPriceTask].toSelf.eagerly())