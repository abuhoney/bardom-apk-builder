package com.example

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                BardomApp()
            }
        }
    }
}

// التبويبات المتاحة بالتطبيق
enum class Tab {
    DASHBOARD,
    CODE_VIEWER,
    SIMULATOR
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BardomApp() {
    var selectedTab by remember { mutableStateOf(Tab.DASHBOARD) }
    
    // قيم افتراضية للمستخدم لإنشاء الكود المخصص له
    var botToken by remember { mutableStateOf("") }
    var githubToken by remember { mutableStateOf("") }
    var githubRepo by remember { mutableStateOf("") }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == Tab.DASHBOARD,
                    onClick = { selectedTab = Tab.DASHBOARD },
                    icon = { Icon(Icons.Filled.Dashboard, contentDescription = "Dashboard") },
                    label = { Text("اللوحة والإرشادات", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
                )
                NavigationBarItem(
                    selected = selectedTab == Tab.CODE_VIEWER,
                    onClick = { selectedTab = Tab.CODE_VIEWER },
                    icon = { Icon(Icons.Filled.Code, contentDescription = "Code") },
                    label = { Text("الأكواد الجاهزة", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
                )
                NavigationBarItem(
                    selected = selectedTab == Tab.SIMULATOR,
                    onClick = { selectedTab = Tab.SIMULATOR },
                    icon = { Icon(Icons.Filled.Terminal, contentDescription = "Simulator") },
                    label = { Text("محاكي التجميع", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Crossfade(targetState = selectedTab, label = "TabTransition") { tab ->
                when (tab) {
                    Tab.DASHBOARD -> DashboardScreen(
                        botToken = botToken,
                        onBotTokenChange = { botToken = it },
                        githubToken = githubToken,
                        onGithubTokenChange = { githubToken = it },
                        githubRepo = githubRepo,
                        onGithubRepoChange = { githubRepo = it },
                        onNavigateToCodes = { selectedTab = Tab.CODE_VIEWER }
                    )
                    Tab.CODE_VIEWER -> CodeViewerScreen(
                        botToken = botToken,
                        githubToken = githubToken,
                        githubRepo = githubRepo
                    )
                    Tab.SIMULATOR -> SimulatorScreen(
                        botToken = if (botToken.isEmpty()) "MOCK_TOKEN_XYZ" else botToken,
                        githubRepo = if (githubRepo.isEmpty()) "demo/android-project" else githubRepo
                    )
                }
            }
        }
    }
}

// ---------------- DASHBOARD SCREEN ----------------
@Composable
fun DashboardScreen(
    botToken: String,
    onBotTokenChange: (String) -> Unit,
    githubToken: String,
    onGithubTokenChange: (String) -> Unit,
    githubRepo: String,
    onGithubRepoChange: (String) -> Unit,
    onNavigateToCodes: () -> Unit
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // بانر هيرو جذاب مولد بالذكاء الاصطناعي
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.img_hero_banner_1783912311923),
                        contentDescription = "Bardom Hero Banner",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Text(
                            text = "Bardom APK Builder 🤖",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "ابنِ ملفات الـ APK الخاصة بك مباشرة عبر سحابة GitHub وتليجرام",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // مقدمة وتعريف
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ما هي فكرة المشروع؟ 🤔",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "يقوم النظام بربط بوت تليجرام خفيف الوزن (مكتوب بلغة بايثون) بسيرفرات GitHub. عند إرسال ملف مشروع أندرويد ZIP عبر البوت، يقوم البوت برفعه تلقائياً ومباشرة إلى سيرفرات GitHub، لتقوم أدوات GitHub Actions ببناء ملف الـ APK بأعلى كفاءة وسرعة، ثم إعادة إرساله لك في تليجرام تلقائياً عند انتهائه!",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        textAlign = TextAlign.Right,
                        lineHeight = 22.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // إدخال بيانات التخصيص
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "تخصيص وتوليد الأكواد المباشرة ⚙️",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Right
                    )
                    Text(
                        text = "أدخل بياناتك هنا لتحديث الأكواد تلقائياً ببياناتك الحقيقية والجاهزة للنسخ!",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Right
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = botToken,
                        onValueChange = onBotTokenChange,
                        label = { Text("توكن بوت تليجرام (Telegram Bot Token)", fontSize = 11.sp) },
                        placeholder = { Text("مثال: 123456:ABC-DEF...") },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Left, fontFamily = FontFamily.Monospace),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = githubToken,
                        onValueChange = onGithubTokenChange,
                        label = { Text("رمز الوصول الشخصي لجيت هاب (GitHub PAT)", fontSize = 11.sp) },
                        placeholder = { Text("مثال: ghp_abc123XYZ...") },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Left, fontFamily = FontFamily.Monospace),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = githubRepo,
                        onValueChange = onGithubRepoChange,
                        label = { Text("مستودع جيت هاب (GitHub Repository)", fontSize = 11.sp) },
                        placeholder = { Text("username/repository-name") },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Left, fontFamily = FontFamily.Monospace)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (botToken.isNotEmpty() && githubToken.isNotEmpty() && githubRepo.isNotEmpty()) {
                                onNavigateToCodes()
                                Toast.makeText(context, "تم تخصيص الأكواد! انتقل لتبويب الأكواد لنسخها 🚀", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "يرجى ملء الحقول لتوليد كود كامل ومخصص ببياناتك ⚠️", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Filled.Build, contentDescription = "Generate")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("توليد وتخصيص الأكواد الجاهزة الآن", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // خطوات الإعداد السهلة
        item {
            Text(
                text = "🔧 خطوات الإعداد والتركيب الأساسية",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                textAlign = TextAlign.Right
            )
        }

        val steps = listOf(
            StepItem("1️⃣ إنشاء بوت تليجرام", "اذهب لبوت @BotFather في تليجرام وأرسل الأمر `/newbot` للحصول على توكن البوت الخاص بك (Bot Token)."),
            StepItem("2️⃣ مستودع GitHub", "أنشئ مستودعاً جديداً (عمومي أو خاص) على GitHub باسم مناسب، وقم بإنشاء فرع (Branch) باسم `build-run`."),
            StepItem("3️⃣ استخراج الـ PAT", "من إعدادات GitHub، اذهب إلى Developer Settings ثم Personal Access Tokens وولد توكن وصول يحمل صلاحيات الرفع (Repo)."),
            StepItem("4️⃣ تفعيل الأكشن", "أنشئ ملف الأكشن داخل المستودع بمسار `.github/workflows/build.yml` والصق الكود الموجود بالتبويب الثاني."),
            StepItem("5️⃣ تشغيل البوت", "شغّل كود البوت `bot.py` المولد ببياناتك على أي سيرفر أو حاسوب محلي، وابدأ بإرسال مشاريعك واستمتع ببناء حقيقي وسريع! 🚀")
        )

        items(steps) { step ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = step.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Right
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = step.desc,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Right,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}

data class StepItem(val title: String, val desc: String)

// ---------------- CODE VIEWER SCREEN ----------------
@Composable
fun CodeViewerScreen(botToken: String, githubToken: String, githubRepo: String) {
    var showWorkflowFile by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val d = "$"

    // تخصيص القيم للعرض
    val displayBotToken = if (botToken.isEmpty()) "YOUR_TELEGRAM_BOT_TOKEN" else botToken
    val displayGithubToken = if (githubToken.isEmpty()) "YOUR_GITHUB_PERSONAL_ACCESS_TOKEN" else githubToken
    val displayGithubRepo = if (githubRepo.isEmpty()) "YOUR_USERNAME/YOUR_REPOSITORY" else githubRepo

    // كود bot.py المحدث
    val botPyCode = """# -*- coding: utf-8 -*-
import os
import json
import base64
import requests
import telebot
from telebot import types

# الإعدادات المخصصة تلقائياً
BOT_TOKEN = "$displayBotToken"
GITHUB_TOKEN = "$displayGithubToken"
GITHUB_REPO = "$displayGithubRepo"
GITHUB_BRANCH = "build-run"

bot = telebot.TeleBot(BOT_TOKEN)
user_states = {}

print("⚡ بوت Bardom APK Builder يعمل الآن بنجاح...")

@bot.message_handler(commands=['start', 'help'])
def send_welcome(message):
    welcome_text = (
        "👋 أهلاً بك في بوت Bardom APK Builder!\n\n"
        "📥 طريقة الاستخدام:\n"
        "1️⃣ أرسل الأمر /Bardom لبدء عملية بناء جديدة.\n"
        "2️⃣ قم بضغط مشروع الأندرويد بالكامل بصيغة ZIP وإرساله للبوت.\n"
        "3️⃣ سيقوم البوت تلقائياً برفع المشروع إلى سيرفرات GitHub وبنائه وإرساله لك!"
    )
    bot.reply_to(message, welcome_text, parse_mode='Markdown')

@bot.message_handler(commands=['Bardom'])
def request_zip_file(message):
    chat_id = message.chat.id
    user_states[chat_id] = "WAITING_FOR_ZIP"
    markup = types.ForceReply(selective=False)
    bot.send_message(
        chat_id, 
        "📤 من فضلك أرسل الآن ملف المشروع مضغوطاً بصيغة ZIP.",
        reply_markup=markup
    )

@bot.message_handler(content_types=['document'])
def handle_document(message):
    chat_id = message.chat.id
    if user_states.get(chat_id) != "WAITING_FOR_ZIP":
        bot.reply_to(message, "⚠️ يرجى إرسال الأمر /Bardom أولاً.")
        return

    document = message.document
    if not document.file_name.lower().endswith('.zip'):
        bot.reply_to(message, "❌ خطأ! يجب إرسال ملف ZIP فقط.")
        return

    status_msg = bot.reply_to(message, "⏳ جارٍ تحميل ملف ZIP... يرجى الانتظار.")
    try:
        file_info = bot.get_file(document.file_id)
        downloaded_file = bot.download_file(file_info.file_path)
        
        bot.edit_message_text("📤 جارٍ الرفع والتحضير على GitHub... 🚀", chat_id, status_msg.message_id)
        encoded_content = base64.b64encode(downloaded_file).decode('utf-8')

        url = f"https://api.github.com/repos/{displayGithubRepo}/contents/project.zip"
        headers = {"Authorization": f"token {displayGithubToken}", "Accept": "application/vnd.github.v3+json"}

        sha = None
        get_res = requests.get(url + "?ref=" + GITHUB_BRANCH, headers=headers)
        if get_res.status_code == 200: sha = get_res.json().get("sha")

        payload = {"message": f"Build triggered by TG {chat_id}", "content": encoded_content, "branch": GITHUB_BRANCH}
        if sha: payload["sha"] = sha

        put_res = requests.put(url, headers=headers, json=payload)
        if put_res.status_code in [200, 201]:
            # رفع ملف الإعدادات
            config_url = f"https://api.github.com/repos/{displayGithubRepo}/contents/tg_config.json"
            config_data = {"chat_id": str(chat_id), "message_id": str(status_msg.message_id), "bot_token": BOT_TOKEN}
            
            c_sha = None
            get_c = requests.get(config_url + "?ref=" + GITHUB_BRANCH, headers=headers)
            if get_c.status_code == 200: c_sha = get_c.json().get("sha")
                
            c_encoded = base64.b64encode(json.dumps(config_data).encode('utf-8')).decode('utf-8')
            c_payload = {"message": "Update Config", "content": c_encoded, "branch": GITHUB_BRANCH}
            if c_sha: c_payload["sha"] = c_sha
                
            requests.put(config_url, headers=headers, json=c_payload)

            bot.edit_message_text(
                "✅ تم الرفع بنجاح! خوادم GitHub Actions تقوم بالبناء الآن.\n🔔 سيصلك ملف APK فور جاهزيته!",
                chat_id, status_msg.message_id
            )
        else:
            bot.edit_message_text(f"❌ فشل رفع الملف: {put_res.status_code}", chat_id, status_msg.message_id)
    except Exception as e:
        bot.edit_message_text(f"❌ خطأ: {str(e)}", chat_id, status_msg.message_id)
    finally:
        user_states[chat_id] = None

if __name__ == '__main__':
    bot.infinity_polling()
"""

    // كود الأكشن المحدث
    val workflowYamlCode = """name: Bardom Android APK Builder

on:
  push:
    branches:
      - build-run
    paths:
      - 'project.zip'

jobs:
  build:
    name: Build Android APK
    runs-on: ubuntu-latest

    steps:
      - name: Checkout Code
        uses: actions/checkout@v4

      - name: Read Telegram Config
        id: tg_config
        run: |
          if [ -f "tg_config.json" ]; then
            CHAT_ID=${d}(jq -r '.chat_id' tg_config.json)
            MESSAGE_ID=${d}(jq -r '.message_id' tg_config.json)
            BOT_TOKEN=${d}(jq -r '.bot_token' tg_config.json)
            echo "CHAT_ID=${d}CHAT_ID" >> ${d}GITHUB_ENV
            echo "MESSAGE_ID=${d}MESSAGE_ID" >> ${d}GITHUB_ENV
            echo "BOT_TOKEN=${d}BOT_TOKEN" >> ${d}GITHUB_ENV
          else
            exit 1
          fi

      - name: Send Building Update
        run: |
          curl -s -X POST "https://api.telegram.org/bot${d}{{ env.BOT_TOKEN }}/sendMessage" \\
            -d "chat_id=${d}{{ env.CHAT_ID }}" \\
            -d "reply_to_message_id=${d}{{ env.MESSAGE_ID }}" \\
            -d "text=⚙️ بدأت الآن عملية إعداد وحل الاعتماديات لمشروع Gradle على خوادم GitHub Actions... يرجى الانتظار ⏳"

      - name: Extract Project ZIP
        run: |
          mkdir -p android_project
          unzip -o project.zip -d android_project
          # تأكيد نقل الملفات للجذر
          if [ -d "android_project/app" ] || [ -f "android_project/build.gradle" ]; then
            echo "Direct root files"
          else
            SUBDIR=${d}(find android_project -maxdepth 1 -mindepth 1 -type d | head -n 1)
            if [ -n "${d}SUBDIR" ]; then
              mv ${d}SUBDIR/* android_project/ 2>/dev/null || true
            fi
          fi

      - name: Setup Java JDK
        uses: actions/setup-java@v4
        with:
          distribution: 'zulu'
          java-version: '17'
          cache: 'gradle'

      - name: Grant Execute Permissions
        run: |
          cd android_project
          if [ -f "gradlew" ]; then
            chmod +x gradlew
          else
            gradle wrapper --gradle-version 8.0
            chmod +x gradlew
          fi

      - name: Build Debug APK
        run: |
          cd android_project
          ./gradlew assembleDebug
          APK_PATH=${d}(find app/build/outputs/apk/debug/ -name "*.apk" | head -n 1)
          echo "APK_PATH=android_project/${d}APK_PATH" >> ${d}GITHUB_ENV

      - name: Upload APK to Telegram
        run: |
          curl -F chat_id="${d}{{ env.CHAT_ID }}" \\
            -F reply_to_message_id="${d}{{ env.MESSAGE_ID }}" \\
            -F document=@"${d}{{ env.APK_PATH }}" \\
            -F caption="🎉 تم بناء ملف الـ APK بنجاح وبشكل حقيقي! 🚀" \\
            "https://api.telegram.org/bot${d}{{ env.BOT_TOKEN }}/sendDocument"
"""

    val currentCode = if (showWorkflowFile) workflowYamlCode else botPyCode
    val currentFileName = if (showWorkflowFile) ".github/workflows/build.yml" else "bot.py"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.End
    ) {
        Text(
            text = "📋 الأكواد البرمجية المخصصة",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Right
        )
        Text(
            text = "هذه الأكواد جاهزة وتعمل بشكل كامل، تم حقن بياناتك المدخلة بداخلها لتسهيل عملية النسخ والتشغيل المباشر.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Right
        )
        Spacer(modifier = Modifier.height(12.dp))

        // أزرار التبديل بين الملفين
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surface),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = { showWorkflowFile = false },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (!showWorkflowFile) MaterialTheme.colorScheme.primary else Color.Transparent,
                    contentColor = if (!showWorkflowFile) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .padding(4.dp)
            ) {
                Text("bot.py (ملف البوت)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Button(
                onClick = { showWorkflowFile = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (showWorkflowFile) MaterialTheme.colorScheme.primary else Color.Transparent,
                    contentColor = if (showWorkflowFile) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .padding(4.dp)
            ) {
                Text("build.yml (الأكشن)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // رأس كود بوكس المجلد والاسم
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .background(Color(0xFF1E293B))
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                IconButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(currentCode))
                        Toast.makeText(context, "تم نسخ الكود إلى الحافظة! 📋", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Filled.ContentCopy, contentDescription = "Copy", tint = Color.LightGray, modifier = Modifier.size(18.dp))
                }
                IconButton(
                    onClick = {
                        val sendIntent: Intent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, currentCode)
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, "مشاركة ملف $currentFileName")
                        context.startActivity(shareIntent)
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Filled.Share, contentDescription = "Share", tint = Color.LightGray, modifier = Modifier.size(18.dp))
                }
            }
            Text(
                text = currentFileName,
                color = Color(0xFF38BDF8),
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // الكود الملون بشكل خفيف داخل صندوق التمرير
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
                .background(Color(0xFF0F172A))
                .verticalScroll(rememberScrollState())
                .horizontalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = currentCode,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = Color(0xFFE2E8F0),
                lineHeight = 16.sp
            )
        }
    }
}

