# UltraBoost Native Engine CMake Build Configuration

cmake_minimum_required(VERSION 3.22.1)

project("ultraboost_native")

# Set C++ standard
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Add native library
add_library(
    ultraboost_native
    SHARED
    native_engine.cpp
)

# Find required libraries
find_library(
    log-lib
    log
)

find_library(
    android-lib
    android
)

# Link libraries
target_link_libraries(
    ultraboost_native
    ${log-lib}
    ${android-lib}
    android
)

# Compiler flags for optimization
target_compile_options(
    ultraboost_native
    PRIVATE
    -O2
    -ffast-math
    -Wall
    -Wextra
)

# Include directories
include_directories(
    src/main/cpp/include
)
