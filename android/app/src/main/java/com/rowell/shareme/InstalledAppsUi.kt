package com.rowell.shareme

import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Android
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun InstalledAppsSheet(onDismiss:()->Unit,onChoose:(InstalledApp)->Unit) {
    val context=LocalContext.current
    var apps by remember{mutableStateOf<List<InstalledApp>?>(null)}
    var error by remember{mutableStateOf(false)}
    var search by remember{mutableStateOf("")}
    LaunchedEffect(Unit){runCatching{withContext(Dispatchers.IO){InstalledApps.list(context)}}.onSuccess{apps=it}.onFailure{error=true}}
    ModalBottomSheet(onDismissRequest=onDismiss,containerColor=Bg,sheetState=rememberModalBottomSheetState(skipPartiallyExpanded=true)) {
        Column(Modifier.fillMaxWidth().fillMaxHeight(.9f).padding(horizontal=24.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
            Text("Choose an installed app",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold)
            Text("Sends its installer, including APK parts. Accounts and saves stay on this phone. Some apps need the store or extra downloads.",color=Muted,style=MaterialTheme.typography.bodySmall)
            OutlinedTextField(search,{search=it.take(80)},label={Text("Search apps")},singleLine=true,modifier=Modifier.fillMaxWidth())
            if(error)Text("Couldn't read installed apps. You can choose an APK using Choose files.",color=Muted)
            else if(apps==null)CircularProgressIndicator(Modifier.padding(20.dp).align(Alignment.CenterHorizontally))
            else {
                val filtered=apps.orEmpty().filter{it.name.contains(search,true)||it.packageName.contains(search,true)}
                if(filtered.isEmpty())Text("No matching apps.",color=Muted)
                LazyColumn(Modifier.weight(1f),contentPadding=PaddingValues(bottom=28.dp),verticalArrangement=Arrangement.spacedBy(4.dp)) {
                    items(filtered,key={it.packageName}){app->
                        Row(Modifier.fillMaxWidth().clickable{onChoose(app)}.padding(vertical=12.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                            InstalledAppIcon(app.packageName)
                            Column(Modifier.weight(1f)){Text(app.name,fontWeight=FontWeight.SemiBold);Text("${app.version} · ${size(app.bytes)}${if(app.split)" · APK bundle" else ""}",color=Muted,style=MaterialTheme.typography.bodySmall)}
                            Text("Add",color=Mint,style=MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        }
    }
}
@Composable private fun InstalledAppIcon(packageName:String) {
    val context=LocalContext.current
    val icon by produceState<ImageBitmap?>(null,packageName){value=withContext(Dispatchers.IO){runCatching{context.packageManager.getApplicationIcon(packageName).toBitmap(72,72).asImageBitmap()}.getOrNull()}}
    if(icon!=null)Image(icon!!,null,Modifier.size(40.dp)) else Icon(Icons.Rounded.Android,null,Modifier.size(40.dp),tint=Mint)
}
