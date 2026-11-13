package com.rowell.shareme

import android.graphics.Bitmap
import android.content.Intent
import android.net.Uri
import android.graphics.Canvas
import android.view.View
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.util.ReflectionHelpers
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[35],qualifiers="w390dp-h844dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class PolishUiTest {
    @get:Rule val compose=createAndroidComposeRule<MainActivity>()
    private val profile=ShareProfile("12345678-abcd","Rowell",AvatarStyle(4,1,0,0,1))
    @Before fun reset(){compose.runOnIdle{val existingId=ProfileStore.load(compose.activity).id;Model.set{ScreenState(profile=profile.copy(id=existingId),uiVisible=true,avatarAnimations=false)}}}
    @Test fun signatureCharacterHidesCustomizationAndCanSwitchBack(){
        compose.runOnIdle{
            Model.set{it.copy(journey=AdventurerJourney(Adventure.threshold(100)))}
            ProfileStore.save(compose.activity,Model.value.profile.copy(avatar=profile.avatar))
            ProfileStore.save(compose.activity,Model.value.profile.copy(avatar=profile.avatar.copy(role=8)))
        }
        compose.onNodeWithContentDescription("Edit your name and avatar").performClick()
        compose.onNodeWithText("Signature character · fixed design").assertIsDisplayed()
        compose.onNodeWithText("Hair").assertDoesNotExist()
        compose.onNodeWithText("Girl").assertDoesNotExist()
        compose.onNodeWithText("Surprise me").assertDoesNotExist()
        capture("fixed-character")
        compose.onNodeWithText("Your nearby name").performTextReplacement("Teacher device")
        assertEquals("Teacher device",ProfileStore.load(compose.activity).name)
        compose.onNodeWithText("Choose a customizable character").performScrollTo().performClick()
        compose.onNodeWithText("Equip character").performClick()
        assertEquals(profile.avatar,ProfileStore.load(compose.activity).avatar)
    }
    @Test fun profileEditorSavesNameAndHairAndSurvivesReload(){
        compose.onNodeWithContentDescription("Edit your name and avatar").performClick()
        compose.onNodeWithText("Your nearby name").performTextReplacement("Rowell Mode")
        compose.onNodeWithText("Girl").performScrollTo().performClick()
        compose.onNodeWithText("Girl").assertIsSelected()
        compose.onNodeWithText("Long").performScrollTo().performClick()
        capture("profile-editor")
        compose.onNodeWithText("Surprise me").performScrollTo().performClick()
        compose.onNodeWithText("Girl").assertIsSelected()
        compose.onNodeWithText("Long").performScrollTo().performClick()
        compose.onNodeWithText("Done").performClick()
        compose.runOnIdle{assertEquals("Rowell Mode",Model.value.profile.name);assertEquals(3,Model.value.profile.avatar.hair);assertEquals(1,Model.value.profile.avatar.look);assertEquals(Model.value.profile,ProfileStore.load(compose.activity))}
    }
    @Test fun customizationIsPersistedBeforeDoneAndLockedCharactersOnlyPreview(){
        compose.onNodeWithContentDescription("Edit your name and avatar").performClick()
        compose.onNodeWithText("Your nearby name").performTextReplacement("Auto saved")
        compose.onNodeWithText("Girl").performScrollTo().performClick()
        compose.runOnIdle{assertEquals("Auto saved",ProfileStore.load(compose.activity).name);assertEquals(1,ProfileStore.load(compose.activity).avatar.look)}
        compose.onNodeWithText("Characters").performClick()
        compose.onNodeWithContentDescription("Rune Mage, locked").performClick()
        compose.onNodeWithText("Unlocks at Lv. 8").assertIsDisplayed()
        compose.onNodeWithText("Equip character").assertDoesNotExist()
        compose.runOnIdle{assertEquals(0,ProfileStore.load(compose.activity).avatar.role);assertEquals(0L,Model.value.journey.xp)}
        capture("characters-locked")
        compose.activity.onBackPressedDispatcher.onBackPressed()
        compose.waitForIdle()
        compose.runOnIdle{assertEquals("Auto saved",ProfileStore.load(compose.activity).name)}
    }
    @Test fun unlockedCharacterCanBeEquippedAndAllRanksPreviewed(){
        compose.runOnIdle{Model.set{it.copy(journey=AdventurerJourney(Adventure.threshold(80)))}}
        compose.onNodeWithContentDescription("Edit your name and avatar").performClick()
        compose.onNodeWithText("Characters").performClick()
        compose.onNodeWithContentDescription("Heavenly Seraph").performScrollTo().performClick()
        compose.onNodeWithText("Equip character").performClick()
        compose.runOnIdle{assertEquals(5,ProfileStore.load(compose.activity).avatar.role)}
        capture("seraph-character")
        compose.onNodeWithText("Ranks").performClick()
        compose.onNodeWithText("God-tier").performScrollTo().assertIsDisplayed()
        capture("rank-collection")
    }
    @Test @Config(qualifiers="w320dp-h640dp-xhdpi") fun profileDoneAndTabsFitSmallPhone(){
        compose.onNodeWithContentDescription("Edit your name and avatar").performClick()
        compose.onNodeWithText("Done").assertIsDisplayed()
        compose.onNodeWithText("Characters").assertIsDisplayed()
        compose.onNodeWithText("Ranks").assertIsDisplayed()
        capture("small-profile")
    }
    @Test fun autosaveUpdatesQrNameWithoutResettingTheListenerOrInterruptedTransfer(){
        val pairing=Pairing(listOf("127.0.0.1"),48390,"a".repeat(64),"example".repeat(6),"receive",profile.name)
        compose.runOnIdle{Model.set{it.copy(pairing=pairing,mode="receive",connection=PeerConnection.begin("Other phone").advance(ConnectionPhase.FAILED))}}
        compose.onNodeWithContentDescription("Edit your name and avatar").performClick()
        compose.onNodeWithText("Your nearby name").performTextReplacement("New name")
        compose.onNodeWithText("Done").performClick()
        compose.runOnIdle{
            assertEquals(pairing.copy(name="New name"),Model.value.pairing)
            assertEquals(ConnectionPhase.FAILED,Model.value.connection.phase)
            assertNull(org.robolectric.Shadows.shadowOf(compose.activity.application).nextStartedService)
        }
    }
    @Test fun nearbyPickerDisplaysAvatarsAndKeepsQrAvailable(){
        val peer=NearbyPeer("87654321-abcd","Cozy Mochi",AvatarStyle(3,3,2,2,1,look=1),"192.168.1.23",48390,"abcdef0123456789".repeat(4))
        compose.runOnIdle{Model.set{it.copy(files=listOf(Prepared(FileMeta("sample","Weekend notes.txt",12345,"a".repeat(64)),Uri.parse("content://example/document"))),nearbyPeers=listOf(peer))}}
        compose.onNodeWithText("Choose receiver · 1 nearby").performClick()
        assertEquals("browse",org.robolectric.Shadows.shadowOf(compose.activity.application).nextStartedService?.action)
        compose.onNodeWithText("Cozy Mochi").assertIsDisplayed()
        capture("nearby-receivers")
        compose.onNodeWithText("Scan QR · phone or PC").assertIsDisplayed()
    }
    @Test fun bothNearbyApprovalsRequireTheMatchingTransferCode(){
        for(sending in listOf(true,false)){
            val request=Approval("Cozy Mochi",listOf(FileMeta("sample","Weekend notes.txt",12345,"a".repeat(64))),sending,deviceCode="ABCD EF01 2345 6789",avatar=AvatarStyle())
            compose.runOnIdle{Model.set{it.copy(approval=request,busy=true,connection=PeerConnection.begin("Cozy Mochi",ConnectionPhase.APPROVAL),verificationCode=request.deviceCode)}}
            val confirm=compose.onAllNodes(hasText(if(sending)"Send" else "Receive") and hasClickAction()).onLast()
            confirm.assertIsNotEnabled()
            compose.onNodeWithText("ABCD EF01 2345 6789").assertIsDisplayed()
            compose.onNode(isToggleable()).performClick()
            confirm.assertIsEnabled()
            assertFalse(request.answer.isDone)
        }
    }
    @Test fun sendScreenKeepsInstalledAppsAndQrAvailable(){
        compose.onNodeWithText("Choose files").assertIsDisplayed()
        compose.onNodeWithText("Send apps").assertIsDisplayed()
        capture("send")
    }
    @Test fun interruptedSelectionOffersResumeAndKeepsTheAppPicker(){
        compose.runOnIdle{Model.set{it.copy(mode="send",files=listOf(Prepared(FileMeta("one","notes.txt",20,"a".repeat(64)),Uri.parse("content://files/one"))),connection=PeerConnection.begin("Other phone").advance(ConnectionPhase.FAILED))}}
        compose.onNodeWithText("Retry / resume").performScrollTo().performClick()
        compose.onNodeWithText("Who are we sending to?").assertIsDisplayed()
        compose.onNodeWithText("Scan QR · phone or PC").assertIsDisplayed()
        assertEquals(1,Model.value.files.size)
    }
    @Test fun sharingIntoARunningAppPreservesSelectionUntilConfirmed(){
        val shared=Uri.parse("content://external.files/second")
        compose.runOnIdle{
            Model.set{it.copy(files=listOf(Prepared(FileMeta("one","notes.txt",20,"a".repeat(64)),Uri.parse("content://files/one"))))}
            compose.activity.onNewIntent(Intent(Intent.ACTION_SEND).setType("application/pdf").putExtra(Intent.EXTRA_STREAM,shared))
        }
        compose.onNodeWithText("Add shared files?").assertIsDisplayed()
        assertEquals(1,Model.value.files.size)
        compose.onNodeWithText("Add files").performClick()
        val service=org.robolectric.Shadows.shadowOf(compose.activity.application).nextStartedService!!
        assertEquals("prepare",service.action)
        assertEquals(listOf(shared.toString()),service.getStringArrayListExtra("uris"))
        assertFalse(service.getBooleanExtra("replace_selection",false))
        assertEquals(shared,service.clipData!!.getItemAt(0).uri)
    }
    @Test fun incomingShareWaitsForActiveTransferAndCanBeDismissed(){
        compose.runOnIdle{
            Model.set{it.copy(busy=true,mode="receive",connection=PeerConnection.begin("Other phone",ConnectionPhase.RECEIVING))}
            compose.activity.onNewIntent(Intent(Intent.ACTION_SEND).setType("image/png").putExtra(Intent.EXTRA_STREAM,Uri.parse("content://external.files/photo")))
        }
        compose.onNodeWithText("Shared files are waiting. Finish or cancel this transfer first.").assertIsDisplayed()
        assertTrue(Model.value.busy)
        assertEquals(ConnectionPhase.RECEIVING,Model.value.connection.phase)
        compose.onNodeWithText("Dismiss").performClick()
        assertNull(org.robolectric.Shadows.shadowOf(compose.activity.application).nextStartedService)
    }
    @Test @Config(qualifiers="w320dp-h640dp-xhdpi") fun receiveShowsIdentityAndComparisonOnASmallPhone(){
        compose.runOnIdle{Model.set{it.copy(pairing=Pairing(listOf("127.0.0.1"),48390,"abcdef0123456789".repeat(4),"example".repeat(6),"receive",profile.name),nearbyVisible=true)}}
        compose.onNodeWithText("Receive").performClick()
        compose.onNodeWithText("Rowell").assertIsDisplayed()
        val qr=compose.onNodeWithContentDescription("Pairing QR code").fetchSemanticsNode().boundsInRoot
        val navigation=compose.onNodeWithText("Recent").fetchSemanticsNode().boundsInRoot
        assertTrue("QR must fit above bottom navigation",qr.bottom<navigation.top-40*compose.activity.resources.displayMetrics.density)
        capture("receive-example")
    }
    @Test fun splitInstallationDoesNotOfferAnIncompleteOfflineApk(){
        compose.runOnIdle{compose.activity.applicationInfo.splitSourceDirs=arrayOf("/example/split.apk")}
        compose.onNodeWithContentDescription("More options").performClick()
        compose.onNodeWithText("Share Mode installer").performClick()
        compose.onNodeWithText("Share Play Store link").assertIsDisplayed()
        compose.onNodeWithText("Share complete APK bundle").assertIsDisplayed()
        compose.onNodeWithText("Show local download QR").assertDoesNotExist()
    }
    private fun capture(name:String){
        val folder=File(checkNotNull(System.getProperty("sharemode.screenshots"))).apply{mkdirs()}
        // PixelCopy waits for a real display frame; render the native view tree in Robolectric.
        compose.runOnIdle {
            val global=ReflectionHelpers.callStaticMethod<Any>(Class.forName("android.view.WindowManagerGlobal"),"getInstance")
            val views=ReflectionHelpers.getField<List<View>>(global,"mViews")
            val view=views.lastOrNull{it.width>0&&it.height>0}?:compose.activity.window.decorView
            val bitmap=Bitmap.createBitmap(view.width,view.height,Bitmap.Config.ARGB_8888)
            view.draw(Canvas(bitmap))
            File(folder,"$name.png").outputStream().use{bitmap.compress(Bitmap.CompressFormat.PNG,100,it)}
        }
    }
}
