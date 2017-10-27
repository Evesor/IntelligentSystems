package edu.swin.hets.controller.gateway

import java.io.Serializable

data class ChangeBehaviourRequest(val agentId: String, val message: String) : Serializable