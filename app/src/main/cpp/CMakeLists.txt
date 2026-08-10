cmake_minimum_required(VERSION 3.22.1)

project("mealdiary")

add_library(
    mealdiary
    SHARED
    mealdiary-native.cpp
)

find_library(
    log-lib
    log
)

target_link_libraries(
    mealdiary
    ${log-lib}
)
