package com.forestry.counter.presentation.coachmark

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CoachMarkControllerTest {

    @Test
    fun should_start_at_first_step_when_tour_is_pending() {
        val controller = CoachMarkController()

        controller.start()

        assertTrue(controller.isActive)
        assertEquals(0, controller.stepIndex)
        assertEquals(COACH_MARK_STEPS.first(), controller.currentStep)
    }

    @Test
    fun should_finish_after_last_step_when_next_is_called() {
        val controller = CoachMarkController()
        controller.start()

        repeat(COACH_MARK_STEPS.size) { controller.next() }

        assertFalse(controller.isActive)
        assertNull(controller.stepIndex)
        assertNull(controller.currentStep)
    }

    @Test
    fun should_ignore_next_when_tour_is_inactive() {
        val controller = CoachMarkController()

        controller.next()

        assertFalse(controller.isActive)
        assertNull(controller.currentStep)
    }

    @Test
    fun should_stop_immediately_when_tour_is_skipped() {
        val controller = CoachMarkController()
        controller.start()

        controller.stop()

        assertFalse(controller.isActive)
        assertNull(controller.currentStep)
    }

    @Test
    fun should_resume_when_pending_tour_was_interrupted() {
        assertTrue(
            shouldStartCoachMarkTour(
                pending = true,
                completed = false,
                isTopLevel = true,
                isActive = false,
            )
        )
    }

    @Test
    fun should_not_restart_when_tour_is_completed() {
        assertFalse(
            shouldStartCoachMarkTour(
                pending = true,
                completed = true,
                isTopLevel = true,
                isActive = false,
            )
        )
    }

    @Test
    fun should_wait_for_top_level_screen_before_starting() {
        assertFalse(
            shouldStartCoachMarkTour(
                pending = true,
                completed = false,
                isTopLevel = false,
                isActive = false,
            )
        )
    }
}
