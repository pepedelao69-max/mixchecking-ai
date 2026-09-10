
# BUILD MIXCHECK AI APK/AAB

Requisitos: Android Studio Hedgehog+, JDK17, SDK34

Pasos:
1. Abrir carpeta android/ en Android Studio
2. Sync Gradle
3. ./gradlew assembleDebug -> APK
4. Crear keystore y ./gradlew bundleRelease -> AAB para Play Store

Product IDs configurables en billing/ProductIds.kt:
mixcheck_pro_monthly, mixcheck_pro_yearly, mixcheck_studio_monthly, mixcheck_studio_yearly, credits_10, credits_20

Backend Fase2:
cd backend
pip install -r requirements.txt
uvicorn app.main:app --reload
