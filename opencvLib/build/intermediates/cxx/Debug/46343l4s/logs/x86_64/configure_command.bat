@echo off
"D:\\AndroidDevelopTool\\AndroidStudio\\android-sdk-windows\\cmake\\3.18.1\\bin\\cmake.exe" ^
  "-HD:\\AndroidDevelopTool\\AndroidStudio\\AndroidStudioProjects\\InfraredTouch\\opencvLib\\libcxx_helper" ^
  "-DCMAKE_SYSTEM_NAME=Android" ^
  "-DCMAKE_EXPORT_COMPILE_COMMANDS=ON" ^
  "-DCMAKE_SYSTEM_VERSION=21" ^
  "-DANDROID_PLATFORM=android-21" ^
  "-DANDROID_ABI=x86_64" ^
  "-DCMAKE_ANDROID_ARCH_ABI=x86_64" ^
  "-DANDROID_NDK=D:\\AndroidDevelopTool\\AndroidStudio\\android-sdk-windows\\ndk\\23.1.7779620" ^
  "-DCMAKE_ANDROID_NDK=D:\\AndroidDevelopTool\\AndroidStudio\\android-sdk-windows\\ndk\\23.1.7779620" ^
  "-DCMAKE_TOOLCHAIN_FILE=D:\\AndroidDevelopTool\\AndroidStudio\\android-sdk-windows\\ndk\\23.1.7779620\\build\\cmake\\android.toolchain.cmake" ^
  "-DCMAKE_MAKE_PROGRAM=D:\\AndroidDevelopTool\\AndroidStudio\\android-sdk-windows\\cmake\\3.18.1\\bin\\ninja.exe" ^
  "-DCMAKE_LIBRARY_OUTPUT_DIRECTORY=D:\\AndroidDevelopTool\\AndroidStudio\\AndroidStudioProjects\\InfraredTouch\\opencvLib\\build\\intermediates\\cxx\\Debug\\46343l4s\\obj\\x86_64" ^
  "-DCMAKE_RUNTIME_OUTPUT_DIRECTORY=D:\\AndroidDevelopTool\\AndroidStudio\\AndroidStudioProjects\\InfraredTouch\\opencvLib\\build\\intermediates\\cxx\\Debug\\46343l4s\\obj\\x86_64" ^
  "-DCMAKE_BUILD_TYPE=Debug" ^
  "-BD:\\AndroidDevelopTool\\AndroidStudio\\AndroidStudioProjects\\InfraredTouch\\opencvLib\\.cxx\\Debug\\46343l4s\\x86_64" ^
  -GNinja ^
  "-DANDROID_STL=c++_shared"
