package com.forestry.counter.domain.location

import org.junit.Assert.*
import org.junit.Test

class WktUtilsTest {

    @Test
    fun `parse POINT with lon lat`() {
        val (lon, lat, alt) = WktUtils.parsePointZ("POINT (2.3488 48.8534)")
        assertEquals(2.3488, lon!!, 0.0001)
        assertEquals(48.8534, lat!!, 0.0001)
        assertNull(alt)
    }

    @Test
    fun `parse POINT Z with lon lat alt`() {
        val (lon, lat, alt) = WktUtils.parsePointZ("POINT Z (2.3488 48.8534 35.5)")
        assertEquals(2.3488, lon!!, 0.0001)
        assertEquals(48.8534, lat!!, 0.0001)
        assertEquals(35.5, alt!!, 0.1)
    }

    @Test
    fun `parse with extra whitespace`() {
        val (lon, lat, _) = WktUtils.parsePointZ("  POINT  (  2.3488   48.8534  )  ")
        assertEquals(2.3488, lon!!, 0.0001)
        assertEquals(48.8534, lat!!, 0.0001)
    }

    @Test
    fun `null input returns nulls`() {
        val (lon, lat, alt) = WktUtils.parsePointZ(null)
        assertNull(lon)
        assertNull(lat)
        assertNull(alt)
    }

    @Test
    fun `blank input returns nulls`() {
        val (lon, lat, alt) = WktUtils.parsePointZ("   ")
        assertNull(lon)
        assertNull(lat)
        assertNull(alt)
    }

    @Test
    fun `invalid WKT returns nulls`() {
        val (lon, lat, alt) = WktUtils.parsePointZ("LINESTRING (0 0, 1 1)")
        assertNull(lon)
        assertNull(lat)
        assertNull(alt)
    }

    @Test
    fun `negative coordinates`() {
        val (lon, lat, _) = WktUtils.parsePointZ("POINT (-0.5792 44.8378)")
        assertEquals(-0.5792, lon!!, 0.0001)
        assertEquals(44.8378, lat!!, 0.0001)
    }
}
