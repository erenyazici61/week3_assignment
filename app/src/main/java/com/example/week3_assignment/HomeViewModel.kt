package com.example.week3_assignment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class HomeViewModel : ViewModel() {

    private val allRooms = "Tümü"

    // ödevde verilen 10 cihazlık veri seti
    private val initialDevices = listOf(
        Device(1, "Köşe Lambası", DeviceCategory.LIGHT, "Salon", isOnline = true, isActive = true, consumptionWatt = 15.0),
        Device(2, "Tavan Avizesi", DeviceCategory.LIGHT, "Salon", isOnline = true, isActive = false, consumptionWatt = 60.0),
        Device(3, "Bahçe Kamerası", DeviceCategory.CAMERA, "Bahçe", isOnline = true, isActive = true, consumptionWatt = 8.5),
        Device(4, "Giriş Kamerası", DeviceCategory.CAMERA, "Antre", isOnline = true, isActive = true, consumptionWatt = 8.5),
        Device(5, "Merkezi Termostat", DeviceCategory.THERMOSTAT, "Salon", isOnline = true, isActive = true, consumptionWatt = 12.0),
        Device(6, "Yatak Odası Kliması", DeviceCategory.THERMOSTAT, "Yatak Odası", isOnline = true, isActive = false, consumptionWatt = 1200.0),
        Device(7, "Mutfak Spotları", DeviceCategory.LIGHT, "Mutfak", isOnline = true, isActive = false, consumptionWatt = 45.0),
        Device(8, "Roborock S7", DeviceCategory.VACUUM, "Koridor", isOnline = true, isActive = true, consumptionWatt = 50.0),
        Device(9, "Balkon Aydınlatması", DeviceCategory.LIGHT, "Balkon", isOnline = false, isActive = false, consumptionWatt = 25.0),
        Device(10, "Çalışma Odası Lambası", DeviceCategory.LIGHT, "Ofis", isOnline = true, isActive = true, consumptionWatt = 12.0)
    )

    private val _rawDevices = MutableStateFlow(initialDevices)
    private val _searchQuery = MutableStateFlow("")
    private val _selectedRoom = MutableStateFlow(allRooms)

    private val availableRooms: List<String> =
        listOf(allRooms) + initialDevices.map { it.room }.distinct()

    val uiState: StateFlow<HomeUiState> = combine(
        _rawDevices,
        _searchQuery,
        _selectedRoom
    ) { devices, query, room ->
        // sadece online ve aktif cihazların tüketimi toplanır
        val totalPower = devices
            .filter { it.isOnline && it.isActive }
            .sumOf { it.consumptionWatt }

        // arama ve oda filtresi
        val filteredList = devices.filter { device ->
            val matchesQuery = device.name.contains(query.trim(), ignoreCase = true)
            val matchesRoom = room == allRooms || device.room == room
            matchesQuery && matchesRoom
        }

        HomeUiState(
            devices = filteredList,
            totalActivePowerWatt = totalPower,
            searchQuery = query,
            selectedRoom = room,
            availableRooms = availableRooms
        )
    }
        .flowOn(Dispatchers.Default) // hesaplar arka thread'de
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HomeUiState(
                devices = initialDevices,
                totalActivePowerWatt = initialDevices
                    .filter { it.isOnline && it.isActive }
                    .sumOf { it.consumptionWatt },
                availableRooms = availableRooms
            )
        )

    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun onRoomSelected(room: String) {
        _selectedRoom.value = room
    }

    fun toggleDeviceState(deviceId: Int) {
        _rawDevices.update { list ->
            list.map { device ->
                if (device.id == deviceId) device.copy(isActive = !device.isActive) else device
            }
        }
    }
}