// ---------------- SIMULATOR SCREEN ----------------
enum class SimState {
    IDLE,
    WAITING_FOR_ZIP,
    UPLOADING,
    COMPILING,
    DELIVERED
}

data class ChatMessage(
    val sender: String, // "USER" or "BOT"
    val text: String,
    val isApk: Boolean = false,
    val logs: List<String> = emptyList()
)

@Composable
fun SimulatorScreen(botToken: String, githubRepo: String) {
    val context = LocalContext.current
    var simState by remember { mutableStateOf(SimState.IDLE) }
    var chatMessages by remember { mutableStateOf(listOf(
        ChatMessage("BOT", "👋 مرحباً بك في بوت الـ APK Builder التفاعلي!\n\nيمكنك تجربة واختبار طريقة عمل البوت الحقيقية من هنا مباشرة عبر المحاكاة والاطلاع على سجل بناء الخوادم المباشر (GitHub Action Logs).\n\nاضغط على الزر بالأسفل لبدء تجربة محاكاة التجميع 🚀")
    )) }
    
    var currentLogIndex by remember { mutableStateOf(0) }
    var currentLogs = remember { mutableStateListOf<String>() }
    var compileProgress by remember { mutableStateOf(0f) }

    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // محاكاة سجلات جيت هاب للبناء
    val githubLogs = listOf(
        "⚡ Starting GitHub Runner virtual machine (Ubuntu-latest)...",
        "📥 Action: checkout repository code '$githubRepo' on branch 'build-run'...",
        "⚙️ Parsing 'tg_config.json' configuration...",
        "✅ Found valid Target Chat ID! Masking BOT_TOKEN to prevent exposure...",
        "☕ Setup Java SDK Environment with Zulu JDK 17... (Success)",
        "📂 Extracting uploaded 'project.zip' project...",
        "🔍 Detected standard Android project with valid 'app/build.gradle.kts'...",
        "⚙️ Running Gradle Daemon initialization...",
        "🚀 Executing Command: ./gradlew assembleDebug...",
        "   > Task :app:preBuild UP-TO-DATE",
        "   > Task :app:preDebugBuild UP-TO-DATE",
        "   > Task :app:mergeDebugResources (Downloading Gradle libraries... 20%)",
        "   > Task :app:compileDebugKotlin (Analyzing source code structure... 55%)",
        "   > Task :app:dexBuilderDebug (Converting code to Android Dex bytecode... 75%)",
        "   > Task :app:packageDebug (Assembling APK components... 90%)",
        "✅ Task :app:assembleDebug SUCCESSFUL in 18s",
        "📦 APK Compiled successfully: app-debug.apk (size: 4.86 MB)",
        "📤 Connecting to Telegram API (sendDocument endpoint)...",
        "🚀 Dispatching file payload to Chat ID... 100% completed!",
        "🎉 Build Success! Exiting workflow run with exit-code 0."
    )

    // تمرير التلقائي لأسفل الشات
    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    // تأثير لتشغيل سجلات الخادم تدريجياً أثناء المحاكاة
    LaunchedEffect(simState) {
        if (simState == SimState.COMPILING) {
            currentLogIndex = 0
            currentLogs.clear()
            compileProgress = 0f
            while (currentLogIndex < githubLogs.size) {
                delay(1200) // زمن تأخير محاكاة السجل
                currentLogs.add(githubLogs[currentLogIndex])
                currentLogIndex++
                compileProgress = (currentLogIndex.toFloat() / githubLogs.size.toFloat())
            }
            delay(1000)
            simState = SimState.DELIVERED
            chatMessages = chatMessages + ChatMessage(
                "BOT",
                "🎉 تم بناء ملف الـ APK بنجاح وبشكل حقيقي! 🚀",
                isApk = true
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // شريط عنوان الشات والتشغيل
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // صورة رمزية للبوت
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.SmartToy,
                        contentDescription = "Bot Avatar",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column {
                    Text(
                        text = "Bardom APK Builder Bot",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "نشط ومستعد للبناء 🟢",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            IconButton(
                onClick = {
                    simState = SimState.IDLE
                    chatMessages = listOf(
                        ChatMessage("BOT", "👋 مرحباً بك في بوت الـ APK Builder التفاعلي!\n\nيمكنك تجربة واختبار طريقة عمل البوت الحقيقية من هنا مباشرة عبر المحاكاة والاطلاع على سجل بناء الخوادم المباشر (GitHub Action Logs).\n\nاضغط على الزر بالأسفل لبدء تجربة محاكاة التجميع 🚀")
                    )
                    currentLogs.clear()
                    compileProgress = 0f
                }
            ) {
                Icon(Icons.Filled.Refresh, contentDescription = "Reset", tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // منطقة رسائل الشات
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.4f))
                .border(1.dp, MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(chatMessages) { msg ->
                val isUser = msg.sender == "USER"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                ) {
                    Box(
                        modifier = Modifier
                            .clip(
                                RoundedCornerShape(
                                    topStart = 12.dp,
                                    topEnd = 12.dp,
                                    bottomStart = if (isUser) 12.dp else 0.dp,
                                    bottomEnd = if (isUser) 0.dp else 12.dp
                                )
                            )
                            .background(
                                if (isUser) MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
                                else MaterialTheme.colorScheme.surface
                            )
                            .padding(12.dp)
                            .widthIn(max = 280.dp)
                    ) {
                        Column {
                            if (msg.isApk) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Filled.FileDownload,
                                            contentDescription = "APK",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = "Bardom_App_debug.apk",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "4.86 MB • جاهز للتثبيت",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        Toast.makeText(context, "تم تحميل ملف APK بنجاح! 📱🎉", Toast.LENGTH_LONG).show()
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), contentColor = MaterialTheme.colorScheme.primary),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Icon(Icons.Filled.Check, contentDescription = "Install")
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("تنزيل وتثبيت التطبيق", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Text(
                                    text = msg.text,
                                    fontSize = 13.sp,
                                    color = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 20.sp,
                                    textAlign = TextAlign.Right,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }

        // لوحة البناء المباشر للـ Actions (تظهر أثناء البناء)
        AnimatedVisibility(
            visible = simState == SimState.COMPILING || simState == SimState.DELIVERED,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF0F172A))
                    .border(1.dp, Color(0xFF334155), RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "محاكاة البناء: ${(compileProgress * 100).toInt()}%",
                        color = Color(0xFF38BDF8),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "💻 سجل بناء خوادم GitHub Cloud",
                        color = Color.LightGray,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = compileProgress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = Color(0xFF00E5FF),
                    trackColor = Color(0xFF1E293B)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        currentLogs.forEach { log ->
                            Text(
                                text = log,
                                color = if (log.startsWith("✅") || log.startsWith("🎉")) Color(0xFF4ADE80)
                                        else if (log.startsWith("❌")) Color(0xFFF87171)
                                        else Color(0xFFCBD5E1),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // الأزرار والتفاعلات للتحكم في البوت بالمحاكاة
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "⚙️ لوحة تحكم المحاكاة التفاعلية",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            simState = SimState.WAITING_FOR_ZIP
                            chatMessages = chatMessages + ChatMessage("USER", "/Bardom")
                            coroutineScope.launch {
                                delay(1000)
                                chatMessages = chatMessages + ChatMessage(
                                    "BOT",
                                    "📤 **من فضلك أرسل الآن ملف المشروع مضغوطاً بصيغة ZIP**.\n\n⚠️ يرجى التأكد من ضغط محتويات المشروع مباشرة لضمان العثور على ملف build.gradle."
                                )
                            }
                        },
                        enabled = simState == SimState.IDLE,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("1️⃣ إرسال /Bardom", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }

                    Button(
                        onClick = {
                            simState = SimState.COMPILING
                            chatMessages = chatMessages + ChatMessage("USER", "📁 project.zip (مشروع أندرويد مضغوط)")
                            coroutineScope.launch {
                                delay(800)
                                chatMessages = chatMessages + ChatMessage(
                                    "BOT",
                                    "⏳ جارٍ تحميل ملف ZIP من خوادم تليجرام... يرجى الانتظار."
                                )
                                delay(1200)
                                chatMessages = chatMessages + ChatMessage(
                                    "BOT",
                                    "📤 جارٍ رفع الملف وتحضير بيئة البناء على سيرفرات GitHub... 🚀"
                                )
                                delay(1200)
                                chatMessages = chatMessages + ChatMessage(
                                    "BOT",
                                    "✅ تم الرفع بنجاح! بدأت خوادم GitHub Actions في التجميع... 🛠️\n\n(راجع سجل خوادم GitHub Actions المباشر بالأسفل لمتابعة العملية)"
                                )
                            }
                        },
                        enabled = simState == SimState.WAITING_FOR_ZIP,
                        modifier = Modifier.weight(1.5f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("2️⃣ إرسال ملف ZIP المضغوط", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
