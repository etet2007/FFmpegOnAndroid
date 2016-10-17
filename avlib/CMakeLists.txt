# Sets the minimum version of CMake required to build the native
# library. You should either keep the default value or only pass a
# value of 3.4.0 or lower.

cmake_minimum_required(VERSION 3.4.1)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds it for you.
# Gradle automatically packages shared libraries with your APK.

# add by lqt.
add_library(
             decodec
             SHARED
             src/main/cpp/decodec.c)

include_directories(src/main/cpp/armeabi-v7a/include )

add_library(
     avcodec
     STATIC
     IMPORTED
 )
set_target_properties(avcodec
                       PROPERTIES IMPORTED_LOCATION
                       src/main/cpp/armeabi-v7a/lib/libavcodec.a )
add_library(
     avfilter
     STATIC
     IMPORTED
 )
set_target_properties(avfilter
                       PROPERTIES IMPORTED_LOCATION
                       src/main/cpp/armeabi-v7a/lib/libavfilter.a )


find_library( # Sets the name of the path variable.
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )

target_link_libraries( decodec
                       avcodec
                       avfilter
                       ${log-lib} )
