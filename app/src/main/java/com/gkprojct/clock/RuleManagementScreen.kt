package com.gkprojct.clock

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gkprojct.clock.vm.RuleEntity
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RuleManagementScreen(
    onBackClick: () -> Unit,
    onAddRuleClick: () -> Unit,
    onRuleClick: (Rule) -> Unit,
    ruleViewModel: RuleViewModel
) {
    val context = LocalContext.current
    val contentResolver = context.contentResolver
    val scope = rememberCoroutineScope()
    val rules: List<RuleEntity> by ruleViewModel.allRules.collectAsState(initial = emptyList())

    val gson = remember {
        GsonBuilder()
            .registerTypeAdapter(RuleCriteria::class.java, RuleCriteriaAdapter())
            .registerTypeAdapter(RuleAction::class.java, RuleActionAdapter())
            .create()
    }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
        onResult = { uri: Uri? ->
            uri?.let {
                try {
                    contentResolver.openOutputStream(it)?.use { outputStream ->
                        val jsonString = gson.toJson(rules)
                        outputStream.write(jsonString.toByteArray())
                    }
                    Toast.makeText(context, "Rules exported successfully", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    )

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            uri?.let {
                try {
                    contentResolver.openInputStream(it)?.use { inputStream ->
                        val jsonString = inputStream.reader().readText()
                        val typeToken = object : TypeToken<List<RuleEntity>>() {}.type
                        val importedRules: List<RuleEntity> = gson.fromJson(jsonString, typeToken)

                        scope.launch {
                            importedRules.forEach { ruleEntity ->
                                val newRule = Rule(
                                    id = UUID.randomUUID(), // new ID
                                    name = ruleEntity.name,
                                    description = ruleEntity.description,
                                    enabled = ruleEntity.enabled,
                                    targetAlarmIds = ruleEntity.targetAlarmIds,
                                    calendarIds = ruleEntity.calendarIds,
                                    criteria = ruleEntity.criteria,
                                    action = ruleEntity.action
                                )
                                ruleViewModel.saveRule(newRule)
                            }
                        }
                    }
                    Toast.makeText(context, "Rules imported successfully", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Import failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("管理规则") },
                navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.Filled.ArrowBack, contentDescription = "返回") } },
                actions = {
                    IconButton(onClick = { exportLauncher.launch("rules.json") }) {
                        Icon(Icons.Default.Upload, contentDescription = "导出规则")
                    }
                    IconButton(onClick = { importLauncher.launch(arrayOf("application/json")) }) {
                        Icon(Icons.Default.Download, contentDescription = "导入规则")
                    }
                    IconButton(onClick = onAddRuleClick) {
                        Icon(Icons.Filled.Add, contentDescription = "添加规则")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize().padding(horizontal = 16.dp)) {
            if (rules.isEmpty()) {
                Text("您还没有创建任何规则。")
                Spacer(Modifier.height(16.dp))
                Button(onClick = onAddRuleClick) { Text("添加第一条规则") }
            } else {
                Text(
                    text = "已定义的规则:",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Divider()
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(rules, key = { it.id }) { ruleEntity ->
                        val rule = Rule(
                            id = ruleEntity.id,
                            name = ruleEntity.name,
                            description = ruleEntity.description,
                            enabled = ruleEntity.enabled,
                            targetAlarmIds = ruleEntity.targetAlarmIds,
                            calendarIds = ruleEntity.calendarIds,
                            criteria = ruleEntity.criteria,
                            action = ruleEntity.action
                        )
                        RuleItem(rule = rule, onClick = { onRuleClick(rule) })
                        Divider()
                    }
                }
            }
        }
    }
}

@Composable
fun RuleItem(rule: Rule, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
            Text(text = rule.name, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Text(text = rule.description, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            Text(
                text = "影响 ${rule.targetAlarmIds.size} 个闹钟 | 使用 ${rule.calendarIds.size} 个日历 | 条件: ${rule.criteria.toSummaryString()}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = if (rule.enabled) "启用" else "禁用",
                fontSize = 14.sp,
                color = if (rule.enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "编辑规则 ${rule.name}",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RuleManagementScreenPreview() {
    // Preview code remains the same
}
