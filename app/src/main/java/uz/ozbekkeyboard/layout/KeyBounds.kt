package uz.ozbekkeyboard.layout

/**
 * Platform-independent rectangular bounding box for keyboard keys.
 * Operates without android.graphics.RectF dependencies so bounds calculation
 * and hit testing can be thoroughly tested in JVM unit tests.
 */
data class KeyBounds(
    var left: Float = 0f,
    var top: Float = 0f,
    var right: Float = 0f,
    var bottom: Float = 0f
) {
    val width: Float get() = (right - left).coerceAtLeast(0f)
    val height: Float get() = (bottom - top).coerceAtLeast(0f)
    val isEmpty: Boolean get() = left >= right || top >= bottom

    fun centerX(): Float = (left + right) / 2f
    fun centerY(): Float = (top + bottom) / 2f

    fun contains(x: Float, y: Float): Boolean {
        return x in left..right && y in top..bottom
    }

    fun set(l: Float, t: Float, r: Float, b: Float) {
        left = l
        top = t
        right = r
        bottom = b
    }
}
