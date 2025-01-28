package com.melendez.known.colour

data class ColorSpec(
    val chroma: (Double) -> Double = { it },
    val hueShift: (Double) -> Double = { 0.0 }
)
