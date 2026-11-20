package com.stupidtree.hita.ui.theta

import android.content.Context
import android.view.View
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.stupidtree.component.data.DataState
import com.stupidtree.hita.api.ComposeUiPlugin
import com.stupidtree.hita.api.PluginUiHost
import com.stupidtree.hita.api.ThetaService
import com.stupidtree.hita.api.ThetaTopic

class ThetaUiPlugin : ComposeUiPlugin {
    override val id = "com.stupidtree.hita.ui.theta"
    override val displayName = "社区"
    override val requiresApi = 7

    override fun createView(context: Context, host: PluginUiHost): View = ComposeView(context).apply {
        setContent { MaterialTheme { ThetaScreen(host.service(ThetaService.KEY)) } }
    }
}

@Composable
private fun ThetaScreen(service: ThetaService?) {
    if (service == null) {
        Text("未安装 theta T1 core", modifier = Modifier.padding(24.dp))
        return
    }
    var topics by remember { mutableStateOf<List<ThetaTopic>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }
    var refresh by remember { mutableStateOf(0) }
    LaunchedEffect(refresh) {
        error = null
        service.topics("hot", 1).observeOnce { state ->
            when (state.state) {
                DataState.STATE.SUCCESS -> topics = state.data.orEmpty()
                else -> error = state.message ?: "加载失败"
            }
        }
    }
    Column(Modifier.fillMaxSize().padding(12.dp)) {
        Button(onClick = { refresh++ }) { Text("刷新") }
        error?.let { Text("错误：$it", Modifier.padding(top = 8.dp)) }
        LazyColumn(Modifier.fillMaxSize().padding(top = 8.dp)) {
            items(topics, key = { it.id }) { topic ->
                Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Column(Modifier.padding(12.dp)) {
                        Text(topic.name, style = MaterialTheme.typography.subtitle1)
                        if (topic.description.isNotBlank()) Text(topic.description, style = MaterialTheme.typography.caption)
                        Text("${topic.articleNum} 篇", style = MaterialTheme.typography.caption)
                    }
                }
            }
        }
    }
}

private fun <T> LiveData<T>.observeOnce(onChanged: (T) -> Unit) {
    val observer = object : Observer<T> {
        override fun onChanged(value: T) { removeObserver(this); onChanged(value) }
    }
    observeForever(observer)
}
