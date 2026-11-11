package com.inouiw.vastra.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/**
 * Stroke icons copied from the prototype's SVG markup (24-unit viewBox, round caps and joins).
 * Each icon is built from the same path data the HTML uses.
 */
private fun stroked(name: String, width: Float = 1.7f, build: ImageVector.Builder.() -> Unit): ImageVector =
    ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f).apply(build).build()

private fun ImageVector.Builder.sp(d: String, width: Float = 1.7f) {
    addPath(
        pathData = addPathNodes(d),
        stroke = SolidColor(Color.Black),
        strokeLineWidth = width,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
    )
}

private fun ImageVector.Builder.sc(cx: Float, cy: Float, r: Float, width: Float = 1.7f) {
    // Circle as two arcs, matching SVG <circle>.
    sp("M${cx - r} ${cy}a$r $r 0 1 0 ${2 * r} 0a$r $r 0 1 0 ${-2 * r} 0", width)
}

private fun ImageVector.Builder.sr(x: Float, y: Float, w: Float, h: Float, rx: Float, width: Float = 1.7f) {
    sp("M${x + rx} ${y}h${w - 2 * rx}a$rx $rx 0 0 1 $rx $rx v${h - 2 * rx}a$rx $rx 0 0 1 ${-rx} $rx h${-(w - 2 * rx)}a$rx $rx 0 0 1 ${-rx} ${-rx} v${-(h - 2 * rx)}a$rx $rx 0 0 1 $rx ${-rx}z", width)
}

object VIcons {
    /** .ic bell */
    val Bell = stroked("bell") { sp("M6 16V11a6 6 0 0 1 12 0v5l1.5 2h-15z"); sp("M10 20a2 2 0 0 0 4 0") }

    /** .ic search */
    val Search = stroked("search") { sc(11f, 11f, 6.5f); sp("M16 16l4 4") }

    /** .ic sliders */
    val Sliders = stroked("sliders") {
        sp("M4 7h10M18 7h2M4 17h2M10 17h10")
        sc(16f, 7f, 2f)
        sc(8f, 17f, 2f)
    }

    /** back chevron */
    val ChevronLeft = stroked("chevronLeft") { sp("M15 6l-6 6 6 6") }

    /** Home tab */
    val Home = stroked("home") { sp("M4 11l8-6 8 6v8a1 1 0 0 1-1 1h-4v-6h-6v6H5a1 1 0 0 1-1-1z") }

    /** Planner tab (calendar) */
    val Calendar = stroked("calendar") { sr(4f, 5f, 16f, 15f, 2f); sp("M4 10h16M9 3v4M15 3v4") }

    /** Bag tab */
    val Bag = stroked("bag") { sp("M5 8h14l-1 12H6z"); sp("M9 8V6a3 3 0 0 1 6 0v2") }

    /** Profile tab */
    val User = stroked("user") { sc(12f, 8f, 4f); sp("M4 21c1.5-4 4.5-6 8-6s6.5 2 8 6") }

    /** Checkout / pickup pin row */
    val Check = stroked("check") { sp("M5 12.5l4.5 4.5L19 7.5") }

    /** Four-square "All" category icon */
    val Grid = stroked("grid") {
        sr(4f, 4f, 6.5f, 6.5f, 1.5f, 1.8f)
        sr(13.5f, 4f, 6.5f, 6.5f, 1.5f, 1.8f)
        sr(4f, 13.5f, 6.5f, 6.5f, 1.5f, 1.8f)
        sr(13.5f, 13.5f, 6.5f, 6.5f, 1.5f, 1.8f)
    }

    val Close = stroked("close") { sp("M6 6l12 12M18 6L6 18") }
    val Plus = stroked("plus") { sp("M12 5v14M5 12h14") }
    val Minus = stroked("minus") { sp("M5 12h14") }
    val Arrow = stroked("arrow") { sp("M5 12h14"); sp("M13 6l6 6-6 6") }
    val ChevronRight = stroked("chevronRight") { sp("M9 6l6 6-6 6") }
    val Pin = stroked("pin") { sp("M12 21s-6-5.5-6-11a6 6 0 0 1 12 0c0 5.5-6 11-6 11z"); sc(12f, 10f, 2f) }
    val Phone = stroked("phone") { sp("M5 4h4l2 5-2.5 1.5a11 11 0 0 0 5 5L15 13l5 2v4a2 2 0 0 1-2 2A16 16 0 0 1 3 6a2 2 0 0 1 2-2z") }
    val Camera = stroked("camera") { sp("M4 8h4l2-3h4l2 3h4v11H4z"); sc(12f, 13f, 3f) }
    val Spark = stroked("spark") { sp("M12 3l1.8 5.2L19 10l-5.2 1.8L12 17l-1.8-5.2L5 10l5.2-1.8z") }
    val Moon = stroked("moon") { sp("M20 14a8 8 0 1 1-10-10a6 6 0 0 0 10 10z") }
    val Trash = stroked("trash") { sp("M4 7h16"); sp("M9 7V4h6v3"); sp("M6 7l1 13h10l1-13") }
}
