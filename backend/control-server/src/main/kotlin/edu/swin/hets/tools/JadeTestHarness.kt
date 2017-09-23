package edu.swin.hets.tools

import edu.swin.hets.Application
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * For lack of a proper testing environment
 */
class JADETestHarness {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(JADETestHarness::class.java)
    }
}

fun main(args: Array<String>) {
    val harness = JADETestHarness()
}