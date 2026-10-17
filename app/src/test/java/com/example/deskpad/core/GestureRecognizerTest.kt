package com.example.deskpad.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GestureRecognizerTest {

    private fun feed(r: GestureRecognizer, vararg frames: PadFrame): List<GestureAction> =
        frames.flatMap { r.onFrame(it) }

    @Test fun singleTapIsLeftClick() {
        val out = feed(
            GestureRecognizer(),
            PadFrame(PadPhase.DOWN, 1, 100f, 100f, 0),
            PadFrame(PadPhase.UP, 1, 102f, 101f, 80),
        )
        assertEquals(listOf(GestureAction.LeftClick), out)
    }

    @Test fun oneFingerDragEmitsMoveNotClick() {
        val out = feed(
            GestureRecognizer(),
            PadFrame(PadPhase.DOWN, 1, 100f, 100f, 0),
            PadFrame(PadPhase.MOVE, 1, 140f, 100f, 16),
            PadFrame(PadPhase.UP, 1, 140f, 100f, 40),
        )
        assertTrue(out.any { it is GestureAction.Move })
        assertFalse(out.contains(GestureAction.LeftClick))
    }

    @Test fun twoFingerTapIsRightClick() {
        val out = feed(
            GestureRecognizer(),
            PadFrame(PadPhase.DOWN, 1, 100f, 100f, 0),
            PadFrame(PadPhase.DOWN, 2, 100f, 100f, 10),
            PadFrame(PadPhase.UP, 0, 101f, 101f, 60),
        )
        assertEquals(listOf(GestureAction.RightClick), out)
    }

    @Test fun twoFingerDragIsScroll() {
        val out = feed(
            GestureRecognizer(),
            PadFrame(PadPhase.DOWN, 1, 100f, 100f, 0),
            PadFrame(PadPhase.DOWN, 2, 100f, 100f, 10),
            PadFrame(PadPhase.MOVE, 2, 100f, 130f, 26),
        )
        assertTrue(out.any { it is GestureAction.Scroll })
    }

    @Test fun framesBeforeDownAreIgnored() {
        val r = GestureRecognizer()
        assertTrue(r.onFrame(PadFrame(PadPhase.MOVE, 1, 10f, 10f, 0)).isEmpty())
        assertTrue(r.onFrame(PadFrame(PadPhase.UP, 1, 10f, 10f, 10)).isEmpty())
    }

    @Test fun tapThenHoldDragEmitsDragSequence() {
        val out = feed(
            GestureRecognizer(),
            PadFrame(PadPhase.DOWN, 1, 100f, 100f, 0),
            PadFrame(PadPhase.UP, 1, 100f, 100f, 60),
            PadFrame(PadPhase.DOWN, 1, 100f, 100f, 120),
            PadFrame(PadPhase.MOVE, 1, 150f, 100f, 140),
            PadFrame(PadPhase.UP, 1, 150f, 100f, 180),
        )
        assertTrue(out.contains(GestureAction.DragStart))
        assertTrue(out.any { it is GestureAction.DragMove })
        assertTrue(out.contains(GestureAction.DragEnd))
    }

    @Test fun threeFingerTapTogglesScrollMode() {
        val out = feed(
            GestureRecognizer(),
            PadFrame(PadPhase.DOWN, 1, 100f, 100f, 0),
            PadFrame(PadPhase.DOWN, 2, 100f, 100f, 10),
            PadFrame(PadPhase.DOWN, 3, 100f, 100f, 20),
            PadFrame(PadPhase.UP, 3, 101f, 101f, 80),
        )
        assertEquals(listOf(GestureAction.ToggleScrollMode), out)
    }

    @Test fun scrollModeOneFingerSlideScrollsInsteadOfMoving() {
        val r = GestureRecognizer().apply { scrollMode = true }
        val out = feed(
            r,
            PadFrame(PadPhase.DOWN, 1, 100f, 100f, 0),
            PadFrame(PadPhase.MOVE, 1, 100f, 140f, 16),
            PadFrame(PadPhase.MOVE, 1, 100f, 170f, 32),
            PadFrame(PadPhase.UP, 1, 100f, 170f, 48),
        )
        assertTrue(out.isNotEmpty())
        assertTrue(out.all { it is GestureAction.Scroll })
    }

    @Test fun scrollModeTapStillClicks() {
        val r = GestureRecognizer().apply { scrollMode = true }
        val out = feed(
            r,
            PadFrame(PadPhase.DOWN, 1, 100f, 100f, 0),
            PadFrame(PadPhase.UP, 1, 101f, 100f, 80),
        )
        assertEquals(listOf(GestureAction.LeftClick), out)
    }

    @Test fun scrollModeTapThenSlideScrollsRatherThanDrags() {
        val r = GestureRecognizer().apply { scrollMode = true }
        val out = feed(
            r,
            PadFrame(PadPhase.DOWN, 1, 100f, 100f, 0),
            PadFrame(PadPhase.UP, 1, 100f, 100f, 60),
            PadFrame(PadPhase.DOWN, 1, 100f, 100f, 120),
            PadFrame(PadPhase.MOVE, 1, 100f, 150f, 140),
            PadFrame(PadPhase.UP, 1, 100f, 150f, 180),
        )
        assertFalse(out.contains(GestureAction.DragStart))
        assertTrue(out.any { it is GestureAction.Scroll })
    }
}
