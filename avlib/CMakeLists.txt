# Sets the minimum version of CMake required to build the native
# library. You should either keep the default value or only pass a
# value of 3.4.0 or lower.

cmake_minimum_required(VERSION 3.4.1)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds it for you.
# Gradle automatically packages shared libraries with your APK.

add_library(lib_ffmpeg SHARED IMPORTED)
set_target_properties(lib_ffmpeg PROPERTIES IMPORTED_LOCATION
    ${CMAKE_SOURCE_DIR}/src/main/cpp/armeabi-v7a/libffmpeg.so)

#set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=gnu++11")

add_library( avcodec
             SHARED
             ${CMAKE_SOURCE_DIR}/src/main/cpp/decodec.c)

target_include_directories(avcodec PRIVATE ${CMAKE_SOURCE_DIR}/src/main/cpp/armeabi-v7a/include )

target_link_libraries(
                       avcodec
                       jnigraphics
                       lib_ffmpeg
                       android
                       log )

