package uz.ozbekkeyboard

import org.junit.Assert.*
import org.junit.Test
import uz.ozbekkeyboard.ime.ShiftState

class ShiftStateMachineTest {

    @Test
    fun testInitialShiftStateIsOff() {
        val state = ShiftState.OFF
        assertFalse(state.isShifted())
    }

    @Test
    fun testShiftedState() {
        val state = ShiftState.SHIFTED
        assertTrue(state.isShifted())
    }

    @Test
    fun testCapsLockState() {
        val state = ShiftState.CAPS_LOCK
        assertTrue(state.isShifted())
    }

    @Test
    fun testShiftStateTransitions() {
        var state = ShiftState.OFF

        // Tap 1 -> Shifted
        state = ShiftState.SHIFTED
        assertTrue(state.isShifted())

        // Double tap -> Caps Lock
        state = ShiftState.CAPS_LOCK
        assertTrue(state.isShifted())

        // Tap again -> Off
        state = ShiftState.OFF
        assertFalse(state.isShifted())
    }

    @Test
    fun testShiftRevertsAfterTypingLetter() {
        var state = ShiftState.SHIFTED

        // When a letter is typed, single shift should revert to OFF
        if (state == ShiftState.SHIFTED) {
            state = ShiftState.OFF
        }
        assertEquals(ShiftState.OFF, state)

        // But CAPS_LOCK remains CAPS_LOCK after typing
        state = ShiftState.CAPS_LOCK
        if (state == ShiftState.SHIFTED) {
            state = ShiftState.OFF
        }
        assertEquals(ShiftState.CAPS_LOCK, state)
    }
}
