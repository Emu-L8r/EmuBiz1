package com.emul8r.bizap.ui.gui3.components.effects

import androidx.compose.ui.graphics.Color
import timber.log.Timber
import kotlin.math.random.Random

/**
 * Particle: Reusable data object for GPU particle effects
 *
 * Mutable to support object pooling (reduce GC pressure)
 */
data class Particle(
    var x: Float = 0f,
    var y: Float = 0f,
    var vx: Float = 0f,
    var vy: Float = 0f,
    var alpha: Float = 1f,
    var size: Float = 4f,
    var color: Color = Color.Green,
    var lifespan: Int = 255,
    var isActive: Boolean = false
)

/**
 * ParticlePool: Object pool for GPU particle rendering
 *
 * Pre-allocates N particles upfront, reuses them in a loop.
 * Eliminates garbage collection during animation (critical for 60 FPS).
 *
 * Usage:
 *   val pool = ParticlePool(500)
 *   val p = pool.acquire()
 *   if (p != null) {
 *       p.x = 100f; p.y = 200f
 *       p.isActive = true
 *   }
 *   // Later, when particle dies:
 *   pool.release(p)
 */
class ParticlePool(initialCapacity: Int = 500) {
    private val particles = Array(initialCapacity) { Particle() }
    private var activeCount = 0

    init {
        Timber.d("ParticlePool: Initialized with capacity=$initialCapacity")
    }

    /**
     * Acquire an inactive particle from the pool
     * Returns null if pool is exhausted
     */
    fun acquire(): Particle? {
        for (i in particles.indices) {
            if (!particles[i].isActive) {
                particles[i].isActive = true
                activeCount++
                return particles[i]
            }
        }
        // Pool exhausted
        if (activeCount % 100 == 0) {
            Timber.w("ParticlePool exhausted: $activeCount/$capacity particles active")
        }
        return null
    }

    /**
     * Release a particle back to the pool (deactivate it)
     */
    fun release(particle: Particle) {
        if (particle.isActive) {
            particle.isActive = false
            activeCount--
        }
    }

    /**
     * Get all active particles
     */
    fun getActive(): List<Particle> {
        return particles.filter { it.isActive }
    }

    /**
     * Clear all particles from the pool
     */
    fun clear() {
        particles.forEach { it.isActive = false }
        activeCount = 0
        Timber.d("ParticlePool cleared")
    }

    /**
     * Get count of active particles
     */
    fun getActiveCount() = activeCount

    /**
     * Get pool capacity
     */
    fun getCapacity() = particles.size

    /**
     * Get utilization percentage
     */
    fun getUtilizationPercent() = (activeCount * 100) / particles.size
}

/**
 * Randomize a particle for emission
 */
fun Particle.randomize(width: Int, height: Int) {
    x = Random.nextFloat() * width
    y = -10f  // Start above screen
    vx = (Random.nextFloat() - 0.5f) * 2f
    vy = Random.nextFloat() * 3f + 2f
    alpha = Random.nextFloat() * 0.8f + 0.2f
    size = Random.nextFloat() * 2f + 2f
    lifespan = 255
    isActive = true
}

