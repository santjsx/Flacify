package com.flacify.music.core.ui.state

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class FlacifyStateTest {

    @Test
    fun flacifyStateData_containsWhatWhyNext() {
        var primaryClicked = false
        var secondaryClicked = false

        val state = FlacifyStateData(
            whatHappened = "Library is Empty",
            whyItHappened = "No music files found in storage",
            primaryActionLabel = "Scan Device",
            onPrimaryAction = { primaryClicked = true },
            secondaryActionLabel = "Import Folder",
            onSecondaryAction = { secondaryClicked = true },
            severity = StateSeverity.INFO
        )

        assertEquals("Library is Empty", state.whatHappened)
        assertEquals("No music files found in storage", state.whyItHappened)
        assertEquals("Scan Device", state.primaryActionLabel)
        assertEquals("Import Folder", state.secondaryActionLabel)
        assertEquals(StateSeverity.INFO, state.severity)

        state.onPrimaryAction?.invoke()
        assertEquals(true, primaryClicked)

        state.onSecondaryAction?.invoke()
        assertEquals(true, secondaryClicked)
    }

    @Test
    fun flacifyStateData_defaultSeverity_isInfo() {
        val state = FlacifyStateData(
            whatHappened = "Offline",
            whyItHappened = "No internet connection"
        )

        assertEquals(StateSeverity.INFO, state.severity)
        assertNull(state.primaryActionLabel)
        assertNull(state.onPrimaryAction)
        assertNull(state.icon)
    }

    @Test
    fun stateSeverity_hasExpectedEnumValues() {
        val values = StateSeverity.entries
        assertEquals(5, values.size)
        assertNotNull(StateSeverity.valueOf("INFO"))
        assertNotNull(StateSeverity.valueOf("SUCCESS"))
        assertNotNull(StateSeverity.valueOf("WARNING"))
        assertNotNull(StateSeverity.valueOf("ERROR"))
        assertNotNull(StateSeverity.valueOf("DESTRUCTIVE"))
    }
}
