package com.inouiw.vastra.data

import androidx.compose.ui.graphics.Path

/** Garment silhouettes, drawn in the prototype's 70x84 viewBox. */
object Garments {
    private fun body0() = Path().apply {
        moveTo(26f, 6f); quadraticTo(35f, 12f, 44f, 6f); lineTo(50f, 16f); quadraticTo(48f, 28f, 46f, 36f)
        quadraticTo(52f, 60f, 58f, 78f); lineTo(12f, 78f); quadraticTo(18f, 60f, 24f, 36f); quadraticTo(22f, 28f, 20f, 16f); close()
    }
    private fun collar0() = Path().apply { moveTo(31f, 6f); quadraticTo(35f, 14f, 39f, 6f) }
    private fun body1() = Path().apply {
        moveTo(27f, 6f); lineTo(43f, 6f); lineTo(46f, 14f); lineTo(50f, 78f); lineTo(20f, 78f); lineTo(24f, 14f); close()
    }
    private fun collar1() = Path().apply { moveTo(30f, 6f); lineTo(28f, 16f); moveTo(40f, 6f); lineTo(42f, 16f) }
    private fun body2() = Path().apply {
        moveTo(22f, 6f); lineTo(31f, 4f); lineTo(39f, 4f); lineTo(48f, 6f); lineTo(49f, 78f); lineTo(21f, 78f); close()
    }
    private fun collar2() = Path().apply { moveTo(35f, 4f); lineTo(35f, 78f) }
    private fun body3() = Path().apply {
        moveTo(18f, 12f); lineTo(28f, 6f); quadraticTo(35f, 10f, 42f, 6f); lineTo(52f, 12f); lineTo(56f, 58f); lineTo(50f, 60f)
        lineTo(48f, 46f); lineTo(48f, 78f); lineTo(22f, 78f); lineTo(22f, 46f); lineTo(20f, 60f); lineTo(14f, 58f); close()
    }
    private fun collar3() = Path().apply { moveTo(35f, 10f); lineTo(30f, 34f); lineTo(35f, 40f); lineTo(40f, 34f); close() }
    private fun body4() = Path().apply {
        moveTo(22f, 10f); lineTo(30f, 5f); lineTo(35f, 9f); lineTo(40f, 5f); lineTo(48f, 10f); lineTo(54f, 26f); lineTo(48f, 30f)
        lineTo(46f, 24f); lineTo(46f, 78f); lineTo(24f, 78f); lineTo(24f, 24f); lineTo(22f, 30f); lineTo(16f, 26f); close()
    }
    private fun collar4() = Path().apply { moveTo(35f, 9f); lineTo(33f, 18f); lineTo(35f, 22f); lineTo(37f, 18f); close() }
    private fun body5() = Path().apply {
        moveTo(24f, 10f); lineTo(30f, 5f); quadraticTo(35f, 10f, 40f, 5f); lineTo(46f, 10f); lineTo(58f, 24f); lineTo(52f, 30f)
        lineTo(46f, 26f); lineTo(46f, 78f); lineTo(24f, 78f); lineTo(24f, 26f); lineTo(18f, 30f); lineTo(12f, 24f); close()
    }
    private fun collar5() = Path()
    private fun body6() = Path().apply {
        moveTo(24f, 12f); lineTo(30f, 6f); quadraticTo(35f, 10f, 40f, 6f); lineTo(46f, 12f); lineTo(49f, 42f); lineTo(21f, 42f); close()
    }
    private fun collar6() = Path()
    private fun body7() = Path().apply {
        moveTo(20f, 6f); lineTo(50f, 6f); lineTo(52f, 78f); lineTo(40f, 78f); lineTo(35f, 36f); lineTo(30f, 78f); lineTo(18f, 78f); close()
    }
    private fun collar7() = Path().apply { moveTo(35f, 36f); lineTo(35f, 6f) }
    private fun body8() = Path().apply { moveTo(22f, 6f); lineTo(48f, 6f); lineTo(56f, 78f); lineTo(14f, 78f); close() }
    private fun collar8() = Path().apply { moveTo(35f, 6f); lineTo(35f, 78f) }
    private fun body9() = Path().apply {
        moveTo(20f, 6f); lineTo(50f, 6f); lineTo(52f, 78f); lineTo(39f, 78f); lineTo(35f, 36f); lineTo(31f, 78f); lineTo(18f, 78f); close()
    }
    private fun collar9() = Path().apply { moveTo(35f, 6f); lineTo(35f, 36f) }
    private fun body10() = Path().apply {
        moveTo(6f, 58f); quadraticTo(18f, 52f, 30f, 46f); lineTo(44f, 36f); quadraticTo(48f, 36f, 48f, 42f); lineTo(44f, 46f)
        lineTo(40f, 62f); lineTo(34f, 62f); lineTo(34f, 56f); lineTo(30f, 56f); quadraticTo(18f, 60f, 6f, 62f); close()
    }
    private fun collar10() = Path()
    private fun body11() = Path().apply {
        moveTo(6f, 56f); quadraticTo(8f, 44f, 22f, 46f); lineTo(44f, 48f); quadraticTo(54f, 50f, 54f, 58f); lineTo(54f, 62f); lineTo(6f, 62f); close()
    }
    private fun collar11() = Path().apply { moveTo(14f, 52f); lineTo(36f, 52f) }
    private fun body12() = Path().apply {
        moveTo(6f, 50f); quadraticTo(10f, 40f, 24f, 42f); lineTo(40f, 44f); quadraticTo(52f, 46f, 54f, 56f); lineTo(54f, 64f); lineTo(6f, 64f); close()
    }
    private fun collar12() = Path().apply { moveTo(10f, 60f); lineTo(52f, 60f) }

    fun body(shape: Int): Path = when (shape) {
        0 -> body0(); 1 -> body1(); 2 -> body2(); 3 -> body3(); 4 -> body4(); 5 -> body5(); 6 -> body6()
        7 -> body7(); 8 -> body8(); 9 -> body9(); 10 -> body10(); 11 -> body11(); 12 -> body12()
        else -> body0()
    }

    fun collar(shape: Int): Path = when (shape) {
        0 -> collar0(); 1 -> collar1(); 2 -> collar2(); 3 -> collar3(); 4 -> collar4(); 5 -> collar5(); 6 -> collar6()
        7 -> collar7(); 8 -> collar8(); 9 -> collar9(); 10 -> collar10(); 11 -> collar11(); 12 -> collar12()
        else -> collar0()
    }
}
