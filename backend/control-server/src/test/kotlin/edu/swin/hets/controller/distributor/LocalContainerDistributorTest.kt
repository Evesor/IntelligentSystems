package edu.swin.hets.controller.distributor

import edu.swin.hets.agent.ApplianceAgent
import edu.swin.hets.agent.HomeAgent
import extension.mock
import jade.core.Profile
import jade.core.Runtime
import jade.wrapper.ContainerController
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when`

internal class LocalContainerDistributorTest {
    private val profile: Profile = mock<Profile>()
    private val containerController: ContainerController = mock<ContainerController>()
    private val systemDefinition: SystemDefinition = mock<SystemDefinition>()
    private val jadeRuntime: Runtime = mock<Runtime>()
    private lateinit var localContainerDistributor: LocalContainerDistributor

    @BeforeEach
    fun before() {
        `when`(jadeRuntime.createAgentContainer(Mockito.any())).thenReturn(containerController)
    }

    private val invalidContainerDefinition = ContainerDefinition(
            "invalidContainer",
            listOf(
                    AgentDefinition("asdf", HomeAgent::class.java.name),
                    AgentDefinition(name = "asdfg", className = ApplianceAgent::class.java.name, owner = "asdfg")
            )
    )

    @Test
    fun startUpAgentsShouldStartupOnValidSystemDefinition() {
        localContainerDistributor = setupDistributor(SystemDefinition(listOf()))
        localContainerDistributor.startUpAgents(containerController, ContainerDefinition("testContainer", listOf()))
    }

    private fun setupDistributor(systemDefinition: SystemDefinition): LocalContainerDistributor {
        return LocalContainerDistributor(jadeRuntime, systemDefinition)
    }
}