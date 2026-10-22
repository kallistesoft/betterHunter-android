package ca.kallistesoft.hunt.core.model

/** Same photo all day for everyone: the day number picks it. */
fun photoOfDay(pool: List<Question>, today: Long): Question? =
    if (pool.isEmpty()) null else pool[Math.floorMod(today, pool.size.toLong()).toInt()]
