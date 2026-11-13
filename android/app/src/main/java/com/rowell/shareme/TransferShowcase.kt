package com.rowell.shareme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable fun TransferAvatarShowcase(state:ScreenState,showProgress:Boolean=false) {
    val sending=state.connection.phase==ConnectionPhase.SENDING || state.mode=="send"
    val edge=if(LocalConfiguration.current.screenHeightDp<680)52.dp else 64.dp
    val moving=state.avatarAnimations&&state.uiVisible
    @Composable fun Person(name:String,appearance:PeerAppearance?,modifier:Modifier) {
        Surface(modifier=modifier,color=Bg,shape=PixelCorners,border=BorderStroke(1.dp,Mint.copy(alpha=.28f))) {
            Column(Modifier.padding(6.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(2.dp)) {
                if(appearance!=null)ChibiAvatar(appearance.avatar,Modifier.size(edge),"$name avatar. Tap to animate.",appearance.level,animated=moving,interactive=true)
                else Box(Modifier.size(edge),contentAlignment=Alignment.Center){Text("?",color=Muted,style=MaterialTheme.typography.headlineSmall)}
                Text(name,maxLines=1,overflow=TextOverflow.Ellipsis,fontWeight=FontWeight.Medium,style=MaterialTheme.typography.labelMedium)
                Text(appearance?.let{"Lv. ${it.level}"}?:"Device avatar unavailable",maxLines=1,color=Muted,style=MaterialTheme.typography.labelSmall)
            }
        }
    }
    val own=PeerAppearance(state.profile.avatar,state.journey.level)
    Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(8.dp)) {
        Person(if(sending)"You" else state.connection.name,if(sending)own else state.peerAppearance,Modifier.width(edge+12.dp))
        Column(Modifier.weight(1f),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(6.dp)) {
            if(showProgress){
                val fraction=if(state.total>0)(state.done.toDouble()/state.total).toFloat().coerceIn(0f,1f)else null
                Text(fraction?.let{"${(it*100).toInt()}%"}?:"…",color=Mint,fontWeight=FontWeight.Bold,style=MaterialTheme.typography.titleLarge)
                if(fraction!=null)LinearProgressIndicator(progress={fraction},modifier=Modifier.fillMaxWidth())else LinearProgressIndicator(Modifier.fillMaxWidth())
                if(state.fileCount>0)Text("File ${state.fileIndex} of ${state.fileCount}",style=MaterialTheme.typography.labelSmall,color=Muted,textAlign=TextAlign.Center)
                if(state.total>0)Text("${size(state.done)} / ${size(state.total)}",style=MaterialTheme.typography.labelSmall,color=Muted,textAlign=TextAlign.Center)
            }else Text(if(state.connection.phase==ConnectionPhase.FINISHED)"✓" else "→",color=Mint,style=MaterialTheme.typography.titleLarge)
        }
        Person(if(sending)state.connection.name else "You",if(sending)state.peerAppearance else own,Modifier.width(edge+12.dp))
    }
}
