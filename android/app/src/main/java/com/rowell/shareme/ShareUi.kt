package com.rowell.shareme

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.util.Size
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

val Bg=Color(0xFF0C131E);val Panel=Color(0xFF162232);val Mint=Color(0xFF70E2C2);val Muted=Color(0xFFABBCD0)
@Composable fun ShareTheme(content:@Composable ()->Unit) { MaterialTheme(colorScheme=darkColorScheme(primary=Mint,onPrimary=Bg,background=Bg,surface=Panel,onSurface=Color(0xFFF5F8FF),secondary=Mint,surfaceVariant=Color(0xFF1D2D3F),onSurfaceVariant=Muted,outline=Color(0xFF51677E)),shapes=Shapes(small=RoundedCornerShape(12.dp),medium=RoundedCornerShape(18.dp),large=RoundedCornerShape(24.dp)),content=content) }
@Composable fun CardBox(content:@Composable ColumnScope.()->Unit) { Surface(color=Panel,shape=RoundedCornerShape(20.dp),modifier=Modifier.fillMaxWidth()){Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp),content=content)} }
@Composable fun FileThumb(uri:Uri?,name:String,modifier:Modifier=Modifier.size(52.dp)) {
    val context=LocalContext.current
    var bitmap by remember(uri) { mutableStateOf<Bitmap?>(null) }
    LaunchedEffect(uri) {
        bitmap=if(uri==null)null else withContext(Dispatchers.IO) {
            runCatching { context.contentResolver.loadThumbnail(uri,Size(192,192),null) }.getOrNull()?:
                if(mimeType(name).startsWith("image/"))runCatching {
                    ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver,uri)){decoder,info,_->
                        val scale=192f/maxOf(info.size.width,info.size.height).coerceAtLeast(192);decoder.setTargetSize((info.size.width*scale).toInt().coerceAtLeast(1),(info.size.height*scale).toInt().coerceAtLeast(1))
                    }
                }.getOrNull() else null
        }
    }
    Surface(color=Mint.copy(alpha=.10f),shape=RoundedCornerShape(12.dp),modifier=modifier) {
        val image=bitmap
        if(image!=null)Image(image.asImageBitmap(),null,Modifier.fillMaxSize(),contentScale=ContentScale.Crop)
        else Box(contentAlignment=Alignment.Center){Text(fileType(name).take(5),color=Mint,style=MaterialTheme.typography.labelSmall,fontWeight=FontWeight.Bold)}
    }
}
@Composable fun FileRow(name:String,bytes:Long,uri:Uri?,onOpen:()->Unit,onRemove:(()->Unit)?=null) {
    Surface(color=Panel,shape=RoundedCornerShape(16.dp),modifier=Modifier.fillMaxWidth()) {
        Row(Modifier.clickable(onClick=onOpen).padding(start=12.dp,top=12.dp,bottom=12.dp,end=4.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
            FileThumb(uri,name)
            Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(4.dp)) { Text(name,maxLines=3,overflow=TextOverflow.Ellipsis,style=MaterialTheme.typography.bodyLarge,fontWeight=FontWeight.Medium);Text("${size(bytes)} · ${fileType(name)}",color=Muted,style=MaterialTheme.typography.bodySmall) }
            if(onRemove!=null)IconButton(onClick=onRemove){Icon(Icons.Rounded.Close,"Remove $name from selection",Modifier.size(20.dp),tint=Muted)} else Spacer(Modifier.width(8.dp))
        }
    }
}
@Composable fun TransferBanner(state:ScreenState,onCancel:()->Unit) {
    Surface(color=Panel,tonalElevation=2.dp) {
        Column(Modifier.fillMaxWidth().padding(20.dp,12.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) { Text(if(state.preparing)"Preparing files" else if(state.connection.visible)state.connection.phase.label else "Getting ready",style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.Bold,color=Mint);if(state.connection.visible)Text(state.connection.name,color=Muted,style=MaterialTheme.typography.bodySmall) }
                TextButton(onClick=onCancel){Text("Cancel")}
            }
            if(state.current.isNotEmpty())Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                FileThumb(state.currentUri.takeIf{it.isNotEmpty()}?.let(Uri::parse),state.current,Modifier.size(44.dp))
                Text(state.current,maxLines=3,overflow=TextOverflow.Ellipsis,modifier=Modifier.weight(1f),style=MaterialTheme.typography.bodyMedium)
            }
            if(state.total>0)LinearProgressIndicator(progress={(state.done.toDouble()/state.total).toFloat().coerceIn(0f,1f)},modifier=Modifier.fillMaxWidth())else LinearProgressIndicator(Modifier.fillMaxWidth())
            if(state.fileCount>0)Text("File ${state.fileIndex} of ${state.fileCount} · ${size(state.done)} / ${size(state.total)}",color=Muted,style=MaterialTheme.typography.bodySmall)
            else Text(state.status,color=Muted,style=MaterialTheme.typography.bodySmall)
        }
    }
}
@Composable fun QrCard(text:String,hint:String) {
    val bitmap=remember(text) { val matrix=QRCodeWriter().encode(text,BarcodeFormat.QR_CODE,800,800,mapOf(EncodeHintType.MARGIN to 4));Bitmap.createBitmap(800,800,Bitmap.Config.ARGB_8888).also { image->val pixels=IntArray(800*800){index->if(matrix[index%800,index/800])android.graphics.Color.BLACK else android.graphics.Color.WHITE};image.setPixels(pixels,0,800,0,0,800,800) } }
    Column(Modifier.fillMaxWidth(),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(10.dp)) { Image(bitmap.asImageBitmap(),"Pairing QR code",Modifier.fillMaxWidth(.92f).widthIn(max=320.dp).aspectRatio(1f).clip(RoundedCornerShape(12.dp)).background(Color.White));Text(hint,color=Muted,style=MaterialTheme.typography.bodyMedium) }
}
@Composable fun HotspotControls(state:ScreenState,enabled:Boolean,onStart:()->Unit) {
    if(state.ssid.isNotEmpty())Column(verticalArrangement=Arrangement.spacedBy(4.dp)) { Text("Hotspot is on",color=Mint,fontWeight=FontWeight.SemiBold);SelectionContainer{Text("${state.ssid}\nPassword: ${state.password}",color=Muted,style=MaterialTheme.typography.bodySmall)} }
    else OutlinedButton(onClick=onStart,enabled=enabled,modifier=Modifier.fillMaxWidth()){Icon(Icons.Rounded.Wifi,null,Modifier.size(18.dp));Spacer(Modifier.width(8.dp));Text("No Wi-Fi? Create a hotspot")}
}
fun fileType(name:String)=name.substringAfterLast('.',"FILE").uppercase().take(10)
fun size(bytes:Long):String { var value=bytes.toDouble();val units=arrayOf("B","KB","MB","GB","TB");var index=0;while(value>=1024&&index<4){value/=1024;index++};return if(index==0)"$bytes B" else "%.1f %s".format(value,units[index]) }
