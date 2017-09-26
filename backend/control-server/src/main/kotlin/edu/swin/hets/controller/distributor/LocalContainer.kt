package edu.swin.hets.controller.distributor

import jade.core.Agent


data class LocalContainer(val name: String, val agents: Map<String, Class<Agent>>)
data class ContainerMap(val containers: Map<String, LocalContainer>)
