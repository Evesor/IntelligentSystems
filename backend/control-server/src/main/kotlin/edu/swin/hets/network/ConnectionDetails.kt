package edu.swin.hets.network

import java.net.InetAddress
data class ConnectionDetails(val name: String, val address: InetAddress, val privateKey: String) {
    constructor(name: String, address: String, privateKey: String) : this(name, InetAddress.getByName(address), privateKey)
}