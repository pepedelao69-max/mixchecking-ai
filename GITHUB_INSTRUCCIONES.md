
# Cómo obtener tu APK en tu celular SIN PC (GitHub)

## Pasos (2 minutos):

1. Crea cuenta gratis en https://github.com si no tienes

2. Crea nuevo repositorio:
   - Ve a https://github.com/new
   - Nombre: mixcheck-ai
   - Público
   - Dale Create

3. Sube el código:
   - En tu nuevo repo, dale a "uploading an existing file" o "Add file > Upload files"
   - Arrastra TODO el contenido del ZIP MIXCHECK_AI_PROYECTO_COMPLETO.zip (descomprimido)
   - Importante: sube la carpeta android/, .github/, docs/, backend/, web-mvp/
   - Dale Commit

4. Activa el Action que te compila el APK:
   - Ve a la pestaña "Actions" en tu repo
   - Verás "Build MIXCHECK AI APK" > Dale "Run workflow" > Run
   - Espera 3-5 minutos (verás circulito amarillo -> verde)

5. Descarga tu APK directo a tu celular:
   - Cuando termine (verde), entra al workflow
   - Baja hasta "Artifacts" > Descarga "MIXCHECK-AI-APK"
   - Es un ZIP que dentro trae app-debug.apk
   - En tu Android, abre el APK y dale Instalar (permite orígenes desconocidos)

¡Listo! App nativa 100% instalada sin PC.

## Alternativa aún más fácil:
Si no quieres subir archivos, puedo darte un repo template listo para hacer Fork.

