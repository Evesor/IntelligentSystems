package edu.swin.hets.controller.distributor

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class ContainerDistributorTest {
    @BeforeEach
    fun setUp() {
    }

    @Test
    fun defaultContainerDefinitionShouldBeValid() {
        assert(validateSystemDefinition(ContainerDistributor.DEFAULT_CONTAINER_CONFIGURATION))
    }
}