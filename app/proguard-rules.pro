# kotlinx.serialization keeps the generated serializers referenced reflectively.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class com.samarth.courseapp.data.remote.dto.** {
    *** Companion;
    kotlinx.serialization.KSerializer serializer(...);
}
