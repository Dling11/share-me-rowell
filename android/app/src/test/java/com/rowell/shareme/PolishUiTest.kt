package com.rowell.shareme

import android.graphics.Bitmap
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
    @Before fun reset(){compose.runOnIdle{val existingId=ProfileStore.load(compose.activity).id;Model.set{ScreenState(profile=profile.copy(id=existingId),uiVisible=true)}}}
    @Test fun profileEditorSavesNameAndHairAndSurvivesReload(){
        compose.onNodeWithContentDescription("Edit your name and avatar").performClick()
        compose.onNodeWithText("Your name").performTextReplacement("Rowell Mode")
        compose.onNodeWithText("Girl").performScrollTo().performClick()
        compose.onNodeWithText("Girl").assertIsSelected()
        compose.onNodeWithText("Long").performScrollTo().performClick()
        capture("profile-editor")
        compose.onNodeWithText("Surprise me").performScrollTo().performClick()
        compose.onNodeWithText("Girl").assertIsSelected()
        compose.onNodeWithText("Long").performScrollTo().performClick()
        compose.onNodeWithText("Save my look").performScrollTo().performClick()
        compose.runOnIdle{assertEquals("Rowell Mode",Model.value.profile.name);assertEquals(3,Model.value.profile.avatar.hair);assertEquals(1,Model.value.profile.avatar.look);assertEquals(Model.value.profile,ProfileStore.load(compose.activity))}
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
        compose.onNodeWithText("Choose installed app").assertIsDisplayed()
        capture("send")
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
