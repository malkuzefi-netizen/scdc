# تشغيل المراسلة عبر Firebase

تم تعديل التطبيق ليستخدم Firebase Firestore كمصدر مشترك للرسائل بين الأجهزة، مع Room كنسخة محلية عند انقطاع الإنترنت.

## 1) إنشاء مشروع Firebase
1. افتح Firebase Console وأنشئ مشروعاً جديداً.
2. أضف تطبيق Android بالـ Package Name التالي بالضبط:
   `com.aistudio.southerncyber.qtzmrk`
3. نزّل ملف `google-services.json`.
4. ضع الملف داخل مجلد `app/` بحيث يصبح المسار:
   `app/google-services.json`

## 2) تفعيل تسجيل الدخول المجهول
من Firebase Console:
Authentication > Sign-in method > Anonymous > Enable

هذا مطلوب حالياً حتى لا تكون قاعدة Firestore مفتوحة للعامة.

## 3) إنشاء Firestore
من Firebase Console:
Firestore Database > Create database

بعد الإنشاء افتح Rules وانسخ محتوى ملف `firestore.rules` الموجود في جذر المشروع ثم Publish.

## 4) ماذا تم تعديله؟
- إضافة Firebase Firestore.
- إضافة Firebase Auth (Anonymous).
- إرسال الرسائل إلى collection باسم `tactical_messages`.
- Listener لحظي لتلقي الرسائل على الأجهزة الأخرى.
- حفظ نسخة Room محلية.
- إعادة محاولة الرسائل غير المتزامنة.
- مزامنة الحذف ومسح الأرشيف.
- إضافة `clientUuid` لمنع تكرار نفس الرسالة.
- تصفية الرسائل الخاصة داخل الواجهة بحيث تظهر للمرسل/المستلم أو المدير فقط.

## ملاحظة أمنية مهمة
هذه النسخة تحقق المزامنة الفعلية بين الأجهزة، لكنها ليست نظام تشفير طرفي End-to-End وليست مناسبة وحدها لبيانات عسكرية حقيقية أو عالية الحساسية.
قواعد Firestore الحالية تشترط مستخدماً Firebase مسجلاً فقط، وتستخدم Anonymous Auth كبداية تشغيلية.
للإنتاج الحساس يجب نقل حسابات المستخدمين نفسها إلى Firebase Auth أو نظام هوية مركزي، وربط الصلاحيات بقواعد Firestore/Custom Claims وعدم الاعتماد على معرفات الحسابات المحلية فقط.
