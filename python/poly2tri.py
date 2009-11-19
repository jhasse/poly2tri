#!/usr/bin/env python2.6
from framework import Game, draw_polygon, reset_zoom

from seidel import Triangulator

class Poly2Tri(Game):

    screen_size = 800.0, 600.0

    def __init__(self):
        super(Poly2Tri, self).__init__(*self.screen_size)       
        
        # Load point set
        file_name = "../data/nazca_monkey.dat"
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
        reset_zoom(7, (0, 0), self.screen_size)
        red = 255, 0, 0
        for t in self.triangles:
            draw_polygon(t, red)
        green = 0, 255, 0
        draw_polygon(self.points, green)
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

bad = (544.80998999999997, 579.86046999999996), (544.80998999999997, 450.57477), (594.09569999999997, 450.57477), (643.38142000000005, 450.57477), (643.38142000000005, 525.26486999999997), (643.38142000000005, 599.95487000000003), (603.67391999999995, 654.55056999999999), (563.96655999999996, 709.14621999999997), (554.38819999999998, 709.14621999999997), (544.80998999999997, 709.14621999999997)
        
if __name__ == '__main__':
    demo = Poly2Tri()