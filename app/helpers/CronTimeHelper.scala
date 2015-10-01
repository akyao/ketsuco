package helpers

object CronTimeHelper{

  def calcTimes(time: String, to: Int): Seq[Int] ={

    if (time == "*") {
      return Range(0, 60)
    }

    val h = time.replaceAll("\\s", "")
    var times = List[Int]()

    for (he <- h.split(",")) {

      val dash = he.contains("-")
      val slash = he.contains("/")

      if (dash && !slash){
        // 2-14
        val hFromTo = he.split("-")
        times = times ++ Range(hFromTo(0).toInt, hFromTo(1).toInt)
      } else if(!dash && slash){
        // TODO */n ?
        val n = he.split("/")(1).toInt
        times = times ++ Range(0, to, n)
      } else if (dash && slash) {
        // 2-15/5
        val frontAndBack = he.split("/")
        val (front, back) = (frontAndBack(0), frontAndBack(1).toInt)
        val frontFromAndTo = front.split("-")
        val (frontFrom, frontTo) = (frontFromAndTo(0).toInt, frontFromAndTo(1).toInt)
        times = times ++ Range(frontFrom, frontTo, back)
      } else {
        // 2 数字だけ
        times = times :+ he.toInt
      }
    }

    return times
  }
}