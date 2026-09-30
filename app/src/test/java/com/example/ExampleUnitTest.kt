package com.example

import com.example.data.service.CentralOpsService
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun routineThreatGeneration_returnsValidData() {
        val routineThreat = CentralOpsService.generateRoutineAttack()
        assertNotNull(routineThreat)
        assertTrue(routineThreat.designation.isNotBlank())
        assertTrue(routineThreat.sector.isNotBlank())
        assertTrue(routineThreat.latitude > 0.0)
        assertTrue(routineThreat.longitude > 0.0)
        assertTrue(routineThreat.launchOrigin.isNotBlank())
    }
}
