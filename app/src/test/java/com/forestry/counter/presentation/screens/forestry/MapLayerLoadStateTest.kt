package com.forestry.counter.presentation.screens.forestry

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MapLayerLoadStateTest {

    @Test
    fun `should start requested layer when idle`() {
        val decision = MapLayerLoadState(activeIndex = 0).request(3)

        assertEquals(3, decision.requestToStart?.layerIndex)
        assertTrue(decision.state.isLoading)
        assertEquals(3, decision.state.displayedIndex)
    }

    @Test
    fun `should queue latest request when style is loading`() {
        val first = MapLayerLoadState(activeIndex = 0).request(2)
        val queued = first.state.request(5).state.request(4)

        assertNull(queued.requestToStart)
        assertEquals(4, queued.state.pendingIndex)
        assertEquals(2, queued.state.loadingRequest?.layerIndex)
    }

    @Test
    fun `should start queued layer after current load succeeds`() {
        val first = MapLayerLoadState(activeIndex = 0).request(2)
        val request = requireNotNull(first.requestToStart)
        val queuedState = first.state.request(4).state

        val completed = queuedState.succeed(request.id)

        assertEquals(2, completed.state.activeIndex)
        assertEquals(4, completed.requestToStart?.layerIndex)
        assertTrue(completed.state.isLoading)
    }

    @Test
    fun `should expose failure and unlock picker when load fails`() {
        val started = MapLayerLoadState(activeIndex = 1).request(6)
        val request = requireNotNull(started.requestToStart)

        val failed = started.state.fail(request.id)

        assertFalse(failed.state.isLoading)
        assertEquals(1, failed.state.activeIndex)
        assertEquals(6, failed.state.failedIndex)
        assertNull(failed.requestToStart)
    }

    @Test
    fun `should ignore stale callback from an older request`() {
        val first = MapLayerLoadState(activeIndex = 0).request(2)
        val firstRequest = requireNotNull(first.requestToStart)
        val second = first.state.request(4).state.succeed(firstRequest.id)
        val secondRequest = requireNotNull(second.requestToStart)

        val stale = second.state.fail(firstRequest.id)

        assertEquals(secondRequest, stale.state.loadingRequest)
        assertNull(stale.state.failedIndex)
        assertNull(stale.requestToStart)
    }

    @Test
    fun `should retry failed layer with a new request id`() {
        val started = MapLayerLoadState(activeIndex = 0).request(3)
        val firstRequest = requireNotNull(started.requestToStart)
        val failedState = started.state.fail(firstRequest.id).state

        val retry = failedState.retry()

        assertEquals(3, retry.requestToStart?.layerIndex)
        assertTrue(requireNotNull(retry.requestToStart).id > firstRequest.id)
        assertNull(retry.state.failedIndex)
    }
}
