# -*- coding: utf-8 -*-
"""
Bardom APK Builder Bot
هذا هو الكود البرمجي الكامل لبوت تليجرام يقوم باستقبال ملفات مشاريع أندرويد بصيغة ZIP
ثم يقوم تلقائياً برفعها إلى مستودع GitHub لتشغيل الـ Workflow وبناء ملف الـ APK
وإرساله مباشرة إلى المستخدم في تليجرام عند انتهائه.
"""

import os
import json
import base64
import requests
import telebot
from telebot import types

# ---------------- CONFIGURATION / الإعدادات ---------------- #
# يفضل دائماً استخدام متغيرات البيئة لحماية التوكن والبيانات الحساسة
# يمكنك كتابة التوكن مباشرة هنا للتجربة السريعة أو ضبطها كمتغيرات بيئة
BOT_TOKEN = os.getenv("TELEGRAM_BOT_TOKEN", "YOUR_TELEGRAM_BOT_TOKEN")
GITHUB_TOKEN = os.getenv("GITHUB_PERSONAL_ACCESS_TOKEN", "YOUR_GITHUB_PAT")
GITHUB_REPO = os.getenv("GITHUB_REPOSITORY", "YOUR_USERNAME/YOUR_REPO_NAME")
GITHUB_BRANCH = "build-run"  # الفرع المخصص لاستقبال عمليات البناء

# تهيئة البوت
bot = telebot.TeleBot(BOT_TOKEN)

# قاموس لتتبع حالة المستخدمين
user_states = {}

print("⚡ بوت Bardom APK Builder يعمل الآن بنجاح...")


@bot.message_handler(commands=['start', 'help'])
def send_welcome(message):
    """
    رسالة الترحيب والتعليمات
    """
    welcome_text = (
        "👋 أهلاً بك في بوت **Bardom APK Builder**!\n\n"
        "هذا البوت يساعدك على بناء وتجميع مشاريع أندرويد (Gradle) وتحويلها إلى ملف APK مباشرة عبر سيرفرات GitHub Cloud!\n\n"
        "📥 **طريقة الاستخدام:**\n"
        "1️⃣ أرسل الأمر /Bardom لبدء عملية بناء جديدة.\n"
        "2️⃣ قم بضغط مشروع الأندرويد بالكامل بصيغة **ZIP** وإرساله للبوت.\n"
        "3️⃣ سيقوم البوت برفع المشروع إلى سيرفرات GitHub، وتثبيت بيئة أندرويد وبناء التطبيق.\n"
        "4️⃣ فور انتهاء البناء، سيصلك ملف الـ APK جاهزاً للتحميل والتثبيت هنا في الشات!\n\n"
        "💡 _تأكد من أن مشروعك يحتوي على ملفات gradle السليمة في الجذر ليتم البناء بنجاح._"
    )
    bot.reply_to(message, welcome_text, parse_mode='Markdown')


@bot.message_handler(commands=['Bardom'])
def request_zip_file(message):
    """
    استقبال أمر /Bardom لطلب ملف ZIP
    """
    chat_id = message.chat.id
    user_states[chat_id] = "WAITING_FOR_ZIP"
    
    markup = types.ForceReply(selective=False)
    msg = bot.send_message(
        chat_id, 
        "📤 **من فضلك أرسل الآن ملف المشروع مضغوطاً بصيغة ZIP**.\n\n"
        "⚠️ يرجى التأكد من ضغط محتويات المشروع مباشرة وليس المجلد الرئيسي لضمان العثور على ملف build.gradle في جذر الملف المضغوط.",
        parse_mode='Markdown',
        reply_markup=markup
    )


