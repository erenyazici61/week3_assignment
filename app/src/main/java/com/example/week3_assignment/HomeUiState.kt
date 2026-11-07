package com.example.week3_assignment

data class HomeUiState(
    val devices: List<Device> = emptyList(),
    val totalActivePowerWatt: Double = 0.0,
    val searchQuery: String = "",
    val selectedRoom: String = "Tümü",
    val availableRooms: List<String> = emptyList()
)