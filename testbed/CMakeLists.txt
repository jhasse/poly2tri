# Dependencies
find_package(glfw3 3.3 REQUIRED)
find_package(OpenGL REQUIRED)

# Build testbed
add_executable(testbed
    main.cc
)

target_link_libraries(testbed
    PRIVATE
    glfw
    OpenGL::GL
    poly2tri
)
