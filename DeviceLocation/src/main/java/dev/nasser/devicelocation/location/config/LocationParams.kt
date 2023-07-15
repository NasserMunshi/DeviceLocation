package dev.nasser.devicelocation.location.config

/**
 * Created by Nasser Munshi on 04-Jul-2023.
 * @author Nasser Munshi
 */
class LocationParams internal constructor(
    accuracy: LocationAccuracy,
    interval: Long,
    distance: Float
) {
    private val interval: Long
    private val distance: Float
    private val accuracy: LocationAccuracy

    init {
        this.interval = interval
        this.distance = distance
        this.accuracy = accuracy
    }

    fun getInterval(): Long {
        return interval
    }

    fun getDistance(): Float {
        return distance
    }

    fun getAccuracy(): LocationAccuracy? {
        return accuracy
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LocationParams) return false
        return other.distance.compareTo(distance) == 0 && interval == other.interval && accuracy === other.accuracy
    }

    override fun hashCode(): Int {
        var result = (interval xor (interval ushr 32)).toInt()
        result =
            31 * result + if (distance != +0.0f) java.lang.Float.floatToIntBits(distance) else 0
        result = 31 * result + accuracy.hashCode()
        return result
    }

    class Builder {
        private var accuracy: LocationAccuracy = LocationAccuracy.MEDIUM
        private var interval: Long = 0
        private var distance = 0f
        fun setAccuracy(accuracy: LocationAccuracy): Builder {
            this.accuracy = accuracy
            return this
        }

        fun setInterval(interval: Long): Builder {
            this.interval = interval
            return this
        }

        fun setDistance(distance: Float): Builder {
            this.distance = distance
            return this
        }

        fun build(): LocationParams {
            return LocationParams(accuracy, interval, distance)
        }
    }

    companion object {
        // Defaults
        val NAVIGATION =
            Builder().setAccuracy(LocationAccuracy.HIGH).setDistance(0f).setInterval(500).build()
        val BEST_EFFORT =
            Builder().setAccuracy(LocationAccuracy.MEDIUM).setDistance(150f).setInterval(2500)
                .build()
        val LAZY =
            Builder().setAccuracy(LocationAccuracy.LOW).setDistance(500f).setInterval(5000).build()
    }
}