@bot.message_handler(content_types=['document'])
def handle_document(message):
    """
    معالجة الملف المرسل من قبل المستخدم
    """
    chat_id = message.chat.id
    
    # التحقق مما إذا كان المستخدم في حالة انتظار ملف ZIP
    if user_states.get(chat_id) != "WAITING_FOR_ZIP":
        bot.reply_to(message, "⚠️ يرجى إرسال الأمر /Bardom أولاً قبل إرسال الملف.")
        return

    document = message.document
    file_name = document.file_name

    # التحقق من أن الملف مضغوط بصيغة ZIP
    if not file_name.lower().endswith('.zip'):
        bot.reply_to(message, "❌ خطأ! يجب أن يكون الملف المرسل بصيغة ZIP فقط.")
        return

    # إعلام المستخدم ببدء تحميل ومعالجة الملف
    status_msg = bot.reply_to(message, "⏳ جارٍ تحميل ملف ZIP من خوادم تليجرام... يرجى الانتظار.")
    
    try:
        # تحميل الملف من سيرفرات تليجرام
        file_info = bot.get_file(document.file_id)
        downloaded_file = bot.download_file(file_info.file_path)
        
        bot.edit_message_text(
            "📤 جارٍ رفع الملف وتحضير بيئة البناء على سيرفرات GitHub... 🚀",
            chat_id=chat_id,
            message_id=status_msg.message_id
        )

        # تحويل محتوى الملف المضغوط إلى Base64 لرفعه عبر GitHub API
        encoded_content = base64.b64encode(downloaded_file).decode('utf-8')

        # تجهيز بيانات رفع الملف إلى GitHub المستهدف
        # سنقوم بتحديث ملف project.zip في الفرع المخصص للبناء
        url = f"https://api.github.com/repos/{GITHUB_REPO}/contents/project.zip"
        headers = {
            "Authorization": f"token {GITHUB_TOKEN}",
            "Accept": "application/vnd.github.v3+json"
        }

        # الحصول على sha الخاص بالملف إذا كان موجوداً سابقاً (لتجنب تعارض الرفع)
        sha = None
        get_response = requests.get(url + f"?ref={GITHUB_BRANCH}", headers=headers)
        if get_response.status_code == 200:
            sha = get_response.json().get("sha")

        # بيانات الالتزام (Commit Data)
        # سنقوم بتمرير معلومات تليجرام في رسالة الالتزام أو كملف بيانات إضافي
        commit_message = f"Build triggered by Telegram Chat ID {chat_id}"
        
        payload = {
            "message": commit_message,
            "content": encoded_content,
            "branch": GITHUB_BRANCH
        }
        if sha:
            payload["sha"] = sha

        # رفع الملف إلى GitHub
        put_response = requests.put(url, headers=headers, json=payload)
        
        if put_response.status_code in [200, 201]:
            # سنقوم أيضاً برفع ملف JSON صغير يحتوي على معلومات التليجرام لكي يستطيع الـ Workflow قراءتها
            # وإرسال الـ APK للشات الصحيح
            config_url = f"https://api.github.com/repos/{GITHUB_REPO}/contents/tg_config.json"
            config_data = {
                "chat_id": str(chat_id),
                "message_id": str(status_msg.message_id),
                "bot_token": BOT_TOKEN
            }
            
            # الحصول على sha لـ tg_config.json إذا كان موجوداً
            config_sha = None
            get_config_res = requests.get(config_url + f"?ref={GITHUB_BRANCH}", headers=headers)
            if get_config_res.status_code == 200:
                config_sha = get_config_res.json().get("sha")
                
            config_encoded = base64.b64encode(json.dumps(config_data).encode('utf-8')).decode('utf-8')
            config_payload = {
                "message": f"Update TG Config for chat {chat_id}",
                "content": config_encoded,
                "branch": GITHUB_BRANCH
            }
            if config_sha:
                config_payload["sha"] = config_sha
                
            requests.put(config_url, headers=headers, json=config_payload)

            # تعديل الرسالة لتأكيد البدء
            bot.edit_message_text(
                "✅ تم رفع مشروعك بنجاح إلى GitHub!\n"
                "🛠️ بدأت خوادم GitHub Actions في تجميع وبناء ملف الـ APK الآن.\n"
                "📊 قد تستغرق عملية بناء الـ Gradle وتنزيل الحزم من 3 إلى 7 دقائق.\n"
                "🔔 سيقوم البوت بإرسال ملف الـ APK النهائي لك هنا فور جاهزيته تلقائياً!",
                chat_id=chat_id,
                message_id=status_msg.message_id
            )
        else:
            bot.edit_message_text(
                f"❌ فشل رفع الملف إلى GitHub.\nرمز الخطأ: {put_response.status_code}\nيرجى التحقق من صحة التوكن واسم المستودع في الإعدادات.",
                chat_id=chat_id,
                message_id=status_msg.message_id
            )
            
    except Exception as e:
        bot.edit_message_text(
            f"❌ حدث خطأ غير متوقع أثناء معالجة الملف:\n`{str(e)}`",
            chat_id=chat_id,
            message_id=status_msg.message_id,
            parse_mode='Markdown'
        )
    finally:
        # إعادة تعيين حالة المستخدم
        user_states[chat_id] = None


# تشغيل البوت باستمرار (Polling)
if __name__ == '__main__':
    bot.infinity_polling()
