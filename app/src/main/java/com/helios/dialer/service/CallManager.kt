package com.helios.dialer.service

import android.telecom.Call
import android.telecom.CallAudioState
import android.telecom.VideoProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object CallManager {
    private val _currentCall = MutableStateFlow<Call?>(null)
    val currentCall: StateFlow<Call?> = _currentCall.asStateFlow()

    private val _callState = MutableStateFlow(Call.STATE_DISCONNECTED)
    val callState: StateFlow<Int> = _callState.asStateFlow()

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    private val _audioRoute = MutableStateFlow(CallAudioState.ROUTE_EARPIECE)
    val audioRoute: StateFlow<Int> = _audioRoute.asStateFlow()

    private val calls = mutableListOf<Call>()

    private val callback = object : Call.Callback() {
        override fun onStateChanged(call: Call, state: Int) {
            if (call == _currentCall.value) {
                _callState.value = state
            }
            if (state == Call.STATE_DISCONNECTED) {
                removeCall(call)
            }
        }
    }

    fun addCall(call: Call) {
        if (!calls.contains(call)) {
            calls += call
            try {
                call.registerCallback(callback)
            } catch (_: Exception) {
            }
        }
        _currentCall.value = call
        _callState.value = call.state
    }

    fun removeCall(call: Call) {
        try {
            call.unregisterCallback(callback)
        } catch (_: Exception) {
        }
        calls.remove(call)
        val next = calls.lastOrNull()
        _currentCall.value = next
        _callState.value = next?.state ?: Call.STATE_DISCONNECTED
        if (next == null) _isMuted.value = false
    }

    fun setMuted(muted: Boolean) {
        _isMuted.value = muted
        CallService.instance?.setMuteState(muted)
    }

    fun setAudioRoute(route: Int) {
        _audioRoute.value = route
        CallService.instance?.setAudioRouteState(route)
    }

    fun answer() {
        _currentCall.value?.answer(VideoProfile.STATE_AUDIO_ONLY)
    }

    fun reject() {
        _currentCall.value?.reject(false, null)
    }

    fun disconnect() {
        _currentCall.value?.disconnect()
    }

    fun sendDtmf(c: Char) {
        _currentCall.value?.playDtmfTone(c)
    }

    fun stopDtmf() {
        _currentCall.value?.stopDtmfTone()
    }

    fun updateAudioState(state: CallAudioState?) {
        if (state != null) {
            _audioRoute.value = state.route
            _isMuted.value = state.isMuted
        }
    }
}
