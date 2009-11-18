#!/usr/bin/env python2.6
from framework import Game, draw_polygon, reset_zoom

from seidel import Triangulator

class Poly2Tri(Game):

    screen_size = 800.0, 600.0

    def __init__(self):
        super(Poly2Tri, self).__init__(*self.screen_size)       
        
        # Load point set
        file_name = "../data/dude.dat"
        self.points = self.load_points(file_name)
        
        # Triangulate
        t1 = self.time
        seidel = Triangulator(self.points)
        dt = (self.time - t1) * 1000.0
        
        self.triangles = seidel.triangles()
        self.trapezoids = seidel.trapezoids
        #self.trapezoids = seidel.trapezoidal_map.map
        print "time (ms) = %f  , num triangles = %d" % (dt, len(self.triangles))
            
        self.main_loop()
        
    def update(self):
        pass
        
    def render(self):
        reset_zoom(2.0, (400, 500), self.screen_size)
        red = 255, 0, 0
        for t in self.triangles:
            draw_polygon(t, red)
        green = 0, 255, 0
        #draw_polygon(self.points, green)
        '''
        yellow = 255, 255, 0
        for t in self.trapezoids:
            #verts = self.trapezoids[key].vertices()
            verts = t.vertices()
            draw_polygon(verts, yellow)
        '''
        
    def load_points(self, file_name):
        infile = open(file_name, "r")
        points = []
        while infile:
            line = infile.readline()
            s = line.split()
            if len(s) == 0:
                break
            points.append((float(s[0]), float(s[1])))
        return points
        
if __name__ == '__main__':
    demo = Poly2Tri()