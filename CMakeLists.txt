cmake_minimum_required(VERSION 3.6)

project(poly2tri LANGUAGES CXX)
set(CMAKE_CXX_STANDARD 14)

file(GLOB SOURCES poly2tri/common/*.cc poly2tri/sweep/*.cc)
add_library(poly2tri ${SOURCES})
target_include_directories(poly2tri INTERFACE ${CMAKE_CURRENT_SOURCE_DIR})
