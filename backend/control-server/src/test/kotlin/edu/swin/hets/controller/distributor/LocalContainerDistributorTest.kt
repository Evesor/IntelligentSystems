package edu.swin.hets.controller.distributor

import edu.swin.hets.agent.ApplianceAgent
import edu.swin.hets.agent.HomeAgent
import extension.mock
import jade.core.Profile
import jade.core.Runtime
import jade.wrapper.AgentContainer
import jade.wrapper.AgentController
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.*

internal class LocalContainerDistributorTest {
    private val profile: Profile = mock<Profile>()
    private val containerController: AgentContainer = mock<AgentContainer>()
    private val agentController: AgentController = mock<AgentController>()
    private val jadeRuntime: Runtime = mock<Runtime>()
    private lateinit var localContainerDistributor: LocalContainerDistributor

    @BeforeEach
    fun before() {
        `when`(jadeRuntime.createAgentContainer(Mockito.any())).thenReturn(containerController)
        `when`(containerController.createNewAgent(any(), any(), any())).thenReturn(agentController)
        localContainerDistributor = setupDistributor(SystemDefinition(listOf()))
    }

    @Test
    fun shouldStartUpAgentsOnSingleContainer() {
        localContainerDistributor.startUpAgents(validContainerDefinition)
        val numberOfAgents = numberOfAgents(validContainerDefinition)

        verify(containerController, times(numberOfAgents)).createNewAgent(any(), any(), any())
        verify(agentController, times(numberOfAgents)).start()
    }

    @Test
    fun shouldThrowAnErrorWhenApplianceOwnerDoesNotExist() {
        assertThrows(IllegalStateException::class.java, {
            localContainerDistributor.startUpAgents(containerWithInvalidOwner)
        })
    }

    @Test
    fun shouldThrowAnErrorWhenApplianceIsMissingOwner() {
        assertThrows(IllegalStateException::class.java, {
            localContainerDistributor.startUpAgents(containerWithMissingOwner)
        })
    }

    private fun setupDistributor(systemDefinition: SystemDefinition): LocalContainerDistributor {
        return LocalContainerDistributor(jadeRuntime, systemDefinition)
    }

    private fun numberOfAgents(systemDefinition: SystemDefinition): Int {
        return systemDefinition.containers.
                fold(0) { acc, containerDefinition -> acc + containerDefinition.agents.size }
    }

    private val validContainerDefinition = SystemDefinition(
            listOf(
                    ContainerDefinition(
                            "validContainer",
                            listOf(
                                    AgentDefinition("home1", HomeAgent::class.java.name),
                                    AgentDefinition(name = "appliance1", className = ApplianceAgent::class.java.name, owner = "home1")
                            ))
            )
    )

    private val containerWithMissingOwner = SystemDefinition(
            listOf(
                    ContainerDefinition(
                            "invalidContainer",
                            listOf(
                                    AgentDefinition("home1", HomeAgent::class.java.name),
                                    AgentDefinition(name = "appliance1", className = ApplianceAgent::class.java.name)
                            ))
            )
    )

    private val containerWithInvalidOwner = SystemDefinition(
            listOf(
                    ContainerDefinition(
                            "invalidContainer",
                            listOf(
                                    AgentDefinition("home1", HomeAgent::class.java.name),
                                    AgentDefinition(name = "appliance1", className = ApplianceAgent::class.java.name, owner = "nonexistentHome")
                            ))
            )
    )

    private val containerDefinition = ContainerDefinition(
            "invalidContainer", listOf()
    )

}