cmake_minimum_required(VERSION 3.15)

project(poly2tri LANGUAGES CXX)
set(CMAKE_CXX_STANDARD 14)

include(FetchContent)
FetchContent_Declare(
  gsl
  GIT_REPOSITORY https://github.com/microsoft/GSL.git
  GIT_TAG 1d036585ccea8a57dc5fdc84406181db3d1f3205)
FetchContent_MakeAvailable(gsl)

option(P2T_BUILD_TESTS "Build tests" OFF)
option(P2T_BUILD_TESTBED "Build the testbed application" OFF)

file(GLOB SOURCES poly2tri/common/*.cc poly2tri/sweep/*.cc)
file(GLOB HEADERS poly2tri/*.h poly2tri/common/*.h poly2tri/sweep/*.h)
add_library(poly2tri ${SOURCES} ${HEADERS})
target_include_directories(poly2tri INTERFACE ${CMAKE_CURRENT_SOURCE_DIR})
target_link_libraries(poly2tri PUBLIC GSL)

get_target_property(poly2tri_target_type poly2tri TYPE)
if(poly2tri_target_type STREQUAL SHARED_LIBRARY)
  target_compile_definitions(poly2tri PRIVATE P2T_SHARED_EXPORTS)
else()
  target_compile_definitions(poly2tri PUBLIC P2T_STATIC_EXPORTS)
endif()

if(P2T_BUILD_TESTS)
    enable_testing()
    add_subdirectory(unittest)
endif()

if(P2T_BUILD_TESTBED)
    add_subdirectory(testbed)
endif()
