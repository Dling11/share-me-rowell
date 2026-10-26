package com.rowell.shareme

import org.junit.Assert.*
import org.junit.Test

class PeerConnectionTest{
    @Test fun disconnectWinsOverLateProgressAndCompletion(){
        val session=PeerConnection.begin("Rowell PC").advance(ConnectionPhase.SENDING).disconnect()
        assertEquals(ConnectionPhase.DISCONNECTED,session.advance(ConnectionPhase.VERIFYING).advance(ConnectionPhase.FINISHED).phase)
        assertFalse(session.phase.active)
    }
    @Test fun aSocketCloseAfterVerifiedCompletionKeepsTheFinishedOutcome(){
        val session=PeerConnection.begin("POCO F5").advance(ConnectionPhase.RECEIVING).advance(ConnectionPhase.FINISHED)
        assertEquals(ConnectionPhase.FINISHED,session.advance(ConnectionPhase.FAILED).disconnect().phase)
    }
    @Test fun decliningNeverLeavesAConnectedBadge(){
        val session=PeerConnection.begin("Phone",ConnectionPhase.APPROVAL).advance(ConnectionPhase.DECLINED)
        assertEquals(ConnectionPhase.DECLINED,session.advance(ConnectionPhase.FAILED).phase)
        assertFalse(session.phase.active)
    }
    @Test fun freshPairingReplacesThePreviousDeviceAndTerminalStatus(){
        val old=PeerConnection.begin("First phone").advance(ConnectionPhase.FAILED)
        val next=PeerConnection.begin("Second phone")
        assertFalse(old.phase.active);assertTrue(next.phase.active)
        assertEquals("Second phone",next.name);assertEquals(ConnectionPhase.CONNECTING,next.phase)
    }
    @Test fun deviceNamesCannotInjectLinesOrChangeTextDirection(){
        assertEquals("MyPC",PeerConnection.begin("\nMy\u202ePC\t").name)
        assertEquals("Nearby device",PeerConnection.begin("\n").name)
        assertEquals(64,PeerConnection.begin("x".repeat(1000)).name.length)
    }
}
