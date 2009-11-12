##
## GL convenience layer
##
from math import pi as PI

from gl cimport *

#from triangulator import Point

include "triangulator.pyx"

cdef extern from 'math.h':
    double cos(double)
    double sin(double)

SEGMENTS = 25
INCREMENT = 2.0 * PI / SEGMENTS
    
def init_gl(width, height):
    #glEnable(GL_LINE_SMOOTH)
    glEnable(GL_BLEND)
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
    glClearColor(0.0, 0.0, 0.0, 0.0)
    glHint (GL_LINE_SMOOTH_HINT, GL_NICEST)
    
def reset_zoom(float zoom, center, size):

    zinv = 1.0 / zoom
    left = -size[0] * zinv
    right = size[0] * zinv
    bottom = -size[1] * zinv
    top = size[1] * zinv
    
    # Reset viewport
    glLoadIdentity()
    glMatrixMode(GL_PROJECTION)
    glLoadIdentity()
    
    # Reset ortho view
    glOrtho(left, right, bottom, top, 1, -1)
    glTranslatef(-center[0], -center[1], 0)
    glMatrixMode(GL_MODELVIEW)
    glDisable(GL_DEPTH_TEST)
    glLoadIdentity()
    
    # Clear the screen
    glClear(GL_COLOR_BUFFER_BIT)
    
def draw_polygon(verts, color):
    r, g, b = color
    glColor3f(r, g, b)
    glBegin(GL_LINE_LOOP)
    for v in verts:
        glVertex2f(v[0], v[1])
    glEnd()
       
##
## Game engine / main loop / UI
##
from glfw cimport *

import sys

cdef extern from 'math.h':
    double cos(double)
    double sin(double)
    double sqrt(double)

# Keyboard callback wrapper
kbd_callback_method = None

cdef extern void __stdcall kbd_callback(int id, int state):
    kbd_callback_method(id, state)


cdef class Game:

    title = "Poly2Tri"
    
    def __init__(self, window_width, window_height):
        
        p1 = Point(12, 10)
        p2 = Point(50, 47)
        print p1.cross(p2)
     
        glfwInit()
        
        # 16 bit color, no depth, alpha or stencil buffers, windowed
        if not glfwOpenWindow(window_width, window_height, 8, 8, 8, 8, 24, 0, GLFW_WINDOW):
            glfwTerminate()
            raise SystemError('Unable to create GLFW window')
        
        glfwEnable(GLFW_STICKY_KEYS)
        glfwSwapInterval(1) #VSync on

    def register_kbd_callback(self, f):
        global kbd_callback_method
        glfwSetKeyCallback(kbd_callback)
        kbd_callback_method = f

    def main_loop(self):
        
        frame_count = 1
        start_time = glfwGetTime()

        running = True
        while running:
            
            current_time = glfwGetTime()
            
            #Calculate and display FPS (frames per second)
            if (current_time - start_time) > 1 or frame_count == 0:
                frame_rate = frame_count / (current_time - start_time)
                t = self.title + " (%d FPS)" % frame_rate
                glfwSetWindowTitle(t)
                start_time = current_time
                frame_count = 0
                
            frame_count = frame_count + 1

            # Check if the ESC key was pressed or the window was closed
            running = ((not glfwGetKey(GLFW_KEY_ESC))
                       and glfwGetWindowParam(GLFW_OPENED))
             
            self.update()
            self.render()
        
            glfwSwapBuffers()

            
        glfwTerminate()
        
    property window_title:
        def __set__(self, title): self.title = title
        
    property time:
        def __get__(self): return glfwGetTime()
        def __set__(self, t): glfwSetTime(t)