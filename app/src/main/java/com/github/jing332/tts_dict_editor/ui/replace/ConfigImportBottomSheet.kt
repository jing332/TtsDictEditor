@file:Suppress("DEPRECATION")

package com.github.jing332.tts_dict_editor.ui.replace

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.drake.net.Net
import com.drake.net.exception.NetException
import com.drake.net.okhttp.trustSSLCertificate
import com.drake.net.utils.withIO
import com.github.jing332.tts_dict_editor.R
import com.github.jing332.tts_dict_editor.app
import com.github.jing332.tts_dict_editor.help.GroupWithReplaceRule
import com.github.jing332.tts_dict_editor.ui.widget.ErrorDialog
import com.github.jing332.tts_dict_editor.ui.widget.LoadingDialog
import com.github.jing332.tts_dict_editor.utils.FileUtils.readAllText
import com.github.jing332.tts_server_android.util.toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import okhttp3.Response
import splitties.systemservices.clipboardManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigImportBottomSheet(
    onImportFromJson: (String) -> Unit,
    initialFilePath: String = "",
    state: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    onDismiss: () -> Unit = {},
) {
    var isVisibleLoadingDialog by remember { mutableStateOf(false) }
    if (isVisibleLoadingDialog)
        LoadingDialog(
            dismissOnBackPress = true,
            onDismissRequest = {
                isVisibleLoadingDialog = false
            },
        )

    val coroutine = rememberCoroutineScope()
    var filePath by remember { mutableStateOf(initialFilePath) }
    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) {
        it?.let {
            filePath = it.toString()
        }
    }
    ModalBottomSheet(
        onDismissRequest = {
            coroutine.launch {
                state.hide()
                onDismiss.invoke()
            }
        }, sheetState = state,
        modifier = Modifier.fillMaxSize()
    ) {
        var url by remember { mutableStateOf("") }
        Column(Modifier.padding(horizontal = 8.dp)) {
            Text(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 8.dp),
                text = stringResource(id = R.string.config_import),
                style = MaterialTheme.typography.displaySmall
            )
            val buttons = listOf(
                stringResource(R.string.clipboard),
                stringResource(R.string.file),
                stringResource(R.string.url)
            )
            var selectedIndex by remember { mutableIntStateOf(0) }

            Row(
                Modifier
                    .wrapContentWidth()
                    .align(Alignment.CenterHorizontally)
            ) {
                buttons.forEachIndexed { index, txt ->
                    Row(
                        Modifier
                            .selectable(
                                index == selectedIndex,
                                onClick = { selectedIndex = index },
                                role = Role.RadioButton
                            )
                            .padding(4.dp)
                    ) {
                        RadioButton(selected = index == selectedIndex, onClick = null)
                        Text(txt)
                    }
                }
            }
            Box(Modifier.fillMaxWidth()) {
                when (selectedIndex) {
                    1 -> {
                        OutlinedTextField(
                            label = { Text(stringResource(id = R.string.file_path)) },
                            trailingIcon = {
                                IconButton(onClick = {
                                    filePicker.launch(arrayOf("text/*", "application/json"))
                                }) {
                                    Icon(
                                        Icons.Filled.FileOpen,
                                        stringResource(id = R.string.select_file),
                                        tint = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                            },
                            value = filePath,
                            onValueChange = { },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    2 -> {
                        OutlinedTextField(
                            label = { Text(stringResource(id = R.string.url)) },
                            value = url,
                            onValueChange = { url = it },
                            modifier = Modifier
                                .fillMaxWidth()
                        )
                    }
                }
            }

            val context = LocalContext.current
            var errorDialog by remember { mutableStateOf<Throwable?>(null) }
            errorDialog?.let {
                ErrorDialog(it) {
                    errorDialog = null
                }
            }

            OutlinedButton(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(vertical = 16.dp),
                onClick = {
                    coroutine.launch(Dispatchers.Main) {
                        isVisibleLoadingDialog = true
                        kotlin.runCatching {
                            onImportFromJson(getJson(selectedIndex, context, url, filePath))
                        }.onFailure {
                            errorDialog = it
                        }
                        isVisibleLoadingDialog = false
                    }
                }) {
                Text(stringResource(id = R.string.config_import))
            }
        }
    }
}

private suspend fun getJson(
    selectedIndex: Int,
    context: Context,
    url: String? = null,
    uri: String? = null
): String {
    return when (selectedIndex) {
        2 -> {
            withIO {
                val resp: Response =
                    Net.get(url!!) { setClient { trustSSLCertificate() } }.execute()
                if (resp.isSuccessful) {
                    resp.body?.string() ?: ""
                } else throw NetException(resp.request, "GET失败, 状态码: ${resp.code}")
            }
        }

        1 -> {
            withIO { Uri.parse(uri).readAllText(context) }
        }

        else -> {
            clipboardManager.text.toString() ?: ""
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun PreviewScreen() {
    ConfigImportBottomSheet(onImportFromJson = {
        app.toast(it)
    })
}

@Composable
fun ConfigImportSelectDialog(
    groupWithRules: List<GroupWithReplaceRule>,
    onConfirm: (List<GroupWithReplaceRule>) -> Unit,
    onDismissRequest: () -> Unit
) {
    val selectedList =
        remember { mutableStateListOf(*groupWithRules.flatMap { it.list }.toTypedArray()) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        text = {
            LazyColumn {
                for (gwr in groupWithRules) {
                    items(gwr.list, key = { it.id }) {
                        val index = selectedList.indexOf(it)
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = index != -1,
                                    onClick = {
                                        if (index == -1) selectedList.add(it)
                                        else selectedList.removeAt(index)
                                    },
                                    role = Role.Checkbox,
                                    indication = rememberRipple(bounded = true),
                                    interactionSource = remember { MutableInteractionSource() }
                                )
                                .padding(vertical = 4.dp)
                        ) {
                            Checkbox(
                                checked = index != -1,
                                onCheckedChange = null,
                                modifier = Modifier.align(Alignment.CenterVertically)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(it.name, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    gwr.group.name,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                modifier = Modifier.padding(horizontal = 4.dp),
                onClick = {
                    val list = mutableListOf<GroupWithReplaceRule>()
                    for (rule in selectedList) {
                        val gInAll = groupWithRules.find { it.group.id == rule.groupId } ?: continue
                        val gIndexInList = list.indexOfFirst { it.group.id == gInAll.group.id }

                        val index = if (gIndexInList > -1) {
                            gIndexInList
                        } else {
                            list.add(GroupWithReplaceRule(gInAll.group, mutableListOf()))
                            list.size - 1
                        }
                        list[index].let { gwr ->
                            (gwr.list as MutableList).add(rule)
                        }
                    }

                    onConfirm.invoke(list)
                }
            ) {
                Text(stringResource(id = R.string.confirm))
            }
        }
    )
}

@Preview
@Composable
fun PreviewSelectDialog() {
    var isVisible by remember { mutableStateOf(true) }

    val json =
        """[{"group":{"id":1,"name":"默认分组"},"list":[{"id":207,"name":"李朝歌➡️李zhāo歌","pattern":"李朝歌","replacement":"李钊歌","order":6}]},{"group":{"id":1676833236984,"name":"字词_正则","order":1},"list":[{"id":101,"groupId":1676833236984,"name":"㊣ 长➡️zhǎng","isEnabled":false,"isRegex":true,"pattern":"(?<!头发)长(?=公主|见识|[孙老者相房势史幼官膘进子兄])|(?<=董事|[生学成家连道师队族首市科州助兄厂机屯村])长(?!头发|乐公|[期久])","replacement":"掌","order":9},{"id":102,"groupId":1676833236984,"name":"㊣ 参➡️shēn","isEnabled":false,"isRegex":true,"pattern":"(?<=(高丽|百济|新罗|西洋|[山老卖的党海白杏沙丹人]))(参)(?!考)|(参)(?=[商茶](?!考))","replacement":"身","order":5},{"id":106,"groupId":1676833236984,"name":"㊣ 拨➡️bō","isEnabled":false,"isRegex":true,"pattern":"(拨)(?=(过去|电话|出电话|号码|出号码|[通号]))","replacement":"波","order":20},{"id":109,"groupId":1676833236984,"name":"㊣ 率➡️shuài","isEnabled":false,"isRegex":true,"pattern":"(?<=[内副左右卫])(率)","replacement":"帅","order":32},{"id":203,"groupId":1676833236984,"name":"㊣ 东都➡️东dū","isEnabled":false,"isRegex":true,"pattern":"(?<!北.)(?<=[东首旧京])都","replacement":"督","order":41},{"id":204,"groupId":1676833236984,"name":"㊣ 着➡️zhuó","isEnabled":false,"isRegex":true,"pattern":"着(?=边际|[陆装眼笔色墨力想意落甲])|(?<=穿红|[穿附])着","replacement":"浊","order":16},{"id":208,"groupId":1676833236984,"name":"㊣ 着➡️zháo","isEnabled":false,"isRegex":true,"pattern":"着(?=[边凉急忙风迷火])|(?<=上不|下不|躺下就)着","replacement":"zháo","order":27},{"id":213,"groupId":1676833236984,"name":"㊣ 令狐➡️lìng狐","isEnabled":false,"isRegex":true,"pattern":"令狐(?=[大少]侠|[公小]子|大[人少]|冲)","replacement":"另狐","order":42},{"id":1675908651030,"groupId":1676833236984,"name":"㊣ 重➡️chóng","isEnabled":false,"isRegex":true,"pattern":"(?<!隆)重(?=归|回|来)|重(?=八(?!公斤|千克|[斤两克吨打]))","replacement":"虫","order":7},{"id":1676742652751,"groupId":1676833236984,"name":"㊣ 石➡️dàn","isEnabled":false,"isRegex":true,"pattern":"(?<=[零一二三四五六七八九十百千万0-9]{1,15})石(?!中间|之间|[零一二三四五六七八九十百千万]{1,15}鸟|击起|激起|[斛头块子片山峰缝间])","replacement":"旦","order":14},{"id":1676744280696,"groupId":1676833236984,"name":"㊣ 石➡️shí","isEnabled":false,"isRegex":true,"pattern":"石(?=中间|之间|[零一二三四五六七八九十百千万]{1,15}鸟|击起|激起|[斛头块子片山峰缝间])","replacement":"时","order":15},{"id":1676744878357,"groupId":1676833236984,"name":"㊣ 重重➡️chóng⺀","isEnabled":false,"isRegex":true,"pattern":"重重(?=叠叠|大山|山峦|山峰|大雾|云雾|迷雾|树影|人影|鬼影|包围|杀机|心事|隐患|危险|危机|危难|疑问|疑点|机关|因素)|(?<=大山|山峦|山峰|大雾|云雾|迷雾|树影|人影|鬼影|困难|杀机|危难|机关|心事|隐患|危险|危机|疑问|疑点|谍影)重重(?![的地放打拍抽扇推拉踢踹压踩撞跌磕跪摔扔拽丢往向朝把咬咳哼])","replacement":"虫虫","order":3},{"id":1676748266328,"groupId":1676833236984,"name":"㊣ 大夫➡️dài夫 ① (和下一条规则配合避免误替换)","isEnabled":false,"isRegex":true,"pattern":"(?<!中散|光禄|谏议|[士卿乡遂朝冢公官中])大夫(?!人|妻|妇)","replacement":"带夫","order":17},{"id":1676748343273,"groupId":1676833236984,"name":"㊣ 大夫➡️dà夫 ② ","isEnabled":false,"isRegex":true,"pattern":"大夫(?=人|妻|妇)|(?<=中散|光禄|谏议|[士卿乡遂朝冢公官中])大夫","replacement":"眔夫","order":21},{"id":1676829025129,"groupId":1676833236984,"name":"㊣ 行➡️xíng","isEnabled":false,"isRegex":true,"pattern":"(行)(?=大礼|万里路|[至到了侠驶商])|(?<=任我|一路同)(行)","replacement":"型","order":28},{"id":1676829663851,"groupId":1676833236984,"name":"㊣ 行➡️háng","isEnabled":false,"isRegex":true,"pattern":"(?<!(一路|一起).?)(?<=典当|老本|排成|排成[零一二三四五六七八九十百千万]{1,15}|[茶牙车马银排在懂内同铁])行(?!至|到|驶|动)|行(?=[家列伍货当会规话])","replacement":"航","order":29},{"id":1676830868943,"groupId":1676833236984,"name":"㊣ 了➡️liǎo","isEnabled":false,"isRegex":true,"pattern":"了(?=无生趣|得)|(?<=之事一|受不)了","replacement":"鄝","order":35},{"id":1676831702652,"groupId":1676833236984,"name":"㊣ 重重➡️zhòng⺀","isEnabled":false,"isRegex":true,"pattern":"(?<!大山|山峦|山峰|大雾|云雾|迷雾|树影|人影|鬼影|困难|杀机|危难|机关|心事|隐患|危险|危机|疑问|疑点|谍影)重重(?!叠叠|大山|山峦|山峰|大雾|云雾|迷雾|树影|人影|鬼影|包围|杀机|心事|隐患|危险|危机|危难|疑问|疑点|机关|因素)(?=奖赏|有赏|[地放打拍抽扇推拉踢踹压踩撞跌磕跪摔扔拽丢往向朝把咬咳哼])","replacement":"众众"},{"id":1676832129519,"groupId":1676833236984,"name":"㊣ 还➡️huán","isEnabled":false,"isRegex":true,"pattern":"还(?=治其人)|(?<=迟早要|一日)还(?!是|要|想)","replacement":"环","order":31},{"id":1676832438865,"groupId":1676833236984,"name":"㊣ 差➡️chāi","isEnabled":false,"isRegex":true,"pattern":"(?!误)差(?=服|事|役)|(?<=出.{0,3}|外)差(?!错)","replacement":"钗","order":24},{"id":1676832716808,"groupId":1676833236984,"name":"㊣ 差➡️chā","isEnabled":false,"isRegex":true,"pattern":"差(?=错)|(?<=误)差","replacement":"插","order":19},{"id":1676833240316,"groupId":1676833236984,"name":"㊣ 贾➡️gǔ","isEnabled":false,"isRegex":true,"pattern":"(?<=余勇可|[商大巨书富市行善])贾","replacement":"股","order":38},{"id":1676833540496,"groupId":1676833236984,"name":"㊣ 难➡️nàn","isEnabled":false,"isRegex":true,"pattern":"难(?=民(?!众))|(?<=多灾多|遭.{0,3}|经.{0,3}|逃过.{0,3}|[患落磨责逃避发罹])难","replacement":"婻","order":25},{"id":1676834061999,"groupId":1676833236984,"name":"㊣ 难➡️nuó","isEnabled":false,"isRegex":true,"pattern":"难(?=[戏舞])|(?<=佩玉之)难","replacement":"傩","order":26},{"id":1676834947646,"groupId":1676833236984,"name":"㊣ 好➡️hào","isEnabled":false,"isRegex":true,"pattern":"好(?=这一口|这口|事者|面子|管闲事|[胜客恶战施色])","replacement":"耗","order":36},{"id":1676835770198,"groupId":1676833236984,"name":"㊣ 长➡️cháng","isEnabled":false,"isRegex":true,"pattern":"长(?=相思|相忆|乐公主|[期久])|(?<=头发|[非所])长","replacement":"肠","order":37},{"id":1676835912252,"groupId":1676833236984,"name":"㊣ 长长➡️zhǎng⺀","isEnabled":false,"isRegex":true,"pattern":"长长(?=记性|见识|眼)","replacement":"掌掌","order":18},{"id":1676836793259,"groupId":1676833236984,"name":"㊣ 重➡️zhòng","isEnabled":false,"isRegex":true,"pattern":"重(?=打[一二三四五六七八九十]{1,2}|谢)|(?<=孰轻孰)重","replacement":"众","order":13},{"id":1676836990969,"groupId":1676833236984,"name":"㊣ 没➡️mò","isEnabled":false,"isRegex":true,"pattern":"没(?=齿(?!了))|(?<=[辱淹沉])没","replacement":"莫","order":34},{"id":1676837389725,"groupId":1676833236984,"name":"㊣ 地➡️dì","isEnabled":false,"isRegex":true,"pattern":"地(?=位(?![置阶子次于居号序分]))|(?<=不怨|[平田])地","replacement":"蒂","order":23},{"id":1676837633366,"groupId":1676833236984,"name":"㊣ 否➡️pǐ","isEnabled":false,"isRegex":true,"pattern":"否(?=[泰臧极德性运妇])|(?<=[臧泰贤])否","replacement":"痞","order":39},{"id":1676864016630,"groupId":1676833236984,"name":"㊣ 称➡️chèn","isEnabled":false,"isRegex":true,"pattern":"称(?=心如意|[意早心愿职手托])|(?<=[匀对相])称","replacement":"趁","order":40},{"id":1676864445802,"groupId":1676833236984,"name":"㊣ 兴➡️xìng","isEnabled":false,"isRegex":true,"pattern":"(?<=扫.{0,6}|[助即游败雅])兴","replacement":"幸","order":33},{"id":1676943722075,"groupId":1676833236984,"name":"㊣ 重重的➡️zhòng⺀的 ① (和下一条规则配合避免误替换)","isEnabled":false,"isRegex":true,"pattern":"重重的(?!大山|山峦|山峰|大雾|云雾|迷雾|树影|人影|鬼影|包围|杀机|心事|隐患|危险|危机|危难|疑问|疑点|机关)(?=奖赏|有赏|[放打拍抽扇推拉踢踹压踩撞跌磕跪摔扔拽丢往向朝把咬咳])","replacement":"众众的","order":2},{"id":1676944464103,"groupId":1676833236984,"name":"㊣ 重重的➡️chóng⺀的 ② (配合上一条，避免误替换)","isEnabled":false,"isRegex":true,"pattern":"重重的(?!奖赏|有赏|[放打拍抽扇推拉踢踹压踩撞跌磕跪摔扔拽丢往向朝把咬咳])(?=大山|山峦|山峰|大雾|云雾|迷雾|树影|人影|鬼影|包围|杀机|心事|隐患|危险|危机|危难|疑问|疑点|机关)","replacement":"虫虫的","order":43},{"id":1676944650284,"groupId":1676833236984,"name":"㊣ 重重的➡️zhòng⺀的","isEnabled":false,"isRegex":true,"pattern":"重重的(?=奖赏|有赏|[放打拍抽扇推拉踢踹压踩撞跌磕跪摔扔拽丢往向朝把咬咳])","replacement":"众众地","order":12},{"id":1677222511683,"groupId":1676833236984,"name":"㊣ 将➡️jiàng","isEnabled":false,"isRegex":true,"pattern":"将(?=才)|(?<=[大麻])将(?!军)","replacement":"犟","order":10},{"id":1677222980643,"groupId":1676833236984,"name":"㊣ 将➡️jiāng","isEnabled":false,"isRegex":true,"pattern":"将(?=军)","replacement":"江","order":4},{"id":1677258103597,"groupId":1676833236984,"name":"㊣ 朝➡️cháo","isEnabled":false,"isRegex":true,"pattern":"朝(?=[拜臣代房服纲贡见觐门圣廷向阳野政])|(?<=坐北|[隋唐宋元明清])朝","replacement":"巢","order":6},{"id":1677259147253,"groupId":1676833236984,"name":"㊣ 朝➡️zhāo","isEnabled":false,"isRegex":true,"pattern":"朝(?=不保夕|发夕至|晖夕阴|秦暮楚|三暮四|生暮死|思暮想|闻夕改|令夕改|[露暮气夕霞阳菌晖思])|(?<=[今])朝(?=有酒)","replacement":"钊","order":22},{"id":1677274868605,"groupId":1676833236984,"name":"㊣ 圈➡️juàn","isEnabled":false,"isRegex":true,"pattern":"圈(?=[牢舍养])|(?<=[牛羊马猪])圈","replacement":"倦","order":11},{"id":1677275431431,"groupId":1676833236984,"name":"㊣ 圈➡️juān","isEnabled":false,"isRegex":true,"pattern":"圈(?=起来|[闭禁住])","replacement":"捐","order":1},{"id":1677286031332,"groupId":1676833236984,"name":"㊣ 便➡️pián","isEnabled":false,"isRegex":true,"pattern":"便(?=宜(?!行事))","replacement":"骈","order":30},{"id":1677286072375,"groupId":1676833236984,"name":"㊣ 便便➡️pián⺀","isEnabled":false,"isRegex":true,"pattern":"(?<=大肚|大腹)便便","replacement":"骈骈","order":8}]},{"group":{"id":1676831573484,"name":"符号_对话处理识别","order":2},"list":[{"id":104,"groupId":1676831573484,"name":"㊣  儿➡️ér （减少不必要的儿化音）","isEnabled":false,"isRegex":true,"pattern":"(?<!跳高|叫好|口罩|绝着|口哨|蜜枣|鱼漂|火苗|跑调|面条|豆角|开窍|衣兜|小偷|门口|纽扣|线轴|小丑|顶牛|抓阄|棉球|加油|火锅|做活|大伙|邮戳|小说|被窝|耳膜|粉末|打盹|胖墩|砂轮|冰棍|没准|开春|小瓮|瓜子|刀把|号码|戏法|在哪|找茬|打杂|板擦|名牌|鞋带|壶盖|小孩|加塞|快板|老伴|蒜瓣|脸盘|脸蛋|收摊|栅栏|包干|笔杆|门槛|药方|赶趟|香肠|瓜瓤|掉价|一下|豆芽|小辫|照片|扇面|差点|一点|雨点|聊天|拉链|冒尖|坎肩|牙签|露馅|心眼|鼻梁|透亮|花样|脑瓜|大褂|麻花|笑话|牙刷|一块|茶馆|饭馆|火罐|落款|打转|拐弯|好玩|大腕|蛋黄|打晃|天窗|烟卷|手绢|出圈|包圆|人缘|绕远|杂院|刀背|摸黑|老本|花盆|嗓门|把门|哥们|纳闷|后跟|别针|一阵|走神|大婶|杏仁|刀刃|钢镚|夹缝|脖颈|提成|半截|小鞋|旦角|主角|跑腿|一会|耳垂|墨水|围嘴|走味|抽空|酒盅|小葱|小熊|红包|灯泡|半道|手套|石子|没词|挑刺|墨汁|锯齿|记事|针鼻|垫底|肚脐|玩意|有劲|送信|脚印|花瓶|打鸣|图钉|门铃|眼镜|蛋清|火星|人影|毛驴|小曲|痰盂|合群|模特|逗乐|唱歌|挨个|打嗝|饭盒|碎步|没谱|儿媳妇|梨核|泪珠|有数|果冻|门洞|胡同|小人|一会|[头今明])儿(?=[，砸子女孙化媳童郎背语家拜竖婿稚息])|(?<=[\\u4e00-\\u9fa5])(?<!跳高|叫好|口罩|绝着|口哨|蜜枣|鱼漂|火苗|跑调|面条|豆角|开窍|衣兜|小偷|门口|纽扣|线轴|小丑|顶牛|抓阄|棉球|加油|火锅|做活|大伙|邮戳|小说|被窝|耳膜|粉末|打盹|胖墩|砂轮|冰棍|没准|开春|小瓮|瓜子|刀把|号码|戏法|在哪|找茬|打杂|板擦|名牌|鞋带|壶盖|小孩|加塞|快板|老伴|蒜瓣|脸盘|脸蛋|收摊|栅栏|包干|笔杆|门槛|药方|赶趟|香肠|瓜瓤|掉价|一下|豆芽|小辫|照片|扇面|差点|一点|雨点|聊天|拉链|冒尖|坎肩|牙签|露馅|心眼|鼻梁|透亮|花样|脑瓜|大褂|麻花|笑话|牙刷|一块|茶馆|饭馆|火罐|落款|打转|拐弯|好玩|大腕|蛋黄|打晃|天窗|烟卷|手绢|出圈|包圆|人缘|绕远|杂院|刀背|摸黑|老本|花盆|嗓门|把门|哥们|纳闷|后跟|别针|一阵|走神|大婶|杏仁|刀刃|钢镚|夹缝|脖颈|提成|半截|小鞋|旦角|主角|跑腿|一会|耳垂|墨水|围嘴|走味|抽空|酒盅|小葱|小熊|红包|灯泡|半道|手套|石子|没词|挑刺|墨汁|锯齿|记事|针鼻|垫底|肚脐|玩意|有劲|送信|脚印|花瓶|打鸣|图钉|门铃|眼镜|蛋清|火星|人影|毛驴|小曲|痰盂|合群|模特|逗乐|唱歌|挨个|打嗝|饭盒|碎步|没谱|儿媳妇|梨核|泪珠|有数|果冻|门洞|胡同|小人|一会|[头今明])儿","replacement":"而","order":3},{"id":111,"groupId":1676831573484,"name":"㊣ 非对白“”➡️‘’","isEnabled":false,"isRegex":true,"pattern":"([\\u4e00-\\u9fa5])“([\\u4e00-\\u9fa5]{1,10})”([\\u4e00-\\u9fa5])","replacement":"${'$'}1'${'$'}2'${'$'}3","order":5},{"id":1110,"groupId":1676831573484,"name":"㊣ xxx➡️某某某","isEnabled":false,"isRegex":true,"pattern":"([\\u4e00-\\u9fa5])(xxx|XXX)([\\u4e00-\\u9fa5])","replacement":"${'$'}1某某某${'$'}3"},{"id":1676825121912,"groupId":1676831573484,"name":"(?<=：|:|\\s)\"","isEnabled":false,"isRegex":true,"pattern":"(?<=：|:|\\s)\"","replacement":"“","order":2},{"id":1676825155783,"groupId":1676831573484,"name":"(?<=。|……|......|？|！|\\?|\\!)\"","isEnabled":false,"isRegex":true,"pattern":"(?<=。|……|......|？|！|\\?|\\!)\"","replacement":"”","order":4},{"id":1676827194503,"groupId":1676831573484,"name":"-","isEnabled":false,"pattern":"-","order":6},{"id":1676862126964,"groupId":1676831573484,"name":"\\s(?!“|\")","isEnabled":false,"isRegex":true,"pattern":"\\s(?!“|\")","order":1}]},{"group":{"id":1676832325672,"name":"字词_非正则","order":3},"list":[{"id":201,"groupId":1676832325672,"name":"佛然➡️fú然","isEnabled":false,"pattern":"佛然","replacement":"弗然","order":5},{"id":205,"groupId":1676832325672,"name":"倒很➡️dào很","isEnabled":false,"pattern":"倒很","replacement":"dào很","order":12},{"id":214,"groupId":1676832325672,"name":"没了➡️méi了","isEnabled":false,"pattern":"没了","replacement":"眉了","order":11},{"id":222,"groupId":1676832325672,"name":"咳➡️ké","isEnabled":false,"pattern":"咳","replacement":"翗","order":13},{"id":1676832329531,"groupId":1676832325672,"name":"目的地➡️目的dì","isEnabled":false,"pattern":"目的地","replacement":"目的弟","order":1},{"id":1676833092243,"groupId":1676832325672,"name":"朝朝➡️zhāo⺀","isEnabled":false,"pattern":"朝朝","replacement":"钊钊","order":4},{"id":1676834535269,"groupId":1676832325672,"name":"通传➡️通chuán","isEnabled":false,"pattern":"通传","replacement":"通船","order":6},{"id":1676834653395,"groupId":1676832325672,"name":"神教➡️神jiào","isEnabled":false,"pattern":"神教","replacement":"神轿","order":7},{"id":1676834764804,"groupId":1676832325672,"name":"考中➡️考zhòng","isEnabled":false,"pattern":"考中","replacement":"考众","order":8},{"id":1676834804515,"groupId":1676832325672,"name":"干的➡️gān的","isEnabled":false,"pattern":"干的","replacement":"肝的","order":9},{"id":1676835375612,"groupId":1676832325672,"name":"过分➡️过fèn","isEnabled":false,"pattern":"过分","replacement":"过份","order":10},{"id":1676835484349,"groupId":1676832325672,"name":"东躲西藏➡️东躲西cáng","isEnabled":false,"pattern":"东躲西藏","replacement":"东躲西鑶","order":2},{"id":1677272021392,"groupId":1676832325672,"name":"了了➡️liǎo⺀","isEnabled":false,"pattern":"了了","replacement":"鄝鄝","order":3},{"id":1677272117832,"groupId":1676832325672,"name":"一了百了➡️一liǎo百liǎo","isEnabled":false,"pattern":"一了百了","replacement":"一鄝百鄝"},{"id":1678150182671,"groupId":1676832325672,"name":"彟➡️yuē","isEnabled":false,"pattern":"彟","replacement":"曰","order":14}]}]"""
    val items = Json.decodeFromString<List<GroupWithReplaceRule>>(json)

    if (isVisible)
        ConfigImportSelectDialog(
            groupWithRules = items,
            onConfirm = {
                app.toast(it.size.toString())
                isVisible = false
            }, onDismissRequest = {
                isVisible = false
            }
        )
}